package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.config.Config;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.csv.CSVReader;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by david on 10/9/15.
 */
public abstract class DSVCorpus extends ColumnBasedFormat {

  protected final char delimiter;
  protected final boolean hasHeader;

  public DSVCorpus(String configProperty, char delimiter) {
    super(configProperty);
    this.delimiter = delimiter;
    this.hasHeader = Config.get(configProperty, "hasHeader").asBooleanValue(false);
  }

  @Override
  final Iterable<List<String>> rows(Resource resource) {
    try {
      com.davidbracewell.io.CSV csv = com.davidbracewell.io.CSV.builder().delimiter(delimiter);
      CSVReader reader;
      if (hasHeader) {
        csv.hasHeader();
        fieldNames.clear();
        reader = csv.reader(resource);
        fieldNames.addAll(reader.getHeader().stream().map(String::toUpperCase).collect(Collectors.toList()));
      } else {
        csv.header(fieldNames.asList());
        reader = csv.reader(resource);
      }
      return reader.readAll();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }



}
