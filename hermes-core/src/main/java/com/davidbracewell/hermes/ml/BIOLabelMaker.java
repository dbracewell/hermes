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

package com.davidbracewell.hermes.ml;

import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.attribute.EntityType;
import lombok.NonNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class BIOLabelMaker implements SerializableFunction<Annotation, String> {
   private static final long serialVersionUID = 1L;
   private final AnnotationType annotationType;
   private final Set<String> validTags;

   public BIOLabelMaker(@NonNull AnnotationType annotationType) {
      this.annotationType = annotationType;
      this.validTags = Collections.emptySet();
   }

   public BIOLabelMaker(@NonNull AnnotationType annotationType, Set<String> validTags) {
      this.annotationType = annotationType;
      this.validTags = validTags;
   }

   @Override
   public String apply(Annotation annotation) {
      Optional<Annotation> target = annotation.get(annotationType).stream().findFirst();
      return target.map(a -> {
         EntityType type = a.get(Types.ENTITY_TYPE).as(EntityType.class);
         if (type != null && (validTags.isEmpty() || validTags.contains(type.name()))) {
            if (a.start() == annotation.start()) {
               return "B-" + type.name();
            }
            return "I-" + type.name();
         }
         return "O";
      }).orElse("O");
   }
}//END OF BIOLabelMaker
