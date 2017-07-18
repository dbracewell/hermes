package com.davidbracewell.hermes.driver;

import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.io.resource.Resource;

/**
 * @author David B. Bracewell
 */
public class EmbeddingConverter extends CommandLineApplication {

   @Option(description = "The word2vec text source.", required = true)
   private Resource in;
   @Option(description = "Where to save the output.", required = true)
   private Resource out;
   @Option(description = "fast nearest neighbor search", defaultValue = "false")
   private boolean fast;

   public static void main(String[] args) {
      new EmbeddingConverter().run(args);
   }

   @Override
   protected void programLogic() throws Exception {
      Embedding.fromWord2VecTextFile(in, fast).write(out);
   }
}// END OF EmbeddingConverter
