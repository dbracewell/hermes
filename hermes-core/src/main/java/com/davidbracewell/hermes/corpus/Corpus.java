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

import com.davidbracewell.SystemInfo;
import com.davidbracewell.apollo.ml.Instance;
import com.davidbracewell.apollo.ml.LabeledDatum;
import com.davidbracewell.apollo.ml.data.Dataset;
import com.davidbracewell.apollo.ml.data.DatasetType;
import com.davidbracewell.apollo.ml.featurizer.Featurizer;
import com.davidbracewell.apollo.ml.sequence.Sequence;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.SequenceInput;
import com.davidbracewell.apollo.stat.measure.Association;
import com.davidbracewell.apollo.stat.measure.ContingencyTable;
import com.davidbracewell.apollo.stat.measure.ContingencyTableCalculator;
import com.davidbracewell.collection.Streams;
import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.collection.counter.Counters;
import com.davidbracewell.concurrent.Broker;
import com.davidbracewell.concurrent.IterableProducer;
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.function.SerializableConsumer;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.function.Unchecked;
import com.davidbracewell.guava.common.base.Preconditions;
import com.davidbracewell.guava.common.collect.ArrayListMultimap;
import com.davidbracewell.guava.common.collect.Multimap;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.corpus.processing.CorpusProcessor;
import com.davidbracewell.hermes.corpus.processing.ProcessorContext;
import com.davidbracewell.hermes.extraction.NGramExtractor;
import com.davidbracewell.hermes.extraction.TermExtractor;
import com.davidbracewell.hermes.filter.StopWords;
import com.davidbracewell.hermes.lexicon.Lexicon;
import com.davidbracewell.io.AsyncWriter;
import com.davidbracewell.io.MultiFileWriter;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Loggable;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.stream.StreamingContext;
import com.davidbracewell.stream.accumulator.MLongAccumulator;
import com.davidbracewell.tuple.Tuple;
import lombok.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
public interface Corpus extends Iterable<Document>, AutoCloseable, Loggable {


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
      return new InMemoryCorpus(Streams.asStream(documentIterable).collect(Collectors.toList()));
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
    * Applies a lexicon to the corpus creating annotations of the given type for matches.
    *
    * @param lexicon the lexicon to match
    * @param type    the annotation type to give the matches
    * @return the corpus
    */
   default Corpus applyLexicon(@NonNull Lexicon lexicon, @NonNull AnnotationType type) {
      return map(doc -> {
         if (!doc.isCompleted(type)) {
            lexicon.match(doc).forEach(match -> doc.annotationBuilder()
                                                   .type(type)
                                                   .bounds(match)
                                                   .attributes(match)
                                                   .createAttached());
         }
         return doc;
      });
   }

   /**
    * As classification data set dataset.
    *
    * @param featurizer the featurizer
    * @return the dataset
    */
   default Dataset<Instance> asClassificationDataSet(@NonNull Featurizer<HString> featurizer) {
      return Dataset.classification().type(getDataSetType()).source(stream().map(featurizer::extractInstance));
   }

   /**
    * As classification data set dataset.
    *
    * @param featurizer         the featurizer
    * @param labelAttributeType the label attribute
    * @return the dataset
    */
   default Dataset<Instance> asClassificationDataSet(@NonNull Featurizer<HString> featurizer, @NonNull AttributeType labelAttributeType) {
      return Dataset
                .classification()
                .type(getDataSetType())
                .source(asLabeledStream(labelAttributeType).map(featurizer::extractInstance))
         ;
   }

   /**
    * As classification data set dataset.
    *
    * @param featurizer    the featurizer
    * @param labelFunction the label function
    * @return the dataset
    */
   default Dataset<Instance> asClassificationDataSet(@NonNull Featurizer<HString> featurizer, @NonNull SerializableFunction<HString, Object> labelFunction) {
      return Dataset
                .classification()
                .type(getDataSetType())
                .source(asLabeledStream(labelFunction).map(featurizer::extractInstance));
   }

   /**
    * As embedding dataset dataset.
    *
    * @return the dataset
    */
   default Dataset<Sequence> asEmbeddingDataset() {
      return asEmbeddingDataset(Types.TOKEN);
   }

