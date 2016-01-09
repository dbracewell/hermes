package com.davidbracewell.hermes.ml.chunker;

import com.davidbracewell.apollo.ml.preprocess.PreprocessorList;
import com.davidbracewell.apollo.ml.preprocess.filter.CountFilter;
import com.davidbracewell.apollo.ml.sequence.Sequence;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.SequenceLabelerLearner;
import com.davidbracewell.apollo.ml.sequence.TransitionFeatures;
import com.davidbracewell.apollo.ml.sequence.linear.StructuredPerceptronLearner;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.ml.BIOTrainer;
import com.davidbracewell.hermes.ml.BIOValidator;

/**
 * The type Phrase chunk trainer.
 *
 * @author David B. Bracewell
 */
public class PhraseChunkTrainer extends BIOTrainer {
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Phrase chunk trainer.
   */
  public PhraseChunkTrainer() {
    super("PhraseChunkTrainer", Types.PHRASE_CHUNK);
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    new PhraseChunkTrainer().run(args);
  }

  @Override
  protected PreprocessorList<Sequence> getPreprocessors() {
    return PreprocessorList.create(new CountFilter(d -> d >= 5).asSequenceProcessor());
  }

  @Override
  protected SequenceFeaturizer<Annotation> getFeaturizer() {
    return new PhraseChunkFeaturizer();
  }

  @Override
  protected SequenceLabelerLearner getLearner() {
    SequenceLabelerLearner learner = new StructuredPerceptronLearner();
    learner.setTransitionFeatures(TransitionFeatures.SECOND_ORDER);
    learner.setValidator(new BIOValidator());
    learner.setParameter("maxIterations", 100);
    learner.setParameter("tolerance", 1E-8);
    learner.setParameter("verbose", true);
    return learner;
  }

}// END OF PhraseChunkTrainer
