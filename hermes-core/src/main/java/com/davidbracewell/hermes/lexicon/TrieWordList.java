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

import com.davidbracewell.collection.Trie;
import com.davidbracewell.guava.common.collect.Iterators;
import com.davidbracewell.guava.common.collect.Maps;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Simple word list.
 *
 * @author David B. Bracewell
 */
public class TrieWordList implements WordList, PrefixSearchable, Serializable {
   private static final long serialVersionUID = 1L;
   private final Trie<Boolean> words;

   /**
    * Instantiates a new Simple word list.
    *
    * @param words the words
    */
   public TrieWordList(@NonNull Set<String> words) {
      this.words = new Trie<>(Maps.asMap(words, s -> Boolean.TRUE));
   }

   @Override
   public boolean contains(String string) {
      return words.containsKey(string);
   }

   @Override
   public boolean isPrefixMatch(HString hString) {
      return !words.prefix(hString.toString()).isEmpty();
   }

   @Override
   public boolean isPrefixMatch(String hString) {
      return !words.prefix(hString).isEmpty();
   }

   @Override
   public Iterator<String> iterator() {
      return Iterators.unmodifiableIterator(words.keySet().iterator());
   }

   @Override
   public int size() {
      return words.size();
   }

   public static TrieWordList read(@NonNull Resource resource) throws IOException {
      return new TrieWordList(resource.readLines().stream()
                                      .map(line -> line.trim().toLowerCase())
                                      .collect(Collectors.toSet()));
   }

}//END OF SimpleWordList
