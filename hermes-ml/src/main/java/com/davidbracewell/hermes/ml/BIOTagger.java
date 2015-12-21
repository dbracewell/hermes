package com.davidbracewell.hermes.ml;

import com.davidbracewell.apollo.ml.sequence.LabelingResult;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.SequenceInput;
import com.davidbracewell.apollo.ml.sequence.SequenceLabeler;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.annotator.SentenceLevelAnnotator;

/**
 * @author David B. Bracewell
 */
public abstract class BIOTagger extends SentenceLevelAnnotator {
  private static final long serialVersionUID = 1L;
  private final SequenceFeaturizer<Annotation> featurizer;
  private final Attribute tagAttribute;
  private final AnnotationType annotationType;

  public BIOTagger(SequenceFeaturizer<Annotation> featurizer, Attribute tagAttribute, AnnotationType annotationType) {
    this.featurizer = featurizer;
    this.tagAttribute = tagAttribute;
    this.annotationType = annotationType;
  }

  protected abstract SequenceLabeler getLabeler();

  @Override
  public void annotate(Annotation sentence) {
    SequenceInput<Annotation> sequenceInput = new SequenceInput<>(sentence.tokens());
    LabelingResult result = getLabeler().label(featurizer.extractSequence(sequenceInput.iterator()));
    for (int i = 0; i < sentence.tokenLength(); ) {
      if (result.getLabel(i).equals("O")) {
        i++;
      } else {
        int start = sentence.tokenAt(i).start();
        String type = result.getLabel(i).substring(2);
        i++;
        while (i < sentence.tokenLength() && !result.getLabel(i).equals("O") && !result.getLabel(i).equals("B")) {
          i++;
        }
        int end = (i < sentence.tokenLength()) ? sentence.tokenAt(i - 1).end() : sentence.end();
        sentence.document()
          .createAnnotation(annotationType, start, end)
          .put(tagAttribute, tagAttribute.getValueType().convert(type));
      }
    }
  }

}// END OF BIOTagger
