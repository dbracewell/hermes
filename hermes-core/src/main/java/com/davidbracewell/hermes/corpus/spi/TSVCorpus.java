package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.hermes.corpus.CorpusFormat;
import org.kohsuke.MetaInfServices;

/**
 * Created by david on 10/9/15.
 */
@MetaInfServices(CorpusFormat.class)
public class TSVCorpus extends DSVCorpus {

  public TSVCorpus() {
    super("TSVCorpus", '\t');
  }

  @Override
  public String name() {
    return "TSV";
  }
}
