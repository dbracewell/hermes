/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.davidbracewell.hermes;

import com.davidbracewell.Language;
import com.davidbracewell.concurrent.Broker;
import com.davidbracewell.hermes.annotator.Annotator;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusFormats;
import com.davidbracewell.io.AsyncWriter;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Logger;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * <p>A pipeline wraps the process of annotating a document with one or more annotations. By constructing a pipeline
 * documents can be processed in parallel possibly lowering the amount of time needed to annotation a document
 * collection.</p>
 *
 * @author David B. Bracewell
 */
public final class Pipeline implements Serializable {

  private static final Logger log = Logger.getLogger(Pipeline.class);
  private static final long serialVersionUID = 1L;
  private final AnnotatableType[] annotationTypes;
  private final int numberOfThreads;
  private long totalTime;
  private final Stopwatch timer = Stopwatch.createUnstarted();
  private final java.util.function.Consumer<Document> onComplete;
  private final int queueSize;
  private AtomicLong documentsProcessed = new AtomicLong();
  private final boolean returnCorpus;


  private Pipeline(int numberOfThreads, int queueSize, Consumer<Document> onComplete, Collection<AnnotatableType> annotationTypes, boolean returnCorpus) {
    this.returnCorpus = returnCorpus;
    Preconditions.checkArgument(numberOfThreads > 0, "Number of threads must be > 0");
    Preconditions.checkArgument(queueSize > 0, "Queue size must be > 0");
    this.queueSize = queueSize;
    this.annotationTypes = Preconditions.checkNotNull(annotationTypes).toArray(new AnnotatableType[annotationTypes.size()]);
    this.numberOfThreads = numberOfThreads;
    this.onComplete = Preconditions.checkNotNull(onComplete);
  }

  public double getElapsedTime(@NonNull TimeUnit timeUnit) {
    double totalNanoSeconds = totalTime + timer.elapsed(TimeUnit.NANOSECONDS);
    if (timeUnit == TimeUnit.NANOSECONDS) {
      return totalNanoSeconds;
    }
    return totalNanoSeconds / TimeUnit.NANOSECONDS.convert(1, timeUnit);
  }

  /**
   * Convenience method for getting a Pipeline Builder
   *
   * @return the pipeline builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Annotates a document with the given annotation types.
   *
   * @param textDocument    the document to be the annotate
   * @param annotationTypes the annotation types to be annotated
   */
  public static void process(Document textDocument, AnnotatableType... annotationTypes) {
    if (annotationTypes == null || annotationTypes.length == 0) {
      return;
    }

    for (AnnotatableType annotationType : annotationTypes) {
      if (annotationType == null) {
        continue;
      }

      if (textDocument.getAnnotationSet().isCompleted(annotationType)) {
        continue;
      }

      if (log.isLoggable(Level.FINEST)) {
        log.finest("Annotating for {0}", annotationType);
      }

      Annotator annotator = AnnotatorCache.getInstance().get(annotationType, textDocument.getLanguage());

      if (annotator == null) {
        throw new IllegalStateException("Could not get annotator for " + annotationType);
      }

      if (!annotator.satisfies().contains(annotationType)) {
        throw new IllegalStateException(annotator.getClass().getName() + " does not satisfy " + annotationType);
      }

      //Get the requirements out of the way
      for (AnnotatableType prereq : annotator.requires()) {
        process(textDocument, prereq);
      }

      annotator.annotate(textDocument);
      for (AnnotatableType type : annotator.satisfies()) {
        textDocument.getAnnotationSet().setIsCompleted(type, true, annotator.getClass().getName() + "::" + annotator.getVersion());
      }

    }
  }

  /**
   * The number of documents processed per second
   *
   * @return the number of documents processed per second
   */
  public double documentsPerSecond() {
    return (double) documentsProcessed.get() / getElapsedTime(TimeUnit.SECONDS);
  }

  /**
   * Annotates documents with the annotation types defined in the pipeline.
   *
   * @param documents the source of documents to be annotated
   */
  public Corpus process(Corpus documents) {
    timer.start();

    Broker.Builder<Document> builder = Broker.<Document>builder()
      .addProducer(new Producer(documents))
      .bufferSize(queueSize);

    Corpus corpus = documents;
    if (returnCorpus) {

      Resource tempFile = Resources.temporaryFile();
      tempFile.deleteOnExit();
      try (AsyncWriter writer = new AsyncWriter(tempFile.writer())) {
        builder.addConsumer(new AnnotateConsumer(annotationTypes, onComplete, documentsProcessed, writer), numberOfThreads)
          .build().run();
        writer.close();
        corpus = Corpus.builder().source(CorpusFormats.JSON_OPL, tempFile).build();
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }


    } else {

      builder
        .addConsumer(new AnnotateConsumer(annotationTypes, onComplete, documentsProcessed, null), numberOfThreads)
        .build()
        .run();

    }

    timer.stop();
    totalTime += timer.elapsed(TimeUnit.NANOSECONDS);
    timer.reset();

    return corpus;
  }

