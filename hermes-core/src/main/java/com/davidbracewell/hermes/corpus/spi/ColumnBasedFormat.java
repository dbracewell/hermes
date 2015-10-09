package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.Language;
import com.davidbracewell.collection.Collect;
import com.davidbracewell.collection.Index;
import com.davidbracewell.collection.Indexes;
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Val;
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

  public ColumnBasedFormat(String configProperty) {
    fieldNames = Indexes.newIndex();
    for (String field : Config.get(configProperty).asList(String.class)) {
      fieldNames.add(field.toUpperCase());
    }
  }


  final Document createDocument(List<String> row, DocumentFactory documentFactory) {
    String id = null;
    String content = StringUtils.EMPTY;
    Language language = documentFactory.getDefaultLanguage();
    Map<Attribute, Object> attributeMap = new HashMap<>();
    for (int i = 0; i < row.size() && i < fieldNames.size(); i++) {
      String field = row.get(i);
      switch (fieldNames.get(i)) {
        case "ID":
          id = field;
          break;
        case "CONTENT":
          content = field;
          break;
        case "LANGUAGE":
          language = Language.fromString(field);
          break;
        default:
          Attribute attribute = Attribute.create(field);
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
