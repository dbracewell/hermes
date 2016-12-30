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

package com.davidbracewell.hermes.corpus;

import com.davidbracewell.Copyable;
import com.davidbracewell.hermes.ml.feature.AbstractNGramFeatureSpec;
import lombok.NonNull;

/**
 * @author David B. Bracewell
 */
public class NGramSpec extends AbstractNGramFeatureSpec<NGramSpec> implements Copyable<NGramSpec> {
   private static final long serialVersionUID = 1L;

   public NGramSpec() {
   }

   public NGramSpec(@NonNull NGramSpec copy) {
      super(copy);
   }

   public static NGramSpec create() {
      return new NGramSpec();
   }

   public static NGramSpec unigrams() {
      return new NGramSpec();
   }

   public static NGramSpec bigrams() {
      return new NGramSpec().order(2);
   }

   public static NGramSpec trigrams() {
      return new NGramSpec().order(3);
   }

   public static NGramSpec order(int min, int max) {
      return new NGramSpec().min(min).max(max);
   }

   @Override
   public NGramSpec copy() {
      return new NGramSpec(this);
   }

}//END OF NGramSpec
