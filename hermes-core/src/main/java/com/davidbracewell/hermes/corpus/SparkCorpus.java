package com.davidbracewell.hermes.corpus;

import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.stream.Streams;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;


/**
 * The type Spark corpus.
 */
public class SparkCorpus implements Corpus, Serializable {
  private static final long serialVersionUID = 1L;
  private final SparkDocumentStream stream;

  /**
   * Instantiates a new Spark corpus.
   *
   * @param corpusLocation the corpus location
   */
  public SparkCorpus(@NonNull String corpusLocation) {
    this.stream = new SparkDocumentStream(Streams.textFile(corpusLocation, true));
  }

  public SparkCorpus(@NonNull Collection<Document> documents){
    this.stream = new SparkDocumentStream(Streams.of(
      documents.stream().map(Document::toJson).collect(Collectors.toList()),
      true
    ));
  }

  @Override
  public DocumentFactory getDocumentFactory() {
    return DocumentFactory.getInstance();
  }

  @Override
  public MStream<Document> stream() {
    return stream;
  }

  @Override
  public Corpus annotate(@NonNull AnnotationType... types) {
    stream.annotate(types);
    return this;
  }

  @Override
  public Corpus sample(int count) {
    return new MStreamCorpus(stream.sample(count), getDocumentFactory());
  }

}
