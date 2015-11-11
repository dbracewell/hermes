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

import com.davidbracewell.hermes.HString;
import com.davidbracewell.string.StringUtils;
import com.google.common.collect.Iterators;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class SetLexicon extends BaseLexicon {
  private static final long serialVersionUID = 1L;
  private final Set<String> lexicon = new HashSet<>();

  public SetLexicon(boolean caseSensitive) {
    this(caseSensitive, new HashSet<>(0));
  }

  public SetLexicon(boolean caseSensitive, @NonNull Set<String> set) {
    super(caseSensitive);
    set.forEach(this::add);
  }

  @Override
  public Iterator<String> iterator() {
    return Iterators.unmodifiableIterator(lexicon.iterator());
  }

  @Override
  public int size() {
    return lexicon.size();
  }

  @Override
  public Optional<String> getMatch(HString hString) {
    if (hString == null) {
      return Optional.empty();
    }
    if (lexicon.contains(hString.toString())) {
      return Optional.of(hString.toString());
    }
    if (!isCaseSensitive() && lexicon.contains(hString.toLowerCase())) {
      return Optional.of(hString.toLowerCase());
    }
    return Optional.empty();
  }

  public void add(String lexicalItem) {
    if (!StringUtils.isNullOrBlank(lexicalItem)) {
      lexicon.add((isCaseSensitive() ? lexicalItem : lexicalItem.toLowerCase()));
    }
  }

  public void addAll(Iterable<String> lexicalItems) {
    if (lexicalItems != null) {
      lexicalItems.forEach(this::add);
    }
  }


}//END OF SetLexicon
