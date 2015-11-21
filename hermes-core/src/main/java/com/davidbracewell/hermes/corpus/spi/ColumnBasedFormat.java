package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.Language;
import com.davidbracewell.collection.Collect;
import com.davidbracewell.collection.Index;
import com.davidbracewell.collection.Indexes;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.StringUtils;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.*;

/**
 * Created by david on 10/9/15.
 */
public abstract class ColumnBasedFormat extends FileBasedFormat {

  protected final Index<String> fieldNames;
  protected final String contentField;
  protected final String idField;
  protected final String languageField;

  public ColumnBasedFormat(String configProperty) {
    fieldNames = Indexes.newIndex();
    if (Config.hasProperty(configProperty)) {
      for (String field : Config.get(configProperty).asList(String.class)) {
        fieldNames.add(field.toUpperCase());
      }
    }
    contentField = Config.get(configProperty, "contentField").asString("CONTENT").toUpperCase();
    idField = Config.get(configProperty, "idField").asString("ID").toUpperCase();
    languageField = Config.get(configProperty, "languageField").asString("LANGUAGE").toUpperCase();
  }


  final Document createDocument(List<String> row, DocumentFactory documentFactory) {
    String id = StringUtils.EMPTY;
    String content = StringUtils.EMPTY;
    Language language = documentFactory.getDefaultLanguage();
    Map<Attribute, Object> attributeMap = new HashMap<>();
    for (int i = 0; i < row.size() && i < fieldNames.size(); i++) {
      String field = row.get(i);
      String fieldName = fieldNames.get(i);
      if (idField.equalsIgnoreCase(fieldName)) {
        id = field;
      } else if (contentField.equalsIgnoreCase(fieldName)) {
        content = field;
      } else if (languageField.equalsIgnoreCase(fieldName)) {
        language = Language.fromString(field);
      } else {
        Attribute attribute = Attribute.create(fieldName);
        attributeMap.put(attribute, attribute.getValueType().convert(field));
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

  abstract Iterable<List<String>> rows(Resource resource);

}
