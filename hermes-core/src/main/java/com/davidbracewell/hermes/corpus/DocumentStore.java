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
 * The interface Document store.
 * @author David B. Bracewell
 */
public interface DocumentStore extends Iterable<Document> {

  /**
   * Size int.
   *
   * @return the int
   */
  int size();

  /**
   * Is empty.
   *
   * @return the boolean
   */
  boolean isEmpty();

  /**
   * Put boolean.
   *
   * @param document the document
   * @return the boolean
   */
  boolean put(Document document);

  /**
   * Get optional.
   *
   * @param id the id
   * @return the optional
   */
  Optional<Document> get(String id);

  /**
   * Query collection.
   *
   * @param query the query
   * @return the collection
   */
  Collection<Document> query(String query) throws ParseException;


}//END OF DocumentStore
