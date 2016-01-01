package com.davidbracewell.hermes.ml.pos;

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.sequence.ContextualIterator;
import com.davidbracewell.apollo.ml.sequence.Sequence;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.string.StringPredicates;

import java.util.HashSet;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class DefaultPOSFeaturizer implements SequenceFeaturizer<Annotation> {

  private String toWordForm(String word) {
    if (StringPredicates.IS_DIGIT.test(word)) {
      try {
        int d = Integer.valueOf(word);
        if (d >= 1800 && d <= 2100) {
          return "!YEAR";
        }
      } catch (Exception e) {
        return "!DIGIT";
      }
    }
    return word;
  }

  @Override
  public Set<Feature> apply(ContextualIterator<Annotation> iterator) {
    String word = toWordForm(iterator.getCurrent().toString());
    Set<Feature> features = new HashSet<>();

    String next = null, nextNext = null;
    if (iterator.getContext(1).isPresent()) {
      next = toWordForm(iterator.getContext(1).get().toString());
      if (iterator.getContext(2).isPresent()) {
        nextNext = toWordForm(iterator.getContext(2).get().toString());
      } else {
        nextNext = Sequence.EOS;
      }
    } else {
      next = Sequence.EOS;
    }

    String prev = null, prevPrev = null;
    if (iterator.getContext(-11).isPresent()) {
      prev = toWordForm(iterator.getContext(-1).get().toString());
      if (iterator.getContext(-2).isPresent()) {
        prevPrev = toWordForm(iterator.getContext(-2).get().toString());
      } else {
        prevPrev = Sequence.BOS;
      }
    } else {
      prev = Sequence.BOS;
    }


    features.add(Feature.TRUE("w[0]=" + word));

    if (iterator.getIndex() == 0) {
      features.add(Feature.TRUE("__BOS__"));
    } else if (!iterator.hasNext()) {
      features.add(Feature.TRUE("__EOS__"));
    }

    if (!word.equals("!DIGIT") && !word.equals("!YEAR")) {
      for (int li = 0, ll = 3; li < ll; li++) {
        features.add(
          Feature.TRUE(
            "suffix[" + (li + 1) + "]=" +
              word.substring(Math.max(word.length() - li - 1, 0))
          )
        );
      }
      for (int li = 0, ll = 3; li < ll; li++) {
        features.add(
          Feature.TRUE(
            "prefix[" + (li + 1) + "]=" +
              word.substring(0, Math.min(li + 1, word.length()))
          )
        );
      }
    }

    features.add(Feature.TRUE("w[-1]=" + prev));
    if (!prev.equals("!DIGIT") && !prev.equals("!YEAR") && prev.length() > 3) {
      features.add(Feature.TRUE("suffix[-1]", prev.substring(prev.length() - 3)));
    }
    if (prevPrev != null) {
      features.add(Feature.TRUE("w[-2]=" + prevPrev));
    }

    features.add(Feature.TRUE("w[+1]=" + next));
    if (!next.equals("!DIGIT") && !next.equals("!YEAR") && next.length() > 3) {
      features.add(Feature.TRUE("suffix[+1]", next.substring(next.length() - 3)));
    }
    if (nextNext != null) {
      features.add(Feature.TRUE("w[+2]=" + nextNext));
    }


    if (word.endsWith("es")) {
      features.add(Feature.TRUE("ENDING_ES"));
    } else if (word.endsWith("s")) {
      features.add(Feature.TRUE("ENDING_S"));
    }


    return features;
  }

}// END OF POSFeaturizer
