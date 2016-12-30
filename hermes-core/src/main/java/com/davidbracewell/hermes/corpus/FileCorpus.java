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

import com.davidbracewell.collection.Streams;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.Unchecked;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Pipeline;
import com.davidbracewell.io.AsyncWriter;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Logger;
import com.davidbracewell.stream.MStream;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * <p>
 * An implementation of <code>Corpus</code> that recursively reads files from disk. The full corpus is never loaded
 * into memory. File based corpora are read only, i.e. they do not support the <code>put</code> operator.
 * </p>
 *
 * @author David B. Bracewell
 */
public class FileCorpus implements Corpus, Serializable {
   private static final long serialVersionUID = 1L;
   private static final Logger log = Logger.getLogger(FileCorpus.class);

   private final CorpusFormat corpusFormat;
   private final Resource resource;
   private final DocumentFactory documentFactory;
   private long size = -1;

   /**
    * Instantiates a new file based corpus
    *
    * @param corpusFormat    the corpus format
    * @param resource        the resource containing the corpus
    * @param documentFactory the document factory to use when constructing documents
    */
   public FileCorpus(@NonNull CorpusFormat corpusFormat, @NonNull Resource resource, @NonNull DocumentFactory documentFactory) {
      this.corpusFormat = corpusFormat;
      this.resource = resource;
      this.documentFactory = documentFactory;
   }

   @Override
   public void close() throws Exception {

   }

   @Override
   public CorpusType getCorpusType() {
      return CorpusType.OFF_HEAP;
   }

//   @Override
//   public void forEachParallel(@NonNull SerializableConsumer<? super Document> consumer) {
//      StreamSupport.stream(spliterator(),true).forEach(consumer);
//   }


   private class RSI implements Spliterator<Document> {
      List<Resource> resources;
      int start;
      int end;
      Iterator<Document> documentIterator;

      public RSI(Resource r) {
         if (r.isDirectory()) {
            resources = r.getChildren(true);
         } else {
            resources = Collections.singletonList(r);
         }
         this.start = 0;
         this.end = resources.size();
      }

      public RSI(List<Resource> r, int start, int end) {
         this.resources = r;
         this.start = start;
         this.end = end;
      }

      @Override
      public boolean tryAdvance(Consumer<? super Document> action) {
         if (documentIterator != null && documentIterator.hasNext()) {
            action.accept(documentIterator.next());
            return true;
         }

         while (start < end && (documentIterator == null || !documentIterator.hasNext())) {
            documentIterator = new RecursiveDocumentIterator(resources.get(start), documentFactory,
                                                             (resource1, documentFactory1) -> {
                                                                try {
                                                                   return corpusFormat.read(resource1,
                                                                                            documentFactory1);
                                                                } catch (IOException e) {
                                                                   log.warn("Error reading {0} : {1}", resource1, e);
                                                                   return Collections.emptyList();
                                                                }
                                                             });
            start++;
            if (documentIterator.hasNext()) {
               action.accept(documentIterator.next());
               return true;
            }
         }
         return false;
      }

      @Override
      public Spliterator<Document> trySplit() {
         int low = start;
         int high = ((low + end) >>> 1) & ~1;
         if (low < high) {
            this.start = high;
            return new RSI(resources, low, high);
         }
         return null;
      }

      @Override
      public long estimateSize() {
         return (long) ((end - start) / 2.0);
      }

      @Override
      public int characteristics() {
         return NONNULL;
      }
   }

   @Override
   public Spliterator<Document> spliterator() {
      return new RSI(resource);
   }

   @Override
   public Iterator<Document> iterator() {
      return new RecursiveDocumentIterator(resource, documentFactory, ((resource1, documentFactory1) -> {
         try {
            return corpusFormat.read(resource1, documentFactory1);
         } catch (IOException e) {
            log.warn("Error reading {0} : {1}", resource1, e);
            return Collections.emptyList();
         }
      }
      ));
   }

   @Override
   public Corpus write(@NonNull String format, @NonNull Resource resource) throws IOException {
      CorpusFormat corpusFormat = CorpusFormats.forName(format);
      if (corpusFormat.name().equals(this.corpusFormat.name())) {
         if ((resource.exists() && resource.isDirectory()) || (!resource.exists() && !resource.path().contains("."))) {
            this.resource.copy(resource);
         } else {
            try (AsyncWriter writer = new AsyncWriter(resource.writer())) {
               for (Resource child : this.resource.getChildren()) {
                  try (MStream<String> lines = child.lines()) {
                     lines.forEach(Unchecked.consumer(writer::write));
                  } catch (RuntimeException re) {
                     throw new IOException(re.getCause());
                  } catch (Exception e) {
                     throw new IOException(e);
                  }
               }
            }
         }
         return Corpus.builder().source(resource).format(corpusFormat).build();
      } else {
         return Corpus.super.write(format, resource);
      }
   }


   @Override
   public MStream<Document> stream() {
      return getStreamingContext().stream(StreamSupport.stream(spliterator(), true));
   }

   @Override
   public Corpus annotate(@NonNull AnnotatableType... types) {
      return Pipeline.builder().addAnnotations(types).returnCorpus(true).build().process(this);
   }

   @Override
   public DocumentFactory getDocumentFactory() {
      return documentFactory;
   }

   @Override
   public long size() {
      if (size == -1) {
         if (corpusFormat.isOnePerLine()) {
            Iterator<Resource> itr = resource.isDirectory() ?
                                     resource.childIterator(true) :
                                     Collections.singleton(resource).iterator();
            AtomicInteger sz = new AtomicInteger(0);
            Streams.asParallelStream(itr).forEach(r -> {
               try (MStream<String> lineStream = r.lines()) {
                  lineStream.forEach(l -> sz.addAndGet(1));
               } catch (Exception e) {
                  throw Throwables.propagate(e);
               }
            });
            size = sz.get();
         } else {
            size = stream().count();
         }
      }
      return size;
   }

   @Override
   public boolean isOffHeap() {
      return true;
   }

   @Override
   public Corpus map(@NonNull SerializableFunction<Document, Document> function) {
      return Corpus.builder().offHeap().addAll(stream().map(function).collect()).build();
   }
}//END OF FileCorpus
