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
import com.davidbracewell.HierarchicalEnumValue;
import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.annotator.Annotator;
import com.davidbracewell.reflection.BeanUtils;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;
import lombok.NonNull;

import java.io.ObjectStreamException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * An <code>AnnotationType</code> serves to define the structure and source of a specific annotation. The definition
 * provided by the type facilitates the portability of the annotation between different modules. An annotation type
 * defines the type name, parent type, and optionally a set of attributes that are expected to be associated with an
 * annotation of this type.
 * </p>
 * <p>
 * Annotation types are hierarchical and all types have a parent defined. If no parent is explicitly declared, its
 * parent is resolved to the <code>ROOT</code> type. Annotation types inherit their parent's attributes. Attribute
 * information on the type serves as documentation and is not type checked. Additionally, a "tag" can be defined for a
 * type using the <code>tag</code> property, which defines the attribute to return on calls to <code>getTag()</code>.
 * </p>
 * <p>Type information is defined via configuration. An Example is as follows:</p>
 * {@code
 * Annotation{
 * ENTITY {
 * attributes = ENTITY_TYPE, CONFIDENCE
 * tag = ENTITY_TYPE
 * }
 * REGEX_ENTITY {
 * parent = ENTITY
 * annotator = @{DEFAULT_ENTITY_REGEX}
 * annotator {
 * ENGLISH = @{ENGLISH_ENTITY_REGEX}
 * JAPANESE = @{JAPANESE_ENTITY_REGEX}
 * }
 * attributes = PATTERN
 * }
 * }
 * }*
 * <p>
 * In the example shown above, we define the <code>ENTITY</code> and <code>REGEX_ENTITY</code> types. The
 * <code>Entity</code> type has two attributes associated with it which relate to the type of entity and the confidence
 * that the span of text is an entity of the given type. The <code>REGEX_ENTITY</code> is a sub-type (child) of
 * <code>ENTITY</code> and inherits all of its attributes. It defines annotators for English and Japanese which are
 * beans defined elsewhere in the configuration. Finally, it defines a <code>PATTERN</code> attribute relating to the
 * pattern that was used to identify the entity.
 * </p>
 *
 * @author David B. Bracewell
 */
public final class AnnotationType extends HierarchicalEnumValue {

  private static final DynamicEnum<AnnotationType> index = new DynamicEnum<>();
  private static final long serialVersionUID = 1L;

  /**
   * The constant ROOT representing the base annotation type.
   */
  public static AnnotationType ROOT = create("ROOT");

  private volatile transient Set<Attribute> definedAttributes = null;

  private AnnotationType(String name) {
    super(name);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<AnnotationType> getChildren() {
    return index.values().stream().filter(t -> t.getParent().equals(this)).collect(Collectors.toList());
  }

  /**
   * Creates a new Annotation Type or retrieves an already existing one for a given name
   *
   * @param name   the name
   * @param parent the parent
   * @return the annotation type
   * @throws IllegalArgumentException name is invalid, or annotation type already exists with different parent
   */
  public static AnnotationType create(String name, @NonNull AnnotationType parent) {
    if (StringUtils.isNullOrBlank(name)) {
      throw new IllegalArgumentException(name + " is invalid");
    }
    if (index.isDefined(name) && !index.valueOf(name).getParent().equals(parent)) {
      throw new IllegalArgumentException("Attempting to register an existing annotation type with a different parent type.");
    }
    AnnotationType type = index.register(new AnnotationType(name));
    Config.setProperty("Annotation." + type.name() + ".parent", parent.name());
    return type;
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
   * Gets tag attribute.
   *
   * @return the tag attribute
   */
  public Attribute getTagAttribute() {
    String attribute = Config.get("Annotation", name(), "tag").asString();
    if (StringUtils.isNullOrBlank(attribute) && !AnnotationType.ROOT.equals(getParent())) {
      return getParent().getTagAttribute();
    } else if (StringUtils.isNullOrBlank(attribute)) {
      return Attrs.TAG;
    }
    return Attribute.create(attribute);
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
    return Cast.as(super.getParent());
  }

  @Override
  protected AnnotationType getParentConfig() {
    return AnnotationType.create(Config.get("Annotation", name(), "parent").asString("ROOT"));
  }

  /**
   * Gets the annotator associated with this type for a given language.
   *
   * @param language the language for which the annotator is needed.
   * @return the annotator for this type and the given langauge
   * @throws IllegalStateException If this type is a gold standard annotation.
   */
  public Annotator getAnnotator(@NonNull Language language) {
    String key = Config.closestKey("Annotation", language, name(), "annotator");
    if (StringUtils.isNullOrBlank(key)) {
      throw new IllegalStateException("No annotator is defined for " + name() + " and " + language);
    }

    Annotator annotator = BeanUtils.parameterizeObject(Config.get(key).as(Annotator.class));
    Preconditions.checkNotNull(annotator, "Could not create the annotator [" + Config.get(key) + "] for " + name());
    Preconditions.checkArgument(annotator.satisfies().contains(this), "Attempting to register " + annotator.getClass().getName() + " for " + name() + " which it does not provide.");
    return annotator;
  }

  /**
   * <p>Checks if this type is an instance of another type. Type B is an instance of Type A if A == B, B is the gold
   * standard version of A, or A is in B's parent tree.</p>
   *
   * @param type the annotation type we are checking against
   * @return True if this is an instance of the given type, False otherwise
   */
  public boolean isInstance(AnnotationType type) {
    if (type == null) {
      return false;
    } else if (this.equals(type)) {
      return true;
    }
    AnnotationType parent = getParent();
    while (!parent.equals(ROOT)) {
      if (parent.equals(type)) {
        return true;
      }
      parent = parent.getParent();
    }
    return type.equals(ROOT);
  }

  private Object readResolve() throws ObjectStreamException {
    if (isDefined(name())) {
      return index.valueOf(name());
    }
    return index.register(this);
  }


}//END OF AnnotationType
