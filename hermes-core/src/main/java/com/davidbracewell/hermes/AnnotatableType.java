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

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.annotator.Annotator;
import com.davidbracewell.reflection.BeanUtils;
import com.davidbracewell.reflection.Reflect;
import com.davidbracewell.reflection.ReflectionException;
import com.davidbracewell.reflection.ReflectionUtils;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;
import lombok.NonNull;

/**
 * <p>
 * An annotatable type is one that can be added to a document through the use of a {@link Pipeline}.
 * The interface exists to unify {@link AnnotationType}s, {@link AttributeType}s, and {@link RelationType}s.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface AnnotatableType {
   static String ANNOTATOR_PACKAGE = "com.davidbracewell.hermes.annotator";

   /**
    * Gets the annotator associated with this type for a given language.
    *
    * @param language the language for which the annotator is needed.
    * @return the annotator for this type and the given langauge
    * @throws IllegalStateException If this type is a gold standard annotation.
    */
   default Annotator getAnnotator(@NonNull Language language) {
      //Step 1: Check for a config override
      String key = Config.closestKey(type(), language, name(), "annotator");

      Annotator annotator = null;

      if (StringUtils.isNotNullOrBlank(key)) {
         annotator = Config.get(key).as(Annotator.class);

      } else {
         String typeName = StringUtils.toTitleCase(name().replaceAll("[^a-zA-z]+", " ")
                                                         .trim()
                                                         .toLowerCase())
                                      .replaceAll("\\s+", "");
         String languageName = StringUtils.toTitleCase(language.name().toLowerCase());
         Class<?> annotatorClass = ReflectionUtils.getClassForNameQuietly(
            ANNOTATOR_PACKAGE + ".Default" + languageName + typeName + "Annotator");

         if (annotatorClass == null) {
            annotatorClass = ReflectionUtils
                                .getClassForNameQuietly(ANNOTATOR_PACKAGE + ".Default" + typeName + "Annotator");
         }

         try {
            annotator = Reflect.onClass(annotatorClass).create().get();
         } catch (ReflectionException e) {
            annotator = null;
         }

      }

      if (annotator == null) {
         throw new IllegalStateException("No annotator is defined for " + name() + " and " + language);
      }

      annotator = BeanUtils.parameterizeObject(annotator);
      Preconditions.checkArgument(annotator.satisfies().contains(this),
                                  "Attempting to register " + annotator.getClass()
                                                                       .getName() + " for " + name() + " which it does not provide.");
      return annotator;
   }

   /**
    * The type (Annotation, Attribute, Relation)
    *
    * @return the type
    */
   String type();

   /**
    * The annotatable type's name (e.g. TOKEN, PART_OF_SPEECT)
    *
    * @return the name
    */
   String name();

   /**
    * The canonical name in the form of "type.name"
    *
    * @return the canonical form of the name
    */
   default String canonicalName() {
      return type() + "." + name();
   }


}//END OF Annotatable
