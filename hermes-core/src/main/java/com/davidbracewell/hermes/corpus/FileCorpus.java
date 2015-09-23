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
import com.davidbracewell.logging.Logger;
import lombok.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

/**
 * <p>
 * An implementation of <code>Corpus</code> that recursively reads files from disk. The full corpus is never loaded
 * into memory. File based corpora are read only, i.e. they do not support the <code>put</code> operator.
 * </p>
 *
 * @author David B. Bracewell
 */
public class FileCorpus extends Corpus {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(FileCorpus.class);

  private final CorpusFormat corpusFormat;
  private final Resource resource;
  private final DocumentFactory documentFactory;
  private int size = -1;

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
  public DocumentFactory getDocumentFactory() {
    return documentFactory;
  }

  @Override
  public int size() {
    if (size == -1) {
      size = super.size();
    }
    return size;
  }

}//END OF FileCorpus
