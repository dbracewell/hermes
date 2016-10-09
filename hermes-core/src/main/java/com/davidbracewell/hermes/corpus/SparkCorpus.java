package com.davidbracewell.hermes.corpus;

import com.davidbracewell.config.Config;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Hermes;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.resource.StringResource;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.stream.SparkStream;
import com.davidbracewell.stream.SparkStreamingContext;
import com.davidbracewell.stream.StreamingContext;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.broadcast.Broadcast;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;


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
    * @param corpusFormat    the document format
    * @param documentFactory the document factory
    */
   @SuppressWarnings("unchecked")
   public SparkCorpus(@NonNull String corpusLocation, @NonNull CorpusFormat corpusFormat, @NonNull DocumentFactory documentFactory) {
      if (corpusFormat.isOnePerLine() && corpusFormat.extension().toUpperCase().startsWith("JSON")) {
         this.stream = new SparkDocumentStream(StreamingContext.distributed().textFile(corpusLocation));
      } else if (corpusFormat.isOnePerLine()) {
         this.stream = new SparkDocumentStream(StreamingContext.distributed().textFile(corpusLocation).flatMap(
            line -> corpusFormat.create(Resources.fromString(line), documentFactory)
                                .stream()
                                .map(Document::toJson)
                                .javaStream()
                                                                                                              )
         );
      } else {
         Broadcast<Config> configBroadcast = SparkStreamingContext.INSTANCE.broadcast(Config.getInstance());
         JavaRDD<String> rdd = StreamingContext.distributed().sparkContext()
                                               .wholeTextFiles(corpusLocation)
                                               .values()
                                               .flatMap(str -> {
                                                  Hermes.initializeWorker(configBroadcast.getValue());
                                                  return corpusFormat.create(Resources.fromString(str), documentFactory)
                                                                     .stream()
                                                                     .map(Document::toJson)
                                                                     .iterator();
                                               });
         this.stream = new SparkDocumentStream(new SparkStream<>(rdd));
      }
   }

   @Override
   public CorpusType getCorpusType() {
      return CorpusType.DISTRIBUTED;
   }

   /**
    * Repartition.
    *
    * @param numPartitions the num partitions
    */
   @Override
   public Corpus repartition(int numPartitions) {
      stream.repartition(numPartitions);
      return this;
   }

   /**
    * Instantiates a new Spark corpus.
    *
    * @param documents the documents
    */
   public SparkCorpus(@NonNull Collection<Document> documents) {
      this.stream = new SparkDocumentStream(StreamingContext.distributed().stream(
         documents.stream().map(Document::toJson)
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
   public Corpus annotate(@NonNull AnnotatableType... types) {
      return new SparkCorpus(stream.annotate(types));
   }

   @Override
   public Corpus cache() {
      return new SparkCorpus(new SparkDocumentStream(stream.getSource().cache()));
   }

   @Override
   public Corpus sample(int size) {
      return new MStreamCorpus(stream.sample(false, size), getDocumentFactory());
   }

   @Override
   public Corpus write(@NonNull String format, @NonNull Resource resource) throws IOException {
      return write(format, resource.descriptor());
   }

   @Override
   public Corpus write(@NonNull String format, @NonNull String resource) throws IOException {
      CorpusFormat outFormat = CorpusFormats.forName(format);
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
   public boolean isDistributed() {
      return true;
   }

   @Override
   public Corpus map(@NonNull SerializableFunction<Document, Document> function) {
      return new SparkCorpus(new SparkDocumentStream(stream.map(d -> function.apply(d).toJson())));
   }

   @Override
   public StreamingContext getStreamingContext() {
      return stream.getContext();
   }

}//END OF SparkCorpus

