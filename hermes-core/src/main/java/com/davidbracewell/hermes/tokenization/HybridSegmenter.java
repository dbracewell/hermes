package com.davidbracewell.hermes.tokenization;

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.Model;
import com.davidbracewell.apollo.ml.sequence.Labeling;
import com.davidbracewell.apollo.ml.sequence.Sequence;
import com.davidbracewell.apollo.ml.sequence.SequenceLabeler;
import com.davidbracewell.apollo.ml.sequence.WindowDecoder;
import com.davidbracewell.guava.common.primitives.Chars;
import com.davidbracewell.hermes.lexicon.SimpleWordList;
import com.davidbracewell.hermes.lexicon.WordList;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Loggable;
import com.davidbracewell.string.StringUtils;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class HybridSegmenter implements Loggable, Serializable {
   private static final long serialVersionUID = 1L;

   private final WordList dictionary;
   private final SequenceLabeler model;
   private final int maxSpanSize;

   /**
    * Instantiates a new Hybrid segmenter.
    *
    * @param dictionaryFile the dictionary file
    * @param model          the model
    * @param maxSpanSize    the max span size
    * @throws Exception the exception
    */
   public HybridSegmenter(Resource dictionaryFile, Resource model, int maxSpanSize) throws Exception {
      dictionary = SimpleWordList.read(dictionaryFile, true);
      this.model = Model.read(model);
      this.model.setDecoder(new WindowDecoder());
      this.maxSpanSize = maxSpanSize;
   }

   private double multi(double d1, double d2) {
      double m = d1 * d2;
      if (m < d1) {
         return Double.MAX_VALUE;
      }
      return m;
   }

   private double scoreWord(int start, int end, String word, Labeling labels) {
      double spanLength = word.length();
      if (dictionary.contains(word.toLowerCase())) {
         return spanLength;
      }
      if (start > 0 && Feature.isFalse(labels.getLabel(start - 1))) {
         return 1d;
      }
      for (int i = start; i < end - 1; i++) {
         if (Feature.isTrue(labels.getLabel(i))) {
            return 1d;
         }
      }
      return Feature.isTrue(labels.getLabel(end - 1)) ? spanLength : 1d;
   }

   /**
    * Tokenize list.
    *
    * @param sentence the sentence
    * @return the list
    */
   public List<Tokenizer.Token> tokenize(String sentence) {
      Sequence seq = Sequence.create(Chars.asList(sentence.toCharArray())
                                          .stream()
                                          .map(Object::toString));
      Labeling labels = model.label(seq);

      int n = sentence.length();
      int[][] spans = new int[n + 1][2];
      double[] best = new double[n + 1];
      best[0] = 1d;

      for (int i = 1; i <= n; i++) {
         for (int j = Math.max(0, i - maxSpanSize); j < i; j++) {
            int w = i - j;
            String span = sentence.substring(j, j + w);
            double score = scoreWord(j, j + w, span, labels);
            double segmentScore = multi(best[i - w], score);
            logFiner("'{0}' > '{1}' : {2} : {3}",
                     sentence.substring(spans[i - w][0], spans[i - w][1]),
                     span,
                     segmentScore,
                     best[i]);
            if (segmentScore >= best[i]) {
               best[i] = segmentScore;
               spans[i][0] = j;
               spans[i][1] = j + w;
            }
         }
      }
      int i = n;
      LinkedList<Tokenizer.Token> tokens = new LinkedList<>();
      while (i > 0) {
         String token = StringUtils.trim(sentence.substring(spans[i][0], spans[i][1]));
         if (!StringUtils.isNullOrBlank(token)) {
            tokens.addFirst(
               new Tokenizer.Token(token, TokenType.UNKNOWN, spans[i][0], spans[i][0] + token.length(), 0));
         }
         i = i - (spans[i][1] - spans[i][0]);
      }
      i = 0;
      for (Tokenizer.Token token : tokens) {
         token.index = i;
         i++;
      }

      return tokens;
   }


}//END OF HybridSegmenter