   /**
    * As embedding dataset dataset.
    *
    * @param types the types
    * @return the dataset
    */
   default Dataset<Sequence> asEmbeddingDataset(@NonNull AnnotationType... types) {
      return Dataset.embedding(getDataSetType(),
                               stream().parallel()
                                       .flatMap(document -> {
                                          List<List<String>> sentences = new ArrayList<>();
                                          document.sentences()
                                                  .forEach(sentence ->
                                                              sentences.add(
                                                                 sentence.interleaved(types)
                                                                         .stream()
                                                                         .filter(StopWords.isNotStopWord())
                                                                         .map(HString::getLemma)
                                                                         .collect(Collectors.toList()))
                                                          );
                                          return sentences.stream();
                                       }),
                               Collection::stream);
   }

   default MStream<Instance> asInstanceStream(@NonNull Featurizer<HString> featurizer) {
      return stream().map(featurizer::extractInstance);
   }

   default MStream<Instance> asInstanceStream(@NonNull Featurizer<HString> featurizer, @NonNull AttributeType labelAttributeType) {
      return asLabeledStream(labelAttributeType).map(featurizer::extractInstance);
   }

   default MStream<Instance> asInstanceStream(@NonNull Featurizer<HString> featurizer, @NonNull SerializableFunction<HString, ?> labelFunction) {
      return asLabeledStream(labelFunction).map(featurizer::extractInstance);
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
    * As regression data set dataset.
    *
    * @param featurizer the featurizer
    * @return the dataset
    */
   default Dataset<Instance> asRegressionDataSet(@NonNull Featurizer<HString> featurizer) {
      return Dataset.regression().type(getDataSetType()).source(stream().map(featurizer::extractInstance));
   }

   /**
    * As regression data set dataset.
    *
    * @param featurizer         the featurizer
    * @param labelAttributeType the label attribute
    * @return the dataset
    */
   default Dataset<Instance> asRegressionDataSet(@NonNull Featurizer<HString> featurizer, @NonNull AttributeType labelAttributeType) {
      return Dataset
                .regression()
                .type(getDataSetType())
                .source(asLabeledStream(labelAttributeType).map(featurizer::extractInstance));
   }

   /**
    * As regression data set dataset.
    *
    * @param featurizer    the featurizer
    * @param labelFunction the label function
    * @return the dataset
    */
   default Dataset<Instance> asRegressionDataSet(@NonNull Featurizer<HString> featurizer, @NonNull SerializableFunction<HString, Double> labelFunction) {
      return Dataset
                .regression()
                .type(getDataSetType())
                .source(asLabeledStream(labelFunction).map(featurizer::extractInstance))
         ;
   }

   /**
    * As sequence data set dataset.
    *
    * @param featurizer the featurizer
    * @return the dataset
    */
   default Dataset<Sequence> asSequenceDataSet(@NonNull SequenceFeaturizer<? super Annotation> featurizer) {
      return Dataset
                .sequence()
                .type(DatasetType.InMemory)
                .source(asSequenceStream().map(seq -> featurizer.extractSequence(seq.iterator())))
         ;
   }

   /**
    * As sequence data set dataset.
    *
    * @param sequenceType the sequence type
    * @param featurizer   the featurizer
    * @return the dataset
    */
   default Dataset<Sequence> asSequenceDataSet(@NonNull AnnotationType sequenceType, @NonNull SequenceFeaturizer<? super Annotation> featurizer) {
      return Dataset
                .sequence()
                .type(getDataSetType())
                .source(asSequenceStream(sequenceType).map(seq -> featurizer.extractSequence(seq.iterator())))
         ;
   }

   /**
    * As sequence data set dataset.
    *
    * @param labelFunction the label function
    * @param featurizer    the featurizer
    * @return the dataset
    */
   default Dataset<Sequence> asSequenceDataSet(@NonNull Function<? super Annotation, String> labelFunction, @NonNull SequenceFeaturizer<? super Annotation> featurizer) {
      return Dataset
                .sequence()
                .type(getDataSetType())
                .source(asSequenceStream(labelFunction).map(seq -> featurizer.extractSequence(seq.iterator())))
         ;
   }

   /**
    * As sequence data set dataset.
    *
    * @param sequenceType  the sequence type
    * @param labelFunction the label function
    * @param featurizer    the featurizer
    * @return the dataset
    */
   default Dataset<Sequence> asSequenceDataSet(@NonNull AnnotationType sequenceType, @NonNull Function<? super Annotation, String> labelFunction, @NonNull SequenceFeaturizer<? super Annotation> featurizer) {
      return Dataset
                .sequence()
                .type(getDataSetType())
                .source(
                   asSequenceStream(sequenceType, labelFunction).map(seq -> featurizer.extractSequence(seq.iterator())))
         ;
   }

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
      return stream().flatMap(doc -> doc.get(sequenceType).stream()).map(HString::asSequence);
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
      return stream().flatMap(doc -> doc.get(sequenceType).stream()).map(hs -> hs.asSequence(labelFunction));
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
      return new InMemoryCorpus(Streams.asStream(this).collect(Collectors.toList()));
   }

