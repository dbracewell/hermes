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
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.csv.CSVReader;
import com.davidbracewell.reflection.ValueType;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

  public TrieTagLexicon(@NonNull Attribute tagAttribute, boolean isCaseSensitive) {
    super(isCaseSensitive);
    this.tagAttribute = tagAttribute;
  }


  public static TrieTagLexicon read(@NonNull Resource resource, boolean isCaseSensitive, @NonNull Attribute tagAttribute, Tag defaultTag) throws IOException {
    TrieTagLexicon lexicon = new TrieTagLexicon(tagAttribute, isCaseSensitive);
    ValueType tagType = tagAttribute.getValueType();
    try (CSVReader reader = CSV.builder().reader(resource)) {
      reader.forEach(row -> {
        if (row.size() > 1) {
          Tag tag = tagType.convert(row.get(1));
          lexicon.put(lexicon.normalize(row.get(0)), tag);
        } else {
          lexicon.put(lexicon.normalize(row.get(0)), defaultTag);
        }
      });
    }
    return lexicon;
  }

  public void put(String lexicalItem, Tag tag) {
    Preconditions.checkArgument(!StringUtils.isNullOrBlank(lexicalItem), "lexical item must not be null or blank");
    Preconditions.checkNotNull(tag, "Tag must not be null");
    trie.put(normalize(lexicalItem), tag);
  }

  public void putAll(TagLexicon lexicon) {
    if (lexicon != null) {
      lexicon.entrySet().forEach(e -> this.put(e.getKey(), e.getValue()));
    }
  }

  public void putAll(Map<String, ? extends Tag> map) {
    if (map != null) {
      map.forEach(this::put);
    }
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

  @Override
  public Set<Map.Entry<String, Tag>> entrySet() {
    return trie.entrySet();
  }
}//END OF TrieTagLexicon


