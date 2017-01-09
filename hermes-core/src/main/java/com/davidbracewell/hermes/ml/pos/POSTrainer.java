package com.davidbracewell.hermes.ml.pos;

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
 * @author David B. Bracewell
 */
public class POSTrainer extends CommandLineApplication {
   private static final long serialVersionUID = 1L;

   @Option(description = "Location of the corpus to process", required = true)
   Resource corpus;
   @Option(name = "format", description = "Format of the corpus", defaultValue = "JSON_OPL")
   String corpusFormat;
   @Option(description = "Location to save model", required = true)
   Resource model;
   @Option(description = "Minimum count for a feature to be kept", defaultValue = "5")
   int minFeatureCount;
   @Option(description = "TEST or TRAIN", defaultValue = "TRAIN")
   Mode mode;

   private final DefaultPOSFeaturizer featurizer = new DefaultPOSFeaturizer();

   public POSTrainer() {
      super("POSTrainer");
   }

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
                                return featurizer.extractSequence(input.iterator());
                             })
                             .filter(Objects::nonNull)
                           );
   }

   private SequenceLabelerLearner getLearner() {
      SequenceLabelerLearner learner = new WindowedLearner(new AveragedPerceptronLearner().oneVsRest());
      learner.setValidator(new POSValidator());
      learner.setParameter("maxIterations", 200);
      learner.setParameter("tolerance", 1E-4);
      learner.setParameter("verbose", true);
      learner.setTransitionFeatures(TransitionFeatures.FIRST_ORDER);
      return learner;
   }

   protected void train() throws Exception {
      Dataset<Sequence> train = loadDataset();
      if (minFeatureCount > 1) {
         train = train.preprocess(PreprocessorList.create(new MinCountFilter(minFeatureCount).asSequenceProcessor()));
      }
      SequenceLabeler labeler = getLearner().train(train);
      POSTagger tagger = new POSTagger(featurizer, labeler);
      tagger.write(model);
   }


   protected void test() throws Exception {
      POSTagger tagger = POSTagger.read(model);
      Dataset<Sequence> test = loadDataset();
      System.out.println("Read: " + test.size());
      PerInstanceEvaluation evaluation = new PerInstanceEvaluation();
      evaluation.evaluate(tagger.labeler, test);
      evaluation.output(System.out, true);
   }

}// END OF POSTrainer
