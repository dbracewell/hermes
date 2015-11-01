package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.DocumentFormat;
import com.davidbracewell.hermes.corpus.SparkCorpus;
import com.davidbracewell.io.resource.Resource;
import org.kohsuke.MetaInfServices;

import java.io.IOException;


@MetaInfServices(DocumentFormat.class)
public class SparkFormat implements DocumentFormat {

  @Override
  public Corpus create(Resource resource, DocumentFactory documentFactory) {
    return new SparkCorpus(resource.descriptor());
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
