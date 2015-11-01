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
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.util.*;
import java.util.function.BiFunction;

/**
 * The type Recursive document iterator.
 *
 * @author David B. Bracewell
 */
public class RecursiveDocumentIterator implements Iterator<Document> {
  private final Iterator<Resource> resourceIterator;
  private final DocumentFactory documentFactory;
  private final Queue<Document> documentQueue = new LinkedList<>();
  private final BiFunction<Resource, DocumentFactory, Iterable<Document>> resourceReader;

  /**
   * Instantiates a new Recursive document iterator.
   *
   * @param resource        the resource
   * @param documentFactory the document factory
   * @param resourceReader  the resource reader
   */
  public RecursiveDocumentIterator(@NonNull Resource resource, @NonNull DocumentFactory documentFactory, @NonNull BiFunction<Resource, DocumentFactory, Iterable<Document>> resourceReader) {
    this.documentFactory = documentFactory;
    this.resourceReader = resourceReader;
    this.resourceIterator = resource.isDirectory() ? resource.childIterator(true) : Collections.singleton(resource).iterator();
  }

  boolean advance() {
    if (documentQueue.isEmpty()) {
      synchronized (documentQueue) {
        while (resourceIterator.hasNext() && documentQueue.isEmpty()) {
          Resource r = resourceIterator.next();
          if (!r.isDirectory()) {
            if (r.asFile().map(f -> !f.isHidden()).orElse(true)) {
              resourceReader.apply(r, documentFactory).forEach(documentQueue::add);
            }
          }
        }
      }
    }
    return documentQueue.size() > 0;
  }

  @Override
  public boolean hasNext() {
    return advance();
  }

  @Override
  public Document next() {
    if (!advance()) {
      throw new NoSuchElementException();
    }
    return documentQueue.remove();
  }


}//END OF DocumentIterator
