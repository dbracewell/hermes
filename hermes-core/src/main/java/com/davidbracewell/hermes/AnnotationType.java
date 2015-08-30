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
import com.davidbracewell.config.Config;
import com.davidbracewell.string.StringUtils;

import java.io.ObjectStreamException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

  static {
    //Root annotations define an Annotator attribute and its parent is itself
    ROOT.definedAttributes = Collections.emptySet();
    ROOT.parent = ROOT;
  }

  private volatile transient Set<Attribute> definedAttributes = null;
  private volatile transient AnnotationType parent;


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
  public static boolean exists(String name) {
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
   * Gets the set of defined attributes for this type. Attributes are defined via configuration. The advantange of
   * defining attributes is that they allow for their values to be types other than strings.
   *
   * @return the set of defined attributes
   */
  public Set<Attribute> getAttributes() {
    loadCfg();
    return Collections.unmodifiableSet(definedAttributes);
  }

  /**
   * Gets parent type of this one.
   *
   * @return the parent type (ROOT if type is ROOT)
   */
  public AnnotationType getParent() {
    loadCfg();
    return parent;
  }


  /**
   * Checks if an attribute is defined for this anntoation type
   *
   * @param name the name of the attribute
   * @return True if defined, False if not
   */
  public boolean isDefined(Attribute name) {
    loadCfg();
    return definedAttributes.contains(name);
  }

  /**
   * Checks if this type is an instance of another type. Type B is an instance of Type A if A == B or A is in B's
   * parent tree.
   *
   * @param type the annotation type
   * @return the boolean
   */
  public boolean isInstance(AnnotationType type) {
    return type != null && (this.equals(type) || !this.equals(getParent()) && getParent().isInstance(type));
  }

  private void loadCfg() {
    if (definedAttributes == null) {
      synchronized (this) {
        if (definedAttributes == null) {
          parent = Config.get("AnnotationType", name(), "parent").as(AnnotationType.class, ROOT);

          if (name().startsWith("GOLD_") && name().length() > 5) {
            parent = create(name().substring(5));
          }

          this.definedAttributes = Config.get("AnnotationType", name(), "attributes").asCollection(HashSet.class, Attribute.class);
          if( this.definedAttributes == null ){
            this.definedAttributes = new HashSet<>(4);
          }

          if( parent != this) {
            this.definedAttributes.addAll(parent.getAttributes());
          }
        }
      }
    }
  }

  private Object readResolve() throws ObjectStreamException {
    if (exists(name())) {
      return index.valueOf(name());
    }
    return this;
  }


}//END OF AnnotationType
