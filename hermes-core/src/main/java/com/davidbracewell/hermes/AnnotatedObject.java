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

import lombok.NonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * <p> An object containing linguistic <code>Annotation</code>s. </p>
 *
 * @author David B. Bracewell
 */
public interface AnnotatedObject {

   /**
    * Returns the document that the annotated object is associated with
    *
    * @return The document that this object is associated with
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
      return get(type).stream().findFirst().orElseGet(Fragments::detachedEmptyAnnotation);
   }

   /**
    * Gets the first token annotation overlapping this object.
    *
    * @return the forst token annotation
    */
   default Annotation firstToken() {
      if (tokenLength() > 0) {
         return tokenAt(0);
      }
      return Fragments.detachedEmptyAnnotation();
   }

   /**
    * Convenience method for processing annotations of a given type.
    *
    * @param type     the annotation type
    * @param consumer the consumer to use for processing annotations
    */
   default void forEach(@NonNull AnnotationType type, @NonNull Consumer<? super Annotation> consumer) {
      get(type).forEach(consumer);
   }

   /**
    * Gets annotations of a given type and that test positive for the given filter that overlap with this object.
    *
    * @param type   the type of annotation wanted
    * @param filter The filter that annotations must pass in order to be accepted
    * @return the list of annotations of given type meeting the given filter that overlap with this object
    */
   List<Annotation> get(AnnotationType type, Predicate<? super Annotation> filter);

   /**
    * Gets annotations of a given type that overlap with this object.
    *
    * @param type the type of annotation wanted
    * @return the list of annotations of given type that overlap with this object
    */
   List<Annotation> get(AnnotationType type);

   /**
    * Gets all annotations overlapping this object
    *
    * @return all annotations overlapping with this object.
    */
   List<Annotation> getAllAnnotations();

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
   default Annotation last(@NonNull AnnotationType type) {
      List<Annotation> annotations = get(type);
      return annotations.isEmpty() ? Fragments.detachedEmptyAnnotation() : annotations.get(annotations.size() - 1);
   }

   /**
    * Ges the last token annotation overlapping this object
    *
    * @return the last token annotation
    */
   default Annotation lastToken() {
      if (tokenLength() > 0) {
         return tokenAt(tokenLength() - 1);
      }
      return Fragments.detachedEmptyAnnotation();
   }

   /**
    * Gets the sentences overlapping this object
    *
    * @return the sentences overlapping this annotation.
    */
   default List<Annotation> sentences() {
      return get(Types.SENTENCE);
   }

   /**
    * Sentence stream stream.
    *
    * @return the stream
    */
   default Stream<Annotation> sentenceStream() {
      return stream(Types.SENTENCE);
   }

   /**
    * Token stream stream.
    *
    * @return the stream
    */
   default Stream<Annotation> tokenStream() {
      return stream(Types.TOKEN);
   }

   /**
    * Stream stream.
    *
    * @param type the type
    * @return the stream
    */
   default Stream<Annotation> stream(@NonNull AnnotationType type) {
      return get(type).stream();
   }

   /**
    * <p> Gits the token at the given token index which is a relative offset from this object. For example, given the
    * document with the following tokens: <code>["the", "quick", "brown", "fox", "jumps", "over", "the", "lazy",
    * "dog"]</code> and this annotated object spanning <code>["quick", "brown", "fox"]</code> "quick" would have a
    * relative offset in this object of 0 and document offset of 1. </p>
    *
    * @param tokenIndex the token index relative to the tokens overlapping this object.
    * @return the token annotation at the relative offset
    */
   default Annotation tokenAt(int tokenIndex) {
      if (tokenIndex < 0 || tokenIndex >= tokenLength()) {
         return Fragments.detachedEmptyAnnotation();
      }
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
      return get(Types.TOKEN);
   }


}//END OF AnnotatedObject
