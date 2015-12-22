package com.davidbracewell.hermes.ml.pos;

import com.davidbracewell.apollo.ml.sequence.LabelingResult;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.SequenceInput;
import com.davidbracewell.apollo.ml.sequence.SequenceLabeler;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.ml.AnnotationTagger;
import com.davidbracewell.hermes.tag.POS;

/**
 * @author David B. Bracewell
 */
public class POSTagger extends AnnotationTagger {
  /**
   * The Featurizer.
   */
  final SequenceFeaturizer<Annotation> featurizer;
  /**
   * The Labeler.
   */
  final SequenceLabeler labeler;

  public POSTagger(SequenceFeaturizer<Annotation> featurizer, SequenceLabeler labeler) {
    this.featurizer = featurizer;
    this.labeler = labeler;
  }

  @Override
  public void tag(Annotation sentence) {
    SequenceInput<Annotation> sequenceInput = new SequenceInput<>(sentence.tokens());
    LabelingResult result = labeler.label(featurizer.extractSequence(sequenceInput.iterator()));
    for (int i = 0; i < sentence.tokenLength(); i++) {
      sentence.tokenAt(i).put(Attrs.PART_OF_SPEECH, POS.fromString(result.getLabel(i)));
    }
  }

}// END OF POSTagger
