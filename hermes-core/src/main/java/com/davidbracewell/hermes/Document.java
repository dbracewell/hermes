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
import com.davidbracewell.tuple.Tuple2;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>The document is the central object class in the TIPSTER architecture. It serves as repository for Attributes and
 * Annotations. In the TIPSTER architecture a document is part of one or more collections and can only be accessed as a
 * member of that collection. In this architecture the document is independent of the collection, but  can be linked
 * back to the collection through an Attribute.</p>
 * <p>Documents are not normally constructed directly, instead they are built through the {@link DocumentFactory} which
 * takes care of normalizing and parsing the underlying text. Pre-tokenized text can be converted into a document using
 * the {@link DocumentFactory#fromTokens(Iterable)} method.</p>
 *
 * @author David B. Bracewell
 */
public class Document extends HString {

  private static final long serialVersionUID = 1L;
  private final Map<Attribute, Val> attributes = new HashMap<>(5);
  private final String content;
  private final AtomicLong idGenerator = new AtomicLong(0);
  private final AnnotationSet annotationSet;
  private String id;

  Document(String id, @Nonnull String content) {
    super(0, content.length());
    this.content = content;
    setId(id);
    this.annotationSet = Config.get("hermes.AnnotationSetImpl").asOrElse(AnnotationSet.class, DefaultAnnotationSet::new);
  }


  Document(String id, @Nonnull String content, Language language) {
    super(0, content.length());
    this.content = content;
    setId(id);
    setLanguage(language);
    this.annotationSet = Config.get("hermes.AnnotationSetImpl").asOrElse(AnnotationSet.class, DefaultAnnotationSet::new);
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

  /**
   * Gets annotations of the given type that overlap with the given span.
   *
   * @param type the type of annotation
   * @param span the span to search for overlapping annotations
   * @return All annotations of the given type on the document that overlap with the give span.
   */
  public List<Annotation> getOverlapping(AnnotationType type, @Nonnull Span span) {
    return annotationSet.select(span, a -> a.isInstance(type) && a.overlaps(span));
  }

  /**
   * Gets annotations of the given type that are contained within the given span.
   *
   * @param type the type of annotation
   * @param span the span to search for annotations in
   * @return All annotations of the given type on the document that are contained within the give span.
   */
  public List<Annotation> getContaining(AnnotationType type, @Nonnull Span span) {
    return annotationSet.select(span, a -> a.isInstance(type) && a.encloses(span));
  }

  /**
   * Gets annotations of the given type that are during, i.e. fully enclose, the given span.
   *
   * @param type the type of annotation
   * @param span the span to search for annotations in
   * @return All annotations of the given type on the document that are during, i.e. fully enclose, the give span.
   */
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


  /**
   * Creates an annotation of the given type encompassing the given span. The annotation is added to the document and
   * has a unique id assigned.
   *
   * @param type the type of annotation
   * @param span the span of the annotation
   * @return the created annotation
   */
  public Annotation createAnnotation(@Nonnull AnnotationType type, @Nonnull Span span) {
    return createAnnotation(type, span.start(), span.end(), Collections.emptyMap());
  }

  /**
   * Creates an annotation of the given type encompassing the given span. The annotation is added to the document and
   * has a unique id assigned.
   *
   * @param type the type of annotation
   * @param span the span of the annotation
   * @return the created annotation
   */
  public Annotation createAnnotation(@Nonnull AnnotationType type, @Nonnull HString span) {
    return createAnnotation(type, span.start(), span.end(), span.getAttributeMap());
  }


  /**
   * Creates an annotation of the given type encompassing the given span. The annotation is added to the document and
   * has a unique id assigned.
   *
   * @param type  the type of annotation
   * @param start the start of the span
   * @param end   the end of the span
   * @return the created annotation
   */
  public Annotation createAnnotation(@Nonnull AnnotationType type, int start, int end) {
    return createAnnotation(type, start, end, Collections.emptyMap());
  }

  /**
   * Creates an annotation of the given type encompassing the given span and having the given attributes. The
   * annotation
   * is added to the document and has a unique id assigned.
   *
   * @param type         the type of annotation
   * @param start        the start of the span
   * @param end          the end of the span
   * @param attributeMap the attributes associated with the annotation
   * @return the created annotation
   */
  public Annotation createAnnotation(@Nonnull AnnotationType type, int start, int end, @Nonnull Map<Attribute, ?> attributeMap) {
    Preconditions.checkArgument(start >= start(), "Annotation must have a starting position >= the start of the document");
    Preconditions.checkArgument(end <= end(), "Annotation must have a ending position <= the end of the document");
    Annotation annotation = new Annotation(this, type, start, end);
    annotation.setId(idGenerator.getAndIncrement());
    annotation.putAllAttributes(attributeMap);
    annotationSet.add(annotation);
    return annotation;
  }


  /**
   * Gets annotation set associated with the document
   *
   * @return the annotation set
   */
  public AnnotationSet getAnnotationSet() {
    return annotationSet;
  }

  /**
   * Writes the document in a structured format
   *
   * @param format   the format to write in (supports xml and json)
   * @param resource the resource to write to
   * @throws IOException something went wrong writing
   */
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

        writer.beginArray("completed");
        for (AnnotationType type : getAnnotationSet().getCompleted()) {
          writer.beginObject();
          writer.writeKeyValue(type.name(), getAnnotationSet().getAnnotationProvider(type));
          writer.endObject();
        }
        writer.endArray();

        writer.beginArray("annotations");
        for (Annotation annotation : annotationSet) {
          annotation.write(writer);
        }
        writer.endArray();
      }
      writer.endDocument();
    }
  }

  /**
   * Converts the document to json
   *
   * @return JSON representation of the document
   */
  public String toJson() {
    try {
      Resource stringResource = Resources.fromString();
      write(StructuredFormat.JSON, stringResource);
      return stringResource.readToString().trim();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Creates a document from a JSON representation (created by the write or toJson methods)
   *
   * @param jsonString the json string
   * @return the document
   */
  public static Document fromJson(String jsonString) {
    try {
      return read(StructuredFormat.JSON, Resources.fromString(jsonString));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Reads in a document in structured foramt (xml and json are supported).
   *
   * @param format   the format of the file
   * @param resource the resource the file is in
   * @return the document
   * @throws IOException something went wrong reading the file
   */
  public static Document read(@Nonnull StructuredFormat format, @Nonnull Resource resource) throws IOException {
    try (StructuredReader reader = format.createReader(resource)) {
      reader.beginDocument();

      Map<String, Val> docProperties = new HashMap<>();
      List<Annotation> annotations = new LinkedList<>();
      Map<Attribute, Val> attributeValMap = Collections.emptyMap();
      Map<AnnotationType, String> completed = new HashMap<>();

      while (reader.peek() != ElementType.END_DOCUMENT) {
        if (reader.peek() == ElementType.NAME) {

          Collect.put(docProperties, reader.nextKeyValue());

        } else if (reader.peek() == ElementType.BEGIN_OBJECT) {

          String name = reader.beginObject();
          switch (name) {
            case "attributes":
              attributeValMap = Attribute.readAttributeList(reader);
              break;
            case "completed":
              while (reader.peek() != ElementType.END_OBJECT) {
                Tuple2<String, Val> keyValue = reader.nextKeyValue();
                completed.put(AnnotationType.create(keyValue.getKey()), keyValue.getValue().asString());
              }
              break;
            default:
              throw new IOException("Unexpected object named [" + name + "]");
          }

          reader.endObject();

        } else if (reader.peek() == ElementType.BEGIN_ARRAY) {

          String name = reader.beginArray();

          if (name.equals("annotations")) {
            while (reader.peek() != ElementType.END_ARRAY) {
              annotations.add(Annotation.read(reader));
            }
          } else {
            throw new IOException("Unexpected array named [" + name + "]");
          }

          reader.endArray();
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
      completed.entrySet().forEach(e -> document.annotationSet.setIsCompleted(e.getKey(), true, e.getValue()));
      if (annotations.size() > 0) {
        long max = annotations.stream().mapToLong(Annotation::getId).max().orElseGet(() -> 0L);
        document.idGenerator.set(max + 1);
      }
      return document;
    }
  }


}//END OF Document
