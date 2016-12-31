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

package com.davidbracewell.hermes.keyword;

import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.corpus.TermSpec;
import lombok.NonNull;

/**
 * <p>Implementation of a {@link KeywordExtractor} that extracts and scores terms based on a given {@link TermSpec}.</p>
 *
 * @author David B. Bracewell
 */
public class TermSpecExtractor implements KeywordExtractor {
   private static final long serialVersionUID = 1L;
   private final TermSpec termSpec;

   /**
    * Instantiates a new TermSpec keyword extractor that uses a default TermSpec with lowercase words
    */
   public TermSpecExtractor() {
      this(TermSpec.create().lowerCase());
   }

   /**
    * Instantiates a new TermSpec keyword extractor.
    *
    * @param termSpec the specification on how to extract terms
    */
   public TermSpecExtractor(@NonNull TermSpec termSpec) {
      this.termSpec = termSpec;
   }

   @Override
   public Counter<String> extract(@NonNull HString hstring) {
      return termSpec.count(hstring);
   }

}//END OF TermSpecExtractor
