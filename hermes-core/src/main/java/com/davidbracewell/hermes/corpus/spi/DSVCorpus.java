package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.config.Config;
import com.davidbracewell.io.CSVReader;
import com.davidbracewell.io.resource.Resource;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Dsv corpus.
 */
public abstract class DSVCorpus extends ColumnBasedFormat {
  private static final long serialVersionUID = 1L;
  /**
   * The Delimiter.
   */
  protected final char delimiter;

  /**
   * Instantiates a new Dsv corpus.
   *
   * @param configProperty the config property
   * @param delimiter      the delimiter
   */
  public DSVCorpus(String configProperty, char delimiter) {
    super(configProperty);
    this.delimiter = delimiter;
  }


  @Override
  final Iterable<List<String>> rows(Resource resource) {
    try {
      com.davidbracewell.io.CSV csv = com.davidbracewell.io.CSV.builder()
        .delimiter(delimiter);

      if (Config.hasProperty(configProperty, "comment")) {
        csv = csv.comment(Config.get(configProperty,"comment").asCharacterValue());
      }

      CSVReader reader;
      if (Config.get(configProperty, "hasHeader").asBooleanValue(false)) {
        csv.hasHeader();
        fieldNames.clear();
        reader = csv.reader(resource);
        fieldNames.addAll(reader.getHeader().stream().map(String::toUpperCase).collect(Collectors.toList()));
      } else {
        csv.header(getFieldNames().asList());
        reader = csv.reader(resource);
      }
      return reader.readAll();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

}//END OF DSVCorpus
