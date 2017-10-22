package com.davidbracewell.hermes.ml;

import com.davidbracewell.apollo.ml.Instance;
import com.davidbracewell.apollo.ml.TrainTestSplit;
import com.davidbracewell.apollo.ml.classification.Classification;
import com.davidbracewell.apollo.ml.classification.Classifier;
import com.davidbracewell.apollo.ml.classification.ClassifierEvaluation;
import com.davidbracewell.apollo.ml.classification.ClassifierLearner;
import com.davidbracewell.apollo.ml.data.Dataset;
import com.davidbracewell.apollo.ml.featurizer.Featurizer;
import com.davidbracewell.apollo.ml.preprocess.PreprocessorList;
import com.davidbracewell.cli.CommandLineParser;
import com.davidbracewell.cli.NamedOption;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.guava.common.collect.Iterables;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.Hermes;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusType;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

/**
 * The type Basic text classifier.
 *
 * @author David B. Bracewell
 */
public abstract class BasicTextClassifier implements TextClassifier {
   private static final long serialVersionUID = 1L;
   private Classifier classifier;

   @Override
   public final void classify(@NonNull HString text) {
      text.document().annotate(required());
      onClassify(text, classifier.classify(getFeaturizer().extractInstance(text)));
   }

   /**
    * Driver.
    *
    * @param args the args
    */
   public void cli(String[] args) {
      CommandLineParser cli = new CommandLineParser();
      cli.addOption(NamedOption.builder()
                               .name("data")
                               .description("Data to use for training or testing")
                               .required(true)
                               .type(Resource.class)
                               .build());
      cli.addOption(NamedOption.builder()
                               .name("format")
                               .description("Format for reading data")
                               .defaultValue("JSON_OPL")
                               .type(String.class)
                               .build());
      cli.addOption(NamedOption.builder()
                               .name("corpusType")
                               .description("Type of corpus to create (e.g. in-memory)")
                               .defaultValue("IN_MEMORY")
                               .type(CorpusType.class)
                               .build());
      cli.addOption(NamedOption.builder()
                               .name("mode")
                               .description("Train/Test/Split")
                               .defaultValue("TEST")
                               .type(Mode.class)
                               .build());
      cli.addOption(NamedOption.builder()
                               .name("model")
                               .description("Model to save/load")
                               .type(Resource.class)
                               .required(true)
                               .build());
      cli.parse(args);
      Hermes.initializeApplication(args);

      Dataset<Instance> data = getDataset(cli.get("data"), cli.get("format"), cli.get("corpusType"));

      Mode mode = cli.get("mode");
      switch (mode) {
         case TEST:
            try {
               this.classifier = Cast.<BasicTextClassifier>as(TextClassifier.read(cli.get("model"))).classifier;
            } catch (Exception e) {
               throw Throwables.propagate(e);
            }
            test(data);
            break;
         case SPLIT:
            TrainTestSplit<Instance> split = Iterables.getFirst(data.split(0.8), null);
            train(split.getTrain());
            test(split.getTest());
            break;
         case TRAIN:
            train(data);
            try {
               write(cli.get("model"));
            } catch (Exception e) {
               throw Throwables.propagate(e);
            }
            break;
         default:
            System.err.println("Unknown option");
      }
   }

   /**
    * Gets dataset.
    *
    * @param data       the data
    * @param format     the format
    * @param corpusType the corpus type
    * @return the dataset
    */
   protected Dataset<Instance> getDataset(Resource data, String format, CorpusType corpusType) {
      Corpus corpus = Corpus.builder()
                            .format(format)
                            .source(data)
                            .corpusType(corpusType)
                            .build();
      AnnotatableType[] required = required();
      if (required != null && required.length > 0) {
         corpus = corpus.annotate(required);
      }
      return corpus.asClassificationDataSet(getFeaturizer(), getOracle()).preprocess(getPreprocessors()).shuffle();
   }

   /**
    * Gets featurizer.
    *
    * @return the featurizer
    */
   protected abstract Featurizer<HString> getFeaturizer();

   /**
    * Gets learner.
    *
    * @return the learner
    */
   protected abstract ClassifierLearner getLearner();

   /**
    * Gets oracle.
    *
    * @return the oracle
    */
   protected abstract SerializableFunction<HString, Object> getOracle();

   /**
    * Gets preprocessors.
    *
    * @return the preprocessors
    */
   protected PreprocessorList<Instance> getPreprocessors() {
      return PreprocessorList.empty();
   }

   /**
    * On classify.
    *
    * @param text           the text
    * @param classification the classification
    */
   protected abstract void onClassify(HString text, Classification classification);

   /**
    * Required annotatable type [ ].
    *
    * @return the annotatable type [ ]
    */
   protected abstract AnnotatableType[] required();

   /**
    * Test.
    *
    * @param dataset the dataset
    */
   protected void test(Dataset<Instance> dataset) {
      ClassifierEvaluation.evaluateModel(classifier, dataset)
                          .output(System.out);
   }

   /**
    * Train.
    *
    * @param dataset the dataset
    */
   protected void train(Dataset<Instance> dataset) {
      this.classifier = getLearner().train(dataset);
   }


}// END OF BasicTextClassifier