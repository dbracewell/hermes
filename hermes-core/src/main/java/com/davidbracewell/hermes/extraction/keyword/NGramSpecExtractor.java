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

package com.davidbracewell.hermes.extraction.keyword;

import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.extraction.NGramExtractor;
import lombok.NonNull;

import java.util.stream.Collectors;

/**
 * <p>Implementation of a {@link KeywordExtractor} that extracts and scores terms based on a given {@link
 * NGramExtractor}.</p>
 *
 * @author David B. Bracewell
 */
public class NGramSpecExtractor implements KeywordExtractor {
   private static final long serialVersionUID = 1L;
   private final NGramExtractor nGramExtractor;

   /**
    * Instantiates a new NGramSpec keyword extractor that uses a default NGramSpec (unigrams) with lowercase words
    */
   public NGramSpecExtractor() {
      this(NGramExtractor.unigrams().lowerCase());
   }

   /**
    * Instantiates a new NGramSpec keyword extractor.
    *
    * @param nGramExtractor the specification on how to extract terms
    */
   public NGramSpecExtractor(@NonNull NGramExtractor nGramExtractor) {
      this.nGramExtractor = nGramExtractor;
   }


   @Override
   public Counter<String> extract(@NonNull HString hstring) {
      return nGramExtractor.countTuples(hstring).mapKeys(t -> t.stream()
                                                               .map(Object::toString)
                                                               .collect(Collectors.joining(" ")));
   }

}//END OF NGramSpecExtractor
