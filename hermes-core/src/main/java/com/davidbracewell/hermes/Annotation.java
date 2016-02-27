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

import com.davidbracewell.Tag;
import com.davidbracewell.collection.Collect;
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.tag.EntityType;
import com.davidbracewell.hermes.tag.RelationType;
import com.davidbracewell.hermes.tag.Relations;
import com.davidbracewell.io.structured.ElementType;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.io.structured.StructuredWriter;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.base.Preconditions;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Associates a type, e.g. token, sentence, named entity, and a set of attributes, e.g. part of speech and entity type,
 * to  a specific  span of a document, which may include the entire document. Annotation type information is defined
 * via
 * the {@link AnnotationType} class.
 * </p>
 * <p>
 * Commonly, annotations have an associated <code>Tag</code> attribute which acts as label. Examples of tags include
 * part-of-speech and entity type. Tags can be retrieved using the {@link #getTag()} method. Annotation types specify
 * the attribute that represents the tag of an annotation of its type (in some cases annotations may have multiple tags
 * and this definition allows the primary tag to specified). If no tag is specified, a default attribute of
 * <code>TAG</code>.
 * </p>
 *
 * @author David B. Bracewell
 */
public final class Annotation extends Fragment implements Serializable {

  private static final long serialVersionUID = 1L;
  /**
   * The ID associated with a detached annotation
   */
  public static long DETACHED_ID = Long.MIN_VALUE;
  private final AnnotationType annotationType;
  private final Set<Relation> relations = new HashSet<>();
  private long id = DETACHED_ID;
  private volatile transient Annotation[] tokens;

  /**
   * Instantiates a new Annotation.
   *
   * @param owner          the document that owns this annotation
   * @param annotationType The type of annotation
   * @param start          the start
   * @param end            the end
   */
  public Annotation(@NonNull Document owner, @NonNull AnnotationType annotationType, int start, int end) {
    super(owner, start, end);
    Preconditions.checkArgument(start <= end, "Annotations must have a start character index that is less than or equal to the ending index.");
    this.annotationType = annotationType;
  }


  /**
   * Instantiates a new Annotation.
   *
   * @param string         the string that this annotation will encompass
   * @param annotationType the annotation type
   */
  public Annotation(@NonNull HString string, @NonNull AnnotationType annotationType) {
    super(string);
    this.annotationType = annotationType;
  }

  /**
   * Instantiates a new Annotation.
   */
  protected Annotation() {
    this.annotationType = AnnotationType.ROOT;
  }


  /**
   * Instantiates a new Annotation.
   *
   * @param type  the type
   * @param start the start
   * @param end   the end
   */
  protected Annotation(AnnotationType type, int start, int end) {
    super(null, start, end);
    this.annotationType = type == null ? AnnotationType.ROOT : type;
  }

  /**
   * Read annotation.
   *
   * @param reader the reader
   * @return the annotation
   * @throws IOException the io exception
   */
  static Annotation read(StructuredReader reader) throws IOException {
    reader.beginObject();
    Map<String, Val> annotationProperties = new HashMap<>();
    Map<Attribute, Val> attributeValMap = Collections.emptyMap();
    List<Relation> relations = new LinkedList<>();

    while (reader.peek() != ElementType.END_OBJECT) {
      if (reader.peek() == ElementType.NAME) {
        Collect.put(annotationProperties, reader.nextKeyValue());
      } else if (reader.peek() == ElementType.BEGIN_OBJECT) {
        reader.beginObject("attributes");
        attributeValMap = Attribute.readAttributeList(reader);
        reader.endObject();
      } else if (reader.peek() == ElementType.BEGIN_ARRAY) {
        reader.beginArray("relations");
        while (reader.peek() != ElementType.END_ARRAY) {
          reader.beginObject();
          Map<String, Val> rel = reader.nextMap();
          relations.add(new Relation(rel.get("type").as(RelationType.class), rel.get("value").asString(), rel.get("target").asLongValue()));
          reader.endObject();
        }
        reader.endArray();
      } else {
        throw new IOException("Unexpected " + reader.peek());
      }
    }

    Annotation annotation = Fragments.detachedAnnotation(
      AnnotationType.create(annotationProperties.get("type").asString()),
      annotationProperties.get("start").asIntegerValue(),
      annotationProperties.get("end").asIntegerValue()
    );
    annotation.relations.addAll(relations);
    annotation.setId(annotationProperties.get("id").asLongValue());
    annotation.putAll(attributeValMap);
    reader.endObject();
    return annotation;
  }

  @Override
  public void add(@NonNull Relation relation) {
    if (!relations.contains(relation)) {
      relations.add(relation);
    }
  }

  @Override
  public void addAll(@NonNull Collection<Relation> relations) {
    this.relations.addAll(relations);
  }

  @Override
  public Collection<Relation> allRelations(boolean includeSubAnnotations) {
    return getRelationStream(includeSubAnnotations).collect(Collectors.toSet());
  }


  @Override
  public List<Annotation> children() {
    List<Annotation> tokens;
    if (document().getAnnotationSet().isCompleted(Types.SENTENCE)) {
      tokens = first(Types.SENTENCE).tokens();
    } else {
      tokens = document().tokens();
    }
    Set<Annotation> myTokens = new HashSet<>(tokens());
    myTokens.add(this);
    return tokens.stream()
      .filter(t -> !t.overlaps(this))
      .filter(t -> t.parent().filter(myTokens::contains).isPresent())
      .collect(Collectors.toList());
  }

  @Override
  public Optional<Tuple2<String, Annotation>> dependencyRelation() {
    return getRelationStream(true)
      .filter(r -> r.getType() == Relations.DEPENDENCY)
      .filter(r -> r.getTarget(this).isPresent())
      .filter(r -> !this.overlaps(r.getTarget(this).get()))
      .map(r -> Tuple2.of(r.getValue(), r.getTarget(this).get()))
      .findFirst();
  }

  @Override
  public List<Relation> get(@NonNull RelationType relationType, boolean includeSubAnnotations) {
    return getRelationStream(includeSubAnnotations).filter(r -> r.getType().equals(relationType)).collect(Collectors.toList());
  }

  /**
   * Gets the unique id associated with the annotation.
   *
   * @return the id of the annotation that is unique with in its document or <code>Annotation.DETACHED_ID</code> if the
   * annotation is not attached to the document.
   */
  public long getId() {
    return id;
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  void setId(long id) {
    this.id = id;
  }

  private Stream<Relation> getRelationStream(boolean includeSubAnnotations) {
    Stream<Relation> relationStream = relations.stream();
    if (this.getType() != Types.TOKEN && includeSubAnnotations) {
      relationStream = Stream.concat(
        relationStream,
        getAllAnnotations().stream().filter(a -> a != this).flatMap(token -> token.allRelations(false).stream())
      );
    }
    return relationStream;
  }

  /**
   * <p>
   * Gets the tag, if one, associated with the annotation. The tag attribute is defined for an annotation type using
   * the <code>tag</code> configuration property, e.g. <code>Annotation.TYPE.tag=fully.qualified.tag.implementation</code>.
   * Tags must implement the <code>Tag</code> interface. If no tag type is defined, the <code>Attrs.TAG</code>
   * attribute will be retrieved.
   * </p>
   *
   * @return An optional containing the tag if present
   */
  public Optional<Tag> getTag() {
    if (isInstance(Types.TOKEN)) {
      return Optional.ofNullable(getPOS());
    } else if (isInstance(Types.ENTITY)) {
      return Optional.ofNullable(get(Attrs.ENTITY_TYPE).as(EntityType.class));
    }
    Attribute tagAttribute = annotationType.getTagAttribute();
    if (tagAttribute == null) {
      return Optional.ofNullable(get(Attrs.TAG).as(Tag.class));
    }
    return Optional.ofNullable(get(tagAttribute).as(Tag.class));
  }

  /**
   * Gets the type of the annotation
   *
   * @return the annotation type
   */
  public final AnnotationType getType() {
    return annotationType;
  }

  @Override
  public boolean isAnnotation() {
    return true;
  }

  /**
   * Is this annotation detached, i.e. not associated with a document?
   *
   * @return True if the annotation is detached
   */
  public boolean isDetached() {
    return document() == null || id == DETACHED_ID;
  }

  @Override
  public boolean isInstance(AnnotationType type) {
    return this.annotationType.isInstance(type);
  }

  /**
   * Is instance of tag boolean.
   *
   * @param tag the tag
   * @return the boolean
   */
  public boolean isInstanceOfTag(String tag) {
    return !StringUtils.isNullOrBlank(tag) && isInstanceOfTag(Cast.<Tag>as(getType().getTagAttribute().getValueType().convert(tag)));
  }

  /**
   * Is instance of tag boolean.
   *
   * @param tag the tag
   * @return the boolean
   */
  public boolean isInstanceOfTag(Tag tag) {
    return tag != null && getTag().filter(t -> t.isInstance(tag)).isPresent();
  }

  /**
   * Gets the next annotation with the same type as this one
   *
   * @return The next annotation with the same type as this one or an empty fragment
   */
  public Annotation next() {
    return next(annotationType);
  }

  /**
   * Gets the annotation of a given type that is next in order (of span) to this one
   *
   * @param type the type of annotation wanted
   * @return the next annotation of the given type or null
   */
  public Annotation next(@NonNull AnnotationType type) {
    return document() == null ? Fragments.detachedEmptyAnnotation() : document().getAnnotationSet().next(this, type);
  }

  /**
   * Gets the previous annotation with the same type as this one
   *
   * @return The previous annotation with the same type as this one or an empty fragment
   */
  public Annotation previous() {
    return previous(annotationType);
  }

  /**
   * Gets the annotation of a given type that is previous in order (of span) to this one
   *
   * @param type the type of annotation wanted
   * @return the previous annotation of the given type or null
   */
  public Annotation previous(AnnotationType type) {
    return document() == null ? Fragments.detachedEmptyAnnotation() : document().getAnnotationSet().previous(this, type);
  }

  @Override
  public void remove(@NonNull Relation relation) {
    relations.remove(relation);
  }

  @Override
  public List<Annotation> sources(@NonNull RelationType type, @NonNull String value, boolean includeSubAnnotations) {
    Set<Annotation> targets = includeSubAnnotations ? new HashSet<>(getAllAnnotations()) : new HashSet<>();
    targets.add(this);
    return document().getAllAnnotations().stream()
      .filter(a -> !a.overlaps(this))
      .filter(a -> a.targets(type, value, false).stream().filter(targets::contains).count() > 0)
      .collect(Collectors.toList());
  }

  @Override
  public List<Annotation> sources(@NonNull RelationType type, boolean includeSubAnnotations) {
    Set<Annotation> targets = includeSubAnnotations ? new HashSet<>(getAllAnnotations()) : new HashSet<>();
    targets.add(this);
    return document().getAllAnnotations().stream()
      .filter(a -> !a.overlaps(this))
      .filter(a -> a.targets(type, false).stream().filter(targets::contains).count() > 0)
      .collect(Collectors.toList());
  }

  @Override
  public List<Annotation> targets(@NonNull RelationType type, boolean includeSubAnnotations) {
    return getRelationStream(includeSubAnnotations)
      .filter(r -> r.getType().equals(type))
      .filter(r -> r.getTarget(this).isPresent())
      .map(r -> r.getTarget(this).get())
      .collect(Collectors.toList());
  }

  @Override
  public List<Annotation> targets(@NonNull RelationType type, @NonNull String value, boolean includeSubAnnotations) {
    return getRelationStream(includeSubAnnotations)
      .filter(r -> r.getType().equals(type) && StringUtils.safeEquals(r.getValue(), value, true))
      .map(r -> document().getAnnotationSet().get(r.getTarget()))
      .collect(Collectors.toList());
  }

  @Override
  public List<Annotation> tokens() {
    if (tokens == null) {
      synchronized (this) {
        if (tokens == null) {
          List<Annotation> tokenList = super.tokens();
          if (!tokenList.isEmpty()) {
            tokens = tokenList.toArray(new Annotation[tokenList.size()]);
          }
        }
      }
    }
    return tokens == null ? Collections.emptyList() : Arrays.asList(tokens);
  }

  /**
   * Write.
   *
   * @param writer the writer
   * @throws IOException the io exception
   */
  void write(StructuredWriter writer) throws IOException {
    writer.beginObject();

    writer.writeKeyValue("type", annotationType.name());
    writer.writeKeyValue("start", start());
    writer.writeKeyValue("end", end());
    writer.writeKeyValue("id", getId());

    if (Config.get("Annotation.writeContent").asBooleanValue(false)) {
      writer.writeKeyValue("content", toString());
    }

    if (getAttributeMap().size() > 0) {
      writer.beginObject("attributes");
      for (Map.Entry<Attribute, Val> entry : attributeValues()) {
        entry.getKey().write(writer, entry.getValue());
      }
      writer.endObject();
    }

    if (relations.size() > 0) {
      writer.beginArray("relations");
      for (Relation relation : relations) {
        writer.beginObject();
        writer.writeKeyValue("type", relation.getType());
        writer.writeKeyValue("value", relation.getValue());
        writer.writeKeyValue("target", relation.getTarget());
        writer.endObject();
      }
      writer.endArray();
    }

    writer.endObject();
  }

}//END OF Annotation
