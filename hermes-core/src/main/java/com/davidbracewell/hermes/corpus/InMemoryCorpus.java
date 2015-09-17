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

import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * <p>
 * An implementation of a <code>Corpus</code> that stores documents in memory.
 * </p>
 *
 * @author David B. Bracewell
 */
public class InMemoryCorpus extends Corpus {
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
  public Iterator<Document> iterator() {
    return documentMap.values().iterator();
  }


  @Override
  public DocumentFactory getDocumentFactory() {
    return DocumentFactory.getInstance();
  }

  @Override
  public Stream<Document> stream() {
    return documentMap.values().parallelStream();
  }

  @Override
  public int size() {
    return documentMap.size();
  }

  @Override
  public boolean put(Document document) {
    if (document == null) {
      return false;
    }
    documentMap.put(document.getId(), document);
    return true;
  }

}//END OF InMemoryCorpus
