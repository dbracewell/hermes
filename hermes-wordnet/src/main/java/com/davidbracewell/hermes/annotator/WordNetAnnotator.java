package com.davidbracewell.hermes.annotator;

import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.morphology.Lemmatizer;
import com.davidbracewell.hermes.morphology.Lemmatizers;
import com.davidbracewell.hermes.tag.POS;
import com.davidbracewell.hermes.wordnet.Sense;
import com.davidbracewell.hermes.wordnet.WordNet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class WordNetAnnotator extends SentenceLevelAnnotator {
  private static final long serialVersionUID = 1L;

  @Override
  public void annotate(Annotation sentence) {
    List<Annotation> tokens = sentence.tokens();
    WordNet wn = WordNet.getInstance();
    Lemmatizer lemmatizer = Lemmatizers.getLemmatizer(sentence.getLanguage());
    for (int i = 0; i < tokens.size(); ) {
      Annotation bestMatch = null;
      if (lemmatizer.prefixMatch(tokens.get(i).toString())) {
        for (int j = Math.min(tokens.size(), i + 7); j > i; j--) {
          HString temp = HString.union(tokens.subList(i, j));
          POS pos = POS.forText(temp);
          if (pos != null && pos.isInstance(POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB)) {
            List<Sense> entries = wn.getSenses(temp.toString(), pos, sentence.getLanguage());
            if (entries.size() > 0) {
              bestMatch = sentence.document().createAnnotation(Types.WORD_SENSE, temp);
              bestMatch.put(Attrs.SENSE, entries);
              break;
            }
          }
        }
      }
      if (bestMatch != null) {
        i += bestMatch.tokenLength();
      } else {
        i++;
      }
    }
  }

  @Override
  public Set<AnnotationType> satisfies() {
    return Collections.singleton(Types.WORD_SENSE);
  }


  @Override
  protected Set<AnnotationType> furtherRequires() {
    return Collections.singleton(Types.PART_OF_SPEECH);
  }

}// END OF WordNetAnnotator
