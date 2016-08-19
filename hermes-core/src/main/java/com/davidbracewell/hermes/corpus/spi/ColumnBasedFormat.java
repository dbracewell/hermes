package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.Language;
import com.davidbracewell.collection.Collect;
import com.davidbracewell.collection.index.HashMapIndex;
import com.davidbracewell.collection.index.Index;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.AttributeType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.StringUtils;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.*;

/**
 * The type Column based format.
 */
public abstract class ColumnBasedFormat extends FileBasedFormat {
  private static final long serialVersionUID = 1L;
  /**
   * The Field names.
   */
  protected final Index<String> fieldNames = new HashMapIndex<>();
  protected final String configProperty;

  /**
   * Instantiates a new Column based format.
   *
   * @param configProperty the config property
   */
  public ColumnBasedFormat(String configProperty) {
    this.configProperty = configProperty;
  }


  protected Index<String> getFieldNames() {
    if (fieldNames.isEmpty()) {
      synchronized (fieldNames) {
        if (fieldNames.isEmpty()) {
          if (Config.hasProperty(configProperty, "fields")) {
            for (String field : Config.get(configProperty, "fields").asList(String.class)) {
              fieldNames.add(field.toUpperCase());
            }
          }
        }
      }
    }
    return fieldNames;
  }

  public void clearFields() {
    this.fieldNames.clear();
  }

  /**
   * Create document document.
   *
   * @param row             the row
   * @param documentFactory the document factory
   * @return the document
   */
  final Document createDocument(List<String> row, DocumentFactory documentFactory) {
    String id = StringUtils.EMPTY;
    String content = StringUtils.EMPTY;
    Language language = documentFactory.getDefaultLanguage();
    Map<AttributeType, Object> attributeMap = new HashMap<>();

    String idField = Config.get(configProperty, "idField").asString("ID").toUpperCase();
    String contentField = Config.get(configProperty, "contentField").asString("CONTENT").toUpperCase();
    String languageField = Config.get(configProperty, "languageField").asString("LANGUAGE").toUpperCase();
    Index<String> fields = getFieldNames();

    for (int i = 0; i < row.size() && i < fieldNames.size(); i++) {
      String field = row.get(i);
      String fieldName = fields.get(i);
      if (idField.equalsIgnoreCase(fieldName)) {
        id = field;
      } else if (contentField.equalsIgnoreCase(fieldName)) {
        content = field;
      } else if (languageField.equalsIgnoreCase(fieldName)) {
        language = Language.fromString(field);
      } else {
        AttributeType attributeType = AttributeType.create(fieldName);
        attributeMap.put(attributeType, attributeType.getValueType().convert(field));
      }
    }
    return documentFactory.create(id, content, language, attributeMap);
  }

  @Override
  public Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
    final LinkedList<List<String>> rows = Lists.newLinkedList(rows(resource));
    return Collect.asIterable(new Iterator<Document>() {

      @Override
      public boolean hasNext() {
        return !rows.isEmpty();
      }

      @Override
      public Document next() {
        if (rows.isEmpty()) {
          throw new NoSuchElementException();
        }
        return createDocument(rows.removeFirst(), documentFactory);
      }
    });
  }

  /**
   * Rows iterable.
   *
   * @param resource the resource
   * @return the iterable
   */
  abstract Iterable<List<String>> rows(Resource resource);

}
