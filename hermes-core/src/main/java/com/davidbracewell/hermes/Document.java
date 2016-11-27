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
import com.davidbracewell.collection.Streams;
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
import com.google.common.collect.Maps;
import lombok.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
   private final Map<AttributeType, Val> attributes = new HashMap<>(5);
   private final String content;
   private final AtomicLong idGenerator = new AtomicLong(0);
   private final AnnotationSet annotationSet;
   private volatile List<Annotation> tokens;
   private String id;


   /**
    * Instantiates a new Document.
    *
    * @param id      the id
    * @param content the content
    */
   Document(String id, @NonNull String content) {
      this(id, content, null);
   }

   /**
    * Instantiates a new Document.
    *
    * @param id       the id
    * @param content  the content
    * @param language the language
    */
   Document(String id, @NonNull String content, Language language) {
      super(0, content.length());
      this.content = content;
      setId(id);
      setLanguage(language);
      this.annotationSet = new DefaultAnnotationSet();
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param text the text content making up the document
    * @return the document
    */
   public static Document create(@NonNull String text) {
      return DocumentFactory.getInstance().create(text);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param text     the text content making up the document
    * @param language the language of the content
    * @return the document
    */
   public static Document create(@NonNull String text, @NonNull Language language) {
      return DocumentFactory.getInstance().create(text, language);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param text       the text content making up the document
    * @param language   the language of the content
    * @param attributes the attributes, i.e. metadata, associated with the document
    * @return the document
    */
   public static Document create(@NonNull String text, @NonNull Language language, @NonNull Map<AttributeType, ?> attributes) {
      return DocumentFactory.getInstance().create(text, language, attributes);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param text       the text content making up the document
    * @param attributes the attributes, i.e. metadata, associated with the document
    * @return the document
    */
   public static Document create(@NonNull String text, @NonNull Map<AttributeType, ?> attributes) {
      return DocumentFactory.getInstance().create(text, Hermes.defaultLanguage(), attributes);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param id   the document id
    * @param text the text content making up the document
    * @return the document
    */
   public static Document create(@NonNull String id, @NonNull String text) {
      return DocumentFactory.getInstance().create(id, text);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param id       the document id
    * @param text     the text content making up the document
    * @param language the language of the content
    * @return the document
    */
   public static Document create(@NonNull String id, @NonNull String text, @NonNull Language language) {
      return DocumentFactory.getInstance().create(id, text, language);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param id         the document id
    * @param text       the text content making up the document
    * @param language   the language of the content
    * @param attributes the attributes, i.e. metadata, associated with the document
    * @return the document
    */
   public static Document create(@NonNull String id, @NonNull String text, @NonNull Language language, @NonNull Map<AttributeType, ?> attributes) {
      return DocumentFactory.getInstance().create(id, text, language, attributes);
   }

   /**
    * Convenience method for creating a document using the default document factory.
    *
    * @param id         the document id
    * @param text       the text content making up the document
    * @param attributes the attributes, i.e. metadata, associated with the document
    * @return the document
    */
   public static Document create(@NonNull String id, @NonNull String text, @NonNull Map<AttributeType, ?> attributes) {
      return DocumentFactory.getInstance().create(id, text, Hermes.defaultLanguage(), attributes);
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
    * Reads in a document in structured format (xml and json are supported).
    *
    * @param format   the format of the file
    * @param resource the resource the file is in
    * @return the document
    * @throws IOException something went wrong reading the file
    */
   public static Document read(@NonNull StructuredFormat format, @NonNull Resource resource) throws IOException {
      try (StructuredReader reader = format.createReader(resource)) {
         reader.beginDocument();

         Map<String, Val> docProperties = new HashMap<>();
         List<Annotation> annotations = new LinkedList<>();
         Map<AttributeType, Val> attributeValMap = Collections.emptyMap();
         Map<AnnotatableType, String> completed = new HashMap<>();

         while (reader.peek() != ElementType.END_DOCUMENT) {
            if (reader.peek() == ElementType.NAME) {

               com.davidbracewell.collection.map.Maps.put(docProperties, reader.nextKeyValue());

            } else if (reader.peek() == ElementType.BEGIN_OBJECT) {

               String name = reader.beginObject();

               switch (name) {
                  case "attributes":
                     attributeValMap = AttributeType.readAttributeList(reader);
                     break;
                  case "completed":
                     while (reader.peek() != ElementType.END_OBJECT) {
                        Tuple2<String, Val> keyValue = reader.nextKeyValue();
                        completed.put(Types.from(keyValue.getKey()), keyValue.getValue().asString());
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

         if (!docProperties.containsKey("content")) {
            throw new IOException("Malformed document: no \"content\" file is present");
         }

         Document document = new Document(
                                            docProperties.containsKey("id") ? docProperties.get("id").asString() : null,
                                            docProperties.get("content").asString()
         );

         document.putAll(attributeValMap);
         annotations.forEach(annotation -> {
            Annotation newAnnotation = new Annotation(document,
                                                      annotation.getType(),
                                                      annotation.start(),
                                                      annotation.end());
            newAnnotation.putAll(annotation.getAttributeMap());
            newAnnotation.setId(annotation.getId());
            newAnnotation.addAll(annotation.allRelations(false));
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

   @Override
   public Set<AttributeType> attributes() {
      return attributes.keySet();
   }

   @Override
   public char charAt(int index) {
      return content.charAt(index);
   }

   @Override
   public List<Annotation> tokens() {
      if (tokens == null) {
         synchronized (this) {
            if (tokens == null) {
               tokens = get(Types.TOKEN);
            }
         }
      }
      return tokens;
   }

   /**
    * Checks is if a given {@link AnnotatableType} is completed, i.e. been added by an annotator.
    *
    * @param type the type to check
    * @return True if the type is complete, False if not
    */
   public boolean isCompleted(@NonNull AnnotatableType type) {
      return annotationSet.isCompleted(type);
   }

   /**
    * Convenience method for annotating the document with the given annotatable types.
    *
    * @param types the types to annotate
    */
   public void annotate(@NonNull AnnotatableType... types) {
      Pipeline.process(this, types);
   }

   /**
    * Gets the set of completed annotations.
    *
    * @return the set of completed
    */
   public Set<AnnotatableType> completedAnnotations() {
      return annotationSet.getCompleted();
   }

   /**
    * Creates an annotation of the given type encompassing the given span. The annotation is added to the document and
    * has a unique id assigned.
    *
    * @param type the type of annotation
    * @param span the span of the annotation
    * @return the created annotation
    */
   public Annotation createAnnotation(@NonNull AnnotationType type, @NonNull Span span) {
      return createAnnotation(type, span.start(), span.end(), Collections.emptyMap());
   }

   /**
    * Creates an annotation of the given type encompassing the given span. The annotation is added to the document and
    * has a unique id assigned.
    *
    * @param type                 the type of annotation
    * @param span                 the span of the annotation
    * @param copyAttributes       the copy attributes
    * @param filterAttributeTypes the filter attributes
    * @return the created annotation
    */
   public Annotation createAnnotation(@NonNull AnnotationType type, @NonNull HString span, boolean copyAttributes, Set<AttributeType> filterAttributeTypes) {
      Map<AttributeType, ?> map = copyAttributes ? span.getAttributeMap() : Collections.emptyMap();
      if (filterAttributeTypes != null) {
         map = Maps.filterEntries(map, e -> filterAttributeTypes.contains(e.getKey()));
      }
      return createAnnotation(type, span.start(), span.end(), map);
   }

   /**
    * Creates an annotation of the given type encompassing the given span. The annotation is added to the document and
    * has a unique id assigned.
    *
    * @param type           the type of annotation
    * @param span           the span of the annotation
    * @param copyAttributes the copy attributes
    * @return the created annotation
    */
   public Annotation createAnnotation(@NonNull AnnotationType type, @NonNull HString span, boolean copyAttributes) {
      return createAnnotation(type, span, copyAttributes, null);
   }

   /**
    * Creates an annotation of the given type encompassing the given span. The annotation is added to the document and
    * has a unique id assigned.
    *
    * @param type         the type of annotation
    * @param span         the span of the annotation
    * @param attributeMap the attributes associated with the annotation
    * @return the created annotation
    */
   public Annotation createAnnotation(@NonNull AnnotationType type, @NonNull Span span, @NonNull Map<AttributeType, ?> attributeMap) {
      return createAnnotation(type, span.start(), span.end(), attributeMap);
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
   public Annotation createAnnotation(@NonNull AnnotationType type, int start, int end) {
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
   public Annotation createAnnotation(@NonNull AnnotationType type, int start, int end, @NonNull Map<AttributeType, ?> attributeMap) {
      Preconditions.checkArgument(start >= start(),
                                  "Annotation must have a starting position >= the start of the document");
      Preconditions.checkArgument(end <= end(), "Annotation must have a ending position <= the end of the document");
      Annotation annotation = new Annotation(this, type, start, end);
      annotation.setId(idGenerator.getAndIncrement());
      annotation.putAll(attributeMap);
      annotationSet.add(annotation);
      return annotation;
   }

   @Override
   public Document document() {
      return this;
   }

   /**
    * Gets annotations of the given type that overlap with the given span.
    *
    * @param type the type of annotation
    * @param span the span to search for overlapping annotations
    * @return All annotations of the given type on the document that overlap with the give span.
    */
   public List<Annotation> get(AnnotationType type, @NonNull Span span) {
      return annotationSet.select(span, a -> a.isInstance(type) && a.overlaps(span));
   }

   /**
    * Gets annotations of the given type that overlap with the given span and meet the given filter.
    *
    * @param type   the type of annotation
    * @param span   the span to search for overlapping annotations
    * @param filter the filter to use on the annotations
    * @return All annotations of the given type on the document that overlap with the give span and meet the given
    * filter.
    */
   public List<Annotation> get(AnnotationType type, @NonNull Span span, @NonNull Predicate<? super Annotation> filter) {
      return annotationSet.select(span, a -> filter.test(a) && a.isInstance(type) && a.overlaps(span));
   }

   @Override
   public List<Annotation> get(AnnotationType type) {
      return annotationSet.select(a -> a.isInstance(type));
   }

   /**
    * Removes the given annotation from the document
    *
    * @param annotation the annotation to remove
    * @return True if the annotation was successfully removed, False otherwise
    */
   public boolean remove(Annotation annotation) {
      return annotationSet.remove(annotation);
   }

   /**
    * Removes all annotations of a given type.
    *
    * @param type the type of to remove
    */
   public void removeAnnotationType(AnnotationType type) {
      annotationSet.removeAll(type);
   }

   @Override
   public List<Annotation> getAllAnnotations() {
      return Streams.asStream(annotationSet.iterator()).collect(Collectors.toList());
   }

   @Override
   public List<Annotation> get(AnnotationType type, @NonNull Predicate<? super Annotation> filter) {
      if (type == null) {
         return Collections.emptyList();
      }
      return annotationSet.select(a -> filter.test(a) && a.isInstance(type));
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
    * Gets an annotation on the document by its ID.
    *
    * @param id the id of the annotation to retrieve
    * @return the annotation
    */
   public Optional<Annotation> getAnnotation(long id) {
      return Optional.ofNullable(annotationSet.get(id));
   }

   @Override
   protected Map<AttributeType, Val> getAttributeMap() {
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
         this.id = UUID.randomUUID().toString();
      } else {
         this.id = id;
      }
   }

   @Override
   public Language getLanguage() {
      if (contains(Types.LANGUAGE)) {
         return get(Types.LANGUAGE).as(Language.class);
      }
      return Hermes.defaultLanguage();
   }

   @Override
   public boolean isDocument() {
      return true;
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

   @Override
   public String toString() {
      return content;
   }

   /**
    * Writes the document in a structured format
    *
    * @param format   the format to write in (supports xml and json)
    * @param resource the resource to write to
    * @throws IOException something went wrong writing
    */
   public void write(@NonNull StructuredFormat format, @NonNull Resource resource) throws IOException {
      try (StructuredWriter writer = format.createWriter(resource)) {
         writer.beginDocument();
         writer.writeKeyValue("id", getId());
         writer.writeKeyValue("content", toString());

         if (attributes.size() > 0) {
            writer.beginObject("attributes");
            for (Map.Entry<AttributeType, Val> entry : attributeValues()) {
               entry.getKey().write(writer, entry.getValue());
            }
            writer.endObject();
         }

         if (annotationSet.size() > 0) {


            writer.beginObject("completed");
            for (AnnotatableType type : getAnnotationSet().getCompleted()) {
               writer.writeKeyValue(type.canonicalName(), getAnnotationSet().getAnnotationProvider(type));
            }
            writer.endObject();

            writer.beginArray("annotations");
            for (Annotation annotation : annotationSet) {
               annotation.write(writer);
            }
            writer.endArray();
         }
         writer.endDocument();
      }
   }

}//END OF Document
