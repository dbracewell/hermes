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

package com.davidbracewell.hermes.wordnet;

import com.davidbracewell.guava.common.collect.Iterables;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class SynsetPathWalker implements Iterator<Synset> {
  private Synset synset;
  private Set<String> seen = new HashSet<>();

  public SynsetPathWalker(Synset synset) {
    this.synset = synset;
  }

  @Override
  public boolean hasNext() {
    return synset != null;
  }

  @Override
  public Synset next() {
    Synset toReturn = synset;
    seen.add(toReturn.toString());
    synset = Iterables.getFirst(
        Iterables.concat(
            synset.getRelatedSynsets(WordNetRelation.HYPERNYM),
            synset.getRelatedSynsets(WordNetRelation.HYPERNYM_INSTANCE)
        ),
        null
    );
    if (synset != null && seen.contains(synset.toString())) {
      synset = null;
    }
    return toReturn;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}//END OF SynsetPathWalker
