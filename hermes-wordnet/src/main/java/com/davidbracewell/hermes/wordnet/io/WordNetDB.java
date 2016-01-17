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
 * @author dbracewell
 */
public interface WordNetDB {

  boolean containsLemma(String lemma);

  Set<String> getLemmas();

  Set<Sense> getSenses();

  Set<Sense> getSenses(String lemma);

  Synset getSynsetFromId(String id);

  WordNetRelation getRelation(Sense sense1, Sense sense2);

  WordNetRelation getRelation(Synset synset1, Synset synset2);

  Map<Sense, WordNetRelation> getRelations(Sense sense);

  Map<String, WordNetRelation> getRelations(Synset synset);

  Set<Synset> getSynsets();

  Set<Synset> getRoots();

  void putSense(String lemma, Sense sense);

  void putSynset(String id, Synset synset);

  void putRelation(Sense s1, Sense s2, WordNetRelation relation);

  void putRelation(String synsetId1, String synsetId2, WordNetRelation relation);

  void addRoot(Synset root);

  String toSenseRelationIndex(Sense sense);

}//END OF WordNetDB
