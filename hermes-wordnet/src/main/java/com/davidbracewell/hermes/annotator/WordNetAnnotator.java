package com.davidbracewell.hermes.annotator;

import com.davidbracewell.hermes.*;
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

  private Annotation createAnnotation(Document document, Span span) {
    Annotation annotation = document.createAnnotation(Types.WORD_SENSE, span);
    annotation.put(Attrs.SENSE, WordNet.getInstance().getSenses(annotation.toString(), POS.forText(annotation), document.getLanguage()));
    return annotation;
  }

  @Override
  public void annotate(Annotation sentence) {
    List<Annotation> tokens = sentence.tokens();
    Document document = sentence.document();
    Lemmatizer lemmatizer = Lemmatizers.getLemmatizer(sentence.getLanguage());
    for (int i = 0; i < tokens.size(); ) {
      Annotation token = tokens.get(i);
      Set<String> lemmas = lemmatizer.allPossibleLemmasAndPrefixes(tokens.get(i).toString(), POS.ANY);
      if (lemmas.size() > 0) {
        HString bestMatch = null;
        if (lemmas.size() == 1 && lemmatizer.canLemmatize(token.toString(), token.getPOS())) {

          bestMatch = token;

        } else if (lemmas.size() > 1) {

          if (lemmatizer.canLemmatize(token.toString(), token.getPOS())) {
            bestMatch = token;
          }

          int start = token.start();
          int end = i + 1;
          StringBuilder builder = new StringBuilder(token);
          while (end < tokens.size()) {
            token = tokens.get(end);
            if (sentence.getLanguage().usesWhitespace()) {
              builder.append(" ");
            }
            builder.append(token);
            lemmas = lemmatizer.allPossibleLemmasAndPrefixes(builder.toString(), POS.ANY);
            HString span = HString.union(tokens.subList(i, end + 1));
            if (lemmatizer.canLemmatize(span.toString(), POS.forText(span)) && lemmas.contains(lemmatizer.lemmatize(span.toString(), POS.forText(span)))) {
              bestMatch = span;
            }
            if (lemmas.isEmpty()) {
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
  public Set<AnnotationType> satisfies() {
    return Collections.singleton(Types.WORD_SENSE);
  }


  @Override
  protected Set<AnnotationType> furtherRequires() {
    return Collections.singleton(Types.PART_OF_SPEECH);
  }

}// END OF WordNetAnnotator
