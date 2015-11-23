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

import com.davidbracewell.collection.Collect;
import com.davidbracewell.collection.Counter;
import com.davidbracewell.collection.Counters;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.*;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.tuple.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * An implementation of a <code>DocumentStore</code> that represents a collection of documents. Corpus formats are
 * defined
 * via corresponding <code>CorpusFormat</code> objects, which are registered using Java's service loader functionality.
 * When constructing a corpus the format can be appended with <code>_OPL</code> to denote that individual file will
 * have one document per line in the given format. For example, TEXT_OPL would relate to a format where every line of a
 * file equates to a document in plain text format.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface Corpus extends Iterable<Document> {


  /**
   * The constant EMPTY.
   */
  Corpus EMPTY = new InMemoryCorpus(Collections.emptyList());

  /**
   * Builder corpus builder.
   *
   * @return the corpus builder
   */
  static CorpusBuilder builder() {
    return new CorpusBuilder();
  }

  /**
   * Annotates this corpus with the given annotation types and returns a new corpus with the given annotation types
   * present
   *
   * @param types The annotation types to annotate
   * @return A new corpus with the given annotation types present.
   */
  Corpus annotate(AnnotationType... types);

  /**
   * To memory.
   *
   * @return the corpus
   */
  default Corpus cache() {
    if (this instanceof InMemoryCorpus) {
      return this;
    }
    return new InMemoryCorpus(Collect.from(this).collect(Collectors.toList()));
  }

  /**
   * Calculates the document frequency of tokens in the corpus.
   *
   * @param lemmatize True - count lemmas, False - count as is
   * @return A counter containing document frequencies of the given annotation type
   */
  default Counter<String> documentFrequencies(boolean lemmatize) {
    return documentFrequencies(Types.TOKEN, h -> lemmatize ? h.getLemma() : h.toString());
  }

  /**
   * Calculates the document frequency of annotations of the given annotation type in the corpus. Annotations are
   * transformed into strings using the given toString function.
   *
   * @param type     the annotation type to count.
   * @param toString the function to convert Annotations into strings
   * @return A counter containing document frequencies of the given annotation type
   */
  default Counter<String> documentFrequencies(@NonNull AnnotationType type, @NonNull Function<? super Annotation, String> toString) {
    return Counters.newHashMapCounter(
      stream()
        .flatMap(document -> document.get(type).stream().map(toString).distinct().collect(Collectors.toList()))
        .countByValue()
    );
  }

  /**
   * Filter corpus.
   *
   * @param filter the filter
   * @return the corpus
   */
  default Corpus filter(@NonNull SerializablePredicate<? super Document> filter) {
    return new MStreamCorpus(stream().filter(filter), getDocumentFactory());
  }

  /**
   * Gets document factory.
   *
   * @return the document factory
   */
  DocumentFactory getDocumentFactory();

  /**
   * Groups documents in the document store using the given function.
   *
   * @param <K>         The key type
   * @param keyFunction Converts the document into a key to group the documents  by
   * @return A <code>Multimap</code> of key - document pairs.
   */
  default <K> Multimap<K, Document> groupBy(@NonNull SerializableFunction<? super Document, K> keyFunction) {
    Multimap<K, Document> grouping = ArrayListMultimap.create();
    forEach(document -> grouping.put(keyFunction.apply(document), document));
    return grouping;
  }

  /**
   * Is empty boolean.
   *
   * @return the boolean
   */
  default boolean isEmpty() {
    return stream().isEmpty();
  }

  @Override
  default Iterator<Document> iterator() {
    return stream().iterator();
  }

  /**
   * Query collection.
   *
   * @param query the query
   * @return the collection
   * @throws ParseException the parse exception
   */
  default Corpus query(String query) throws ParseException {
    return filter(new QueryParser(QueryParser.Operator.AND).parse(query));
  }

  /**
   * Create a sample of this corpus using <a href="https://en.wikipedia.org/wiki/Reservoir_sampling">Reservoir
   * sampling</a>.
   *
   * @param count the number of documents to include in the sample
   * @return the sampled corpus
   */
  default Corpus sample(int count) {
    return sample(count, new Random());
  }

  /**
   * Create a sample of this corpus using <a href="https://en.wikipedia.org/wiki/Reservoir_sampling">Reservoir
   * sampling</a>.
   *
   * @param count  the number of documents to include in the sample
   * @param random Random number generator to use for selection
   * @return the sampled corpus
   */
  default Corpus sample(int count, @NonNull Random random) {
    if (count <= 0) {
      return builder().inMemory().build();
    }
    List<Document> sample = stream().limit(count).collect();
    AtomicInteger k = new AtomicInteger(count + 1);
    stream().skip(count).forEach(document -> {
      int rndIndex = random.nextInt(k.getAndIncrement());
      if (rndIndex < count) {
        sample.set(rndIndex, document);
      }
    });
    return builder().inMemory().addAll(sample).build();
  }

  /**
   * Size long.
   *
   * @return the long
   */
  default long size() {
    return stream().count();
  }

  /**
   * Stream m stream.
   *
   * @return the m stream
   */
  MStream<Document> stream();


  default Counter<Tuple> tokenNGrams(int order, boolean lemmatize) {
    return tokenNGrams(order, false, hString -> lemmatize ? hString.getLemma() : hString.toString());
  }

  default Counter<Tuple> tokenNGrams(int order, @NonNull SerializableFunction<? super HString, String> toStringFunction) {
    return tokenNGrams(order, false, toStringFunction);
  }

  default Counter<Tuple> tokenNGrams(int order, boolean removeStopWords, @NonNull SerializableFunction<? super HString, String> toStringFunction) {
    return Counters.newHashMapCounter(
      stream()
        .flatMap(document -> document.tokenNGrams(order, removeStopWords).stream().map(hString -> {
          switch (order) {
            case 1:
              return Tuple1.of(toStringFunction.apply(hString));
            case 2:
              return Tuple2.of(toStringFunction.apply(hString.tokenAt(0)), toStringFunction.apply(hString.tokenAt(1)));
            case 3:
              return Tuple3.of(toStringFunction.apply(hString.tokenAt(0)), toStringFunction.apply(hString.tokenAt(1)), toStringFunction.apply(hString.tokenAt(2)));
            case 4:
              return Tuple4.of(toStringFunction.apply(hString.tokenAt(0)), toStringFunction.apply(hString.tokenAt(1)), toStringFunction.apply(hString.tokenAt(2)), toStringFunction.apply(hString.tokenAt(3)));
            default:
              String[] source = new String[order];
              for (int i = 0; i < order; i++) {
                source[i] = toStringFunction.apply(hString.tokenAt(i));
              }
              return NTuple.of(source);
          }
        }).collect(Collectors.toList()))
        .countByValue()
    );
  }

  /**
   * Calculates the total term frequency of the tokens in the corpus.
   *
   * @param lemmatize True - count lemmas, False - count as is
   * @return A counter containing term frequencies of the given annotation type
   */
  default Counter<String> termFrequencies(boolean lemmatize) {
    return termFrequencies(Types.TOKEN, h -> lemmatize ? h.getLemma() : h.toString());
  }

  /**
   * Calculates the total term frequency of annotations of the given type in the corpus. Annotations are transformed
   * into strings using the given toString function.
   *
   * @param type     the annotation type to count.
   * @param toString the function to convert Annotations into strings
   * @return A counter containing total term frequencies of the given annotation type
   */
  default Counter<String> termFrequencies(@NonNull AnnotationType type, @NonNull SerializableFunction<? super Annotation, String> toString) {
    return Counters.newHashMapCounter(
      stream()
        .flatMap(document -> document.get(type).stream().map(toString).collect(Collectors.toList()))
        .countByValue()
    );
  }

  /**
   * Union corpus.
   *
   * @param other the other
   * @return the corpus
   */
  default Corpus union(@NonNull Corpus other) {
    return new UnionCorpus(this, other);
  }

  /**
   * Write corpus.
   *
   * @param format   the format
   * @param resource the resource
   * @return the corpus
   * @throws IOException the io exception
   */
  default Corpus write(@NonNull String format, @NonNull Resource resource) throws IOException {
    DocumentFormat documentFormat = DocumentFormats.forName(format);
    documentFormat.write(resource, this);
    return builder().from(format, resource, getDocumentFactory()).build();
  }

  /**
   * Write corpus.
   *
   * @param resource the resource
   * @return the corpus
   * @throws IOException the io exception
   */
  default Corpus write(@NonNull Resource resource) throws IOException {
    return write(DocumentFormats.JSON_OPL, resource);
  }

  /**
   * Write corpus.
   *
   * @param format   the format
   * @param resource the resource
   * @return the corpus
   * @throws IOException the io exception
   */
  default Corpus write(@NonNull String format, @NonNull String resource) throws IOException {
    return write(format, Resources.from(resource));
  }

  /**
   * Write corpus.
   *
   * @param resource the resource
   * @return the corpus
   * @throws IOException the io exception
   */
  default Corpus write(@NonNull String resource) throws IOException {
    return write(DocumentFormats.JSON_OPL, resource);
  }

  /**
   * Update config corpus.
   *
   * @return the corpus
   */
  default Corpus updateConfig() {
    return this;
  }

}//END OF Corpus2
