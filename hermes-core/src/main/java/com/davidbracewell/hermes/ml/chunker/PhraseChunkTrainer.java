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

package com.davidbracewell.hermes.ml.chunker;

import com.davidbracewell.apollo.ml.data.Dataset;
import com.davidbracewell.apollo.ml.sequence.*;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.POS;
import com.davidbracewell.hermes.Pipeline;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.ml.BIOLabelMaker;
import com.davidbracewell.hermes.ml.BIOTrainer;
import com.davidbracewell.hermes.ml.BIOValidator;

/**
 * The type Phrase chunk trainer.
 *
 * @author David B. Bracewell
 */
public class PhraseChunkTrainer extends BIOTrainer {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Phrase chunk trainer.
    */
   public PhraseChunkTrainer() {
      super("PhraseChunkTrainer", Types.PHRASE_CHUNK);
   }

   /**
    * The entry point of application.
    *
    * @param args the input arguments
    */
   public static void main(String[] args) {
      new PhraseChunkTrainer().run(args);
   }

   @Override
   protected Dataset<Sequence> getDataset(SequenceFeaturizer<Annotation> featurizer) {
      return Corpus.builder()
                   .inMemory()
                   .source(corpus)
                   .format(corpusFormat)
                   .build()
                   .map(d -> {
                      d.getAnnotationSet().setIsCompleted(Types.PART_OF_SPEECH, false, null);
                      Pipeline.process(d, Types.PART_OF_SPEECH);
                      d.get(Types.PHRASE_CHUNK).forEach(annotation -> {
                         if (annotation.get(Types.PART_OF_SPEECH).as(POS.class).isInstance(POS.INTJ,
                                                                                           POS.LST,
                                                                                           POS.UCP)) {
                            d.remove(annotation);
                         }
                      });
                      return d;
                   })
                   .asSequenceDataSet(new BIOLabelMaker(annotationType), featurizer);
   }

   @Override
   public void setup() throws Exception {
      LibraryLoader.INSTANCE.load();
   }

   @Override
   protected SequenceFeaturizer<Annotation> getFeaturizer() {
      return new PhraseChunkFeaturizer();
   }

   @Override
   protected SequenceLabelerLearner getLearner() {
      SequenceLabelerLearner learner = new CRFTrainer();
      learner.setTransitionFeatures(TransitionFeature.FIRST_ORDER);
      learner.setValidator(new BIOValidator());
      learner.setParameter("maxIterations", 200);
      learner.setParameter("verbose", true);
      return learner;
   }

}// END OF PhraseChunkTrainer
