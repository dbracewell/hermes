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

package com.davidbracewell.hermes.corpus;

import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.function.*;
import com.davidbracewell.guava.common.collect.Iterables;
import com.davidbracewell.guava.common.collect.Iterators;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Hermes;
import com.davidbracewell.hermes.Pipeline;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.stream.*;
import lombok.NonNull;
import org.apache.spark.broadcast.Broadcast;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Spark document stream.
 *
 * @author David B. Bracewell
 */
class SparkDocumentStream implements MStream<Document>, Serializable {
   private static final long serialVersionUID = 1L;
   private volatile SparkStream<String> source;
   private volatile Broadcast<Config> configBroadcast;

   /**
    * Instantiates a new Spark document stream.
    *
    * @param source the source
    */
   public SparkDocumentStream(@NonNull MStream<String> source) {
      this.source = new SparkStream<>(source);
      this.configBroadcast = SparkStreamingContext.INSTANCE.getConfigBroadcast();
   }


   @Override
   public MStream<Document> repartition(int numPartition) {
      source = source.repartition(numPartition);
      return this;
   }

   @Override
   public StreamingContext getContext() {
      return source.getContext();
   }

   private SparkDocumentStream of(@NonNull MStream<String> source) {
      return new SparkDocumentStream(source);
   }

   /**
    * Gets source.
    *
    * @return the source
    */
   protected MStream<String> getSource() {
      return source;
   }

   /**
    * Annotate spark document stream.
    *
    * @param types the types
    * @return the spark document stream
    */
   public SparkDocumentStream annotate(@NonNull AnnotatableType... types) {
      return new SparkDocumentStream(source.map(json -> {
         Hermes.initializeWorker(configBroadcast.value());
         if (Document.hasAnnotations(json, types)) {
            return json;
         }
         Document document = Document.fromJson(json);
         Pipeline.process(document, types);
         return document.toJson();
      }));
   }

   @Override
   public void close() throws IOException {
      source.close();
   }

   @Override
   public SerializableRunnable getOnCloseHandler() {
      return source.getOnCloseHandler();
   }

   @Override
   public MStream<Document> filter(@NonNull SerializablePredicate<? super Document> predicate) {
      return of(source.filter(json -> {
         Hermes.initializeWorker(configBroadcast.value());
         return predicate.test(Document.fromJson(json));
      }));
   }

   @Override
   public <R> MStream<R> map(@NonNull SerializableFunction<? super Document, ? extends R> function) {
      return source.map(json -> {
         Hermes.initializeWorker(configBroadcast.value());
         return function.apply(Document.fromJson(json));
      });
   }

   @Override
   public <R> MStream<R> flatMap(@NonNull SerializableFunction<? super Document, Stream<? extends R>> mapper) {
      return source.flatMap(json -> {
         Hermes.initializeWorker(configBroadcast.value());
         return mapper.apply(Document.fromJson(json));
      });
   }

   @Override
   public <R, U> MPairStream<R, U> flatMapToPair(@NonNull SerializableFunction<? super Document, Stream<? extends Map.Entry<? extends R, ? extends U>>> function) {
      return source.flatMapToPair(json -> {
         Hermes.initializeWorker(configBroadcast.value());
         return function.apply(Document.fromJson(json));
      });
   }

   @Override
   public <R, U> MPairStream<R, U> mapToPair(@NonNull SerializableFunction<? super Document, ? extends Map.Entry<? extends R, ? extends U>> function) {
      return source.mapToPair(json -> {
         Hermes.initializeWorker(configBroadcast.value());
         return function.apply(Document.fromJson(json));
      });
   }

   @Override
   public <U> MPairStream<U, Iterable<Document>> groupBy(@NonNull SerializableFunction<? super Document, ? extends U> function) {
      return source.map(json -> Document.fromJson(json)).groupBy(document -> {
         Hermes.initializeWorker(configBroadcast.value());
         return function.apply(document);
      });
   }

   @Override
   public <R> R collect(Collector<? super Document, Document, R> collector) {
      return source.map(json -> Document.fromJson(json)).collect(collector);
   }

   @Override
   public List<Document> collect() {
      return source.map(json -> Document.fromJson(json)).collect();
   }

   @Override
   public Optional<Document> reduce(@NonNull SerializableBinaryOperator<Document> reducer) {
      return source.map(json -> Document.fromJson(json)).reduce(reducer);
   }

   @Override
   public Document fold(@NonNull Document zeroValue, @NonNull SerializableBinaryOperator<Document> operator) {
      return source.map(json -> Document.fromJson(json)).fold(zeroValue, operator);
   }

