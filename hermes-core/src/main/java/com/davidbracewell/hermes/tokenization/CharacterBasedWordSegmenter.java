package com.davidbracewell.hermes.tokenization;

import com.davidbracewell.apollo.ml.sequence.*;
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class CharacterBasedWordSegmenter implements Serializable, Tokenizer {
   private static final long serialVersionUID = 1L;
   private final SequenceLabeler labeler;
   private final SerializableFunction<String, TokenType> tokenClassifier;
   private final SequenceFeaturizer<Character> featurizer;

   public CharacterBasedWordSegmenter(SequenceLabeler labeler, SerializableFunction<String, TokenType> tokenClassifier, SequenceFeaturizer<Character> featurizer) {
      this.labeler = labeler;
      this.tokenClassifier = tokenClassifier;
      this.featurizer = featurizer;
   }

   private Tokenizer.Token createToken(String content, int start, int end, int index) {
      String token = content.substring(start, end);
      return new Tokenizer.Token(token,
                                 tokenClassifier.apply(token),
                                 start,
                                 end,
                                 index
      );
   }

   @Override
   public Iterable<Token> tokenize(String content) {
      Sequence input = featurizer.extractSequence(
         new SequenceInput<>(Convert.convert(content.toCharArray(), List.class, Character.class)).iterator());
      List<Tokenizer.Token> tokens = new ArrayList<>();
      Labeling labeling = labeler.label(input);
      int lastStart = 0;
      int lastEnd = 1;
      for (int i = 0; i < labeling.size(); i++) {
         if (labeling.getLabel(i).equals("true")) {
            lastEnd = i + 1;
            tokens.add(createToken(content, lastStart, lastEnd, tokens.size()));
            lastStart = i + 1;
            lastEnd = -1;
         } else {
            lastEnd = i + 1;
         }
      }

      if (lastEnd != -1) {
         tokens.add(createToken(content, lastStart, lastEnd, tokens.size()));
      }

      return tokens;
   }

   @Override
   public Iterable<Token> tokenize(Reader reader) {
      try {
         String content = Resources.fromReader(reader).readToString();
         return tokenize(content);
      } catch (Exception e) {
         throw Throwables.propagate(e);
      }
   }

   /**
    * Reads the model from the given resource
    *
    * @param modelResource the resource containing the serialized model
    * @return the deserialized model
    * @throws Exception Something went wrong reading the model
    */
   public static CharacterBasedWordSegmenter read(@NonNull Resource modelResource) throws Exception {
      return modelResource.readObject();
   }


   /**
    * Serializes the model to the given resource.
    *
    * @param modelResource the resource to serialize the model to
    * @throws Exception Something went wrong serializing the model
    */
   public void write(@NonNull Resource modelResource) throws Exception {
      modelResource.setIsCompressed(true).writeObject(this);
   }

}// END OF CharacterBasedWordSegmenter
