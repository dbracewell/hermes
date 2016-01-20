package com.davidbracewell.hermes.annotator;

import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.morphology.Lemmatizer;
import com.davidbracewell.hermes.morphology.Lemmatizers;
import com.davidbracewell.hermes.tag.POS;
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
      Set<String> lemmas = lemmatizer.allPossibleLemmasAndPrefixes(tokens.get(i).toString(), POS.ANY);
      if (lemmas.size() > 0) {
        int lastIn = lemmatizer.contains(tokens.get(i).toString(), tokens.get(i).getPOS()) ? i + 1 : -1;
        for (int j = i + 2; j < tokens.size(); j++) {
          HString temp = HString.union(tokens.subList(i, j));
          lemmas = lemmatizer.allPossibleLemmasAndPrefixes(temp.toString(), POS.ANY);
          System.out.println(temp + " > " + lemmas);
          if (lemmatizer.contains(temp.toString())) {
            lastIn = j;
          }
          if (lemmas.size() == 0) {
            break;
          }
        }
        if (lastIn != -1) {
          HString candidate = HString.union(tokens.subList(i, lastIn));
          System.out.println(candidate);
          i = lastIn;
        } else {
          i++;
        }
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
