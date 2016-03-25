package com.davidbracewell.hermes.driver;

import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusBuilder;
import com.davidbracewell.hermes.corpus.CorpusFormats;
import com.davidbracewell.io.resource.Resource;

/**
 * @author David B. Bracewell
 */
public class Annotate extends CommandLineApplication {
  private static final long serialVersionUID = 1L;

  @Option(description = "Input corpus location", required = true)
  Resource input;
  @Option(description = "Output corpus location", required = true)
  Resource output;
  @Option(description = "Format of input corpus", defaultValue = "JSON_OPL")
  String inputFormat;
  @Option(description = "Distributed corpus", defaultValue = "false")
  boolean distributed;
  @Option(description = "Annotations to add", defaultValue = "TOKEN,SENTENCE,PART_OF_SPEECH,LEMMA,DEPENDENCY,PHRASE_CHUNK")
  AnnotationType[] annotations;

  public Annotate() {
    super("CorpusConvert");
  }

  public static void main(String[] args) {
    new Annotate().run(args);
  }

  @Override
  protected void programLogic() throws Exception {
    CorpusBuilder builder = Corpus.builder().from(inputFormat, input, DocumentFactory.getInstance());
    if (distributed) {
      builder = builder.distributed();
    }
    builder.build()
      .annotate(annotations)
      .write(CorpusFormats.JSON_OPL, output);
  }

}// END OF Annotate
