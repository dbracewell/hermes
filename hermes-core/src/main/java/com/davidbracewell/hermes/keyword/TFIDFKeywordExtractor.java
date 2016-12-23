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
import com.davidbracewell.collection.counter.Counters;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.corpus.TermSpec;
import lombok.NonNull;

import java.util.stream.Collectors;

/**
 * The type Tfidf keyword extractor.
 *
 * @author David B. Bracewell
 */
public class TFIDFKeywordExtractor implements KeywordExtractor {
   private static final long serialVersionUID = 1L;
   private final TermSpec termSpec;
   private final Counter<String> inverseDocumentFrequencies;

   /**
    * Instantiates a new Tfidf keyword extractor.
    *
    * @param inverseDocumentFrequencies the document frequencies
    */
   public TFIDFKeywordExtractor(@NonNull Counter<String> inverseDocumentFrequencies) {
      this(TermSpec.create(), inverseDocumentFrequencies);
   }

   /**
    * Instantiates a new Tfidf keyword extractor.
    *
    * @param termSpec                   the term spec
    * @param inverseDocumentFrequencies the document frequencies
    */
   public TFIDFKeywordExtractor(@NonNull TermSpec termSpec, @NonNull Counter<String> inverseDocumentFrequencies) {
      this.termSpec = termSpec;
      this.inverseDocumentFrequencies = inverseDocumentFrequencies;
   }

   @Override
   public Counter<String> extract(@NonNull HString hstring) {
      Counter<String> tf = Counters.newCounter(hstring.get(termSpec.getAnnotationType()).stream()
                                                      .filter(termSpec.getFilter())
                                                      .map(termSpec.getToStringFunction())
                                                      .collect(Collectors.toList()));
      Counter<String> tfidf = Counters.newCounter();
      final double maxTF = tf.maximumCount();
      tf.forEach((kw, freq) -> tfidf.set(kw, (0.5 + (0.5 * freq) / maxTF) * inverseDocumentFrequencies.get(kw)));
      return tfidf;
   }

}//END OF TFIDFKeywordExtractor
