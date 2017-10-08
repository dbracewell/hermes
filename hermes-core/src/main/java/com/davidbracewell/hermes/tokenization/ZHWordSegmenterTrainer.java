package com.davidbracewell.hermes.tokenization;

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.featurizer.PredicateFeaturizer;
import com.davidbracewell.apollo.ml.sequence.*;
import com.davidbracewell.apollo.ml.sequence.feature.NGramSequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.feature.WindowedSequenceFeaturizer;
import com.davidbracewell.io.Resources;
import com.davidbracewell.string.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class ZHWordSegmenterTrainer extends WordSegmenterTrainer {
   public static void main(String[] args) throws Exception {
      CharacterBasedWordSegmenter wordSegmenter = CharacterBasedWordSegmenter.read(Resources.from("tmp.bin"));
      wordSegmenter.tokenize("我爱你").forEach(System.out::println);
//      new ZHWordSegmenterTrainer().run(Hermes.initializeApplication(args));
   }

   @Override
   protected SequenceFeaturizer<Character> getFeaturizer() {
      return SequenceFeaturizer.chain(
         new WindowedSequenceFeaturizer<>(3, 3, true, new PunctFeature()),
         new PrefixFeature(),
         new NGramSequenceFeaturizer<>(3, 3, new CharFeature()),
         new WindowedSequenceFeaturizer<>(3, 3, new CharFeature()));
   }

   @Override
   protected SequenceLabelerLearner getLearner() {
      MEMMLearner learner = new MEMMLearner();
      learner.setTransitionFeatures(TransitionFeature.SECOND_ORDER);
      return learner;
   }

   public static class CharFeature extends PredicateFeaturizer<Character> {
      private static final long serialVersionUID = 1L;

      public CharFeature() {
         super("Char");
      }

      @Override
      public String extractPredicate(Character character) {
         return character.toString();
      }
   }

   public static class PrefixFeature implements SequenceFeaturizer<Character> {
      private static final long serialVersionUID = 1L;

      @Override
      public List<Feature> apply(Context<Character> characterContext) {
         int index = characterContext.getIndex();

         List<Feature> features = new ArrayList<>();
         StringBuilder prefix = new StringBuilder(characterContext.getCurrent());
         for (int i = 1; index - i >= 0 && characterContext.getPreviousLabel(i)
                                                           .map(f -> f.equals("true"))
                                                           .orElse(false);
              i++
            ) {
            prefix.append(characterContext.getPrevious(i));
            features.add(Feature.TRUE("PREFIX=" + prefix));
         }
         return features;
      }
   }

   public static class PunctFeature extends PredicateFeaturizer<Character> {
      private static final long serialVersionUID = 1L;

      /**
       * Instantiates a new Predicate featurizer.
       */
      protected PunctFeature() {
         super("IsPunct");
      }

      @Override
      public String extractPredicate(Character character) {
         if (StringUtils.isPunctuation(character)) {
            return "true";
         }
         return "false";
      }
   }

}//END OF ZHWordSegmenterTrainer
