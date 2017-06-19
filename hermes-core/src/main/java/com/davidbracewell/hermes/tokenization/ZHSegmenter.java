package com.davidbracewell.hermes.tokenization;

import com.davidbracewell.config.Config;
import com.davidbracewell.guava.common.base.Throwables;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @author David B. Bracewell
 */
public class ZHSegmenter implements Serializable {
   private static final long serialVersionUID = 1L;
   static ZHSegmenter INSTANCE;
   final HybridSegmenter tokenizer;

   private ZHSegmenter() throws Exception {
      tokenizer = new HybridSegmenter(Config.get(ZHSegmenter.class, "dictionary").asResource(),
                                      Config.get(ZHSegmenter.class, "model").asResource(),
                                      Config.get(ZHSegmenter.class, "maxTokenSize").asIntegerValue()
      );
   }

   public static ZHSegmenter getInstance() {
      if (INSTANCE == null) {
         synchronized (ZHSegmenter.class) {
            if (INSTANCE == null) {
               try {
                  INSTANCE = new ZHSegmenter();
               } catch (Exception e) {
                  throw Throwables.propagate(e);
               }
            }
         }
      }
      return INSTANCE;
   }

   Iterator<Tokenizer.Token> tokenize(String input) {
      return tokenizer.tokenize(input).iterator();
   }
}// END OF ZHSegmenter