  public Document process(@NonNull Document document) {
    timer.start();
    process(document, annotationTypes);
    timer.stop();
    documentsProcessed.incrementAndGet();
    totalTime += timer.elapsed(TimeUnit.NANOSECONDS);
    timer.reset();
    return document;
  }

  /**
   * Total time processing.
   *
   * @return the total time processing in string representation
   */
  public String totalTimeProcessing() {
    return String.format("%.4g s", getElapsedTime(TimeUnit.SECONDS));
  }

  public static void setAnnotator(@NonNull AnnotationType annotationType, @NonNull Language language, @NonNull Annotator annotator) {
    AnnotatorCache.getInstance().setAnnotator(annotationType, language, annotator);
  }

  private enum NoOpt implements java.util.function.Consumer<Document> {
    INSTANCE;

    @Override
    public void accept(Document input) {
    }
  }

  private static class Producer extends Broker.Producer<Document> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Corpus documents;

    private Producer(Corpus documents) {
      this.documents = documents;
    }

    @Override
    public void produce() {
      start();
      for (Document document : documents) {
        yield(document);
      }
      stop();
    }
  }

  private class AnnotateConsumer implements java.util.function.Consumer<Document>, Serializable {
    private static final long serialVersionUID = 1L;
    private final AnnotatableType[] annotationTypes;
    private final java.util.function.Consumer<Document> onComplete;
    private final AtomicLong counter;
    private final AsyncWriter writer;

    private AnnotateConsumer(AnnotatableType[] annotationTypes, Consumer<Document> onComplete, AtomicLong counter, AsyncWriter writer) {
      this.annotationTypes = annotationTypes;
      this.onComplete = onComplete;
      this.counter = counter;
      this.writer = writer;
    }

    @Override
    public void accept(Document document) {
      if (document != null) {
        process(document, annotationTypes);
        counter.incrementAndGet();
        if (writer != null) {
          try {
            writer.write(document.toJson() + "\n");
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
        }
        onComplete.accept(document);
      }
    }

  }

  /**
   * A builder class for pipelines
   */
  public static class Builder {

    int queueSize = 10000;
    Set<AnnotatableType> annotationTypes = new HashSet<>();
    int numberOfThreads = Runtime.getRuntime().availableProcessors();
    java.util.function.Consumer<Document> onComplete = NoOpt.INSTANCE;
    boolean returnCorpus = true;

    /**
     * Add annotation.
     *
     * @param annotation the annotation
     * @return the builder
     */
    public Builder addAnnotation(AnnotatableType annotation) {
      annotationTypes.add(Preconditions.checkNotNull(annotation));
      return this;
    }

    /**
     * Add annotations.
     *
     * @param annotations the annotations
     * @return the builder
     */
    public Builder addAnnotations(AnnotatableType... annotations) {
      Preconditions.checkNotNull(annotations);
      this.annotationTypes.addAll(Arrays.asList(annotations));
      return this;
    }

    public Builder returnCorpus(boolean returnCorpus) {
      this.returnCorpus = returnCorpus;
      return this;
    }

    /**
     * Build pipeline.
     *
     * @return the pipeline
     */
    public Pipeline build() {
      return new Pipeline(numberOfThreads, queueSize, onComplete, annotationTypes, returnCorpus);
    }

    /**
     * Number of threads.
     *
     * @param threadCount the thread count
     * @return the builder
     */
    public Builder numberOfThreads(int threadCount) {
      this.numberOfThreads = threadCount;
      return this;
    }

    /**
     * On complete.
     *
     * @param onComplete the on complete
     * @return the builder
     */
    public Builder onComplete(java.util.function.Consumer<Document> onComplete) {
      this.onComplete = onComplete;
      return this;
    }

    /**
     * Queue size.
     *
     * @param queueSize the queue size
     * @return the builder
     */
    public Builder queueSize(int queueSize) {
      this.queueSize = queueSize;
      return this;
    }

  }//END OF Pipeline$Builder

}//END OF Pipeline
