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

import javax.annotation.Nonnull;
import java.util.List;

/**
 * <p>Defines the needed methods for an object that has annotations. Annotations are marked up areas of text
 * corresponding to analysis. Annotated objects also implement <code>CharSpan</code> meaning that they have a defined
 * <code>start</code> and <code>end</code> position. The starting and ending positions makes it possible to retrieve
 * annotations that overlap with, enclosing, or enclosed by the annotated object.</p>
 *
 * @author David B. Bracewell
 */
public interface Annotated {

  /**
   * Returns the document that this fragment is a part of.
   *
   * @return The document that this fragment is associated with
   */
  Document document();

  /**
   * Gets the first annotation overlapping this object with the given annotation type.
   *
   * @param type the annotation type
   * @return the first annotation of the given type overlapping this object or a detached empty annotation if there is
   * none.
   */
  default Annotation first(AnnotationType type) {
    return getOverlapping(type).stream().findFirst().orElse(Fragments.detachedEmptyAnnotation());
  }

  /**
   * Gets the annotations that are contained, within this object, i.e. those annotations whose span lays
   * fully within this object.
   *
   * @param type the type  of annotation wanted
   * @return the list of annotations of given type contained within this object
   */
  List<Annotation> getContaining(AnnotationType type);

  /**
   * Gets the annotations that are during to this object, i.e. those annotations whose span starts before and ends
   * after this object.
   *
   * @param type the type  of annotation wanted
   * @return the list of annotations of given type covering this object
   */
  List<Annotation> getDuring(AnnotationType type);

  /**
   * Gets annotations of a given type that overlap with this object.
   *
   * @param type the type of annotation wanted
   * @return the list of annotations of given type that overlap with this object
   */
  List<Annotation> getOverlapping(AnnotationType type);

  /**
   * Gets annotations of a given type that have the same starting offset as this object.
   *
   * @param type the type of annotation wanted
   * @return the list of annotations of given type have the same starting offset as this object.
   */
  List<Annotation> getStartingHere(AnnotationType type);


  /**
   * Gets the last annotation overlapping this object with the given annotation type.
   *
   * @param type the annotation type
   * @return the last annotation of the given type overlapping this object or a detached empty annotation if there is
   * none.
   */
  default Annotation last(@Nonnull AnnotationType type) {
    List<Annotation> annotations = getOverlapping(type);
    return annotations.isEmpty() ? Fragments.detachedEmptyAnnotation() : annotations.get(annotations.size() - 1);
  }

  /**
   * Gets the sentences overlapping this object
   *
   * @return the sentences overlapping this annotation.
   */
  default List<Annotation> sentences() {
    return getOverlapping(Types.SENTENCE);
  }

  /**
   * <p>Gits the token at the given token index which is a relative offset from this object. For example, given the
   * document with the following tokens:<pre>["the", "quick", "brown", "fox", "jumps", "over", "the", "lazy",
   * "dog"]</pre> and this annotation spanning <pre>["quick", "brown", "fox"]</pre> "quick" would have a relative
   * offset in this object of 0 and document offset of 1. </p>
   *
   * @param tokenIndex the token index relative to the tokens overlapping this object.
   * @return the token annotation at the relative offset
   */
  default Annotation tokenAt(int tokenIndex) {
    return tokens().get(tokenIndex);
  }

  /**
   * The length of the object in tokens
   *
   * @return the number of tokens in this annotation
   */
  default int tokenLength() {
    return tokens().size();
  }

  /**
   * Gets the tokens overlapping this object.
   *
   * @return the tokens overlapping this annotation.
   */
  default List<Annotation> tokens() {
    return getOverlapping(Types.TOKEN);
  }


}//END OF Annotated
