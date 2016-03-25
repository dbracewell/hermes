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

import java.io.IOException;

/**
 * <p>
 * Defines the format that a document is in. Examples include plain text, JSON (created by
 * <code>Document.toJson()</code>, and CONLL format. Corpus formats are registered using Java's builtin service
 * provider architecture via the Corpus object.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface CorpusFormat {

  /**
   * Create corpus.
   *
   * @param resource        the resource
   * @param documentFactory the document factory
   * @return the corpus
   */
  Corpus create(Resource resource, DocumentFactory documentFactory);


  /**
   * Read iterable.
   *
   * @param resource        the resource
   * @param documentFactory the document factory
   * @return the iterable
   * @throws IOException the iO exception
   */
  Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException;

  /**
   * Writes the documents in this format from the given resource using the given factory.
   *
   * @param resource  the resource
   * @param documents the documents
   * @throws IOException something went wrong writing
   */
  default void write(Resource resource, Iterable<Document> documents) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * Write void.
   *
   * @param resource the resource
   * @param document the document
   * @throws IOException the iO exception
   */
  default void write(Resource resource, Document document) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * Name string.
   *
   * @return the string
   */
  String name();

  /**
   * Extension string.
   *
   * @return the string
   */
  default String extension() {
    return name().toLowerCase();
  }


  /**
   * Is one per line boolean.
   *
   * @return the boolean
   */
  default boolean isOnePerLine() {
    return false;
  }

}//END OF CorpusFormat
