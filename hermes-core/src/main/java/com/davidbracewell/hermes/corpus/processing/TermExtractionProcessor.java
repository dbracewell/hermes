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

package com.davidbracewell.hermes.corpus.processing;

import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.extraction.TermExtractor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * @author David B. Bracewell
 */
public class TermExtractionProcessor implements ProcessingModule {
   private static final long serialVersionUID = 1L;
   public static final String EXTRACTED_TERMS = "EXTRACTED_TERMS";

   public void setExtractor(@NonNull TermExtractor extractor) {
      this.extractor = extractor;
   }

   @Getter
   private TermExtractor extractor = TermExtractor.create();
   @Getter
   @Setter
   private boolean documentFrequencies = false;

   public TermExtractionProcessor() {

   }

   public TermExtractionProcessor(TermExtractor extractor, boolean documentFrequencies) {
      this.extractor = extractor;
      this.documentFrequencies = documentFrequencies;
   }

   @Override
   public Corpus process(Corpus corpus, ProcessorContext context) throws Exception {
      Counter<String> counts;
      if (documentFrequencies) {
         counts = corpus.documentFrequencies(extractor);
      } else {
         counts = corpus.termFrequencies(extractor);
      }
      context.property(EXTRACTED_TERMS, counts);
      return onComplete(corpus, context, counts);
   }

   protected Corpus onComplete(Corpus corpus, ProcessorContext context, Counter<String> counts) {
      return corpus;
   }

}//END OF TermExtractionProcessor
