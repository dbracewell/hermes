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

import com.davidbracewell.DateUtils;
import com.davidbracewell.concurrent.Broker;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.logging.Logger;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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
  private static final String timestamp = DateUtils.US_STANDARD.format(new Date()) + "_" + DateFormat.getTimeInstance().format(new Date());
  private static final long serialVersionUID = 1L;
  private final AnnotationType[] annotationTypes;
  private final int numberOfThreads;
  private final Stopwatch timer = Stopwatch.createUnstarted();
  private final Function<Document, ?> onComplete;
  private final int queueSize;
  private AtomicLong documentsProcessed = new AtomicLong();


  private Pipeline(int numberOfThreads, int queueSize, Function<Document, ?> onComplete, Collection<AnnotationType> annotationTypes) {
    Preconditions.checkArgument(numberOfThreads > 0, "Number of threads must be > 0");
    Preconditions.checkArgument(queueSize > 0, "Queue size must be > 0");
    this.queueSize = queueSize;
    this.annotationTypes = Preconditions.checkNotNull(annotationTypes).toArray(new AnnotationType[1]);
    this.numberOfThreads = numberOfThreads;
    this.onComplete = Preconditions.checkNotNull(onComplete);
  }

  public long getElapsedTime(TimeUnit timeUnit) {
    return timer.elapsed(timeUnit);
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
  public static void process(Document textDocument, AnnotationType... annotationTypes) {

    if (annotationTypes == null || annotationTypes.length == 0) {
      return;
    }

    for (AnnotationType annotationType : annotationTypes) {
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

      if (!annotator.provides().contains(annotationType)) {
        throw new IllegalStateException(annotator.getClass().getName() + " does not produce " + annotationType);
      }

      //Get the requirements out of the way
      for (AnnotationType prereq : annotator.requires()) {
        process(textDocument, prereq);
      }

      annotator.annotate(textDocument);
      for (AnnotationType type : annotator.provides()) {
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
    return documentsProcessed.get() / (timer.elapsed(TimeUnit.NANOSECONDS) / 1000000000d);
  }

  /**
   * Annotates documents with the annotation types defined in the pipeline.
   *
   * @param documents the source of documents to be annotated
   */
  public void process(Corpus documents) {
    timer.start();
    Broker.<Document>builder()
        .addProducer(new Producer(documents))
        .addConsumer(new Consumer(annotationTypes, onComplete, documentsProcessed), numberOfThreads)
        .bufferSize(queueSize)
        .build()
        .run();
    timer.stop();
  }

  /**
   * Total time processing.
   *
   * @return the total time processing in string representation
   */
  public String totalTimeProcessing() {
    return timer.toString();
  }

  private static enum NoOpt implements Function<Document, Void> {
    /**
     * The INSTANCE.
     */INSTANCE;

    @Nullable
    @Override
    public Void apply(@Nullable Document input) {
      return null;
    }
  }

  private static class Producer extends Broker.Producer<Document> {

    private final Corpus documents;

    private Producer(Corpus documents) {
      this.documents = documents;
    }

    @Override
    public void produce() {
      start();
      for (Document doc : documents) {
        yield(doc);
      }
      stop();
    }
  }

  private static class Consumer implements Function<Document, Object> {

    private final AnnotationType[] annotationTypes;
    private final Function<Document, ?> onComplete;
    private final AtomicLong counter;

    private Consumer(AnnotationType[] annotationTypes, Function<Document, ?> onComplete, AtomicLong counter) {
      this.annotationTypes = annotationTypes;
      this.onComplete = onComplete;
      this.counter = counter;
    }

    @Nullable
    @Override
    public Object apply(@Nullable Document input) {
      if (input != null) {
        Pipeline.process(input, annotationTypes);
        counter.incrementAndGet();
        onComplete.apply(input);
      }
      return null;
    }
  }

  /**
   * A builder class for pipelines
   */
  public static class Builder {

    int queueSize = 10000;
    Set<AnnotationType> annotationTypes = new HashSet<>();
    int numberOfThreads = Runtime.getRuntime().availableProcessors();
    Function<Document, ?> onComplete = NoOpt.INSTANCE;

    /**
     * Add annotation.
     *
     * @param annotation the annotation
     * @return the builder
     */
    public Builder addAnnotation(AnnotationType annotation) {
      annotationTypes.add(Preconditions.checkNotNull(annotation));
      return this;
    }

    /**
     * Add annotations.
     *
     * @param annotations the annotations
     * @return the builder
     */
    public Builder addAnnotations(AnnotationType... annotations) {
      Preconditions.checkNotNull(annotations);
      this.annotationTypes.addAll(Arrays.asList(annotations));
      return this;
    }

    /**
     * Build pipeline.
     *
     * @return the pipeline
     */
    public Pipeline build() {
      return new Pipeline(numberOfThreads, queueSize, onComplete, annotationTypes);
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
    public Builder onComplete(Function<Document, ?> onComplete) {
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
