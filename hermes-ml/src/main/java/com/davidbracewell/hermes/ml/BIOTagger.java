package com.davidbracewell.hermes.ml;

import com.davidbracewell.apollo.ml.sequence.LabelingResult;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.SequenceInput;
import com.davidbracewell.apollo.ml.sequence.SequenceLabeler;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.io.Serializable;

/**
 * The type Bio tagger.
 *
 * @author David B. Bracewell
 */
public class BIOTagger implements Serializable {
  private static final long serialVersionUID = 1L;
  /**
   * The Featurizer.
   */
  final SequenceFeaturizer<Annotation> featurizer;
  /**
   * The Annotation type.
   */
  final AnnotationType annotationType;
  /**
   * The Labeler.
   */
  final SequenceLabeler labeler;

  /**
   * Instantiates a new Bio tagger.
   *
   * @param featurizer     the featurizer
   * @param annotationType the annotation type
   * @param labeler        the labeler
   */
  public BIOTagger(SequenceFeaturizer<Annotation> featurizer, AnnotationType annotationType, SequenceLabeler labeler) {
    this.featurizer = featurizer;
    this.annotationType = annotationType;
    this.labeler = labeler;
  }

  /**
   * Read t.
   *
   * @param resource the resource
   * @return the t
   * @throws Exception the exception
   */
  public static BIOTagger read(@NonNull Resource resource) throws Exception {
    return resource.readObject();
  }

  /**
   * Tag labeling result.
   *
   * @param sentence the sentence
   * @return the labeling result
   */
  public LabelingResult tag(Annotation sentence) {
    SequenceInput<Annotation> sequenceInput = new SequenceInput<>(sentence.tokens());
    LabelingResult result = labeler.label(featurizer.extractSequence(sequenceInput.iterator()));
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
          .put(annotationType.getTagAttribute(), annotationType.getTagAttribute().getValueType().convert(type));
      }
    }
    return result;
  }

  /**
   * Write.
   *
   * @param resource the resource
   * @throws Exception the exception
   */
  public void write(@NonNull Resource resource) throws Exception {
    resource.setIsCompressed(true).writeObject(this);
  }

}// END OF BIOTagger
