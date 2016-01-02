package com.davidbracewell.hermes.ml.pos;

import com.davidbracewell.apollo.ml.Dataset;
import com.davidbracewell.apollo.ml.classification.linear.AveragedPerceptronLearner;
import com.davidbracewell.apollo.ml.preprocess.PreprocessorList;
import com.davidbracewell.apollo.ml.preprocess.filter.CountFilter;
import com.davidbracewell.apollo.ml.sequence.*;
import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.ml.Mode;
import com.davidbracewell.hermes.tag.POS;
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
    } else {
      test();
    }
  }

  protected Dataset<Sequence> loadDataset() throws Exception {
    return Dataset.sequence()
      .source(
        Corpus
          .builder()
          .source(corpus)
          .format(corpusFormat)
          .build()
          .stream()
          .filter(d -> d.getAnnotationSet().isCompleted(Types.PART_OF_SPEECH))
          .flatMap(Document::sentences)
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
      ).build();
  }

  protected void train() throws Exception {
    Dataset<Sequence> train = loadDataset();
    if (minFeatureCount > 1) {
      train = train.preprocess(PreprocessorList.create(new CountFilter(d -> d >= minFeatureCount).asSequenceProcessor()));
    }
    SequenceLabelerLearner learner =
      new WindowedLearner(new AveragedPerceptronLearner().oneVsRest());
//      new CRFTrainer();
    learner.setValidator(new POSValidator());
    learner.setParameter("maxIterations", 100);
    learner.setParameter("verbose", true);
    SequenceLabeler labeler = learner.train(train);
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
