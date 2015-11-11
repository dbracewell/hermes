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

package com.davidbracewell.hermes.lexicon;

import com.davidbracewell.collection.trie.PatriciaTrie;
import com.davidbracewell.collection.trie.TrieMatch;
import com.davidbracewell.hermes.HString;
import com.google.common.base.Preconditions;
import lombok.NonNull;

import java.util.*;

/**
 * <p>Implementation of <code>Lexicon</code> usng a Trie data structure.</p>
 *
 * @param <V> the type parameter
 * @author David B. Bracewell
 */
public abstract class BaseTrieLexicon<V> extends BaseLexicon {
  private static final long serialVersionUID = 1L;
  protected final PatriciaTrie<V> trie;
  private boolean fuzzyMatch = false;

  /**
   * Instantiates a new Trie lexicon.
   *
   * @param lexicon         the lexicon
   * @param isCaseSensitive the is case sensitive
   */
  public BaseTrieLexicon(@NonNull Map<String, V> lexicon, boolean isCaseSensitive) {
    super(isCaseSensitive);
    this.trie = new PatriciaTrie<>();
    lexicon.entrySet().forEach(e -> trie.put(normalize(e.getKey()), e.getValue()));
  }

  public BaseTrieLexicon(boolean isCaseSensitive) {
    super(isCaseSensitive);
    this.trie = new PatriciaTrie<>();
  }

  /**
   * Find matches list.
   *
   * @param text the text
   * @return the list
   */
  public List<HString> findMatches(HString text) {
    Preconditions.checkNotNull(text);
    Preconditions.checkNotNull(trie, "Trie has not been initialized");
    List<HString> matches = new ArrayList<>();
    String content = normalize(text);
    trie.findOccurrencesIn(content, fuzzyMatch).forEach(match -> {
      matches.add(createMatch(text, match));
    });
    return matches;
  }

  /**
   * Create match h string.
   *
   * @param source the source
   * @param match  the match
   * @return the h string
   */
  protected abstract HString createMatch(HString source, TrieMatch<V> match);

  @Override
  public Iterator<String> iterator() {
    return trie.keySet().iterator();
  }

  @Override
  public int size() {
    return trie.size();
  }

  @Override
  public Optional<String> getMatch(HString hString) {
    if (trie.containsKey(normalize(hString))) {
      return Optional.of(hString.toLowerCase());
    }
    if (trie.containsKey(hString.toString())) {
      return Optional.of(hString.toString());
    }
    if (fuzzyMatch && !isCaseSensitive()) {
      return trie.prefixMap(hString.toString().toLowerCase()).keySet().stream().findFirst();
    } else if (fuzzyMatch) {
      return trie.prefixMap(hString.toString()).keySet().stream().findFirst();
    }
    return Optional.empty();
  }

  public boolean prefixMatch(@NonNull HString hString) {
    String content = isCaseSensitive() ? hString.toString() : hString.toLowerCase();
    return trie.prefixMap(content).size() > 0;
  }


  /**
   * Is fuzzy match boolean.
   *
   * @return the boolean
   */
  public boolean isFuzzyMatch() {
    return fuzzyMatch;
  }

  /**
   * Sets fuzzy match.
   *
   * @param fuzzyMatch the fuzzy match
   */
  public void setFuzzyMatch(boolean fuzzyMatch) {
    this.fuzzyMatch = fuzzyMatch;
  }

}//END OF BaseTrieLexicon


