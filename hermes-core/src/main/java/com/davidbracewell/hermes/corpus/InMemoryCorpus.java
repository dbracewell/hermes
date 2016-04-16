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

import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Pipeline;
import com.davidbracewell.stream.JavaMStream;
import com.davidbracewell.stream.MStream;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * An implementation of a <code>Corpus</code> that stores documents in memory.
 * </p>
 *
 * @author David B. Bracewell
 */
public class InMemoryCorpus implements Corpus, Serializable {
  private static final long serialVersionUID = 1L;
  private final List<Document> documents = new LinkedList<>();

  /**
   * Instantiates a new In memory corpus.
   *
   * @param documentCollection the document collection
   */
  public InMemoryCorpus(@NonNull Collection<Document> documentCollection) {
    documents.addAll(documentCollection);
  }

  @Override
  public Corpus annotate(@NonNull AnnotatableType... types) {
    Pipeline.builder().addAnnotations(types).returnCorpus(false).build().process(this);
    return this;
  }

  @Override
  public Corpus filter(@NonNull SerializablePredicate<? super Document> filter) {
    return new InMemoryCorpus(documents.stream().filter(filter).collect(Collectors.toList()));
  }

  @Override
  public Corpus map(@NonNull SerializableFunction<Document, Document> function) {
    return new InMemoryCorpus(documents.stream().map(function).collect(Collectors.toList()));
  }

  @Override
  public DocumentFactory getDocumentFactory() {
    return DocumentFactory.getInstance();
  }

  @Override
  public boolean isEmpty() {
    return documents.isEmpty();
  }

  @Override
  public Iterator<Document> iterator() {
    return documents.iterator();
  }

  @Override
  public long size() {
    return documents.size();
  }

  @Override
  public MStream<Document> stream() {
    return new JavaMStream<>(documents);
  }

  @Override
  public boolean isInMemory() {
    return true;
  }
}//END OF InMemoryCorpus
