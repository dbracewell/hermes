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
import com.davidbracewell.collection.NormalizedStringMap;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.corpus.spi.OnePerLineFormat;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.string.StringUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public abstract class Corpus implements DocumentStore, Serializable {
  private static final Map<String, CorpusFormat> corpusFormats = new NormalizedStringMap<>();
  private static final long serialVersionUID = 1L;

  /**
   * Creates a corpus object by loading documents in the given format from the given resource
   *
   * @param format   the format the documents are in
   * @param resource the resource containing the documents
   * @return the corpus
   * @throws IOException something went wrong reading in the corpus
   */
  public static Corpus from(@NonNull String format, @NonNull final Resource resource) throws IOException {
    return from(format, resource, DocumentFactory.getInstance());
  }

  /**
   * Creates a corpus object by loading documents in the given format from the given resource
   *
   * @param format          the format the documents are in
   * @param resource        the resource containing the documents
   * @param documentFactory The document factory to use
   * @return the corpus
   * @throws IOException something went wrong reading in the corpus
   */
  public static Corpus from(@NonNull String format, @NonNull final Resource resource, @NonNull final DocumentFactory documentFactory) throws IOException {
    final CorpusFormat corpusFormat = getFormat(format);
    if (corpusFormat != null) {
      return corpusFormat.create(resource, documentFactory);
    }
    throw new IllegalArgumentException("No corpus format registered for " + format);
  }

  /**
   * From corpus.
   *
   * @param documentCollection the document collection
   * @return the corpus
   */
  public static Corpus from(@NonNull Collection<Document> documentCollection) {
    return new InMemoryCorpus(documentCollection);
  }

  /**
   * Gets format.
   *
   * @param format the format
   * @return the format
   */
  public static CorpusFormat getFormat(String format) {
    format = StringUtils.trim(format).toUpperCase();
    boolean isOPL = format.endsWith("_OPL");
    final String normFormat = format.replaceAll("_OPL$", "").trim();
    if (corpusFormats.containsKey(normFormat)) {
      return isOPL ? new OnePerLineFormat(getFormat(normFormat)) : corpusFormats.get(normFormat);
    }
    throw new IllegalArgumentException("No corpus format registered for " + format);
  }

  /**
   * Annotates this corpus with the given annotation types and returns a new corpus with the given annotation types
   * present
   *
   * @param types The annotation types to annotate
   * @return A new corpus with the given annotation types present.
   */
  public Corpus annotate(@NonNull AnnotationType... types) {
    return Pipeline.builder().addAnnotations(types).returnCorpus(true).build().process(this);
  }

  /**
   * To memory.
   *
   * @return the corpus
   */
  public Corpus cache() {
    if (this instanceof InMemoryCorpus) {
      return this;
    }
    return new InMemoryCorpus(Collect.from(this).collect(Collectors.toList()));
  }

  /**
   * Concatenate corpus.
   *
   * @param other the other
   * @return the corpus
   */
  public Corpus concatenate(@NonNull Corpus other) {
    return new ConcatenatedCorpus(this, other);
  }

  /**
   * Filter corpus.
   *
   * @param filter the filter
   * @return the corpus
   */
  public Corpus filter(@NonNull final Predicate<? super Document> filter) {
    return new FilteredCorpus(this, filter);
  }

  /**
   * Gets the first document
   *
   * @return The first document as an Optional
   */
  public Optional<Document> first() {
    return Collect.from(this).findFirst();
  }

  @Override
  public Optional<Document> get(String id) {
    return stream().filter(document -> document.getId().equals(id)).findFirst();
  }

  /**
   * Gets document factory.
   *
   * @return the document factory
   */
  public abstract DocumentFactory getDocumentFactory();

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean put(Document document) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Document> query(String query) throws ParseException {
    List<Document> documents = new ArrayList<>();
    filter(new QueryParser(QueryParser.Operator.AND).parse(query)).forEach(
        documents::add
    );
    return documents;
  }

  /**
   * Create a sample of this corpus using <a href="https://en.wikipedia.org/wiki/Reservoir_sampling">Reservoir
   * sampling</a>.
   *
   * @param count the number of documents to include in the sample
   * @return the sampled corpus
   */
  public Corpus sample(int count) {
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
  public Corpus sample(int count, @NonNull Random random) {
    if (count <= 0) {
      return Corpus.from(Collections.emptyList());
    }
    List<Document> sample = stream().limit(count).collect(Collectors.toList());
    AtomicInteger k = new AtomicInteger(count + 1);
    stream().skip(count).forEach(document -> {
      int rndIndex = random.nextInt(k.getAndIncrement());
      if (rndIndex < count) {
        sample.set(rndIndex, document);
      }
    });
    return Corpus.from(sample);
  }

  /**
   * Calculates the total term frequency of the tokens in the corpus.
   *
   * @param lemmatize True - count lemmas, False - count as is
   * @return A counter containing term frequencies of the given annotation type
   */
  public Counter<String> termFrequencies(boolean lemmatize) {
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
  public Counter<String> termFrequencies(@NonNull AnnotationType type, @NonNull Function<? super Annotation, String> toString) {
    return parallelStream()
        .flatMap(document -> document.get(type).parallelStream().map(toString))
        .collect(Counters.collector());
  }

  /**
   * Calculates the document frequency of tokens in the corpus.
   *
   * @param lemmatize True - count lemmas, False - count as is
   * @return A counter containing document frequencies of the given annotation type
   */
  public Counter<String> documentFrequencies(boolean lemmatize) {
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
  public Counter<String> documentFrequencies(@NonNull AnnotationType type, @NonNull Function<? super Annotation, String> toString) {
    return parallelStream()
        .flatMap(document -> document.get(type).parallelStream().map(toString).distinct())
        .collect(Counters.collector());
  }


  @Override
  public int size() {
    return Iterables.size(this);
  }

  /**
   * Get a stream representation of this corpus
   *
   * @return the stream
   */
  public Stream<Document> stream() {
    return Collect.from(this);
  }

  /**
   * Gets a parallel stream representation of this corpus
   *
   * @return the stream
   */
  public Stream<Document> parallelStream() {
    return Collect.parallelFrom(this);
  }

  /**
   * Writes the corpus to given the format
   *
   * @param format   the format to write in
   * @param resource the resource to write to
   * @return the corpus
   * @throws IOException something went wrong writing
   */
  public Corpus write(@NonNull String format, @NonNull Resource resource) throws IOException {
    CorpusFormat corpusFormat = getFormat(format);
    corpusFormat.write(resource, this);
    return from(format, resource, getDocumentFactory());
  }

  private static class FilteredCorpus extends Corpus {
    private static final long serialVersionUID = 1L;
    private final Corpus subCorpus;
    private final Predicate<? super Document> filter;

    private FilteredCorpus(Corpus subCorpus, Predicate<? super Document> filter) {
      this.subCorpus = subCorpus;
      this.filter = filter;
    }

    @Override
    public DocumentFactory getDocumentFactory() {
      return subCorpus.getDocumentFactory();
    }

    @Override
    public Iterator<Document> iterator() {
      return Iterators.filter(subCorpus.iterator(), filter::test);
    }

  }

  private static class ConcatenatedCorpus extends Corpus {
    private static final long serialVersionUID = 1L;
    private final Corpus subCorpus1;
    private final Corpus subCorpus2;

    private ConcatenatedCorpus(Corpus subCorpus1, Corpus subCorpus2) {
      this.subCorpus1 = subCorpus1;
      this.subCorpus2 = subCorpus2;
    }

    @Override
    public DocumentFactory getDocumentFactory() {
      return subCorpus1.getDocumentFactory();
    }

    @Override
    public Iterator<Document> iterator() {
      return Iterators.concat(subCorpus1.iterator(), subCorpus2.iterator());
    }

  }

  static {
    for (CorpusFormat format : ServiceLoader.load(CorpusFormat.class)) {
      corpusFormats.put(format.name(), format);
    }
  }

}//END OF Corpus
