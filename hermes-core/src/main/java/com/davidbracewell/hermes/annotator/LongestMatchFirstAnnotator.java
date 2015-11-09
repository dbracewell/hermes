package com.davidbracewell.hermes.annotator;

import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.HString;

import java.util.List;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public abstract class LongestMatchFirstAnnotator extends SentenceLevelAnnotator {

  final int maxSpanLength;

  public LongestMatchFirstAnnotator(int maxSpanLength) {
    this.maxSpanLength = maxSpanLength;
  }

  @Override
  public void annotate(Annotation sentence) {
    List<Annotation> tokens = sentence.tokens();
    int n = tokens.size();
    for (int i = 0; i < n; ) {
      int maxJ = 0;
      HString maxString = null;
      for (int j = Math.min(n, i + maxSpanLength); j > i; j--) {
        HString string = HString.union(tokens.subList(i, j));
        if (matches(string)) {
          maxJ = j;
          maxString = string;
          break;
        }
      }

      if (maxJ > 0) {
        i = maxJ;
        createAndAttachAnnotation(sentence.document(), maxString);
      } else {
        i++;
      }

    }
  }

  protected abstract boolean matches(HString string);

  /**
   * Given an possible span determines if an annotation should be created and if so creates and attaches it.
   *
   * @param document the document
   * @param span     The span to check
   */
  protected abstract void createAndAttachAnnotation(Document document, HString span);

  @Override
  public Set<AnnotationType> satisfies() {
    return null;
  }

}// END OF LongestMatchFirstAnnotator
