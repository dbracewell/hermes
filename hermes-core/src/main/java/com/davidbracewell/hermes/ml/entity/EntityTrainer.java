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

package com.davidbracewell.hermes.ml.entity;

import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.SequenceLabelerLearner;
import com.davidbracewell.apollo.ml.sequence.TransitionFeatures;
import com.davidbracewell.apollo.ml.sequence.feature.NGramSequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.feature.WindowedSequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.linear.CRFTrainer;
import com.davidbracewell.apollo.ml.sequence.linear.LibraryLoader;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.ml.BIOTrainer;
import com.davidbracewell.hermes.ml.BIOValidator;
import com.davidbracewell.hermes.ml.feature.AffixFeaturizer;
import com.davidbracewell.hermes.ml.feature.WordAndClassFeaturizer;
import com.davidbracewell.hermes.ml.feature.WordClassFeaturizer;
import com.davidbracewell.hermes.ml.feature.WordFeaturizer;

/**
 * @author David B. Bracewell
 */
public class EntityTrainer extends BIOTrainer {
   private static final long serialVersionUID = 1L;

   public EntityTrainer() {
      super("EntityTrainer", Types.ML_ENTITY);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected SequenceFeaturizer<Annotation> getFeaturizer() {
      return SequenceFeaturizer.chain(
            new EntityFeaturizer(),
            new AffixFeaturizer(3,3).asSequenceFeaturizer(),
            new WindowedSequenceFeaturizer<>(2, 2, WordFeaturizer.INSTANCE),
            new WindowedSequenceFeaturizer<>(2, 2, WordClassFeaturizer.INSTANCE),
            new WindowedSequenceFeaturizer<>(2, 2, WordAndClassFeaturizer.INSTANCE),
            new NGramSequenceFeaturizer<>(2, 2, WordFeaturizer.INSTANCE),
            new NGramSequenceFeaturizer<>(2, 2, WordClassFeaturizer.INSTANCE));
   }

   @Override
   public void setup() throws Exception {
      LibraryLoader.INSTANCE.load();
      this.trainingAnnotation = Types.ENTITY;
   }

//   @Override
//   protected Set<String> validTags() {
//      return ImmutableSet.of("PERSON", "LOCATION", "ORGANIZATION", "CARDINAL", "ORDINAL");
//   }

   @Override
   protected SequenceLabelerLearner getLearner() {
      SequenceLabelerLearner learner =
//            new MEMMLearner();
//            new StructuredPerceptronLearner();
            new CRFTrainer();
      learner.setTransitionFeatures(TransitionFeatures.FIRST_ORDER);
      learner.setValidator(new BIOValidator());
      learner.setParameter("maxIterations", 100);
      learner.setParameter("verbose", false);
      return learner;
   }

   public static void main(String[] args) throws Exception {
      new EntityTrainer().run(args);
   }

}//END OF EntityTrainer
