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

import com.davidbracewell.Copyable;
import lombok.NonNull;

/**
 * The type N gram spec.
 *
 * @author David B. Bracewell
 */
public class NGramExtractor extends AbstractNGramExtractor<NGramExtractor> implements Copyable<NGramExtractor> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new N gram spec.
    */
   public NGramExtractor() {
   }

   /**
    * Instantiates a new N gram spec.
    *
    * @param copy the copy
    */
   public NGramExtractor(@NonNull NGramExtractor copy) {
      super(copy);
   }

   /**
    * Create n gram spec.
    *
    * @return the n gram spec
    */
   public static NGramExtractor create() {
      return new NGramExtractor();
   }

   /**
    * Unigrams n gram spec.
    *
    * @return the n gram spec
    */
   public static NGramExtractor unigrams() {
      return new NGramExtractor();
   }

   /**
    * Bigrams n gram spec.
    *
    * @return the n gram spec
    */
   public static NGramExtractor bigrams() {
      return new NGramExtractor().order(2);
   }

   /**
    * Trigrams n gram spec.
    *
    * @return the n gram spec
    */
   public static NGramExtractor trigrams() {
      return new NGramExtractor().order(3);
   }

   /**
    * Order n gram spec.
    *
    * @param min the min
    * @param max the max
    * @return the n gram spec
    */
   public static NGramExtractor order(int min, int max) {
      return new NGramExtractor().min(min).max(max);
   }

   @Override
   public NGramExtractor copy() {
      return new NGramExtractor(this);
   }


}//END OF NGramSpec
