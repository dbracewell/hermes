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
import com.davidbracewell.collection.counter.Counters;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.extraction.TermExtractor;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

import java.util.stream.Collectors;

/**
 * The type Tfidf keyword extractor.
 *
 * @author David B. Bracewell
 */
public class TFIDFKeywordExtractor implements KeywordExtractor {
   private static final long serialVersionUID = 1L;
   private final TermExtractor termExtractor;
   private final Counter<String> inverseDocumentFrequencies;

   /**
    * Instantiates a new TFIDF based keyword extractor.
    *
    * @param inverseDocumentFrequencies the inverse document frequencies
    */
   public TFIDFKeywordExtractor(@NonNull Counter<String> inverseDocumentFrequencies) {
      this(TermExtractor.create().lowerCase(), inverseDocumentFrequencies);
   }

   /**
    * Instantiates a new TFIDF based keyword extractor.
    *
    * @param termExtractor                   the specification for filtering and converting annotations to strings
    * @param inverseDocumentFrequencies the inverse document frequencies
    */
   public TFIDFKeywordExtractor(@NonNull TermExtractor termExtractor, @NonNull Counter<String> inverseDocumentFrequencies) {
      this.termExtractor = termExtractor;
      this.inverseDocumentFrequencies = inverseDocumentFrequencies;
   }

   @Override
   public Counter<String> extract(@NonNull HString hstring) {
      Counter<String> tf = Counters.newCounter(hstring.stream(termExtractor.getAnnotationType())
                                                      .filter(termExtractor.getFilter())
                                                      .map(termExtractor.getToStringFunction())
                                                      .filter(StringUtils::isNotNullOrBlank)
                                                      .collect(Collectors.toList()));
      Counter<String> tfidf = Counters.newCounter();
      final double maxTF = tf.maximumCount();
      tf.forEach((kw, freq) -> tfidf.set(kw, (0.5 + (0.5 * freq) / maxTF) * inverseDocumentFrequencies.get(kw)));
      return tfidf;
   }

}//END OF TFIDFKeywordExtractor
