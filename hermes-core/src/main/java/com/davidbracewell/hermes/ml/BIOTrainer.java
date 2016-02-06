
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

import com.davidbracewell.apollo.ml.Dataset;
import com.davidbracewell.apollo.ml.preprocess.PreprocessorList;
import com.davidbracewell.apollo.ml.preprocess.filter.CountFilter;
import com.davidbracewell.apollo.ml.sequence.*;
import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.io.resource.Resource;

/**
 * @author David B. Bracewell
 */
public abstract class BIOTrainer extends CommandLineApplication {
  private static final long serialVersionUID = 1L;

  protected final AnnotationType annotationType;
  @Option(description = "Location of the corpus to process", required = true)
  protected Resource corpus;
  @Option(name = "format", description = "Format of the corpus", required = true)
  protected String corpusFormat;
  @Option(description = "Location to save model", required = true)
  protected Resource model;
  @Option(description = "Minimum count for a feature to be kept", defaultValue = "5")
  protected int minFeatureCount;
  @Option(description = "TEST or TRAIN", defaultValue = "TEST")
  protected Mode mode;

  public BIOTrainer(String name, AnnotationType annotationType) {
    super(name);
    this.annotationType = annotationType;
  }

  protected SequenceValidator getValidator() {
    return new BIOValidator();
  }

  protected abstract SequenceFeaturizer<Annotation> getFeaturizer();

  protected PreprocessorList<Sequence> getPreprocessors() {
    if (minFeatureCount > 1) {
      return PreprocessorList.create(
        new CountFilter(d -> d >= minFeatureCount).asSequenceProcessor()
      );
    }
    return PreprocessorList.create();
  }

  protected abstract SequenceLabelerLearner getLearner();

  protected Dataset<Sequence> getDataset(SequenceFeaturizer<Annotation> featurizer) {
    return Corpus
      .builder()
      .source(corpus)
      .format(corpusFormat)
      .build()
      .asSequenceDataSet(new BIOLabelMaker(annotationType), featurizer);
  }

  protected void test() throws Exception {
    BIOTagger tagger = BIOTagger.read(model);
    Dataset<Sequence> test = getDataset(tagger.featurizer);
    BIOEvaluation eval = new BIOEvaluation();
    eval.evaluate(tagger.labeler, test);
    eval.output(System.out);
  }

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

  @Override
  protected void programLogic() throws Exception {
    if (mode == Mode.TEST) {
      test();
    } else {
      train();
    }
  }

}// END OF BIOTrainer
