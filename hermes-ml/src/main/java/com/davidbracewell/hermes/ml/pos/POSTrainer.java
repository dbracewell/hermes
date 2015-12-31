package com.davidbracewell.hermes.ml.pos;

import com.davidbracewell.apollo.ml.Dataset;
import com.davidbracewell.apollo.ml.classification.linear.AveragedPerceptronLearner;
import com.davidbracewell.apollo.ml.preprocess.PreprocessorList;
import com.davidbracewell.apollo.ml.preprocess.filter.CountFilter;
import com.davidbracewell.apollo.ml.sequence.PerInstanceEvaluation;
import com.davidbracewell.apollo.ml.sequence.Sequence;
import com.davidbracewell.apollo.ml.sequence.SequenceInput;
import com.davidbracewell.apollo.ml.sequence.SequenceLabeler;
import com.davidbracewell.apollo.ml.sequence.SequenceLabelerLearner;
import com.davidbracewell.apollo.ml.sequence.WindowedLearner;
import com.davidbracewell.apollo.ml.sequence.linear.CRFTrainer;
import com.davidbracewell.apollo.ml.sequence.linear.MEMMLearner;
import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.ml.Mode;
import com.davidbracewell.io.resource.Resource;

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
    } else {
      test();
    }
  }

  protected void train() throws Exception {
    DefaultPOSFeaturizer featurizer = new DefaultPOSFeaturizer();
    Dataset<Sequence> train = Dataset.sequence()
      .source(
        Corpus
          .builder()
          .source(corpus)
          .format(corpusFormat)
          .build()
          .stream()
          .flatMap(Document::sentences)
          .map(sentence -> {
            SequenceInput<Annotation> input = new SequenceInput<>();
            for (int i = 0; i < sentence.tokenLength(); i++) {
              input.add(sentence.tokenAt(i), sentence.tokenAt(i).getPOS().asString());
            }
            return featurizer.extractSequence(input.iterator());
          })
      ).build();

    if (minFeatureCount > 1) {
      train.preprocess(PreprocessorList.create(new CountFilter(d -> d >= minFeatureCount).asSequenceProcessor()));
    }

    SequenceLabelerLearner learner = new MEMMLearner();
      //new WindowedLearner(new AveragedPerceptronLearner().oneVsRest());
      //new CRFTrainer();
    learner.setParameter("maxIterations", 100);
    learner.setParameter("verbose", true);
    SequenceLabeler labeler = learner.train(train);
    POSTagger tagger = new POSTagger(featurizer, labeler);
    tagger.write(model);
  }


  protected void test() throws Exception {
    POSTagger tagger = POSTagger.read(model);
    Dataset<Sequence> test = Dataset.sequence()
      .source(
        Corpus
          .builder()
          .source(corpus)
          .format(corpusFormat)
          .build()
          .stream()
          .flatMap(Document::sentences)
          .map(sentence -> {
            SequenceInput<Annotation> input = new SequenceInput<>();
            for (int i = 0; i < sentence.tokenLength(); i++) {
              input.add(sentence.tokenAt(i), sentence.tokenAt(i).getPOS().asString());
            }
            return tagger.featurizer.extractSequence(input.iterator());
          })
      ).build();
    PerInstanceEvaluation evaluation = new PerInstanceEvaluation();
    evaluation.evaluate(tagger.labeler, test);
    evaluation.output(System.out, true);
  }

}// END OF POSTrainer
