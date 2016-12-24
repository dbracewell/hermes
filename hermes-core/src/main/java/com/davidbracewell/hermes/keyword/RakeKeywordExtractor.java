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
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.TermSpec;
import com.davidbracewell.hermes.filter.StopWords;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the RAKE keyword extraction algorithm as presented in:
 * <pre>
 * Rose, S., Engel, D., Cramer, N., & Cowley, W. (2010). Automatic Keyword Extraction from Individual Documents.
 * In M. W. Berry & J. Kogan (Eds.), Text Mining: Theory and Applications: John Wiley & Sons.
 * </pre>
 *
 * @author David B. Bracewell
 */
public class RakeKeywordExtractor implements KeywordExtractor {
   private static final long serialVersionUID = 1L;
   private final TermSpec termSpec;

   /**
    * Instantiates a new Rake keyword extractor using a default {@link TermSpec} that lower cases words.
    */
   public RakeKeywordExtractor() {
      this(TermSpec.create().lowerCase());
   }


   /**
    * Instantiates a new Rake keyword extractor.
    *
    * @param termSpec the specification for how to convert tokens/phrases to strings (all other options are ignored).
    */
   public RakeKeywordExtractor(@NonNull TermSpec termSpec) {
      this.termSpec = termSpec;
   }

   @Override
   public Counter<String> extract(@NonNull HString hstring) {
      List<HString> spans = new ArrayList<>();
      hstring.document().annotate(Types.SENTENCE);

      //Step 1: Extract candidate phrases
      final StopWords stopWords = StopWords.getInstance(hstring.getLanguage());
      hstring.sentenceStream().forEach(sentence -> {
         List<HString> buffer = new ArrayList<>();
         sentence.tokenStream().forEach(token -> {
            if (stopWords.isStopWord(token) && !buffer.isEmpty()) {
               spans.add(HString.union(buffer));
               buffer.clear();
            } else if (!stopWords.isStopWord(token)) {
               buffer.add(token);
            }
         });
         if (buffer.size() > 0) {
            spans.add(HString.union(buffer));
         }
      });

      //Step 2: Score the candidates
      Counter<String> wordFreqs = Counters.newCounter();
      Counter<String> wordDegree = Counters.newCounter();
      spans.forEach(span -> span.tokenStream().forEach(word -> {
         wordFreqs.increment(termSpec.getToStringFunction().apply(word));
         wordDegree.increment(termSpec.getToStringFunction().apply(word), span.tokenLength() - 1);
      }));

      Counter<String> wordScores = Counters.newCounter();
      wordFreqs.forEach(
         (word, freq) -> wordScores.increment(word, (wordDegree.get(word) + freq) / freq));


      Counter<String> phraseScores = Counters.newCounter();
      spans.forEach(span -> {
         double score = 0;
         for (Annotation word : span.tokens()) {
            score += wordScores.get(termSpec.getToStringFunction().apply(word));
         }
         phraseScores.increment(termSpec.getToStringFunction().apply(span), score);
      });


      return phraseScores;
   }
}//END OF RakeKeywordExtractor
