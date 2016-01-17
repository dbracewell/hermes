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

package com.davidbracewell.hermes.wordnet.io;

import com.davidbracewell.hermes.wordnet.Sense;
import com.davidbracewell.hermes.wordnet.Synset;
import com.davidbracewell.hermes.wordnet.WordNetRelation;

import java.util.Map;
import java.util.Set;

/**
 * The interface Word net db.
 *
 * @author dbracewell
 */
public interface WordNetDB {

  /**
   * Contains lemma boolean.
   *
   * @param lemma the lemma
   * @return the boolean
   */
  boolean containsLemma(String lemma);

  /**
   * Gets lemmas.
   *
   * @return the lemmas
   */
  Set<String> getLemmas();

  /**
   * Gets senses.
   *
   * @return the senses
   */
  Set<Sense> getSenses();

  /**
   * Gets senses.
   *
   * @param lemma the lemma
   * @return the senses
   */
  Set<Sense> getSenses(String lemma);

  /**
   * Gets synset from id.
   *
   * @param id the id
   * @return the synset from id
   */
  Synset getSynsetFromId(String id);

  /**
   * Gets relation.
   *
   * @param sense1 the sense 1
   * @param sense2 the sense 2
   * @return the relation
   */
  WordNetRelation getRelation(Sense sense1, Sense sense2);

  /**
   * Gets relation.
   *
   * @param synset1 the synset 1
   * @param synset2 the synset 2
   * @return the relation
   */
  WordNetRelation getRelation(Synset synset1, Synset synset2);

  /**
   * Gets relations.
   *
   * @param sense the sense
   * @return the relations
   */
  Map<Sense, WordNetRelation> getRelations(Sense sense);

  /**
   * Gets relations.
   *
   * @param synset the synset
   * @return the relations
   */
  Map<String, WordNetRelation> getRelations(Synset synset);

  /**
   * Gets synsets.
   *
   * @return the synsets
   */
  Set<Synset> getSynsets();

  /**
   * Gets roots.
   *
   * @return the roots
   */
  Set<Synset> getRoots();

  /**
   * Put sense.
   *
   * @param lemma the lemma
   * @param sense the sense
   */
  void putSense(String lemma, Sense sense);

  /**
   * Put synset.
   *
   * @param id     the id
   * @param synset the synset
   */
  void putSynset(String id, Synset synset);

  /**
   * Put relation.
   *
   * @param s1       the s 1
   * @param s2       the s 2
   * @param relation the relation
   */
  void putRelation(Sense s1, Sense s2, WordNetRelation relation);

  /**
   * Put relation.
   *
   * @param synsetId1 the synset id 1
   * @param synsetId2 the synset id 2
   * @param relation  the relation
   */
  void putRelation(String synsetId1, String synsetId2, WordNetRelation relation);

  /**
   * Add root.
   *
   * @param root the root
   */
  void addRoot(Synset root);

  /**
   * To sense relation index string.
   *
   * @param sense the sense
   * @return the string
   */
  String toSenseRelationIndex(Sense sense);

}//END OF WordNetDB
