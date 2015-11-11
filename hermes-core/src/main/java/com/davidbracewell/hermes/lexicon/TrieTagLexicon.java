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
import com.davidbracewell.collection.trie.TrieMatch;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.HString;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;

/**
 * <p>Implementation of <code>Lexicon</code> usng a Trie data structure.</p>
 *
 * @author David B. Bracewell
 */
public class TrieTagLexicon extends BaseTrieLexicon<Tag> implements TagLexicon {
  private static final long serialVersionUID = 1L;
  private final Attribute tagAttribute;

  /**
   * Instantiates a new Trie lexicon.
   *
   * @param tagAttribute    the tag attribute
   * @param lexicon         the lexicon
   * @param isCaseSensitive the is case sensitive
   */
  public TrieTagLexicon(@NonNull Attribute tagAttribute, @NonNull Map<String, Tag> lexicon, boolean isCaseSensitive) {
    super(lexicon, isCaseSensitive);
    this.tagAttribute = tagAttribute;
  }

  @Override
  protected HString createMatch(HString source, TrieMatch<Tag> match) {
    HString hString = source.substring(match.start, match.end);
    hString.put(tagAttribute, match.value);
    return hString;
  }

  @Override
  public Optional<Tag> getTag(HString fragment) {
    return getMatch(fragment).map(trie::get);
  }

}//END OF TrieTagLexicon


