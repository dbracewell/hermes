/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.davidbracewell.hermes;

import com.davidbracewell.Language;
import com.davidbracewell.collection.Collect;
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.ElementType;
import com.davidbracewell.io.structured.StructuredFormat;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.io.structured.StructuredWriter;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author David B. Bracewell
 */
public class Document extends HString {

  private static final long serialVersionUID = 1L;
  private final Map<Attribute, Val> attributes = new HashMap<>(5);
  private final String content;
  private final AtomicLong idGenerator = new AtomicLong(0);
  private final AnnotationSet annotationSet;
  private String id;

  public Document(String id, @Nonnull String content, Language language) {
    super(0, content.length());
    this.content = content;
    setId(id);
    setLanguage(language);
    this.annotationSet = Config.get("hermes.AnnotationSetImpl").as(AnnotationSet.class);
  }

  public Document(String id, @Nonnull String content) {
    this(id, content, null);
  }

  public Document(@Nonnull String content, Language language) {
    this(null, content, language);
  }

  public Document(@Nonnull String content) {
    this(null, content, null);
  }


  /**
   * Constructs a document for a pre-tokenized source.
   *
   * @param language The language of the document
   * @param tokens   The tokens.
   * @return A new document consisting of the given tokens
   */
  public static Document fromTokens(Language language, Iterable<String> tokens) {
    Preconditions.checkNotNull(language);
    Preconditions.checkNotNull(tokens);
    StringBuilder content = new StringBuilder();
    List<Span> tokenSpans = new ArrayList<>();
    for (String token : tokens) {
      tokenSpans.add(new Span(content.length(), content.length() + token.length()));
      content.append(token).append(" ");
    }
    Document doc = new Document("", content.toString().trim());
    doc.setLanguage(language);
    for (int idx = 0; idx < tokenSpans.size(); idx++) {
      doc.createAnnotation(Types.TOKEN, tokenSpans.get(idx));
//          MapUtils.map(Attrs.INDEX, idx,
//              Attrs.TOKEN_TYPE, TokenType.UNKNOWN)
//      );
    }
    doc.annotationSet.setIsCompleted(Types.TOKEN, true, "PROVIDED");
    return doc;
  }

  @Override
  public char charAt(int index) {
    return content.charAt(index);
  }

  @Override
  public Document document() {
    return this;
  }

  @Override
  protected Map<Attribute, Val> getAttributeMap() {
    return attributes;
  }

  /**
   * Gets the id of the document
   *
   * @return The id of the document
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id of the document. If a null or blank id is given a random id will generated.
   *
   * @param id The new id of the document
   */
  public void setId(String id) {
    if (StringUtils.isNullOrBlank(id)) {
      this.id = StringUtils.randomHexString(10);
    } else {
      this.id = id;
    }
  }

  @Override
  public String toString() {
    return content;
  }

  @Override
  public boolean isDocument() {
    return true;
  }

  @Override
  public Language getLanguage() {
    if (hasAttribute(Attrs.LANGUAGE)) {
      return getAttribute(Attrs.LANGUAGE).as(Language.class);
    }
    return Hermes.defaultLanguage();
  }

  List<Annotation> getStartingAt(AnnotationType type, int start) {
    return annotationSet.select(a -> a.isInstance(type) && a.start() == start);
  }

  public List<Annotation> getOverlapping(AnnotationType type, @Nonnull Span span) {
    return annotationSet.select(span, a -> a.isInstance(type) && a.overlaps(span));
  }

  public List<Annotation> getContaining(AnnotationType type, @Nonnull Span span) {
    return annotationSet.select(span, a -> a.isInstance(type) && a.encloses(span));
  }

  public List<Annotation> getDuring(AnnotationType type, @Nonnull Span span) {
    return annotationSet.select(span, a -> a.isInstance(type) && span.encloses(a));
  }

  @Override
  public List<Annotation> getOverlapping(AnnotationType type) {
    return annotationSet.select(a -> a.isInstance(type));
  }

  @Override
  public List<Annotation> getContaining(AnnotationType type) {
    return getOverlapping(type);
  }

