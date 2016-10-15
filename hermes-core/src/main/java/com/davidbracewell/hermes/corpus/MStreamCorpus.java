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

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Pipeline;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.stream.StreamingContext;
import lombok.NonNull;

import java.io.Serializable;

/**
 * @author David B. Bracewell
 */
public class MStreamCorpus implements Corpus, Serializable {
  private static final long serialVersionUID = 1L;
  private final MStream<Document> stream;
  private final DocumentFactory documentFactory;

  public MStreamCorpus(MStream<Document> stream, DocumentFactory documentFactory) {
    this.stream = stream;
    this.documentFactory = documentFactory;
  }

  @Override
  public void close() throws Exception {
    stream.close();
  }

  @Override
  public MStream<Document> stream() {
    return stream;
  }

  @Override
  public Corpus annotate(@NonNull AnnotatableType... types) {
    return new MStreamCorpus(stream.map(d -> {
      Pipeline.process(d, types);
      return d;
    }),
      documentFactory
    );
  }

  @Override
  public CorpusType getCorpusType() {
    return null;
  }

  @Override
  public Corpus map(@NonNull SerializableFunction<Document, Document> function) {
    return new MStreamCorpus(stream().map(Cast.as(function)), documentFactory);
  }

  @Override
  public DocumentFactory getDocumentFactory() {
    return documentFactory;
  }

  @Override
  public Corpus filter(@NonNull SerializablePredicate<? super Document> filter) {
    return new MStreamCorpus(stream.filter(filter), documentFactory);
  }

  @Override
  public StreamingContext getStreamingContext() {
    return stream.getContext();
  }

}//END OF MStreamCorpus
