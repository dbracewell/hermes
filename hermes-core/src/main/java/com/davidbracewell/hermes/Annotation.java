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

import com.davidbracewell.collection.Collect;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.io.structured.ElementType;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.io.structured.StructuredWriter;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * <p>An annotation provides information and attributes relating to specific segments of text in a document, which may
 * include the entire document. Annotations on documents are specified as a span which has a start and end character
 * offset associated with it. </p>
 *
 * @author David B. Bracewell
 */
public class Annotation extends Fragment implements Serializable {

  private static final long serialVersionUID = 1L;
  /**
   * The constant DETATCHED_ID.
   */
  public static long DETATCHED_ID = Long.MIN_VALUE;
  private final AnnotationType annotationType;
  private long id = DETATCHED_ID;
  private transient Annotation[] tokens;

  /**
   * Instantiates a new Annotation.
   *
   * @param owner          the document that owns this annotation
   * @param annotationType The type of annotation
   * @param start          the start
   * @param end            the end
   */
  public Annotation(@Nonnull Document owner, @Nonnull AnnotationType annotationType, int start, int end) {
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
  public Annotation(@Nonnull HString string, @Nonnull AnnotationType annotationType) {
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

  static Annotation read(StructuredReader reader) throws IOException {
    reader.beginObject();

    Map<String, Val> annotationProperties = new HashMap<>();
    Map<Attribute, Val> attributeValMap = Collections.emptyMap();

    while (reader.peek() != ElementType.END_OBJECT) {
      if (reader.peek() == ElementType.NAME) {
        Collect.put(annotationProperties, reader.nextKeyValue());
      } else if (reader.peek() == ElementType.BEGIN_OBJECT) {
        String name = reader.beginObject();
        if (name.equals("attributes")) {
          attributeValMap = Attribute.readAttributeList(reader);
        } else {
          throw new IOException("Unexpected object named [" + name + "]");
        }
      } else {
        throw new IOException("Unexpected " + reader.peek());
      }
    }

    Annotation annotation = Fragments.detatchedAnnotation(
        AnnotationType.create(annotationProperties.get("type").asString()),
        annotationProperties.get("start").asIntegerValue(),
        annotationProperties.get("end").asIntegerValue()
    );
    annotation.setId(annotationProperties.get("id").asLongValue());
    annotation.putAllAttributes(attributeValMap);


    reader.endObject();
    return annotation;
  }

  /**
   * Gets the unique id associated with the annotation.
   *
   * @return the id of the annotation that is unique with in its document or <code>Annotation.DETATCHED_ID</code> if the
   * annotation is not attached to the document.
   */
  public long getId() {
    return id;
  }

  void setId(long id) {
    this.id = id;
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
   * @return True if the annotation is detatched
   */
  public boolean isDetached() {
    return document() == null || id == DETATCHED_ID;
  }

  /**
   * Is this annotation a gold standard annotation, i.e. does its type start with <code>@</code>
   *
   * @return True if this annotation is a gold standard annotation
   */
  public boolean isGoldAnnotation() {
    return annotationType.name().startsWith("@");
  }

  @Override
  public boolean isInstance(AnnotationType type) {
    return Objects.equals(type, this.annotationType);
  }

  /**
   * Gets the next annotation with the same type as this one
   *
   * @return The next annotation with the same type as this one or an empty fragment
   */
  public Annotation nextOfSameType() {
    return nextOfType(annotationType);
  }

  /**
   * Gets the annotation of a given type that is next in order (of span) to this one
   *
   * @param type the type of annotation wanted
   * @return the next annotation of the given type or null
   */
  public Annotation nextOfType(@Nonnull AnnotationType type) {
    //return document() == null ? Fragments.emptyOrphan() : document().getAnnotationSet().next(this, type);
    return null;
  }

  /**
   * Gets the previous annotation with the same type as this one
   *
   * @return The previous annotation with the same type as this one or an empty fragment
   */
  public Annotation previousOfSameType() {
    return previousOfType(annotationType);
  }

  /**
   * Gets the annotation of a given type that is previous in order (of span) to this one
   *
   * @param type the type of annotation wanted
   * @return the previous annotation of the given type or null
   */
  public Annotation previousOfType(AnnotationType type) {
    //return document() == null ? Fragments.emptyOrphan() : document().getAnnotationSet().previous(this, type);
    return null;
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

  void write(StructuredWriter writer) throws IOException {
    writer.beginObject();

    writer.writeKeyValue("type", annotationType.name());
    writer.writeKeyValue("start", start());
    writer.writeKeyValue("end", end());
    writer.writeKeyValue("id", getId());

    if (getAttributeMap().size() > 0) {
      writer.beginObject("attributes");
      for (Map.Entry<Attribute, Val> entry : getAttributes()) {
        entry.getKey().write(writer, entry.getValue());
      }
      writer.endObject();
    }

    writer.endObject();
  }

}//END OF Annotation
