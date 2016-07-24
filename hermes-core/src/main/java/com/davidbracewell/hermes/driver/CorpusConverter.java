package com.davidbracewell.hermes.driver;

import com.davidbracewell.application.Application;
import com.davidbracewell.hermes.HermesCommandLineApp;

/**
 * @author David B. Bracewell
 */
@Application.Description("Converts a corpus stored in one format to another format.")
public class CorpusConverter extends HermesCommandLineApp {
  private static final long serialVersionUID = 1L;

  private CorpusConverter() {
    super("CorpusConvert");
  }

  @Override
  protected void programLogic() throws Exception {
    writeCorpus(getCorpus());
  }

  public static void main(String[] args) {
    new CorpusConverter().run(args);
  }

}// END OF CorpusConverter