  @Override
  public List<Annotation> getDuring(AnnotationType type) {
    return Collections.emptyList();
  }


  public Annotation createAnnotation(@Nonnull AnnotationType type, @Nonnull Span span) {
    return createAnnotation(type, span.start(), span.end(), Collections.emptyMap());
  }


  public Annotation createAnnotation(@Nonnull AnnotationType type, int start, int end) {
    return createAnnotation(type, start, end, Collections.emptyMap());
  }

  public Annotation createAnnotation(@Nonnull AnnotationType type, int start, int end, @Nonnull Map<Attribute, ?> attributeMap) {
    Annotation annotation = new Annotation(this, type, start, end);
    annotation.setId(idGenerator.getAndIncrement());
    annotation.putAllAttributes(attributeMap);
    annotationSet.add(annotation);
    return annotation;
  }


  public AnnotationSet getAnnotationSet() {
    return annotationSet;
  }

  public void write(@Nonnull StructuredFormat format, @Nonnull Resource resource) throws IOException {
    try (StructuredWriter writer = format.createWriter(resource)) {
      writer.beginDocument();
      writer.writeKeyValue("id", getId());
      writer.writeKeyValue("content", toString());

      if (attributes.size() > 0) {
        writer.beginObject("attributes");
        for (Map.Entry<Attribute, Val> entry : getAttributes()) {
          entry.getKey().write(writer, entry.getValue());
        }
        writer.endObject();
      }

      if (annotationSet.size() > 0) {
        writer.beginArray("annotations");
        for (Annotation annotation : annotationSet) {
          annotation.write(writer);
        }
        writer.endArray();
      }
      writer.endDocument();
    }
  }

  public String toJson() {
    try {
      Resource stringResource = Resources.fromString();
      write(StructuredFormat.JSON, stringResource);
      return stringResource.readToString().trim();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public static Document fromJson(String jsonString) {
    try {
      return fromJson(Resources.fromString(jsonString));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public static Document fromJson(@Nonnull Resource jsonResource) throws IOException {
    return read(StructuredFormat.JSON, jsonResource);
  }


  public static Document read(@Nonnull StructuredFormat format, @Nonnull Resource resource) throws IOException {
    try (StructuredReader reader = format.createReader(resource)) {
      reader.beginDocument();

      Map<String, Val> docProperties = new HashMap<>();
      List<Annotation> annotations = new LinkedList<>();
      Map<Attribute, Val> attributeValMap = Collections.emptyMap();

      while (reader.peek() != ElementType.END_DOCUMENT) {
        if (reader.peek() == ElementType.NAME) {

          Collect.put(docProperties, reader.nextKeyValue());

        } else if (reader.peek() == ElementType.BEGIN_OBJECT) {

          String name = reader.beginObject();
          if (name.equals("attributes")) {
            attributeValMap = Attribute.readAttributeList(reader);
          } else {
            throw new IOException("Unexpected object named [" + name + "]");
          }

        } else if (reader.peek() == ElementType.BEGIN_ARRAY) {

          String name = reader.beginArray();

          if (name.equals("annotations")) {
            while (reader.peek() != ElementType.END_ARRAY) {
              annotations.add(Annotation.read(reader));
            }
            reader.endArray();
          } else {
            throw new IOException("Unexpected array named [" + name + "]");
          }
        }
      }
      reader.endDocument();

      Document document = new Document(
          docProperties.get("id").asString(),
          docProperties.get("content").asString()
      );

      document.putAllAttributes(attributeValMap);
      annotations.forEach(annotation -> {
        Annotation newAnnotation = new Annotation(document, annotation.getType(), annotation.start(), annotation.end());
        newAnnotation.putAllAttributes(annotation.getAttributeMap());
        newAnnotation.setId(annotation.getId());
        document.annotationSet.add(newAnnotation);
      });
      if (annotations.size() > 0) {
        long max = annotations.stream().mapToLong(Annotation::getId).max().orElseGet(() -> 0L);
        document.idGenerator.set(max + 1);
      }
      return document;
    }
  }


}//END OF Document
