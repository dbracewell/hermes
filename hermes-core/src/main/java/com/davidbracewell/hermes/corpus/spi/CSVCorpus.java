package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.hermes.corpus.CorpusFormat;
import org.kohsuke.MetaInfServices;

/**
 * The type Csv corpus.
 */
@MetaInfServices(CorpusFormat.class)
public class CSVCorpus extends DSVCorpus {
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Csv corpus.
   */
  public CSVCorpus() {
    super("CSVCorpus", ',');
  }

  @Override
  public String name() {
    return "CSV";
  }

}
