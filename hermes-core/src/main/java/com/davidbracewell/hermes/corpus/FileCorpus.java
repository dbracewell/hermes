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
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Pipeline;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Logger;
import com.davidbracewell.stream.JavaMStream;
import com.davidbracewell.stream.MStream;
import lombok.NonNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

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
   * @param corpusFormat  the corpus format
   * @param resource        the resource containing the corpus
   * @param documentFactory the document factory to use when constructing documents
   */
  public FileCorpus(@NonNull CorpusFormat corpusFormat, @NonNull Resource resource, @NonNull DocumentFactory documentFactory) {
    this.corpusFormat = corpusFormat;
    this.resource = resource;
    this.documentFactory = documentFactory;
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
    if (corpusFormat == this.corpusFormat) {
      try (BufferedWriter writer = new BufferedWriter(resource.writer())) {
        try (MStream<String> lines = this.resource.lines()) {
          AtomicLong count = new AtomicLong();
          for (String line : Collect.asIterable(lines.iterator())) {
            writer.write(line);
            writer.write("\n");
            if (count.incrementAndGet() % 1000 == 0) {
              writer.flush();
            }
          }
        }
      }
      return Corpus.builder().format(format).source(resource).build();
    } else {
      return Corpus.super.write(format, resource);
    }
  }


  @Override
  public MStream<Document> stream() {
    return new JavaMStream<>(iterator());
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
      size = stream().count();
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
