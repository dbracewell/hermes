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

import com.davidbracewell.collection.trie.TrieMatch;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.csv.CSVReader;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
public class TrieLexicon extends BaseTrieLexicon<Boolean> implements MutableLexicon {
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Trie lexicon.
   *
   * @param lexicon         the lexicon
   * @param isCaseSensitive the is case sensitive
   */
  public TrieLexicon(@NonNull Map<String, Boolean> lexicon, boolean isCaseSensitive) {
    super(lexicon, isCaseSensitive);
  }

  public TrieLexicon(boolean isCaseSensitive) {
    super(isCaseSensitive);
  }

  public static TrieLexicon read(@NonNull Resource resource, boolean isCaseSensitive) throws IOException {
    TrieLexicon lexicon = new TrieLexicon(isCaseSensitive);
    try (CSVReader reader = CSV.builder().reader(resource)) {
      reader.forEach(row -> {
        if (row.size() > 0) {
          lexicon.add(lexicon.normalize(row.get(0)));
        }
      });
    }
    return lexicon;
  }

  @Override
  protected HString createMatch(HString source, TrieMatch<Boolean> match) {
    return source.substring(match.start, match.end);
  }

  @Override
  public void add(String lexicalItem) {
    Preconditions.checkArgument(!StringUtils.isNullOrBlank(lexicalItem), "lexical item must not be null or blank");
    this.trie.put(normalize(lexicalItem), true);
  }

  @Override
  public void remove(String lexicalItem) {
    if (lexicalItem != null) {
      this.trie.remove(normalize(lexicalItem));
    }
  }


}//END OF TrieLexicon
