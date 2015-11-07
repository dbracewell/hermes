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

import lombok.Builder;
import lombok.Singular;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class SetLexicon extends Lexicon {
  private static final long serialVersionUID = 1L;
  private final Set<String> elements = new HashSet<>();

  /**
   * Instantiates a new Tag lexicon.
   *
   * @param caseSensitive the case sensitive
   */
  @Builder
  protected SetLexicon(boolean caseSensitive, @Singular Set<String> elements) {
    super(caseSensitive);
    this.elements.addAll(elements);
  }


  @Override
  protected boolean containsImpl(String nonNullLexicalItem) {
    return elements.contains(nonNullLexicalItem);
  }

  @Override
  public Set<String> lexicalItems() {
    return Collections.unmodifiableSet(elements);
  }

  @Override
  public int size() {
    return elements.size();
  }

}//END OF SetLexicon
