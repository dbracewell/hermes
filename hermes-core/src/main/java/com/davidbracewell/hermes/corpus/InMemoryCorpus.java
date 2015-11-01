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

import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Pipeline;
import com.davidbracewell.stream.JavaMStream;
import com.davidbracewell.stream.MStream;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;
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
  private final Map<String, Document> documentMap = new HashMap<>();

  /**
   * Instantiates a new In memory corpus.
   *
   * @param documentCollection the document collection
   */
  public InMemoryCorpus(@NonNull Collection<Document> documentCollection) {
    documentCollection.forEach(document -> documentMap.put(document.getId(), document));
  }

  @Override
  public Corpus annotate(@NonNull AnnotationType... types) {
    Pipeline.builder().addAnnotations(types).returnCorpus(false).build().process(this);
    return this;
  }

  @Override
  public Corpus filter(@NonNull SerializablePredicate<Document> filter) {
    return new InMemoryCorpus(documentMap.values().stream().filter(filter).collect(Collectors.toList()));
  }

  @Override
  public Optional<Document> get(String id) {
    return Optional.ofNullable(documentMap.get(id));
  }

  @Override
  public DocumentFactory getDocumentFactory() {
    return DocumentFactory.getInstance();
  }

  @Override
  public boolean isEmpty() {
    return documentMap.isEmpty();
  }

  @Override
  public Iterator<Document> iterator() {
    return documentMap.values().iterator();
  }

  @Override
  public boolean put(Document document) {
    if (document == null) {
      return false;
    }
    documentMap.put(document.getId(), document);
    return true;
  }

  @Override
  public long size() {
    return documentMap.size();
  }

  @Override
  public MStream<Document> stream() {
    return new JavaMStream<>(documentMap.values());
  }
}//END OF InMemoryCorpus
