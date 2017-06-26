package com.davidbracewell.hermes.driver;

import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.io.resource.Resource;

/**
 * @author David B. Bracewell
 */
public class EmbeddingConverter extends CommandLineApplication {

   @Option(description = "The word2vec text source.")
   private Resource in;
   @Option(description = "Where to save the output.")
   private Resource out;


   public static void main(String[] args) {
      new EmbeddingConverter().run(args);
   }

   @Override
   protected void programLogic() throws Exception {
      Embedding.fromWord2VecTextFile(in)
               .write(out);
   }
}// END OF EmbeddingConverter
