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
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.HString;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Implementation of <code>Lexicon</code> usng a Trie data structure.</p>
 *
 * @author David B. Bracewell
 */
public class TrieLexicon extends BaseLexicon {
  private static final long serialVersionUID = 1L;
  protected final PatriciaTrie<List<LexiconEntry>> trie;
  private boolean fuzzyMatch = false;

  public TrieLexicon(boolean isCaseSensitive) {
    super(isCaseSensitive);
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

  public double getProbability(@NonNull HString hString) {
    return getEntries(hString).stream().mapToDouble(LexiconEntry::getProbability).max().orElse(0d);
  }

  public List<LexiconEntry> getEntries(@NonNull HString hString) {
    String str = normalize(hString);
    if (trie.containsKey(str)) {
      return trie.get(str).stream()
        .filter(le -> le.getConstraint() == null || le.getConstraint().test(hString))
        .sorted((l1, l2) -> -Double.compare(l1.getProbability(), l2.getProbability()))
        .collect(Collectors.toList());
    }
    str = normalize(hString.getLemma());
    if (trie.containsKey(str)) {
      return trie.get(str).stream()
        .filter(le -> le.getConstraint() == null || le.getConstraint().test(hString))
        .sorted((l1, l2) -> -Double.compare(l1.getProbability(), l2.getProbability()))
        .collect(Collectors.toList());
    }
    if (fuzzyMatch) {
      str = normalize(hString);
      if (trie.prefixMap(str).size() > 0) {
        return trie.prefixMap(str).values()
          .stream()
          .flatMap(Collection::stream)
          .filter(le -> le.getConstraint() == null || le.getConstraint().test(hString))
          .sorted((l1, l2) -> -Double.compare(l1.getProbability(), l2.getProbability()))
          .collect(Collectors.toList());
      }
      str = normalize(hString.getLemma());
      if (trie.prefixMap(str).size() > 0) {
        return trie.prefixMap(str).values()
          .stream()
          .flatMap(Collection::stream)
          .filter(le -> le.getConstraint() == null || le.getConstraint().test(hString))
          .sorted((l1, l2) -> -Double.compare(l1.getProbability(), l2.getProbability()))
          .collect(Collectors.toList());
      }
    }
    return Collections.emptyList();
  }

  @Override
  public void add(@NonNull LexiconEntry entry) {
    if (!trie.containsKey(entry.getLemma())) {
      trie.put(entry.getLemma(), new LinkedList<>());
    }
    trie.get(entry.getLemma()).add(entry);
  }

  protected boolean isPrefixMatch(@NonNull HString hString) {
    return trie.prefixMap(normalize(hString) + " ").size() > 0 || trie.prefixMap(normalize(hString.getLemma()) + " ").size() > 0;
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


  @Override
  public List<HString> find(@NonNull HString source) {
    List<HString> results = new LinkedList<>();
    List<Annotation> tokens = source.tokens();

    for (int i = 0; i < tokens.size(); ) {
      Annotation token = tokens.get(i);
      if (isPrefixMatch(token)) {

        HString bestMatch = null;
        double bestScore = 0d;
        for (int j = i + 1; j < tokens.size() && j < (i + 5); j++) {
          HString temp = HString.union(tokens.subList(i, j));
          double score = getProbability(temp);
          if (score >= bestScore) {
            bestScore = score;
            bestMatch = temp;
          }
          if (!isPrefixMatch(temp)) {
            break;
          }
        }

        if (bestMatch != null) {
          results.add(bestMatch);
          i += bestMatch.tokenLength();
        } else {
          i++;
        }

      } else if (test(token)) {
        results.add(token);
        i++;
      } else {
        i++;
      }
    }

    return results;
  }

}//END OF BaseTrieLexicon


