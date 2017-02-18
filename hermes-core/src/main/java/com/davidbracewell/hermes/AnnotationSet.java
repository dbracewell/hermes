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


import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * <p>
 * An <code>AnnotationSet</code> acts as the storage mechanism for annotations associated with a document. It
 * provides methods for adding, removing, and navigating the annotations. In particular, a <code>AnnotationSet</code>
 * defines sequential methods of accessing annotations ({@link #next(Annotation, AnnotationType)}, {@link
 * #previous(Annotation, AnnotationType)}), based on criteria {@link #select(Predicate)} and {@link #select(Span,
 * Predicate)}, and by id {@link #get(long)}.
 * </p>
 * <p>
 * Annotation sets also keep track of completed annotation types, i.e. those processed using a <code>Pipeline</code>.
 * This allows the pipeline to ignore further attempts to annotate a type that is marked complete. In addition to being
 * marked complete, information about the annotator is stored.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface AnnotationSet extends Iterable<Annotation> {

   /**
    * <p>Selects all annotations of a given annotation type within a given range and matching a given criteria.</p>
    *
    * @param span     the range in which to search form annotations
    * @param criteria the criteria that an annotation must match
    * @return A list of annotations that are an instance of the given class within the given range and matching the
    * given criteria
    */
   List<Annotation> select(Span span, Predicate<? super Annotation> criteria);

   /**
    * <p>Selects all annotations of a given annotation type and matching a given criteria.</p>
    *
    * @param criteria the criteria that an annotation must match
    * @return A list of annotations that are an instance of the given class and matching the given criteria
    */
   List<Annotation> select(Predicate<? super Annotation> criteria);

   /**
    * Sets the given annotation type as being completed or not
    *
    * @param type                 the annotation type
    * @param isCompleted          True if the annotation is completed, False if not.
    * @param annotatorInformation the annotator information
    */
   void setIsCompleted(AnnotatableType type, boolean isCompleted, String annotatorInformation);

   /**
    * Gets if the given annotation type is completed or not
    *
    * @param type the annotation type
    * @return True if the annotation is completed, False if not.
    */
   boolean isCompleted(AnnotatableType type);

   /**
    * Gets information on what annotator provided the annotation of the given type
    *
    * @param type The annotation type
    * @return String representing the annotation provider or null
    */
   String getAnnotationProvider(AnnotatableType type);

   /**
    * Gets the set of completed annotation types.
    *
    * @return Set of classes for completed annotations
    */
   Set<AnnotatableType> getCompleted();

   /**
    * Removes all annotations of a given type and marks that type as not completed.
    *
    * @param type the type
    * @return The list of annotations that were removed
    */
   default List<Annotation> removeAll(AnnotationType type) {
      if (type != null) {
         setIsCompleted(type, false, null);
         List<Annotation> annotations = select(a -> a.isInstance(type));
         annotations.forEach(this::remove);
         return annotations;
      }
      return Collections.emptyList();
   }

   /**
    * Gets the annotation for the given id
    *
    * @param id The id of the annotation
    * @return The annotation associated with that id or null if one does not exist
    */
   Annotation get(long id);

   /**
    * Checks if an annotation is in the set or not
    *
    * @param annotation The annotation to check
    * @return True if the annotation is  in the set, False if not
    */
   boolean contains(Annotation annotation);

   /**
    * Removes an annotation from the document
    *
    * @param annotation The annotation to detach
    */
   boolean remove(Annotation annotation);

   /**
    * Adds an annotation to the set
    *
    * @param annotation The annotation to attach
    */
   void add(Annotation annotation);

   /**
    * Gets the first annotation after a given one of the given type
    *
    * @param annotation The annotation we want the next for
    * @param type       the type of the next annotation wanted
    * @return The next annotation of the same type or null
    */
   Annotation next(Annotation annotation, AnnotationType type);

   /**
    * Gets the first annotation before a given one of the given type
    *
    * @param annotation The annotation we want the previous for
    * @param type       the type of the previous annotation wanted
    * @return The previous annotation of the same type or null
    */
   Annotation previous(Annotation annotation, AnnotationType type);

   /**
    * The number of annotations in the set
    *
    * @return Number of annotations in the set
    */
   int size();

}//END OF AnnotationSet
