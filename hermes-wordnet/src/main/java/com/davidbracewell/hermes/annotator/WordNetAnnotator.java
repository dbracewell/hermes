package com.davidbracewell.hermes.annotator;

import com.davidbracewell.collection.trie.PatriciaTrie;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.morphology.Lemmatizer;
import com.davidbracewell.hermes.morphology.Lemmatizers;
import com.davidbracewell.hermes.tag.POS;
import com.davidbracewell.hermes.wordnet.WordNet;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public class WordNetAnnotator extends SentenceLevelAnnotator {
  private static final long serialVersionUID = 1L;

  private Annotation createAnnotation(Document document, Span span) {
    Annotation annotation = document.createAnnotation(Types.WORD_SENSE, span);
    annotation.put(Attrs.SENSE, WordNet.getInstance().getSenses(annotation.toString(), POS.forText(annotation), document.getLanguage()));
    return annotation;
  }

  private Set<String> getAllLemmas(HString hString, Lemmatizer lemmatizer) {
    Set<String> all = new HashSet<>(lemmatizer.allPossibleLemmas(hString.toString(), POS.ANY));
    all.add(hString.toLowerCase());
    return all;
  }

  @Override
  public void annotate(Annotation sentence) {
    List<Annotation> tokens = sentence.tokens();
    Document document = sentence.document();
    Lemmatizer lemmatizer = Lemmatizers.getLemmatizer(sentence.getLanguage());
    for (int i = 0; i < tokens.size(); ) {
      Annotation token = tokens.get(i);
      final PatriciaTrie<String> lemmas = lemmatizer.allPossibleLemmasAndPrefixes(tokens.get(i).toString(), POS.ANY);

      if (lemmas.size() > 0) {

        HString bestMatch = null;

        if (lemmas.size() == 1 && lemmatizer.canLemmatize(token.toString(), token.getPOS())) {

          bestMatch = token;

        } else if (lemmas.size() > 1) {

          Set<String> working = getAllLemmas(token, lemmatizer).stream().filter(s -> lemmas.containsKey(s) || lemmas.prefixMap(s + " ").size() > 0).collect(Collectors.toSet());

          if (lemmatizer.canLemmatize(token.toString(), token.getPOS())) {
            bestMatch = token;
          }

          int startChar = token.start();
          int end = i + 1;
          while (end < tokens.size()) {
            boolean matched = false;
            token = tokens.get(end);
            Set<String> nextSet = new HashSet<>();
            for (String previous : working) {
              for (String next : getAllLemmas(token, lemmatizer)) {
                String phrase = previous + " " + next;
                if (lemmas.containsKey(phrase)) {
                  nextSet.add(phrase);
                  matched = true;
                } else if (lemmas.prefixMap(phrase).size() > 0) {
                  nextSet.add(phrase);
                }
              }
            }
            working = nextSet;
            HString span = document.substring(startChar, token.end());
            if (matched) {
              bestMatch = span;
            }
            if (nextSet.isEmpty()) {
              break;
            }
            end++;
          }
        }


        if (bestMatch == null) {
          i++;
        } else {
          createAnnotation(document, bestMatch);
          i += bestMatch.tokenLength();
        }


      } else {
        i++;
      }
    }
  }

  @Override
  public Set<Annotatable> satisfies() {
    return Collections.singleton(Types.WORD_SENSE);
  }


  @Override
  protected Set<Annotatable> furtherRequires() {
    return Collections.singleton(Attrs.PART_OF_SPEECH);
  }

}// END OF WordNetAnnotator
