package com.davidbracewell.hermes.driver;

import com.davidbracewell.apollo.linalg.DenseVector;
import com.davidbracewell.apollo.linalg.Vector;
import com.davidbracewell.apollo.linalg.store.CosineSignature;
import com.davidbracewell.apollo.linalg.store.InMemoryLSH;
import com.davidbracewell.apollo.linalg.store.VectorStore;
import com.davidbracewell.apollo.ml.EncoderPair;
import com.davidbracewell.apollo.ml.NoOptEncoder;
import com.davidbracewell.apollo.ml.NoOptLabelEncoder;
import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.io.resource.Resource;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

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
      List<String> lines = in.readLines();
      int dimension = Integer.parseInt(lines.get(0).split("\\s+")[1]);
      VectorStore<String> vectorStore = InMemoryLSH.builder()
                                                   .dimension(dimension)
                                                   .signatureSupplier(CosineSignature::new)
                                                   .createVectorStore();


      NumberFormat nf = new DecimalFormat("##.0%");
      for (int i = 1; i < lines.size(); i++) {
         Vector v = new DenseVector(dimension);
         String[] parts = lines.get(i).trim().split("\\s+");
         for (int vi = 1; vi < parts.length; vi++) {
            v.set(vi - 1, Double.parseDouble(parts[vi]));
         }
         vectorStore.add(parts[0].replace('_', ' '), v);
         if (i % 100 == 0) {
            System.err.print(
               "Processed " + i + " / " + lines.size() + " (" + nf.format((double) i / lines.size()) + ")\r");
         }
      }
      System.err.println(
         "Processed " + lines.size() + " / " + lines.size() + " (100.0%)");

      Embedding embedding = new Embedding(new EncoderPair(new NoOptLabelEncoder(), new NoOptEncoder()),
                                          vectorStore);
      embedding.write(out);
   }
}// END OF EmbeddingConverter
