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


import com.davidbracewell.DynamicEnum;
import com.davidbracewell.EnumValue;
import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.reflection.BeanUtils;
import com.davidbracewell.string.StringUtils;

import javax.annotation.Nonnull;
import java.io.ObjectStreamException;
import java.util.Collection;

/**
 * <p>
 * An <code>AnnotationType</code> serves to define the structure and source of a specific annotation. The definition
 * provided by the type facillitates the portability of the annotation between different modules. An annotation type
 * defines the type name, parent type, and optionally a set of attributes that are expected to be associated with an
 * annotation of this type.
 * </p>
 * <p>
 * Annotation types are hierarchical and all types have a parent defined. If no parent is explicitly declared, its
 * parent is resolved to the <code>ROOT</code> type. Annotation types inherit their parent's attributes. Attribute
 * information on the type serves as documentation and is not type checked.
 * </p>
 * <p>Type information is defined via configuration. An Example is as follows:</p>
 * <code>
 * Annotation{
 *  ENTITY {
 *    attributes = ENTITY_TYPE, CONFIDENCE
 *  }
 *  REGEX_ENTITY {
 *    parent = ENTITY
 *    annotator {
 *      ENGLISH = @{ENGLISH_ENTITY_REGEX}
 *      JAPANESE = @{JAPANESE_ENTITY_REGEX}
 *    }
 *    attributes = PATTERN
 *  }
 * }
 * </code>
 * <p>
 * In the example shown above, we define the <code>ENTITY</code> and <code>REGEX_ENTITY</code> types. The
 * <code>Entity</code> type has two attributes associated with it which relate to the type of entity and the confidence
 * that the span of text is an entity of the given type. The <code>REGEX_ENTITY</code> is a sub-type (child) of
 * <code>ENTITY</code> and inherits all of its attributes. It defines annotators for English and Japanese which are
 * beans defined elsewhere in the configuration. Finally, it defines a <code>PATTERN</code> attribute relating to the
 * pattern that was used to identify the entity.
 * </p>
 * <h2>Gold Standard Types</h2>
 * <p>
 * A special annotation type for gold standard annotations can be defined using <code>@</code> prepended to the
 * annotation type name. Gold standard annotations inherit all of their information from their non-gold standard
 * counterparts. The gold standard version of annotation type can be easily obtained via the
 * {@link #goldStandardVersion()} method.
 * </p>
 * @author David B. Bracewell
 */
public final class AnnotationType extends EnumValue {

  private static final DynamicEnum<AnnotationType> index = new DynamicEnum<>();
  private static final long serialVersionUID = 1L;

  /**
   * The constant ROOT representing the base annotation type.
   */
  public static AnnotationType ROOT = create("ROOT");


  private AnnotationType(String name) {
    super(name);
  }

  /**
   * Creates a new Annotation Type or retrieves an already existing one for a given name
   *
   * @param name the name
   * @return the annotation type
   */
  public static AnnotationType create(String name) {
    if (StringUtils.isNullOrBlank(name)) {
      throw new IllegalArgumentException(name + " is invalid");
    }
    return index.register(new AnnotationType(name));
  }

  /**
   * Determine if an Annotation type exists for the given name
   *
   * @param name the name
   * @return True if it exists, otherwise False
   */
  public static boolean isDefined(String name) {
    return index.isDefined(name);
  }

  /**
   * Gets the AnnotationType from its name. Throws an <code>IllegalArgumentException</code> if the name is not valid.
   *
   * @param name the name as a string
   * @return the AnnotationType for the string
   */
  public static AnnotationType valueOf(String name) {
    return index.valueOf(name);
  }

  /**
   * Returns the values (annotation types) for this dynamic enum
   *
   * @return All known Annotation Types
   */
  public static Collection<AnnotationType> values() {
    return index.values();
  }


  /**
   * Gets parent type of this one.
   *
   * @return the parent type (ROOT if type is ROOT)
   */
  public AnnotationType getParent() {
    return AnnotationType.create(Config.get("Annotation", nonGoldStandardVersion().name(), "parent").asString("ROOT"));
  }

  /**
   * Gold standard version.
   *
   * @return the annotation type
   */
  public AnnotationType goldStandardVersion() {
    if (isGoldStandard()) {
      return this;
    }
    return AnnotationType.create("@" + name());
  }

  /**
   * Non gold standard version.
   *
   * @return the annotation type
   */
  public AnnotationType nonGoldStandardVersion() {
    if (isGoldStandard()) {
      return AnnotationType.create("@" + name());
    }
    return this;
  }

  /**
   * Is gold standard.
   *
   * @return the boolean
   */
  public boolean isGoldStandard() {
    return name().startsWith("@");
  }

  /**
   * Gets annotator.
   *
   * @param language the language
   * @return the annotator
   */
  public Annotator getAnnotator(@Nonnull Language language) {
    if (isGoldStandard()) {
      throw new IllegalStateException("Gold Standard annotations cannot be annotated");
    }
    String key = Config.closestKey("Annotation", language, name(), "annotator");
    return BeanUtils.parameterizeObject(Config.get(key).as(Annotator.class));
  }

  /**
   * Checks if this type is an instance of another type. Type B is an instance of Type A if A == B or A is in B's
   * parent tree.
   *
   * @param type the annotation type
   * @return the boolean
   */
  public boolean isInstance(AnnotationType type) {
    if (type == null) {
      return false;
    } else if (this.equals(type) || this.goldStandardVersion().equals(type.goldStandardVersion())) {
      return true;
    }
    AnnotationType parent = getParent();
    while (!parent.equals(ROOT)) {
      if (parent.equals(type) || parent.goldStandardVersion().equals(type.goldStandardVersion())) {
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }

  private Object readResolve() throws ObjectStreamException {
    if (isDefined(name())) {
      return index.valueOf(name());
    }
    return this;
  }


}//END OF AnnotationType
