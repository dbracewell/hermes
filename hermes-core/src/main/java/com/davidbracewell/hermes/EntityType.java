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
import com.davidbracewell.guava.common.collect.Sets;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Tag type associated with Entity annotations. Entities are defined in a hierarchy, e.g. <code>Location ->
 * Country</code></p>
 */
public final class EntityType extends HierarchicalEnumValue<EntityType> implements Comparable<EntityType> {
   private static final long serialVersionUID = 1L;
   public static final String CANONICAL_NAME = EntityType.class.getCanonicalName();
   private static final Set<EntityType> values = Sets.newConcurrentHashSet();


   public static final EntityType ROOT = EntityType.create("ROOT");

   @Override
   protected EntityType getSingleRoot() {
      return ROOT;
   }

   private EntityType(String name, EntityType parent) {
      super(CANONICAL_NAME, name, parent);
   }

   /**
    * <p>Creates a new or retrieves an existing instance of EntityType with the given name.</p>
    *
    * @param name the specified name of the EntityType
    * @return The instance of EntityType corresponding th the give name.
    */
   public static EntityType create(@NonNull String name) {
      return create(name, null);
   }

   /**
    * <p>Creates a new or retrieves an existing instance of EntityType with the given name.</p>
    *
    * @param name   the specified name of the EntityType
    * @param parent the parent element of the enum;
    * @return The instance of EntityType corresponding th the give name.
    */
   public static EntityType create(@NonNull String name, EntityType parent) {
      EntityType toReturn = DynamicEnum.register(new EntityType(name, parent));
      if (toReturn.setParentIfAbsent(parent)) {
         Config.setProperty(toReturn.canonicalName() + ".parent", parent.canonicalName());
      }
      values.add(toReturn);
      return toReturn;
   }

   /**
    * <p>Retrieves all currently known values of EntityType.</p>
    *
    * @return An unmodifiable collection of currently known values for EntityType.
    */
   public static Collection<EntityType> values() {
      return Collections.unmodifiableSet(values);
   }

   /**
    * <p>Returns the constant of EntityType with the specified name.The normalized version of the specified name will
    * be matched allowing for case and space variations.</p>
    *
    * @return The constant of EntityType with the specified name
    * @throws IllegalArgumentException if the specified name is not a member of EntityType.
    */
   public static EntityType valueOf(@NonNull String name) {
      return DynamicEnum.valueOf(EntityType.class, name);
   }

   @Override
   @SuppressWarnings("unchecked")
   public List<EntityType> getChildren() {
      return values().stream().filter(v -> this != v && v.getParent() == this).collect(Collectors.toList());
   }

   @Override
   public int compareTo(@NonNull EntityType o) {
      return canonicalName().compareTo(o.canonicalName());
   }


}// END OF EntityType

