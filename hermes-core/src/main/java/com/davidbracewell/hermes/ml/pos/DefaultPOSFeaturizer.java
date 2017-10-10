package com.davidbracewell.hermes.ml.pos;

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.sequence.Context;
import com.davidbracewell.apollo.ml.sequence.Sequence;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.cache.Cached;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.string.StringPredicates;
import com.davidbracewell.string.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class DefaultPOSFeaturizer implements SequenceFeaturizer<Annotation> {
   private static final long serialVersionUID = 1L;

   private void affixes(String word, int position, int length, List<Feature> features) {
      if (word.length() >= length && !word.equals("!DIGIT") && !word.equals("!YEAR") && !word.equals(
         Sequence.BOS) && !word.equals(Sequence.EOS)) {
         for (int li = 0; li < length; li++) {
            features.add(
               Feature.TRUE(
                  "suffix[" + position + "][" + (li + 1) + "]=" +
                     word.substring(Math.max(word.length() - li - 1, 0))
                           )
                        );
         }
         for (int li = 0; li < length; li++) {
            features.add(
               Feature.TRUE(
                  "prefix[" + position + "][" + (li + 1) + "]=" +
                     word.substring(0, Math.min(li + 1, word.length()))
                           )
                        );
         }
      }
   }

   @Override
   @Cached
   public List<Feature> apply(Context<Annotation> iterator) {
      String word = iterator.getCurrent().toString();
      List<Feature> features = new ArrayList<>();

      String next = null, nextNext = null;
      if (iterator.getContext(1).isPresent()) {
         next = iterator.getContext(1).get().toString();
         if (iterator.getContext(2).isPresent()) {
            nextNext = iterator.getContext(2).get().toString();
         } else {
            nextNext = Sequence.EOS;
         }
      } else {
         next = Sequence.EOS;
      }


      String prev = null, prevPrev = null;
      if (iterator.getContext(-1).isPresent()) {
         prev = iterator.getContext(-1).get().toString();
         if (iterator.getContext(-2).isPresent()) {
            prevPrev = iterator.getContext(-2).get().toString();
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

      affixes(word, 0, 3, features);

      features.add(Feature.TRUE("w[-1]=" + prev));

      if (prevPrev != null) {
         features.add(Feature.TRUE("w[-2]=" + prevPrev));
      }

      features.add(Feature.TRUE("w[+1]=" + next));
      if (nextNext != null) {
         features.add(Feature.TRUE("w[+2]=" + nextNext));
      }

      if (StringUtils.isUpperCase(word)) {
         features.add(Feature.TRUE("ALL_UPPERCASE"));
      } else if (Character.isUpperCase(word.charAt(0))) {
         features.add(Feature.TRUE("STARTS_UPPERCASE"));
      }

      String norm = word.toLowerCase();
      if (StringPredicates.IS_DIGIT.test(word) ||
             norm.equals("one") ||
             norm.equals("two") ||
             norm.equals("three") ||
             norm.equals("four") ||
             norm.equals("five") ||
             norm.equals("six") ||
             norm.equals("seven") ||
             norm.equals("eight") ||
             norm.equals("nine") ||
             norm.equals("ten") ||
             norm.equals("eleven") ||
             norm.equals("twelve") ||
             norm.equals("thirteen") ||
             norm.equals("fourteen") ||
             norm.equals("fifteen") ||
             norm.equals("sixteen") ||
             norm.equals("seventeen") ||
             norm.equals("eighteen") ||
             norm.equals("nineteen") ||
             norm.equals("twenty") ||
             norm.equals("thirty") ||
             norm.equals("forty") ||
             norm.equals("fifty") ||
             norm.equals("sixty") ||
             norm.equals("seventy") ||
             norm.equals("eighty") ||
             norm.equals("ninety") ||
             norm.equals("hundred") ||
             norm.equals("thousand") ||
             norm.equals("million") ||
             norm.equals("billion") ||
             norm.equals("trillion") ||
             StringPredicates.IS_DIGIT.test(word.replaceAll("\\W+", ""))
         ) {
         features.add(Feature.TRUE("IS_DIGIT"));
      }

      if (StringPredicates.IS_PUNCTUATION.test(word)) {
         features.add(Feature.TRUE("IS_PUNCTUATION"));
      }

      if (norm.endsWith("es")) {
         features.add(Feature.TRUE("ENDING_ES"));
      } else if (norm.endsWith("s")) {
         features.add(Feature.TRUE("ENDING_S"));
      } else if (norm.endsWith("ed")) {
         features.add(Feature.TRUE("ENDING_ED"));
      } else if (norm.endsWith("ing")) {
         features.add(Feature.TRUE("ENDING_ING"));
      } else if (norm.endsWith("ly")) {
         features.add(Feature.TRUE("ENDING_LY"));
      }


      return features;
   }

}// END OF POSFeaturizer
