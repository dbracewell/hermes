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

import com.davidbracewell.tuple.Tuple2;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The interface Relational object.
 *
 * @author David B. Bracewell
 */
public interface RelationalObject {

   /**
    * Add relation.
    *
    * @param relation the relation
    */
   default void add(@NonNull Relation relation) {
   }

   /**
    * Add all relations.
    *
    * @param relations the relations
    */
   default void addAll(@NonNull Collection<Relation> relations) {
   }

   /**
    * Gets relations.
    *
    * @return the relations
    */
   default Collection<Relation> allRelations() {
      return allRelations(false);
   }

   /**
    * Gets all relations.
    *
    * @param includeSubAnnotations the include sub annotations
    * @return the all relations
    */
   Collection<Relation> allRelations(boolean includeSubAnnotations);

   /**
    * Gets children.
    *
    * @return the children
    */
   List<Annotation> children();

   /**
    * Children list.
    *
    * @param relation the relation
    * @return the list
    */
   default List<Annotation> children(@NonNull String relation) {
      return children().stream().filter(
         a -> a.dependencyRelation().filter(r -> r.v1.equalsIgnoreCase(relation)).isPresent()).collect(
         Collectors.toList());
   }

   /**
    * Get dependency relation optional.
    *
    * @return the optional
    */
   Optional<Tuple2<String, Annotation>> dependencyRelation();

   /**
    * Gets relations.
    *
    * @param relationType the relation type
    * @return the relations
    */
   default List<Relation> get(@NonNull RelationType relationType) {
      return get(relationType, true);
   }

   /**
    * Get list.
    *
    * @param relationType          the relation type
    * @param includeSubAnnotations the include sub annotations
    * @return the list
    */
   List<Relation> get(@NonNull RelationType relationType, boolean includeSubAnnotations);

   /**
    * Gets parent.
    *
    * @return the parent
    */
   default Annotation parent() {
      return dependencyRelation().map(Tuple2::getValue).orElse(Fragments.detachedEmptyAnnotation());
   }

   /**
    * Remove relation.
    *
    * @param relation the relation
    */
   default void remove(@NonNull Relation relation) {


   }

   /**
    * Gets sources.
    *
    * @param type  the type
    * @param value the value
    * @return the sources
    */
   default List<Annotation> sources(@NonNull RelationType type, @NonNull String value) {
      return sources(type, value, true);
   }

   /**
    * Gets sources.
    *
    * @param type                  the type
    * @param value                 the value
    * @param includeSubAnnotations the include sub annotations
    * @return the sources
    */
   List<Annotation> sources(@NonNull RelationType type, @NonNull String value, boolean includeSubAnnotations);

   /**
    * Gets sources.
    *
    * @param type the type
    * @return the sources
    */
   default List<Annotation> sources(@NonNull RelationType type) {
      return sources(type, true);
   }

   /**
    * Gets sources.
    *
    * @param type                  the type
    * @param includeSubAnnotations the include sub annotations
    * @return the sources
    */
   List<Annotation> sources(@NonNull RelationType type, boolean includeSubAnnotations);

   /**
    * Gets targets.
    *
    * @param type the type
    * @return the targets
    */
   default List<Annotation> targets(@NonNull RelationType type) {
      return targets(type, true);
   }

   /**
    * Gets targets.
    *
    * @param type                  the type
    * @param includeSubAnnotations the include sub annotations
    * @return the targets
    */
   List<Annotation> targets(@NonNull RelationType type, boolean includeSubAnnotations);

   /**
    * Gets targets.
    *
    * @param type  the type
    * @param value the value
    * @return the targets
    */
   default List<Annotation> targets(@NonNull RelationType type, @NonNull String value) {
      return targets(type, value, true);
   }

   /**
    * Gets targets.
    *
    * @param type                  the type
    * @param value                 the value
    * @param includeSubAnnotations the include sub annotations
    * @return the targets
    */
   List<Annotation> targets(@NonNull RelationType type, @NonNull String value, boolean includeSubAnnotations);

}//END OF RelationalObject
