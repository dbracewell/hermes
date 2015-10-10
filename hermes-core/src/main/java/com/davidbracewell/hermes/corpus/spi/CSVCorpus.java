package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.hermes.corpus.CorpusFormat;
import org.kohsuke.MetaInfServices;

/**
 * Created by david on 10/9/15.
 */
@MetaInfServices(CorpusFormat.class)
public class CSVCorpus extends DSVCorpus {

  public CSVCorpus() {
    super("CSVCorpus", '\'');
  }

  @Override
  public String name() {
    return "CSV";
  }
}