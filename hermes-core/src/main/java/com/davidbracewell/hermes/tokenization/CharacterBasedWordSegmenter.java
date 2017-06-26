package com.davidbracewell.hermes.tokenization;

import com.davidbracewell.apollo.ml.sequence.SequenceInput;
import com.davidbracewell.apollo.ml.sequence.SequenceLabeler;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.io.Resources;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class CharacterBasedWordSegmenter implements Serializable {
   private SequenceLabeler labeler;
   private SerializableFunction<String, TokenType> tokenClassifier;


   public List<Tokenizer.Token> segment(Reader reader) {
      try {
         String content = Resources.fromReader(reader)
                                   .readToString();

         SequenceInput<Character> input = new SequenceInput<>();
         for (char c : content.toCharArray()) {
            input.add(c);
            if (Character.isWhitespace(c)) {

            } else {

            }
         }

      } catch (IOException e) {
         throw Throwables.propagate(e);
      }

      return Collections.emptyList();
   }


   private void train() {

   }

}// END OF CharacterBasedWordSegmenter
