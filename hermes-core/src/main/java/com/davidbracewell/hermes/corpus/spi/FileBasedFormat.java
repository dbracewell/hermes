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

import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.hermes.corpus.FileCorpus;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author David B. Bracewell
 */
public abstract class FileBasedFormat implements CorpusFormat, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public final Corpus create(Resource resource, DocumentFactory documentFactory) {
    return new FileCorpus(this, resource, documentFactory);
  }

  @Override
  public void write(@NonNull Resource resource, @NonNull Iterable<Document> documents) throws IOException {
    if ((resource.isDirectory() && resource.exists()) || resource.mkdirs()) {
      for (Document document : documents) {
        write(resource.getChild(document.getId() + "." + extension()), document);
      }
    } else {
      throw new IOException("Cannot make directories: " + resource.descriptor());
    }
  }

  @Override
  public void write(@NonNull Resource resource, @NonNull Document document) throws IOException {
    throw new UnsupportedOperationException();
  }


}//END OF FileBasedFormat
