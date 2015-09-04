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
 * Defines a type of annotation which is made up of a unique name and a set of attributes. Attributes, e.g.
 * part-of-speech, can be defined for an annotation type via a configuration setting. These settings following the
 * convention: <code>AnnotationType.NAME.attributes = AttributeName:Type, AttributeName:Type</code> where
 * <code>NAME</code> is the
 * unique annotation type name, <code>AttributeName</code> is the name of the attribute, and <code>Type</code> is
 * class information that corresponds to the expected type of the attribute. Attributes not defined via configuration
 * can be stored on annotation, but will deserialize as a String.
 * </p>
 * <p>
 * Annotation types are hierarchical. A type's parent is defined via configuration:
 * <code>AnnotationType.NAME.parent=PARENT_NAME</code>. If no parent is declared, the ROOT annotation type is used as
 * the parent. Annotation types inherit their parent's attributes. A special annotation type for gold standard
 * annotations can be defined using <code>GOLD_</code> prepended to the annotation type name and will inherit from the
 * annotation type defined after the prefix.
 * </p>
 * <p>
 * Annotation types define their annotator through configuration as well using <code>AnnotationType.NAME.annotator=Class</code>
 * where Class is the fully qualified class name of the annotator.
 * </p>
 *
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
    String key = Config.closestKey("AnnotationType", language, name(), "annotator");
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
