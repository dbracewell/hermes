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

package com.davidbracewell.hermes.ml.feature;

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.filter.StopWords;
import lombok.NonNull;

/**
 * The type N gram feature spec.
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public abstract class AbstractNGramFeatureSpec<T extends AbstractNGramFeatureSpec> extends AbstractFeatureSpec<T> {
   private static final long serialVersionUID = 1L;
   private int min = 1;
   private int max = 1;

   public AbstractNGramFeatureSpec() {
   }

   public AbstractNGramFeatureSpec(@NonNull AbstractNGramFeatureSpec<T> copy) {
      super(copy);
      this.min = copy.min;
      this.max = copy.max;
   }

   public T order(int order) {
      this.min = order;
      this.max = order;
      return Cast.as(this);
   }

   @Override
   public T ignoreStopWords() {
      filter(StopWords.notHasStopWord());
      return Cast.as(this);
   }

   /**
    * Gets max.
    *
    * @return the max
    */
   public int getMax() {
      return max;
   }

   /**
    * Max t.
    *
    * @param max the max
    * @return the t
    */
   public T max(int max) {
      this.max = max;
      return Cast.as(this);
   }

   /**
    * Gets min.
    *
    * @return the min
    */
   public int getMin() {
      return min;
   }

   /**
    * Min t.
    *
    * @param min the min
    * @return the t
    */
   public T min(int min) {
      this.min = min;
      return Cast.as(this);
   }

}//END OF AbstractNGramFeatureSpec
