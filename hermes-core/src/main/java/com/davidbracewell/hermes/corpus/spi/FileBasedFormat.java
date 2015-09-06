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

package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.collection.Collect;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author David B. Bracewell
 */
public abstract class FileBasedFormat implements CorpusFormat {
  private final static Logger log = Logger.getLogger(FileBasedFormat.class);

  @Override
  public final Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
    return Collect.asIterable(new DocumentIterator(resource, documentFactory));
  }


  protected abstract Iterable<Document> readResource(Resource resource, DocumentFactory documentFactory) throws IOException;


  class DocumentIterator implements Iterator<Document>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Iterator<Resource> resourceIterator;
    private final DocumentFactory documentFactory;
    private final Queue<Document> documentQueue = new LinkedList<>();

    DocumentIterator(Resource resource, DocumentFactory documentFactory) {
      this.documentFactory = documentFactory;
      this.resourceIterator = resource.isDirectory() ? resource.childIterator(true) : Collections.singleton(resource).iterator();
    }

    boolean advance() {
      while (resourceIterator.hasNext() && documentQueue.isEmpty()) {
        Resource r = resourceIterator.next();
        if (!r.isDirectory()) {
          if (r.asFile() == null || !r.asFile().isHidden()) {
            try {
              readResource(r, documentFactory).forEach(documentQueue::add);
            } catch (IOException e) {
              log.severe(e);
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
  }


}//END OF FileBasedFormat
