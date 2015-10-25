package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.hermes.corpus.SparkCorpus;
import com.davidbracewell.io.resource.Resource;
import org.kohsuke.MetaInfServices;

import java.io.IOException;

/**
 * Created by david on 10/9/15.
 */
@MetaInfServices(CorpusFormat.class)
public class SparkFormat implements CorpusFormat {

  @Override
  public Corpus create(Resource resource, DocumentFactory documentFactory) {
    return new SparkCorpus(resource.descriptor(), Config.get("SparkFormat", "partitions").asIntegerValue(100));
  }

  @Override
  public Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
    return new SparkCorpus(resource.descriptor());
  }

  @Override
  public String name() {
    return "SPARK";
  }

}
