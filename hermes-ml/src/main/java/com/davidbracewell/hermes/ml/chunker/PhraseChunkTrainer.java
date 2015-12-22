package com.davidbracewell.hermes.ml.chunker;

import com.davidbracewell.apollo.ml.sequence.decoder.BeamDecoder;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.SequenceLabelerLearner;
import com.davidbracewell.apollo.ml.sequence.TransitionFeatures;
import com.davidbracewell.apollo.ml.sequence.linear.StructuredPerceptronLearner;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.ml.BIOTrainer;

/**
 * @author David B. Bracewell
 */
public class PhraseChunkTrainer extends BIOTrainer {
  private static final long serialVersionUID = 1L;

  public PhraseChunkTrainer() {
    super("PhraseChunkTrainer", Types.PHRASE_CHUNK);
  }

  public static void main(String[] args) {
    args = new String[]{
      "--corpus", "/home/david/train.txt",
      "--format", "CONLL",
      "--model", "/home/david/pc.bin",
//      "--minFeatureCount", "5",
      "--mode", "TEST"
    };
    new PhraseChunkTrainer().run(args);
  }

  @Override
  protected SequenceFeaturizer<Annotation> getFeaturizer() {
    return new PhraseChunkFeaturizer();
  }

  @Override
  protected SequenceLabelerLearner getLearner() {
    StructuredPerceptronLearner learner = new StructuredPerceptronLearner();
    learner.setDecoder(new BeamDecoder(5));
    learner.setMaxIterations(100);
    learner.setTransitionFeatures(TransitionFeatures.SECOND_ORDER);
//    CRFTrainer trainer = new CRFTrainer();
//    trainer.setSolver(Solver.LBFGS);
//    trainer.setMaxIterations(100);
    return learner;
  }

}// END OF PhraseChunkTrainer
