package com.davidbracewell.hermes.ml.pos;

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.sequence.ContextualIterator;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.hermes.Annotation;

import java.util.HashSet;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class POSFeaturizer implements SequenceFeaturizer<Annotation> {

  private String toWordForm(String word) {
//    if (StringPredicates.IS_DIGIT.test(word)) {
//      return "!DIGIT";
//    }
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
        nextNext = "**END**";
      }
    } else {
      next = "**END**";
    }

    String prev = null, prevPrev = null;
    if (iterator.getContext(-11).isPresent()) {
      prev = toWordForm(iterator.getContext(-1).get().toString());
      if (iterator.getContext(-2).isPresent()) {
        prevPrev = toWordForm(iterator.getContext(-2).get().toString());
      } else {
        prevPrev = "**END**";
      }
    } else {
      prev = "**END**";
    }


    features.add(Feature.TRUE("w[0]=" + word));
    features.add(Feature.TRUE("default"));

    if (iterator.getIndex() == 0) {
      features.add(Feature.TRUE("__BOS__"));
    } else if (!iterator.hasNext()) {
      features.add(Feature.TRUE("__EOS__"));
    }

    if (!word.equals("!DIGIT")) {
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

    if (prev != null) {
      features.add(Feature.TRUE("w[-1]=" + prev));
      if (prevPrev != null) {
        features.add(Feature.TRUE("w[-2]=" + prevPrev));
      }
    }

    if (next != null) {
      features.add(Feature.TRUE("w[+1]=" + next));
      if (nextNext != null) {
        features.add(Feature.TRUE("w[+2]=" + nextNext));
      }
    }


    return features;
  }

}// END OF POSFeaturizer
