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
import com.davidbracewell.parsing.ParseException;

import java.util.Collection;
import java.util.Optional;

/**
 * <p>
 * A document store is designed for storing, retrieving, and managing documents.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface DocumentStore extends Iterable<Document> {

  /**
   * Gets the document associated with the given id
   *
   * @param id the id of the document to retrieve
   * @return An optional containing the document for the given id or absent if no document is found
   */
  Optional<Document> get(String id);

  /**
   * True if the document store has no documents
   *
   * @return True if the document store does not have any documents.
   */
  boolean isEmpty();

  /**
   * Add a document to the document store overriding any previous document with the same id
   *
   * @param document the document to store
   * @return True if the document was stored
   */
  boolean put(Document document);

  /**
   * Queries the document store for documents matching the given query. The syntax of the query language is defined in
   * the {@link QueryParser} class.
   *
   * @param query the query to perform
   * @return the collection of documents matching the query
   * @throws ParseException the parse exception
   */
  Collection<Document> query(String query) throws ParseException;

  /**
   * The number of documents in the document store
   *
   * @return the number of documents in the store
   */
  long size();

}//END OF DocumentStore
