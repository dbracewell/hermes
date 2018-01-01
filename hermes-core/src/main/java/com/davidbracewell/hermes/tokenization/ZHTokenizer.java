package com.davidbracewell.hermes.tokenization;

import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.io.Resources;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

import static com.davidbracewell.collection.Collect.asIterable;

/**
 * @author David B. Bracewell
 */
public class ZHTokenizer implements Tokenizer, Serializable {
   private static final long serialVersionUID = 1L;

   @Override
   public Iterable<Token> tokenize(Reader reader) {
      try {
         return asIterable(ZHSegmenter.getInstance()
                                      .tokenize(Resources.fromReader(reader)
                                                         .readToString()));
      } catch (IOException e) {
         throw Throwables.propagate(e);
      }
   }

}// END OF ZHTokenizer
