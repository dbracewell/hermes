package com.davidbracewell.hermes.driver;

import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusBuilder;
import com.davidbracewell.io.resource.Resource;

/**
 * @author David B. Bracewell
 */
public class CorpusConverter extends CommandLineApplication {

  @Option(description = "Input corpus location", required = true)
  Resource input;
  @Option(description = "Output corpus location", required = true)
  Resource output;
  @Option(description = "Format of input corpus", required = true)
  String inputFormat;
  @Option(description = "Format of the output corpus", defaultValue = "JSON_OPL")
  String outputFormat;
  @Option(description = "Distributed corpus", defaultValue = "false")
  boolean distributed;

  public CorpusConverter() {
    super("CorpusConvert");
  }

  @Override
  protected void programLogic() throws Exception {
    CorpusBuilder builder = Corpus.builder().from(inputFormat, input, DocumentFactory.getInstance());
    if (distributed) {
      builder = builder.distributed();
    }
    builder.build()
      .write(outputFormat, output);
  }

  public static void main(String[] args) {
    new CorpusConverter().run(args);
  }

}// END OF CorpusConverter
