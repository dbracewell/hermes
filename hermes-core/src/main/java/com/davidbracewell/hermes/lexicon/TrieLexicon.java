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
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.tag.Tag;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.tuple.Tuple2;
import com.davidbracewell.tuple.Tuple3;
import com.google.common.base.Preconditions;

import java.util.*;

/**
 * <p>Implementation of <code>Lexicon</code> usng a Trie data structure.</p>
 *
 * @author David B. Bracewell
 */
public class TrieLexicon extends TagLexicon {

  private static final long serialVersionUID = 1L;
  private final PatriciaTrie<Tuple2<Tag, Double>> trie;
  private final Attribute tagAttribute;
  private boolean fuzzyMatch = false;


  public TrieLexicon(boolean caseSensitive, Attribute tagAttribute, Resource... lexicons) {
    this(caseSensitive, tagAttribute, Arrays.asList(lexicons));
  }

  public TrieLexicon(boolean caseSensitive, Attribute tagAttribute, Collection<Resource> lexicons) {
    super(caseSensitive);
    this.tagAttribute = tagAttribute;
    this.trie = new PatriciaTrie<>(loadLexicon(lexicons, Cast.<Class<? extends Tag>>as(tagAttribute.getValueType().getType())));
  }

  @Override
  protected boolean containsImpl(String nonNullLexicalItem) {
    return trie.containsKey(nonNullLexicalItem) ||
        (fuzzyMatch && !trie.prefixMap(nonNullLexicalItem).isEmpty());
  }

  public List<HString> findMatches(HString text) {
    Preconditions.checkNotNull(text);
    Preconditions.checkNotNull(trie, "ByteTrie has not been initialized");

    List<HString> matches = new ArrayList<>();
    String content = isCaseSensitive() ? text.toString() : text.toString().toLowerCase();
    for (Tuple3<Integer, Integer, Tuple2<Tag, Double>> match : trie.findOccurrencesIn(content, fuzzyMatch)) {
      HString f = text.substring(match.v1, match.v2);
      f.putAttribute(tagAttribute, match.v3.v1);
      f.putAttribute(Attrs.CONFIDENCE, match.v3.v2);
      matches.add(f);
    }

    return matches;
  }

  /**
   * Are the lexical items fuzzy matchable, i.e. non-exact matches.
   *
   * @return true if fuzzy matchable.
   */
  public boolean isFuzzyMatch() {
    return fuzzyMatch;
  }

  @Override
  protected double probabilityImpl(String lexicalItem) {
    Tuple2<Tag, Double> tagValue = trie.get(lexicalItem);
    if (tagValue == null) {
      return 0d;
    }
    return tagValue.getValue() == null ? 0d : tagValue.getValue();
  }

  /**
   * Sets whether or not to use fuzzy match.
   *
   * @param fuzzyMatch True use fuzzy match
   */
  public void setFuzzyMatch(boolean fuzzyMatch) {
    this.fuzzyMatch = fuzzyMatch;
  }

  @Override
  public Set<String> lexicalItems() {
    return Collections.unmodifiableSet(trie.keySet());
  }

  @Override
  protected Optional<Tag> lookupImpl(String lexicalItem) {
    Tuple2<Tag, Double> tagValue = trie.get(lexicalItem);
    if (tagValue == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(tagValue.getKey());
  }

  @Override
  public int size() {
    return trie.size();
  }

}//END OF ByteTrieLexicon


