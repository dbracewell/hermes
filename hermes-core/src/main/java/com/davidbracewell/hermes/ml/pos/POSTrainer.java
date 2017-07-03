package com.davidbracewell.hermes.ml.pos;

import com.davidbracewell.Language;
import com.davidbracewell.Lazy;
import com.davidbracewell.apollo.ml.TrainTestSet;
import com.davidbracewell.apollo.ml.classification.AveragedPerceptronLearner;
import com.davidbracewell.apollo.ml.data.Dataset;
import com.davidbracewell.apollo.ml.data.DatasetType;
import com.davidbracewell.apollo.ml.preprocess.PreprocessorList;
import com.davidbracewell.apollo.ml.preprocess.filter.MinCountFilter;
import com.davidbracewell.apollo.ml.sequence.*;
import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.POS;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.ml.Mode;
import com.davidbracewell.io.resource.Resource;

import java.util.Objects;

/**
 * The type Pos trainer.
 *
 * @author David B. Bracewell
 */
public class POSTrainer extends CommandLineApplication {
   private static final long serialVersionUID = 1L;

   /**
    * The Corpus.
    */
   @Option(description = "Location of the corpus to process", required = true)
   Resource corpus;
   /**
    * The Corpus format.
    */
   @Option(name = "format", description = "Format of the corpus", defaultValue = "JSON_OPL")
   String corpusFormat;
   /**
    * The Model.
    */
   @Option(description = "Location to save model", required = true)
   Resource model;
   /**
    * The Min feature count.
    */
   @Option(description = "Minimum count for a feature to be kept", defaultValue = "5")
   int minFeatureCount;
   /**
    * The Mode.
    */
   @Option(description = "TEST or TRAIN", defaultValue = "TRAIN")
   Mode mode;
   /**
    * The Language.
    */
   @Option(description = "The language of the model to train", defaultValue = "ENGLISH")
   Language language;

   private volatile Lazy<SequenceFeaturizer<Annotation>> featurizer = new Lazy<>(() -> {
      if (language == Language.ENGLISH) {
         return new DefaultPOSFeaturizer();
      }
      return new DefaultPOSFeaturizer();
   });


   /**
    * Instantiates a new Pos trainer.
    */
   public POSTrainer() {
      super("POSTrainer");
   }

   /**
    * The entry point of application.
    *
    * @param args the input arguments
    */
   public static void main(String[] args) {
      new POSTrainer().run(args);
   }

   @Override
   protected void programLogic() throws Exception {
      if (mode == Mode.TRAIN) {
         train();
      } else if (mode == Mode.TEST) {
         test();
      } else {
         TrainTestSet<Sequence> trainTestSplits = loadDataset().shuffle().split(0.8);
         if (minFeatureCount > 1) {
            trainTestSplits.preprocess(() ->
                                          PreprocessorList.create(
                                             new MinCountFilter(minFeatureCount).asSequenceProcessor()));
         }
         trainTestSplits.evaluate(getLearner(), PerInstanceEvaluation::new).output(System.out);
      }
   }

   @Override
   public void setup() throws Exception {
      LibraryLoader.INSTANCE.load();
   }

   /**
    * Load dataset dataset.
    *
    * @return the dataset
    * @throws Exception the exception
    */
   protected Dataset<Sequence> loadDataset() throws Exception {
      return Dataset.sequence()
                    .type(DatasetType.InMemory)
                    .source(
                       Corpus.builder()
                             .source(corpus)
                             .format(corpusFormat)
                             .build()
                             .stream()
                             .filter(d -> d.getAnnotationSet().isCompleted(Types.PART_OF_SPEECH))
                             .flatMap(d -> d.sentences().stream())
                             .map(sentence -> {
                                SequenceInput<Annotation> input = new SequenceInput<>();
                                for (int i = 0; i < sentence.tokenLength(); i++) {
                                   POS pos = sentence.tokenAt(i).getPOS();
                                   if (pos == null) {
                                      return null;
                                   }
                                   input.add(sentence.tokenAt(i), pos.asString());
                                }
                                return featurizer.get().extractSequence(input.iterator());
                             })
                             .filter(Objects::nonNull)
                           );
   }

   private SequenceLabelerLearner getLearner() {
      SequenceLabelerLearner learner = new WindowedLearner(new AveragedPerceptronLearner().oneVsRest());
      if (language == Language.ENGLISH) {
         learner.setValidator(new EnglishPOSValidator());
      } else {
         learner.setValidator(new EnglishPOSValidator());
      }
      learner.setParameter("maxIterations", 25);
      learner.setParameter("tolerance", 1E-5);
      learner.setParameter("verbose", true);
      learner.setTransitionFeatures(TransitionFeature.chain(TransitionFeature.SECOND_ORDER,
                                                            TransitionFeature.fromTemplate("T[-1],w[0]"),
                                                            TransitionFeature.fromTemplate("T[-2],T[-1],w[0]")));
      return learner;
   }

   /**
    * Train.
    *
    * @throws Exception the exception
    */
   protected void train() throws Exception {
      Dataset<Sequence> train = loadDataset();
      if (minFeatureCount > 1) {
         train = train.preprocess(PreprocessorList.create(new MinCountFilter(minFeatureCount).asSequenceProcessor()));
      }
      SequenceLabeler labeler = getLearner().train(train);
      POSTagger tagger = new POSTagger(featurizer.get(), labeler);
      tagger.write(model);
   }


   /**
    * Test.
    *
    * @throws Exception the exception
    */
   protected void test() throws Exception {
      POSTagger tagger = POSTagger.read(model);
      Dataset<Sequence> test = loadDataset();
      System.out.println("Read: " + test.size());
      PerInstanceEvaluation evaluation = new PerInstanceEvaluation();
      evaluation.evaluate(tagger.labeler, test);
      evaluation.output(System.out, true);
   }

}// END OF POSTrainer
