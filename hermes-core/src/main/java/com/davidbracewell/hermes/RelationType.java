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
import com.davidbracewell.guava.common.collect.Sets;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public final class RelationType extends EnumValue implements AnnotatableType, Comparable<RelationType> {
   public static final String CANONICAL_NAME = RelationType.class.getCanonicalName();
   private static final long serialVersionUID = 1L;
   private static final String typeName = "Relation";
   private static final Set<RelationType> values =  Sets.newConcurrentHashSet();

   private RelationType(String name) {
      super(CANONICAL_NAME,name);
   }

   /**
    * <p>Creates a new or retrieves an existing instance of RelationType with the given name.</p>
    *
    * @return The instance of RelationType corresponding th the give name.
    */
   public static RelationType create(@NonNull String name) {
      RelationType toReturn = DynamicEnum.register(new RelationType(name));
      values.add(toReturn);
      return toReturn;
   }

   /**
    * <p>Retrieves all currently known values of RelationType.</p>
    *
    * @return An unmodifiable collection of currently known values for RelationType.
    */
   public static Collection<RelationType> values() {
      return Collections.unmodifiableSet(values);
   }

   /**
    * <p>Returns the constant of RelationType with the specified name.The normalized version of the specified name will
    * be matched allowing for case and space variations.</p>
    *
    * @return The constant of RelationType with the specified name
    * @throws IllegalArgumentException if the specified name is not a member of RelationType.
    */
   public static RelationType valueOf(@NonNull String name) {
      return DynamicEnum.valueOf(RelationType.class, name);
   }

   @Override
   public int compareTo(@NonNull RelationType o) {
      return this.canonicalName().compareTo(o.canonicalName());
   }

   @Override
   public String type() {
      return typeName;
   }


}//END OF RelationType

