package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.hermes.corpus.DocumentFormat;
import org.kohsuke.MetaInfServices;

/**
 * The type Tsv corpus.
 */
@MetaInfServices(DocumentFormat.class)
public class TSVCorpus extends DSVCorpus {
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Tsv corpus.
   */
  public TSVCorpus() {
    super("TSVCorpus", '\t');
  }

  @Override
  public String name() {
    return "TSV";
  }

}//END OF TSVCorpus