package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.csv.CSVReader;
import com.google.common.base.Throwables;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.List;

/**
 * Created by david on 10/9/15.
 */
@MetaInfServices(CorpusFormat.class)
public class CSVCorpus extends ColumnBasedFormat {

  final boolean hasHeader;

  public CSVCorpus() {
    super("CSVCorpus");
    this.hasHeader = Config.get("CSVCorpus.hasHeader").asBooleanValue(false);
  }

  @Override
  Iterable<List<String>> rows(Resource resource) {
    try {
      com.davidbracewell.io.CSV csv = com.davidbracewell.io.CSV.builder();
      CSVReader reader;
      if (hasHeader) {
        csv.hasHeader();
        fieldNames.clear();
        reader = csv.reader(resource);
        fieldNames.addAll(reader.getHeader());
      } else {
        csv.header(fieldNames.asList());
        reader = csv.reader(resource);
      }
      return reader.readAll();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public String name() {
    return "CSV";
  }
}
