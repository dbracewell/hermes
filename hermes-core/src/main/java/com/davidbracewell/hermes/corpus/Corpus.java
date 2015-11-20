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
import com.davidbracewell.collection.InvertedIndex;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.*;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.string.StringUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.NonNull;

import java.io.IOException;
import java.util.*;
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
public interface Corpus extends DocumentStore {


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
    return documentFrequencies(Types.TOKEN, lemmatize ? HString::getLemma : HString::toString);
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
  default Corpus filter(@NonNull SerializablePredicate<Document> filter) {
    return new MStreamCorpus(stream().filter(filter), getDocumentFactory());
  }

  @Override
  default Optional<Document> get(String id) {
    return stream().filter(document -> StringUtils.safeEquals(id, document.getId(), true)).first();
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
   * Indexes the documents in the document store using the given indexer function. The indexer function converts
   * documents into one or more values (e.g. tokens) that can then be used to query documents.
   *
   * @param <T>     the type of object the document is indexed by
   * @param indexer the indexer to index the document by
   * @return the inverted index
   */
  default <T> InvertedIndex<Document, T> index(@NonNull SerializableFunction<Document, Collection<T>> indexer) {
    InvertedIndex<Document, T> invertedIndex = new InvertedIndex<>(indexer);
    invertedIndex.addAll(this);
    return invertedIndex;
  }

  /**
   * Indexes the documents by their lemmatized tokens
   *
   * @return the inverted index
   */
  default InvertedIndex<Document, String> index() {
    return index(document -> document.tokens().stream().map(HString::getLemma).collect(Collectors.toSet()));
  }

  @Override
  default boolean isEmpty() {
    return stream().isEmpty();
  }

  @Override
  default Iterator<Document> iterator() {
    return stream().iterator();
  }

  @Override
  default boolean put(Document document) {
    throw new UnsupportedOperationException();
  }

  @Override
  default Collection<Document> query(String query) throws ParseException {
    return stream().filter(new QueryParser(QueryParser.Operator.AND).parse(query)).collect();
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

  @Override
  default long size() {
    return stream().count();
  }

  /**
   * Stream m stream.
   *
   * @return the m stream
   */
  MStream<Document> stream();

  /**
   * Calculates the total term frequency of the tokens in the corpus.
   *
   * @param lemmatize True - count lemmas, False - count as is
   * @return A counter containing term frequencies of the given annotation type
   */
  default Counter<String> termFrequencies(boolean lemmatize) {
    return termFrequencies(Types.TOKEN, lemmatize ? HString::getLemma : HString::toString);
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
   * Union corpus.
   *
   * @param other the other
   * @return the corpus
   */
  default Corpus union(@NonNull Corpus other) {
    List<Document> documents = new LinkedList<>();
    this.forEach(documents::add);
    other.forEach(documents::add);
    return new InMemoryCorpus(documents);
  }


}//END OF Corpus2