   /**
    * Calculates the document frequency of tokens in the corpus.
    *
    * @return A counter containing document frequencies of the given annotation type
    */
   default Counter<String> documentFrequencies() {
      return documentFrequencies(TermExtractor.create().lowerCase());
   }

   /**
    * Calculates the document frequency of annotations of the given annotation type in the corpus. Annotations are
    * transformed into strings using the given toString function.
    *
    * @param termExtractor the term spec
    * @return A counter containing document frequencies of the given annotation type
    */
   default Counter<String> documentFrequencies(@NonNull TermExtractor termExtractor) {
      MLongAccumulator counter = getStreamingContext().longAccumulator();
      return termExtractor.getValueCalculator()
                          .adjust(Counters.newCounter(stream().parallel()
                                                              .flatMap(
                                                                 doc -> {
                                                                    counter.add(1);
                                                                    counter.report(count -> count % 5_000 == 0,
                                                                                   count -> logFine(
                                                                                      "documentFrequencies: Processed {0} documents",
                                                                                      count));
                                                                    return termExtractor.stream(doc).distinct();
                                                                 })
                                                              .countByValue()));
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
    * For each parallel.
    *
    * @param consumer the consumer
    */
   default void forEachParallel(@NonNull SerializableConsumer<? super Document> consumer) {
      stream().parallel().forEach(consumer);
   }

   /**
    * Gets corpus type.
    *
    * @return the corpus type
    */
   CorpusType getCorpusType();

   /**
    * Gets data set type.
    *
    * @return the data set type
    */
   default DatasetType getDataSetType() {
      if (isInMemory()) {
         return DatasetType.InMemory;
      } else if (isDistributed()) {
         return DatasetType.Distributed;
      }
      return DatasetType.OffHeap;
   }

   /**
    * Gets document factory.
    *
    * @return the document factory
    */
   DocumentFactory getDocumentFactory();

   /**
    * Gets streaming context.
    *
    * @return the streaming context
    */
   default StreamingContext getStreamingContext() {
      return getCorpusType().getStreamingContext();
   }

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
    * Is distributed boolean.
    *
    * @return the boolean
    */
   default boolean isDistributed() {
      return false;
   }

   /**
    * Is empty boolean.
    *
    * @return the boolean
    */
   default boolean isEmpty() {
      return stream().isEmpty();
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
    * Is off heap boolean.
    *
    * @return the boolean
    */
   default boolean isOffHeap() {
      return false;
   }

   @Override
   default Iterator<Document> iterator() {
      return stream().iterator();
   }

   /**
    * Map corpus.
    *
    * @param function the function
    * @return the corpus
    */
   Corpus map(SerializableFunction<Document, Document> function);

   /**
    * Ngrams counter.
    *
    * @param nGramExtractor the n gram spec
    * @return the counter
    */
   default Counter<Tuple> nGramFrequencies(@NonNull NGramExtractor nGramExtractor) {
      MLongAccumulator counter = getStreamingContext().longAccumulator();
      return nGramExtractor.getValueCalculator().adjust(Counters.newCounter(
         stream().parallel().flatMap(doc -> {
                                        counter.add(1);
                                        counter.report(count -> count % 5_000 == 0,
                                                       count -> logFine("nGramCounts: Processed {0} documents", count));
                                        return nGramExtractor.streamTuples(doc);
                                     }
                                    ).countByValue()));
   }

   /**
    * Process corpus.
    *
    * @param processor the processor
    * @return the corpus
    * @throws Exception the exception
    */
   default Corpus process(@NonNull CorpusProcessor processor) throws Exception {
      return processor.process(this, new ProcessorContext());
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
    * Repartition corpus.
    *
    * @param numPartitions the num partitions
    * @return the corpus
    */
   default Corpus repartition(int numPartitions) {
      return this;
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
    * Significant bigrams counter.
    *
    * @param nGramExtractor the term spec
    * @param minCount       the min count
    * @param minScore       the min score
    * @return the counter
    */
   default Counter<Tuple> significantBigrams(@NonNull NGramExtractor nGramExtractor, int minCount, double minScore) {
      return significantBigrams(nGramExtractor, minCount, Association.Mikolov, minScore);
   }

   /**
    * Significant bigrams counter.
    *
    * @param nGramExtractor the n gram spec
    * @param minCount       the min count
    * @param calculator     the calculator
    * @param minScore       the min score
    * @return the counter
    */
   default Counter<Tuple> significantBigrams(@NonNull NGramExtractor nGramExtractor, int minCount, @NonNull ContingencyTableCalculator calculator, double minScore) {
      Counter<Tuple> ngrams = nGramFrequencies(nGramExtractor.min(1).max(2))
                                 .filterByValue(v -> v >= minCount);
      Counter<Tuple> unigrams = ngrams.filterByKey(t -> t.degree() == 1);
      Counter<Tuple> bigrams = ngrams.filterByKey(t -> t.degree() == 2);
      ngrams.clear();
      Counter<Tuple> filtered = Counters.newCounter();
      bigrams.items().forEach(bigram -> {
         double score = calculator.calculate(ContingencyTable.create2X2(bigrams.get(bigram),
                                                                        unigrams.get(bigram.slice(0, 1)),
                                                                        unigrams.get(bigram.slice(1, 2)),
                                                                        unigrams.sum()));
         if (score >= minScore) {
            filtered.set(bigram, score);
         }
      });
      return filtered;
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
    * Terms counter.
    *
    * @return the counter
    */
   default Counter<String> termFrequencies() {
      return termFrequencies(TermExtractor.create());
   }

   /**
    * Terms counter.
    *
    * @param termExtractor the term spec
    * @return the counter
    */
   default Counter<String> termFrequencies(@NonNull TermExtractor termExtractor) {
      MLongAccumulator counter = getStreamingContext().longAccumulator();
      return termExtractor.getValueCalculator().adjust(Counters.newCounter(
         stream().parallel().flatMap(doc -> {
                                        counter.add(1);
                                        counter.report(count -> count % 5_000 == 0,
                                                       count -> logFine("termCounts: Processed {0} documents", count));
                                        return termExtractor.stream(doc);
                                     }
                                    ).countByValue()));
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
      if (format.isOnePerLine()) {
         if ((resource.exists() && resource.isDirectory()) || (!resource.exists() && !resource.path().contains("."))) {
            try (MultiFileWriter writer = new MultiFileWriter(resource, "part-",
                                                              Config.get("files.partition").asIntegerValue(10))) {
               Broker.<Document>builder()
                  .addProducer(new IterableProducer<>(this))
                  .addConsumer(Unchecked.consumer(document -> writer.write(format.toString(document).trim() + "\n")),
                               SystemInfo.NUMBER_OF_PROCESSORS - 1)
                  .build().run();
            } catch (RuntimeException re) {
               if (re.getCause() instanceof IOException) {
                  throw Cast.<IOException>as(re.getCause());
               }
               throw re;
            }
         } else {
            try (AsyncWriter writer = new AsyncWriter(resource.writer())) {
               Broker.<Document>builder()
                  .addProducer(new IterableProducer<>(this))
                  .addConsumer(Unchecked.consumer(document -> writer.write(format.toString(document).trim() + "\n")),
                               SystemInfo.NUMBER_OF_PROCESSORS - 1)
                  .build().run();
            } catch (RuntimeException re) {
               if (re.getCause() instanceof IOException) {
                  throw Cast.<IOException>as(re.getCause());
               }
               throw re;
            }
         }
      } else {
         //None one-per-line formats require multiple files
         Preconditions.checkArgument(!resource.exists() || resource.isDirectory(), "Must specify a directory");
         try {
            Broker.<Document>builder()
               .addProducer(new IterableProducer<>(this))
               .addConsumer(Unchecked.consumer(document ->
                                                  resource.getChild(document.getId() + "." + format.extension())
                                                          .write(format.toString(document))

                                              ), SystemInfo.NUMBER_OF_PROCESSORS - 1)
               .build().run();
         } catch (RuntimeException re) {
            if (re.getCause() instanceof IOException) {
               throw Cast.<IOException>as(re.getCause());
            }
            throw re;
         }
      }
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

}//END OF Corpus
