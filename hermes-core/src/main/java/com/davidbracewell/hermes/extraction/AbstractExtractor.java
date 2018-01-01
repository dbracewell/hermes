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

package com.davidbracewell.hermes.extraction;

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.filter.StopWords;
import com.davidbracewell.hermes.ml.feature.ValueCalculator;
import lombok.NonNull;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The type Feature spec.
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
@ToString
public abstract class AbstractExtractor<T extends AbstractExtractor> implements Serializable {
   private static final long serialVersionUID = 1L;

   private Set<AnnotationType> annotationType = new HashSet<>();
   private SerializableFunction<HString, HString> trimFunction = h -> h;
   private SerializableFunction<HString, String> toStringFunction = HString::toString;
   private SerializablePredicate<? super HString> filter = hString -> true;
   private ValueCalculator valueCalculator = ValueCalculator.Frequency;

   /**
    * Instantiates a new Abstract feature spec.
    */
   public AbstractExtractor() {

   }

   /**
    * Instantiates a new Abstract feature spec.
    *
    * @param copy the copy
    */
   public AbstractExtractor(@NonNull AbstractExtractor<T> copy) {
      this.annotationType = copy.annotationType;
      this.toStringFunction = copy.toStringFunction;
      this.filter = copy.filter;
      this.valueCalculator = copy.valueCalculator;
   }

   /**
    * Value calculator t.
    *
    * @param valueCalculator the value calculator
    * @return the t
    */
   public T valueCalculator(@NonNull ValueCalculator valueCalculator) {
      this.valueCalculator = valueCalculator;
      return Cast.as(this);
   }


   /**
    * Gets value calculator.
    *
    * @return the value calculator
    */
   public ValueCalculator getValueCalculator() {
      return valueCalculator;
   }

   /**
    * Annotation type t.
    *
    * @param annotationType the annotation type
    * @return the t
    */
   public T annotationType(@NonNull AnnotationType annotationType) {
      this.annotationType.add(annotationType);
      return Cast.as(this);
   }

   /**
    * Filter t.
    *
    * @param filter the filter
    * @return the t
    */
   public T filter(@NonNull SerializablePredicate<? super HString> filter) {
      this.filter = filter;
      return Cast.as(this);
   }

   /**
    * Gets annotation type.
    *
    * @return the annotation type
    */
   public AnnotationType getAnnotationType() {
      if (annotationType.isEmpty()) {
         return Types.TOKEN;
      }
      return annotationType.iterator().next();
   }


   public boolean hasMultipleTypes() {
      return annotationType.size() > 1;
   }

   /**
    * Gets annotation type.
    *
    * @return the annotation type
    */
   public AnnotationType[] getAnnotationTypes() {
      if (annotationType.isEmpty()) {
         return new AnnotationType[]{Types.TOKEN};
      }
      return annotationType.toArray(new AnnotationType[annotationType.size()]);
   }

   /**
    * Gets filter.
    *
    * @return the filter
    */
   public SerializablePredicate<? super HString> getFilter() {
      return filter;
   }

   /**
    * Gets to string function.
    *
    * @return the to string function
    */
   public SerializableFunction<HString, String> getToStringFunction() {
      return toStringFunction;
   }

   /**
    * Ignore stop words t.
    *
    * @return the t
    */
   public T ignoreStopWords() {
      this.filter = StopWords.notHasStopWord();
      return Cast.as(this);
   }

   /**
    * Lower case t.
    *
    * @return the t
    */
   public T lowerCase() {
      this.toStringFunction = HString::toLowerCase;
      return Cast.as(this);
   }

   /**
    * Lemmatize t.
    *
    * @return the t
    */
   public T lemmatize() {
      toStringFunction = HString::getLemma;
      return Cast.as(this);
   }

   /**
    * To string function t.
    *
    * @param toStringFunction the to string function
    * @return the t
    */
   public T toStringFunction(@NonNull SerializableFunction<HString, String> toStringFunction) {
      this.toStringFunction = toStringFunction;
      return Cast.as(this);
   }


   /**
    * Get trim function serializable function.
    *
    * @return the serializable function
    */
   public SerializableFunction<HString, HString> getTrimFunction() {
      return this.trimFunction;
   }

   /**
    * Trim function t.
    *
    * @param trimFunction the trim function
    * @return the t
    */
   public T trimFunction(@NonNull SerializableFunction<HString, HString> trimFunction) {
      this.trimFunction = trimFunction;
      return Cast.as(this);
   }

   protected Stream<Annotation> annotationStream(HString hString) {
      Stream<Annotation> stream;
      boolean includeTokens = Stream.of(getAnnotationTypes()).anyMatch(type -> type == Types.TOKEN);
      if (hasMultipleTypes()) {
         stream = hString.interleaved(getAnnotationTypes()).stream();
         if (!includeTokens) {
            stream = stream.filter(a -> !a.isInstance(Types.TOKEN));
         }
      } else {
         stream = hString.stream(getAnnotationType());
      }

      return stream;
   }

}// END OF AbstractFeatureSpec
