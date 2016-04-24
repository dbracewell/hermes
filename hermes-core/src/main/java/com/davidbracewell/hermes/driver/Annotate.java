package com.davidbracewell.hermes.driver;

import com.davidbracewell.cli.Option;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.HermesCommandLineApp;
import com.davidbracewell.hermes.Types;

/**
 * @author David B. Bracewell
 */
public class Annotate extends HermesCommandLineApp {
  private static final long serialVersionUID = 1L;

  @Option(description = "Annotations to add", defaultValue = "Annotation.TOKEN,Annotation.SENTENCE,Attribute.PART_OF_SPEECH,Attribute.LEMMA,Relation.DEPENDENCY,Annotation.PHRASE_CHUNK", aliases = "t")
  String[] types;

  Annotate() {
    super("CorpusConvert");
  }

  public static void main(String[] args) {
    new Annotate().run(args);
  }

  private AnnotatableType[] convert() {
    AnnotatableType[] convert = new AnnotatableType[types.length];
    for (int i = 0; i < types.length; i++) {
      convert[i] = Types.from(types[i]);
    }
    return convert;
  }


  @Override
  protected void programLogic() throws Exception {
    writeCorpus(
      getCorpus().annotate(convert())
    );
  }

}// END OF Annotate
