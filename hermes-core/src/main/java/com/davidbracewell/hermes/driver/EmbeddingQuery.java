package com.davidbracewell.hermes.driver;

import com.davidbracewell.apollo.ml.Model;
import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.cli.Option;
import com.davidbracewell.hermes.HermesCommandLineApp;
import com.davidbracewell.io.resource.Resource;

import java.util.Scanner;

/**
 * @author David B. Bracewell
 */
public class EmbeddingQuery extends HermesCommandLineApp {

   @Option(description = "The embedding model to query.", required = true)
   private Resource model;

   @Override
   protected void programLogic() throws Exception {
      Embedding embedding = Model.read(model);

      String line;
      Scanner scanner = new Scanner(System.in);
      do {
         System.out.print("query:> ");
         line = scanner.nextLine();
         switch (line) {
            case "?q":
               System.exit(0);
            case "?help":
               System.out.println("Help");
               break;
            default:
               if (embedding.contains(line)) {
                  embedding.nearest(line.toLowerCase(), 10).forEach(
                     slv -> System.out.println("  " + slv.getLabel() + " : " + slv.getScore()));
                  System.out.println();
               } else {
                  System.out.println("!! " + line + " is not in the dictionary");
               }
         }

      } while (!line.equals("q!"));

   }

   public static void main(String[] args) throws Exception {
      String[] nargs = new String[args.length + 2];
      System.arraycopy(args, 0, nargs, 0, args.length);
      nargs[nargs.length - 2] = "--input";
      nargs[nargs.length - 1] = "/dev/null";
      new EmbeddingQuery().run(nargs);
   }

}//END OF EmbeddingQuery
