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

import com.davidbracewell.hermes.tag.RelationType;
import com.davidbracewell.tuple.Tuple2;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The interface Relational object.
 *
 * @author David B. Bracewell
 */
public interface RelationalObject {

  /**
   * Add all relations.
   *
   * @param relations the relations
   */
  default void addAll(@NonNull Collection<Relation> relations) {
  }

  /**
   * Add relation.
   *
   * @param relation the relation
   */
  default void add(@NonNull Relation relation) {
  }

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
  default List<Relation> get(@NonNull RelationType relationType, boolean includeSubAnnotations) {
    return Collections.emptyList();
  }

  /**
   * Gets relations.
   *
   * @return the relations
   */
  default Collection<Relation> getAllRelations() {
    return getAllRelations(true);
  }

  /**
   * Gets all relations.
   *
   * @param includeSubAnnotations the include sub annotations
   * @return the all relations
   */
  default Collection<Relation> getAllRelations(boolean includeSubAnnotations) {
    return Collections.emptyList();
  }

  /**
   * Gets children.
   *
   * @return the children
   */
  default List<Annotation> children() {
    return Collections.emptyList();
  }

  /**
   * Get dependency relation optional.
   *
   * @return the optional
   */
  default Optional<Tuple2<String, Annotation>> dependencyRelation() {
    return Optional.empty();
  }

  /**
   * Gets parent.
   *
   * @return the parent
   */
  default Optional<Annotation> parent() {
    return dependencyRelation().map(Tuple2::getValue);
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
  default List<Annotation> sources(@NonNull RelationType type, @NonNull String value, boolean includeSubAnnotations) {
    return Collections.emptyList();
  }

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
  default List<Annotation> sources(@NonNull RelationType type, boolean includeSubAnnotations) {
    return Collections.emptyList();
  }

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
  default List<Annotation> targets(@NonNull RelationType type, boolean includeSubAnnotations) {
    return Collections.emptyList();
  }

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
  default List<Annotation> targets(@NonNull RelationType type, @NonNull String value, boolean includeSubAnnotations) {
    return Collections.emptyList();
  }

  /**
   * Remove relation.
   *
   * @param relation the relation
   */
  default void remove(@NonNull Relation relation) {


  }

}//END OF RelationalObject
