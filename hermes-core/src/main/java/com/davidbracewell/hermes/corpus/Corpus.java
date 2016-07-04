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

import com.davidbracewell.apollo.ml.Dataset;
import com.davidbracewell.apollo.ml.Featurizer;
import com.davidbracewell.apollo.ml.Instance;
import com.davidbracewell.apollo.ml.LabeledDatum;
import com.davidbracewell.apollo.ml.sequence.Sequence;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.SequenceInput;
import com.davidbracewell.apollo.stats.ContingencyTable;
import com.davidbracewell.apollo.stats.ContingencyTableCalculator;
import com.davidbracewell.collection.Collect;
import com.davidbracewell.collection.Counter;
import com.davidbracewell.collection.HashMapCounter;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.*;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.stream.StreamingContext;
import com.davidbracewell.tuple.Tuple;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.davidbracewell.tuple.Tuples.$;

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
   * Of corpus.
   *
   * @param documentStream the document stream
   * @return the corpus
   */
  static Corpus of(@NonNull Stream<Document> documentStream) {
    return new InMemoryCorpus(documentStream.collect(Collectors.toList()));
  }

  /**
   * Of corpus.
   *
   * @param documentStream the document stream
   * @return the corpus
   */
  static Corpus of(@NonNull MStream<Document> documentStream) {
    return new InMemoryCorpus(documentStream.collect());
  }


  /**
   * Of corpus.
   *
   * @param documentIterable the document iterable
   * @return the corpus
   */
  static Corpus of(@NonNull Iterable<Document> documentIterable) {
    return new InMemoryCorpus(Collect.stream(documentIterable).collect(Collectors.toList()));
  }

  /**
   * Of corpus.
   *
   * @param documents the document iterable
   * @return the corpus
   */
  static Corpus of(@NonNull Document... documents) {
    return new InMemoryCorpus(new ArrayList<>(Arrays.asList(documents)));
  }

  /**
   * Of corpus.
   *
   * @param documentCollection the document collection
   * @return the corpus
   */
  static Corpus of(@NonNull Collection<Document> documentCollection) {
    return new InMemoryCorpus(documentCollection);
  }


  /**
   * Annotates this corpus with the given annotation types and returns a new corpus with the given annotation types
   * present
   *
   * @param types The annotation types to annotate
   * @return A new corpus with the given annotation types present.
   */
  Corpus annotate(AnnotatableType... types);


  /**
   * As sequence stream m stream.
   *
   * @return the m stream
   */
  default MStream<SequenceInput<Annotation>> asSequenceStream() {
    return asSequenceStream(Types.SENTENCE);
  }

  /**
   * As sequence stream m stream.
   *
   * @param sequenceType the sequence type
   * @return the m stream
   */
  default MStream<SequenceInput<Annotation>> asSequenceStream(@NonNull AnnotationType sequenceType) {
    return stream().flatMap(doc -> doc.get(sequenceType)).map(HString::asSequence);
  }

  /**
   * As sequence stream m stream.
   *
   * @param labelFunction the label function
   * @return the m stream
   */
  default MStream<SequenceInput<Annotation>> asSequenceStream(@NonNull Function<? super Annotation, String> labelFunction) {
    return asSequenceStream(Types.SENTENCE, labelFunction);
  }

  /**
   * As sequence stream m stream.
   *
   * @param sequenceType  the sequence type
   * @param labelFunction the label function
   * @return the m stream
   */
  default MStream<SequenceInput<Annotation>> asSequenceStream(@NonNull AnnotationType sequenceType, @NonNull Function<? super Annotation, String> labelFunction) {
    return stream().flatMap(doc -> doc.get(sequenceType)).map(hs -> hs.asSequence(labelFunction));
  }

  /**
   * As labeled stream m stream.
   *
   * @param labelFunction the label function
   * @return the m stream
   */
  default MStream<LabeledDatum<HString>> asLabeledStream(@NonNull SerializableFunction<HString, ?> labelFunction) {
    return stream().map(hs -> hs.asLabeledData(labelFunction));
  }

  /**
   * As labeled stream m stream.
   *
   * @param labelAttributeType the label attribute
   * @return the m stream
   */
  default MStream<LabeledDatum<HString>> asLabeledStream(@NonNull AttributeType labelAttributeType) {
    return stream().map(hs -> hs.asLabeledData(labelAttributeType));
  }

  /**
   * As classification data set dataset.
   *
   * @param featurizer the featurizer
   * @return the dataset
   */
  default Dataset<Instance> asClassificationDataSet(@NonNull Featurizer<HString> featurizer) {
    return Dataset.classification().type(getDataSetType()).source(stream().map(featurizer::extract)).build();
  }

  /**
   * As sequence data set dataset.
   *
   * @param featurizer the featurizer
   * @return the dataset
   */
  default Dataset<Sequence> asSequenceDataSet(@NonNull SequenceFeaturizer<Annotation> featurizer) {
    return Dataset.sequence().type(Dataset.Type.InMemory).source(asSequenceStream().map(seq -> featurizer.extractSequence(seq.iterator()))).build();
  }

  /**
   * As sequence data set dataset.
   *
   * @param sequenceType the sequence type
   * @param featurizer   the featurizer
   * @return the dataset
   */
  default Dataset<Sequence> asSequenceDataSet(@NonNull AnnotationType sequenceType, @NonNull SequenceFeaturizer<Annotation> featurizer) {
    return Dataset.sequence().type(getDataSetType()).source(asSequenceStream(sequenceType).map(seq -> featurizer.extractSequence(seq.iterator()))).build();
  }

  /**
   * As sequence data set dataset.
   *
   * @param labelFunction the label function
   * @param featurizer    the featurizer
   * @return the dataset
   */
  default Dataset<Sequence> asSequenceDataSet(@NonNull Function<? super Annotation, String> labelFunction, @NonNull SequenceFeaturizer<Annotation> featurizer) {
    return Dataset.sequence().type(getDataSetType()).source(asSequenceStream(labelFunction).map(seq -> featurizer.extractSequence(seq.iterator()))).build();
  }

  /**
   * As sequence data set dataset.
   *
   * @param sequenceType  the sequence type
   * @param labelFunction the label function
   * @param featurizer    the featurizer
   * @return the dataset
   */
  default Dataset<Sequence> asSequenceDataSet(@NonNull AnnotationType sequenceType, @NonNull Function<? super Annotation, String> labelFunction, @NonNull SequenceFeaturizer<Annotation> featurizer) {
    return Dataset.sequence().type(getDataSetType()).source(asSequenceStream(sequenceType, labelFunction).map(seq -> featurizer.extractSequence(seq.iterator()))).build();
  }

  /**
   * As regression data set dataset.
   *
   * @param featurizer the featurizer
   * @return the dataset
   */
  default Dataset<Instance> asRegressionDataSet(@NonNull Featurizer<HString> featurizer) {
    return Dataset.regression().type(getDataSetType()).source(stream().map(featurizer::extract)).build();
  }

  /**
   * As classification data set dataset.
   *
   * @param featurizer         the featurizer
   * @param labelAttributeType the label attribute
   * @return the dataset
   */
  default Dataset<Instance> asClassificationDataSet(@NonNull Featurizer<HString> featurizer, @NonNull AttributeType labelAttributeType) {
    return Dataset.classification().type(getDataSetType()).source(asLabeledStream(labelAttributeType).map(featurizer::extractLabeled)).build();
  }

  /**
   * As regression data set dataset.
   *
   * @param featurizer         the featurizer
   * @param labelAttributeType the label attribute
   * @return the dataset
   */
  default Dataset<Instance> asRegressionDataSet(@NonNull Featurizer<HString> featurizer, @NonNull AttributeType labelAttributeType) {
    return Dataset.regression().type(getDataSetType()).source(asLabeledStream(labelAttributeType).map(featurizer::extractLabeled)).build();
  }

  /**
   * As classification data set dataset.
   *
   * @param featurizer    the featurizer
   * @param labelFunction the label function
   * @return the dataset
   */
  default Dataset<Instance> asClassificationDataSet(@NonNull Featurizer<HString> featurizer, @NonNull SerializableFunction<HString, Object> labelFunction) {
    return Dataset.classification().type(getDataSetType()).source(asLabeledStream(labelFunction).map(featurizer::extractLabeled)).build();
  }

  /**
   * As regression data set dataset.
   *
   * @param featurizer    the featurizer
   * @param labelFunction the label function
   * @return the dataset
   */
  default Dataset<Instance> asRegressionDataSet(@NonNull Featurizer<HString> featurizer, @NonNull SerializableFunction<HString, Double> labelFunction) {
    return Dataset.regression().type(getDataSetType()).source(asLabeledStream(labelFunction).map(featurizer::extractLabeled)).build();
  }


  /**
   * Map corpus.
   *
   * @param function the function
   * @return the corpus
   */
  Corpus map(SerializableFunction<Document, Document> function);

  /**
   * Gets data set type.
   *
   * @return the data set type
   */
  default Dataset.Type getDataSetType() {
    if (isInMemory()) {
      return Dataset.Type.InMemory;
    } else if (isDistributed()) {
      return Dataset.Type.Distributed;
    }
    return Dataset.Type.OffHeap;
  }

  /**
   * To memory.
   *
   * @return the corpus
   */
  default Corpus cache() {
    if (this instanceof InMemoryCorpus) {
      return this;
    }
    return new InMemoryCorpus(Collect.stream(this).collect(Collectors.toList()));
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
    return new HashMapCounter<>(
      Cast.cast(
        stream()
          .flatMap(document -> document.get(type).stream().map(toString).distinct().collect(Collectors.toList()))
          .countByValue()
      )
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
   * @param size the number of documents to include in the sample
   * @return the sampled corpus
   */
  default Corpus sample(int size) {
    return sample(size, new Random());
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


  /**
   * Ngrams counter.
   *
   * @param nGramSpec the n gram spec
   * @return the counter
   */
  default Counter<Tuple> ngrams(@NonNull NGramSpec nGramSpec) {
    return nGramSpec.getValueCalculator().adjust(new HashMapCounter<>(
      stream().flatMap(doc ->
        doc.ngrams(nGramSpec.getAnnotationType(), nGramSpec.getMin(), nGramSpec.getMax())
          .stream()
          .filter(nGramSpec.getFilter())
          .map(
            hString -> $(
              hString.get(nGramSpec.getAnnotationType())
                .stream()
                .map(nGramSpec.getToStringFunction())
                .collect(Collectors.toList())
            )
          ).collect(Collectors.toList())
      ).countByValue()
    ));
  }

  /**
   * Terms counter.
   *
   * @return the counter
   */
  default Counter<String> terms() {
    return terms(TermSpec.create());
  }

  /**
   * Terms counter.
   *
   * @param termSpec the term spec
   * @return the counter
   */
  default Counter<String> terms(@NonNull TermSpec termSpec) {
    return termSpec.getValueCalculator().adjust(
      new HashMapCounter<>(
        stream().flatMap(doc -> doc.get(termSpec.getAnnotationType()).stream()
          .filter(termSpec.getFilter())
          .map(termSpec.getToStringFunction())
          .collect(Collectors.toList())
        ).countByValue()
      )
    );
  }

  /**
   * Significant bigrams counter.
   *
   * @param minCount        the min count
   * @param calculator      the calculator
   * @param minScore        the min score
   * @param removeStopWords the remove stop words
   * @param lemmatize       the lemmatize
   * @return the counter
   */
  default Counter<Tuple> significantBigrams(int minCount, @NonNull ContingencyTableCalculator calculator, double minScore, boolean removeStopWords, boolean lemmatize) {
    return significantBigrams(minCount, calculator, minScore, removeStopWords, h -> lemmatize ? h.getLemma() : h.toString());
  }

  /**
   * Significant bigrams counter.
   *
   * @param minCount        the min count
   * @param calculator      the calculator
   * @param minScore        the min score
   * @param removeStopWords the remove stop words
   * @param toString        the to string
   * @return the counter
   */
  default Counter<Tuple> significantBigrams(int minCount, @NonNull ContingencyTableCalculator calculator, double minScore, boolean removeStopWords, @NonNull SerializableFunction<? super Annotation, String> toString) {
    Counter<Tuple> unigrams = ngrams(NGramSpec.create().order(1));
    Counter<Tuple> bigrams = ngrams(NGramSpec.create().order(2)).filterByValue(v -> v >= minCount);
    Counter<Tuple> filtered = new HashMapCounter<>();
    bigrams.items().forEach(bigram -> {
      double score = calculator.calculate(
        ContingencyTable.create2X2(bigrams.get(bigram),
          unigrams.get(bigram.slice(0, 1)),
          unigrams.get(bigram.slice(1, 2)),
          unigrams.sum()
        )
      );
      if (score >= minScore) {
        filtered.set(bigram, score);
      }
    });
    return filtered;
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
    return write(CorpusFormats.forName(format), resource);
  }

  /**
   * Write corpus.
   *
   * @param format   the format
   * @param resource the resource
   * @return the corpus
   * @throws IOException the io exception
   */
  default Corpus write(@NonNull CorpusFormat format, @NonNull Resource resource) throws IOException {
    format.write(resource, this);
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
    return write(CorpusFormats.JSON_OPL, resource);
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
   * @param format   the format
   * @param resource the resource
   * @return the corpus
   * @throws IOException the io exception
   */
  default Corpus write(@NonNull CorpusFormat format, @NonNull String resource) throws IOException {
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
    return write(CorpusFormats.JSON_OPL, resource);
  }

  /**
   * Update config corpus.
   *
   * @return the corpus
   */
  default Corpus updateConfig() {
    return this;
  }

  /**
   * Repartition corpus.
   *
   * @param numPartitions the num partitions
   * @return the corpus
   */
  default Corpus repartition(int numPartitions) {
    return this;
  }

  /**
   * Is in memory boolean.
   *
   * @return the boolean
   */
  default boolean isInMemory() {
    return false;
  }

  /**
   * Is distributed boolean.
   *
   * @return the boolean
   */
  default boolean isDistributed() {
    return false;
  }

  /**
   * Is off heap boolean.
   *
   * @return the boolean
   */
  default boolean isOffHeap() {
    return false;
  }


  default StreamingContext getStreamingContext() {
    return StreamingContext.local();
  }

}//END OF Corpus
