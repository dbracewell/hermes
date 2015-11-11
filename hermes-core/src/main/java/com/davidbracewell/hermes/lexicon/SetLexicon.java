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
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class SetLexicon extends BaseLexicon implements MutableLexicon {
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
    String norm = normalize(hString.toString());
    if (lexicon.contains(norm)) {
      return Optional.of(norm);
    }
    return Optional.empty();
  }

  @Override
  public void add(String lexicalItem) {
    Preconditions.checkArgument(!StringUtils.isNullOrBlank(lexicalItem), "lexical item must not be null or blank");
    lexicon.add(normalize(lexicalItem));
  }

  @Override
  public void remove(String lexicalItem) {
    if (!StringUtils.isNullOrBlank(lexicalItem)) {
      lexicon.remove(normalize(lexicalItem));
    }
  }


}//END OF SetLexicon
