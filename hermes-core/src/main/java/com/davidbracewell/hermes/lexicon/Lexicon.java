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

import java.io.Serializable;
import java.util.Set;
import java.util.function.Predicate;

/**
 * <p>Defines a lexicon in which words/phrases are mapped to categories.</p>
 *
 * @author David B. Bracewell
 */
public abstract class Lexicon implements Serializable, Predicate<CharSequence> {
  private static final long serialVersionUID = 1L;

  /**
   * Is the lookup case sensitive or insensitive
   */
  protected final boolean caseSensitive;

  /**
   * Instantiates a new Tag lexicon.
   *
   * @param caseSensitive the case sensitive
   */
  protected Lexicon(boolean caseSensitive) {
    this.caseSensitive = caseSensitive;

  }

  /**
   * Determines if a lexical item is contained in the lexicon or not
   *
   * @param lexicalItem the lexical item to lookup
   * @return true if the lexical item is in the lexicon, false otherwise
   */
  public final boolean contains(String lexicalItem) {
    return lexicalItem != null &&
      (containsImpl(lexicalItem) || (!isCaseSensitive() && containsImpl(lexicalItem.toLowerCase())));
  }

  /**
   * Determines if a content in the fragment is contained in the lexicon or not
   *
   * @param fragment the fragment to lookup
   * @return true if the fragment is in the lexicon, false otherwise
   */
  public final boolean contains(HString fragment) {
    return fragment != null && (contains(fragment.toString()) || contains(fragment.getLemma()));
  }

  /**
   * Contains implementation. Lexical item will non-null and in format that should be in backing dictionary.
   *
   * @param nonNullLexicalItem the non null lexical item
   * @return True if the lexicon contains the lexical item, false if not
   */
  protected abstract boolean containsImpl(String nonNullLexicalItem);

  /**
   * Is the lookup of lexical items case sensitive.
   *
   * @return True if case sensitive, False if case insensitive
   */
  public final boolean isCaseSensitive() {
    return caseSensitive;
  }

  /**
   * Gets the lexical items in the lexicom
   *
   * @return the set of lexical items
   */
  public abstract Set<String> lexicalItems();

  /**
   * The number of lexical items in the lexicon
   *
   * @return the number of lexical items in the lexicon
   */
  public abstract int size();


  @Override
  public final boolean test(CharSequence sequence) {
    if (sequence == null) {
      return false;
    }
    return contains(sequence.toString());
  }

}//END OF Lexicon
