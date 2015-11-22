package com.davidbracewell.hermes.corpus;

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Hermes;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.resource.StringResource;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.stream.Spark;
import com.davidbracewell.stream.SparkStream;
import com.davidbracewell.stream.Streams;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;
import org.apache.spark.broadcast.Broadcast;

import java.io.IOException;
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
   * @param corpusLocation  the corpus location
   * @param documentFormat  the document format
   * @param documentFactory the document factory
   */
  @SuppressWarnings("unchecked")
  public SparkCorpus(@NonNull String corpusLocation, @NonNull DocumentFormat documentFormat, @NonNull DocumentFactory documentFactory) {
    if (documentFormat.isOnePerLine() && documentFormat.extension().toUpperCase().startsWith("JSON")) {
      this.stream = new SparkDocumentStream(Streams.textFile(corpusLocation, true));
    } else if (documentFormat.isOnePerLine()) {
      Broadcast<Config> configBroadcast = Spark.context().broadcast(Config.getInstance());
      this.stream = new SparkDocumentStream(Streams.textFile(corpusLocation, true).flatMap(
        line -> {
          Hermes.initializeWorker(configBroadcast.getValue());
          return documentFormat.create(Resources.fromString(line), documentFactory)
            .stream()
            .map(Document::toJson)
            .collect();
        }
      ));
    } else {
      Broadcast<Config> configBroadcast = Spark.context().broadcast(Config.getInstance());
      this.stream = new SparkDocumentStream(
        new SparkStream<String>(
          Spark.context()
            .wholeTextFiles(corpusLocation)
            .values()
            .flatMap(str -> {
              Hermes.initializeWorker(configBroadcast.getValue());
              return documentFormat.create(Resources.fromString(str), documentFactory)
                .stream()
                .map(Document::toJson)
                .collect();
            })
        )
      );
    }
  }

  /**
   * Instantiates a new Spark corpus.
   *
   * @param documents the documents
   */
  public SparkCorpus(@NonNull Collection<Document> documents) {
    this.stream = new SparkDocumentStream(Streams.of(
      documents.stream().map(Document::toJson).collect(Collectors.toList()),
      true
    ));
  }

  /**
   * Instantiates a new Spark corpus.
   *
   * @param stream the stream
   */
  protected SparkCorpus(SparkDocumentStream stream) {
    this.stream = stream;
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
  public Corpus cache() {
    return new SparkCorpus(new SparkDocumentStream(stream.getSource().cache()));
  }

  @Override
  public Corpus sample(int count) {
    return new MStreamCorpus(stream.sample(count), getDocumentFactory());
  }

  @Override
  public Corpus write(@NonNull String format, @NonNull Resource resource) throws IOException {
    return write(format, resource.descriptor());
  }

  @Override
  public Corpus write(@NonNull String format, @NonNull String resource) throws IOException {
    DocumentFormat outFormat = DocumentFormats.forName(format);
    if (!outFormat.isOnePerLine()) {
      throw new IllegalArgumentException(format + " must be one per line!");
    }
    if (outFormat.extension().toLowerCase().startsWith("json")) {
      stream.saveAsTextFile(resource);
    } else {
      stream.getSource().map(json -> {
        Document document = Document.fromJson(json);
        Resource tmp = new StringResource();
        try {
          outFormat.write(tmp, document);
          return tmp.readToString().trim();
        } catch (IOException e) {
          e.printStackTrace();
          return StringUtils.EMPTY;
        }
      })
        .saveAsTextFile(resource);
    }
    return this;
  }

  @Override
  public Corpus updateConfig() {
    stream.updateConfig();
    return this;
  }
}
