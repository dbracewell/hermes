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

import com.davidbracewell.Tag;
import com.davidbracewell.collection.trie.PatriciaTrie;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.HString;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Implementation of <code>Lexicon</code> usng a Trie data structure.</p>
 *
 * @author David B. Bracewell
 */
public class TrieLexicon extends BaseLexicon implements PrefixSearchable {
  private static final long serialVersionUID = 1L;
  protected final PatriciaTrie<List<LexiconEntry>> trie;

  public TrieLexicon(boolean isCaseSensitive, boolean isProbabilistic, Attribute tagAttribute) {
    super(isCaseSensitive, isProbabilistic, tagAttribute);
    this.trie = new PatriciaTrie<>();
  }

  @Override
  public Iterator<String> iterator() {
    return null;
  }

  @Override
  public int size() {
    return trie.size();
  }

  @Override
  public Optional<String> getMatch(@NonNull HString hString) {
    return getEntries(hString)
      .stream()
      .map(LexiconEntry::getLemma)
      .findFirst();
  }

  @Override
  public double getProbability(@NonNull HString hString) {
    return getEntries(hString).stream().mapToDouble(LexiconEntry::getProbability).max().orElse(0d);
  }

  @Override
  public double getProbability(@NonNull HString hString, @NonNull Tag tag) {
    return getEntries(hString).stream()
      .filter(le -> le.getTag() != null && le.getTag().isInstance(tag))
      .mapToDouble(LexiconEntry::getProbability)
      .max().orElse(0d);
  }

  @Override
  public List<LexiconEntry> getEntries(@NonNull HString hString) {
    String str = normalize(hString);
    if (trie.containsKey(str)) {
      return trie.get(str).stream()
        .filter(le -> le.getConstraint() == null || le.getConstraint().test(hString))
        .sorted()
        .collect(Collectors.toList());
    }
    str = normalize(hString.getLemma());
    if (trie.containsKey(str)) {
      return trie.get(str).stream()
        .filter(le -> le.getConstraint() == null || le.getConstraint().test(hString))
        .sorted()
        .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }


  @Override
  public void add(@NonNull LexiconEntry entry) {
    if (!trie.containsKey(entry.getLemma())) {
      trie.put(entry.getLemma(), new LinkedList<>());
    }
    ensureLongestLemma(entry.getLemma());
    trie.get(entry.getLemma()).add(entry);
  }

  @Override
  public boolean isPrefixMatch(@NonNull HString hString) {
    return trie.prefixMap(normalize(hString) + " ").size() > 0 || trie.prefixMap(normalize(hString.getLemma()) + " ").size() > 0;
  }

}//END OF BaseTrieLexicon


