package com.davidbracewell.hermes.corpus;

import com.davidbracewell.hermes.*;
import lombok.NonNull;
import org.apache.spark.Partition;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Created by david on 10/9/15.
 */
public class SparkCorpus extends Corpus implements Serializable {

  private final JavaRDD<String> corpus;

  public SparkCorpus(String corpusLocation) {
    JavaSparkContext context = new JavaSparkContext();
    this.corpus = context.textFile(corpusLocation);
  }

  public SparkCorpus(String corpusLocation, int numPartitions) {
    JavaSparkContext context = new JavaSparkContext();
    this.corpus = context.textFile(corpusLocation, numPartitions);
  }

  public SparkCorpus(JavaRDD<String> other) {
    this.corpus = other;
  }


  @Override
  public DocumentFactory getDocumentFactory() {
    return DocumentFactory.getInstance();
  }

  @Override
  public Iterator<Document> iterator() {
    return corpus.map(Document::fromJson).collect().iterator();
  }

  @Override
  public Corpus annotate(@NonNull AnnotationType... types) {
    return new SparkCorpus(
      corpus.map(json -> {
        Hermes.initializeApplication("", new String[1]);
        Document document = Document.fromJson(json);
        Pipeline.process(document, types);
        return document.toJson();
      })
    );
  }

  public void test() {

  }

  @Override
  public int size() {
    return (int) corpus.count();
  }

  @Override
  public Corpus sample(int count) {
    return new SparkCorpus(
      corpus.sample(false, (double) count / size())
    );
  }

  @Override
  public void forEach(@NonNull Consumer<? super Document> action) {
    corpus.foreach(json -> {
      Hermes.initializeApplication("", new String[1]);
      Document document = Document.fromJson(json);
      action.accept(document);
    });
  }


}
