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
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.guava.common.collect.Sets;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p> An <code>AnnotationType</code> serves to define the structure and source of a specific annotation. The definition
 * provided by the type facilitates the portability of the annotation between different modules. An annotation type
 * defines the type name, parent type, and optionally a set of attributes that are expected to be associated with an
 * annotation of this type. </p>
 * <p> Annotation types are hierarchical and all types have a parent defined. If no parent
 * is explicitly declared, its parent is resolved to the <code>ROOT</code> type. Annotation types inherit their parent's
 * attributes. Attribute information on the type serves as documentation and is not type checked. Additionally, a "tag"
 * can be defined for a type using the <code>tag</code> property, which defines the attribute to return on calls to
 * <code>getTag()</code>. </p>
 * <p>Type information is defined via configuration. An Example is as follows:
 * <pre> {@code
 * Annotation {
 *       ENTITY {
 *          tag = ENTITY_TYPE
 *       }
 *
 *       REGEX_ENTITY {
 *          parent = ENTITY
 *          annotator = @{DEFAULT_ENTITY_REGEX}
 *       }
 * }
 * }
 * </pre>
 * </p>
 */
public final class AnnotationType extends HierarchicalEnumValue<AnnotationType> implements Comparable<AnnotationType>, AnnotatableType {
   private static final long serialVersionUID = 1L;
   public static final String CANONICAL_NAME = AnnotationType.class.getCanonicalName();
   private static final String typeName = "Annotation";
   private volatile transient AttributeType tagAttributeType = null;
   private static final Set<AnnotationType> values = Sets.newConcurrentHashSet();

   /**
    * The constant ROOT representing the base annotation type.
    */
   public static AnnotationType ROOT = create("ROOT", null);


   private AnnotationType(String name, AnnotationType parent) {
      super(CANONICAL_NAME, name, parent);
   }


   @Override
   protected AnnotationType getSingleRoot() {
      return ROOT;
   }

   /**
    * Creates a new Annotation Type or retrieves an already existing one for a given name
    *
    * @param name   the name
    * @param parent the parent
    * @return the annotation type
    * @throws IllegalArgumentException name is invalid, or annotation type already exists with different parent
    */
   public static AnnotationType create(String name, AnnotationType parent) {
      return create(name, parent, null);
   }

   /**
    * Creates a new Annotation Type or retrieves an already existing one for a given name
    *
    * @param name             the name
    * @param parent           the parent
    * @param tagAttributeType The attribute used as this annotations tag
    * @return the annotation type
    * @throws IllegalArgumentException name is invalid, or annotation type already exists with different parent
    */
   public static AnnotationType create(String name, AnnotationType parent, AttributeType tagAttributeType) {
      AnnotationType toReturn = DynamicEnum.register(new AnnotationType(name, parent));
      if (toReturn.setParentIfAbsent(parent)) {
         Config.setProperty(typeName + "." + toReturn.name() + ".parent", parent.name());
      }
      if (tagAttributeType != null && toReturn.tagAttributeType == null) {
         toReturn.tagAttributeType = tagAttributeType;
         Config.setProperty(typeName + "." + toReturn.name() + ".tag", tagAttributeType.name());
      } else if (tagAttributeType != null && !toReturn.tagAttributeType.equals(tagAttributeType)) {
         throw new IllegalArgumentException("Attempting to change tag of " + name + " from " + toReturn.tagAttributeType + " to " + tagAttributeType);
      }
      values.add(toReturn);
      return toReturn;
   }

   /**
    * Creates a new Annotation Type or retrieves an already existing one for a given name
    *
    * @param name the name
    * @return the annotation type
    */
   public static AnnotationType create(String name) {
      return create(name, null);
   }

   /**
    * <p>Retrieves all currently known values of AnnotationType.</p>
    *
    * @return An unmodifiable collection of currently known values for AnnotationType.
    */
   public static Collection<AnnotationType> values() {
      return Collections.unmodifiableSet(values);
   }

   /**
    * <p>Returns the constant of AnnotationType with the specified name.The normalized version of the specified name
    * will be matched allowing for case and space variations.</p>
    *
    * @return The constant of AnnotationType with the specified name
    * @throws IllegalArgumentException if the specified name is not a member of AnnotationType.
    */
   public static AnnotationType valueOf(@NonNull String name) {
      return DynamicEnum.valueOf(AnnotationType.class, name);
   }

   @Override
   @SuppressWarnings("unchecked")
   public List<AnnotationType> getChildren() {
      return values().stream().filter(v -> this != v && v.getParent() == this).collect(Collectors.toList());
   }

   @Override
   public int compareTo(@NonNull AnnotationType o) {
      return canonicalName().compareTo(o.canonicalName());
   }


   @Override
   protected AnnotationType getParentFromConfig() {
      return Cast.as(Config.get(typeName, name(), "parent").as(getClass(), null));
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
      } else if (this == type) {
         return true;
      } else if (type == ROOT) {
         return true;
      }
      AnnotationType parent = getParent();
      while (!parent.equals(ROOT)) {
         if (parent.equals(type)) {
            return true;
         }
         parent = parent.getParent();
      }
      return false;
   }

   /**
    * Gets tag attribute.
    *
    * @return the tag attribute
    */
   public AttributeType getTagAttributeType() {
      if (tagAttributeType == null) {
         synchronized (this) {
            if (tagAttributeType == null) {
               String attribute = Config.get(typeName, name(), "tag").asString();
               if (StringUtils.isNullOrBlank(attribute) && !AnnotationType.ROOT.equals(getParent())) {
                  tagAttributeType = getParent().getTagAttributeType();
               } else if (StringUtils.isNullOrBlank(attribute)) {
                  tagAttributeType = Types.TAG;
               } else {
                  tagAttributeType = AttributeType.create(attribute);
               }
            }
         }
      }
      return tagAttributeType;
   }


   @Override
   public String type() {
      return typeName;
   }


}//END OF AnnotationType
