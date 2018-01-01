package com.davidbracewell.hermes.tokenization;

import com.davidbracewell.apollo.ml.data.Dataset;
import com.davidbracewell.apollo.ml.data.DatasetType;
import com.davidbracewell.apollo.ml.preprocess.PreprocessorList;
import com.davidbracewell.apollo.ml.preprocess.filter.MinCountFilter;
import com.davidbracewell.apollo.ml.sequence.*;
import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.hermes.ml.Mode;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.string.StringPredicates;
import com.davidbracewell.string.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public abstract class WordSegmenterTrainer extends CommandLineApplication {

   /**
    * The Corpus.
    */
   @Option(description = "Location of the corpus to process", required = true)
   protected Resource corpus;
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


   @Override
   protected void programLogic() throws Exception {
      switch (mode) {
         case TRAIN:
            train();
            break;
      }
   }


   /**
    * Gets dataset.
    *
    * @param featurizer the featurizer
    * @return the dataset
    */
   protected Dataset<Sequence> getDataset(SequenceFeaturizer<Character> featurizer) {
      List<Sequence> trainingData = new ArrayList<>();
      try (MStream<String> lines = corpus.lines()) {
         lines.forEach(line -> {
            line = line.trim();
            if (StringUtils.isNotNullOrBlank(line)) {
               SequenceInput<Character> input = new SequenceInput<>();
               for (int i = 0; i < line.length(); i++) {
                  char c = line.charAt(i);
                  if (Character.isWhitespace(c)) {
                     input.setLabel(input.size() - 1, "true");
                  } else {
                     input.add(c, "false");
                  }
               }
               trainingData.add(featurizer.extractSequence(input.iterator()));
            }
         });
      } catch (Exception e) {
         throw Throwables.propagate(e);
      }

      return Dataset.sequence()
                    .type(DatasetType.OffHeap)
                    .source(trainingData.stream());

   }

   /**
    * Gets featurizer.
    *
    * @return the featurizer
    */
   protected abstract SequenceFeaturizer<Character> getFeaturizer();

   /**
    * Gets learner.
    *
    * @return the learner
    */
   protected abstract SequenceLabelerLearner getLearner();

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

   protected SerializableFunction<String, TokenType> getTokenClassifier() {
      return (string) -> {
         if (StringPredicates.IS_PUNCTUATION.test(string)) {
            return TokenType.PUNCTUATION;
         } else if (StringPredicates.IS_DIGIT.test(string)) {
            return TokenType.NUMBER;
         } else {
            return TokenType.ALPHA_NUMERIC;
         }
      };
   }

   protected void train() throws Exception {
      final SequenceFeaturizer<Character> featurizer = getFeaturizer();
      Dataset<Sequence> train = getDataset(featurizer);
      PreprocessorList<Sequence> preprocessors = getPreprocessors();
      if (preprocessors != null && preprocessors.size() > 0) {
         train = train.preprocess(preprocessors);
      }
      train.encode();
      SequenceLabelerLearner learner = getLearner();
      SequenceLabeler labeler = learner.train(train);
      CharacterBasedWordSegmenter wordSegmenter = new CharacterBasedWordSegmenter(labeler, getTokenClassifier(),
                                                                                  featurizer);
      wordSegmenter.write(model);
   }

}//END OF WordSegmenterTrainer