   @Override
   public void forEach(SerializableConsumer<? super Document> consumer) {
      source.forEach(json -> {
         Hermes.initializeWorker(configBroadcast.value());
         consumer.accept(Document.fromJson(json));
      });
   }

   @Override
   public void forEachLocal(SerializableConsumer<? super Document> consumer) {
      source.forEachLocal(json -> {
         Hermes.initializeWorker(configBroadcast.value());
         consumer.accept(Document.fromJson(json));
      });
   }

   @Override
   public Iterator<Document> iterator() {
      return Iterators.transform(source.iterator(), Document::fromJson);
   }

   @Override
   public Optional<Document> first() {
      return source.first().map(Document::fromJson);
   }

   @Override
   public MStream<Document> sample(boolean withReplacement, int number) {
      return of(source.sample(withReplacement, number));
   }

   @Override
   public long count() {
      return source.count();
   }

   @Override
   public boolean isEmpty() {
      return source.isEmpty();
   }

   @Override
   public Map<Document, Long> countByValue() {
      return source.map(json -> Document.fromJson(json)).countByValue();
   }

   @Override
   public MStream<Document> distinct() {
      return of(source.distinct());
   }

   @Override
   public MStream<Document> limit(long number) {
      return of(source.limit(number));
   }

   @Override
   public List<Document> take(int n) {
      return source.take(n).stream().map(Document::fromJson).collect(Collectors.toList());
   }

   @Override
   public MStream<Document> skip(long n) {
      return of(source.skip(n));
   }

   @Override
   public void onClose(SerializableRunnable closeHandler) {
      source.onClose(closeHandler);
   }

   @Override
   public MStream<Document> sorted(boolean ascending) {
      return of(source.sorted(ascending));
   }

   @Override
   public <R extends Comparable<R>> MStream<Document> sorted(boolean ascending, @NonNull SerializableFunction<? super Document, ? extends R> keyFunction) {
      return of(source.sorted(ascending, k -> {
         Hermes.initializeWorker(configBroadcast.value());
         return keyFunction.apply(Document.fromJson(k));
      }));
   }

   @Override
   public Optional<Document> max(@NonNull SerializableComparator<? super Document> comparator) {
      return source.map(Document::fromJson).max(comparator);
   }

   @Override
   public Optional<Document> min(@NonNull SerializableComparator<? super Document> comparator) {
      return source.map(Document::fromJson).min(comparator);
   }

   @Override
   public <U> MPairStream<Document, U> zip(@NonNull MStream<U> other) {
      return source.map(Document::fromJson).zip(other);
   }

   @Override
   public MPairStream<Document, Long> zipWithIndex() {
      return source.map(Document::fromJson).zipWithIndex();
   }

   @Override
   public MDoubleStream mapToDouble(@NonNull SerializableToDoubleFunction<? super Document> function) {
      return source.mapToDouble(json -> {
         Hermes.initializeWorker(configBroadcast.value());
         return function.applyAsDouble(Document.fromJson(json));
      });
   }

   @Override
   public MStream<Document> cache() {
      return of(source.cache());
   }

   @Override
   public MStream<Document> union(@NonNull MStream<Document> other) {
      if (other instanceof SparkDocumentStream) {
         return of(source.union(Cast.<SparkDocumentStream>as(other).source));
      }
      return of(source.union(other.map(Document::toJson)));
   }

   @Override
   public void saveAsTextFile(@NonNull Resource location) {
      source.saveAsTextFile(location);
   }

   @Override
   public void saveAsTextFile(@NonNull String location) {
      source.saveAsTextFile(location);
   }

   @Override
   public MStream<Document> parallel() {
      return this;
   }

   @Override
   public MStream<Document> shuffle(Random random) {
      return new SparkDocumentStream(source.shuffle(random));
   }

   @Override
   public MStream<Iterable<Document>> split(int n) {
      return source.split(n).map(jsonIterable -> {
         Hermes.initializeWorker(configBroadcast.value());
         return Iterables.transform(jsonIterable, Document::fromJson);
      });
   }

   @Override
   public MStream<Iterable<Document>> partition(long partitionSize) {
      return source.partition(partitionSize).map(jsonIterable -> {
         Hermes.initializeWorker(configBroadcast.value());
         return Iterables.transform(jsonIterable, Document::fromJson);
      });
   }


   @Override
   public void updateConfig() {
      SparkStreamingContext.INSTANCE.updateConfig();
      this.configBroadcast = SparkStreamingContext.INSTANCE.getConfigBroadcast();
   }

}//END OF SparkDocumentStream
