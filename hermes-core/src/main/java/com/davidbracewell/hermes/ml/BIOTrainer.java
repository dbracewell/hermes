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

package com.davidbracewell.hermes.ml;

import com.davidbracewell.apollo.ml.TrainTestSet;
import com.davidbracewell.apollo.ml.data.Dataset;
import com.davidbracewell.apollo.ml.preprocess.PreprocessorList;
import com.davidbracewell.apollo.ml.preprocess.filter.MinCountFilter;
import com.davidbracewell.apollo.ml.sequence.*;
import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusType;
import com.davidbracewell.io.resource.Resource;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

/**
 * The type Bio trainer.
 *
 * @author David B. Bracewell
 */
public abstract class BIOTrainer extends CommandLineApplication {
   private static final long serialVersionUID = 1L;

   /**
    * The Annotation type.
    */
   protected final AnnotationType annotationType;
   /**
    * The Corpus.
    */
   @Option(description = "Location of the corpus to process", required = true)
   protected Resource corpus;
   /**
    * The Corpus format.
    */
   @Option(name = "format", description = "Format of the corpus", required = true)
   protected String corpusFormat;
   /**
    * The Model.
    */
   @Option(description = "Location to save model", required = true)
   protected Resource model;
   /**
    * The Min feature count.
    */
   @Option(description = "Minimum count for a feature to be kept", defaultValue = "5")
   protected int minFeatureCount;
   /**
    * The Mode.
    */
   @Option(description = "TEST or TRAIN", defaultValue = "TEST")
   protected Mode mode;

   @Option(description = "Type of corpus", defaultValue = "IN_MEMORY")
   protected CorpusType corpusType;

   @Option(name = "annotation", description = "The annotation type in the corpus to train with", aliases = "a")
   protected AnnotationType trainingAnnotation;

   /**
    * Instantiates a new Bio trainer.
    *
    * @param name           the name
    * @param annotationType the annotation type
    */
   public BIOTrainer(String name, AnnotationType annotationType) {
      super(name);
      this.annotationType = annotationType;
      if (this.trainingAnnotation == null) {
         this.trainingAnnotation = annotationType;
      }
   }

   /**
    * Gets validator.
    *
    * @return the validator
    */
   protected SequenceValidator getValidator() {
      return new BIOValidator();
   }

   /**
    * Gets featurizer.
    *
    * @return the featurizer
    */
   protected abstract SequenceFeaturizer<Annotation> getFeaturizer();

   /**
    * Gets preprocessors.
    *
    * @return the preprocessors
    */
   protected PreprocessorList<Sequence> getPreprocessors() {
      if (minFeatureCount > 1) {
         return PreprocessorList.create(new MinCountFilter(minFeatureCount).asSequenceProcessor());
      }
      return PreprocessorList.empty();
   }

   /**
    * Gets learner.
    *
    * @return the learner
    */
   protected abstract SequenceLabelerLearner getLearner();

   protected Set<String> validTags() {
      return Collections.emptySet();
   }

   /**
    * Gets dataset.
    *
    * @param featurizer the featurizer
    * @return the dataset
    */
   protected Dataset<Sequence> getDataset(SequenceFeaturizer<Annotation> featurizer) {
      return Corpus
                .builder()
                .corpusType(corpusType)
                .source(corpus)
                .format(corpusFormat)
                .build()
                .asSequenceDataSet(new BIOLabelMaker(trainingAnnotation, validTags()), featurizer);
   }

   /**
    * Test.
    *
    * @throws Exception the exception
    */
   protected void test() throws Exception {
      BIOTagger tagger = BIOTagger.read(model);
      Dataset<Sequence> test = getDataset(tagger.featurizer);
      BIOEvaluation eval = new BIOEvaluation();
      eval.evaluate(tagger.labeler, test);
      eval.output(System.out);
   }

   /**
    * Train.
    *
    * @throws Exception the exception
    */
   protected void train() throws Exception {
      final SequenceFeaturizer<Annotation> featurizer = getFeaturizer();
      Dataset<Sequence> train = getDataset(featurizer);
      PreprocessorList<Sequence> preprocessors = getPreprocessors();
      if (preprocessors != null && preprocessors.size() > 0) {
         train = train.preprocess(preprocessors);
      }
      train.encode();
      SequenceLabelerLearner learner = getLearner();
      learner.setValidator(getValidator());
      SequenceLabeler labeler = learner.train(train);
      BIOTagger tagger = new BIOTagger(featurizer, annotationType, labeler);
      tagger.write(model);
   }

   protected void split() throws Exception {
      final SequenceFeaturizer<Annotation> featurizer = getFeaturizer();
      Dataset<Sequence> all = getDataset(featurizer);
      all = all.shuffle(new Random(56789));
      TrainTestSet<Sequence> trainTest = all.split(0.80);
      trainTest.forEach(tt -> {
         PreprocessorList<Sequence> preprocessors = getPreprocessors();
         if (preprocessors != null && preprocessors.size() > 0) {
            Dataset<Sequence> train = tt.getTrain().preprocess(preprocessors);
            SequenceLabelerLearner learner = getLearner();
            learner.setValidator(getValidator());
            SequenceLabeler labeler = learner.train(train);
            BIOTagger tagger = new BIOTagger(featurizer, annotationType, labeler);
            BIOEvaluation eval = new BIOEvaluation();
            eval.evaluate(tagger.labeler, tt.getTest());
            eval.output(System.out);
         }
      });
   }

   @Override
   protected void programLogic() throws Exception {
      switch (mode) {
         case TEST:
            test();
            break;
         case TRAIN:
            train();
            break;
         case SPLIT:
            split();
            break;
      }
   }

}// END OF BIOTrainer
