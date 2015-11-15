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
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.Fragments;
import com.davidbracewell.hermes.HString;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * <p>Defines a lexicon in which words/phrases are mapped to categories.</p>
 *
 * @author David B. Bracewell
 */
public interface Lexicon extends Predicate<HString>, Iterable<String> {

  /**
   * The number of lexical items in the lexicon
   *
   * @return the number of lexical items in the lexicon
   */
  int size();

  /**
   * Gets match.
   *
   * @param hString the h string
   * @return the match
   */
  default Optional<String> getMatch(HString hString) {
    return getEntries(hString)
      .stream()
      .map(LexiconEntry::getLemma)
      .findFirst();
  }

  @Override
  default boolean test(HString hString) {
    return getMatch(hString).isPresent();
  }

  /**
   * Gets probability.
   *
   * @param hString the h string
   * @return the probability
   */
  default double getProbability(HString hString) {
    return getEntries(hString).stream().mapToDouble(LexiconEntry::getProbability).max().orElse(0d);
  }

  /**
   * Gets tag attribute.
   *
   * @return the tag attribute
   */
  Attribute getTagAttribute();

  /**
   * Gets probability.
   *
   * @param lemma the lemma
   * @return the probability
   */
  default double getProbability(String lemma) {
    return getProbability(Fragments.string(lemma));
  }

  /**
   * Gets tag.
   *
   * @param lemma the lemma
   * @return the tag
   */
  default Optional<Tag> getTag(String lemma) {
    return getTag(Fragments.string(lemma));
  }

  /**
   * Gets tag.
   *
   * @param hString the h string
   * @return the tag
   */
  default Optional<Tag> getTag(HString hString) {
    return getEntries(hString).stream().map(LexiconEntry::getTag).findFirst();
  }

  /**
   * Gets probability.
   *
   * @param hString the h string
   * @param tag     the tag
   * @return the probability
   */
  default double getProbability(HString hString, Tag tag) {
    return getEntries(hString).stream()
      .filter(le -> le.getTag() != null && le.getTag().isInstance(tag))
      .mapToDouble(LexiconEntry::getProbability)
      .max()
      .orElse(0d);
  }

  /**
   * Gets max token length.
   *
   * @return the max token length
   */
  int getMaxTokenLength();

  /**
   * Is case sensitive boolean.
   *
   * @return the boolean
   */
  boolean isCaseSensitive();

  /**
   * Add.
   *
   * @param lemma the lemma
   */
  default void add(@NonNull String lemma) {
    add(new LexiconEntry(lemma, 1.0, null, null));
  }

  /**
   * Add.
   *
   * @param lemma the lemma
   * @param tag   the tag
   */
  default void add(@NonNull String lemma, @NonNull Tag tag) {
    add(new LexiconEntry(lemma, 1.0, null, tag));
  }

  /**
   * Add.
   *
   * @param lemma       the lemma
   * @param probability the probability
   * @param tag         the tag
   */
  default void add(@NonNull String lemma, double probability, @NonNull Tag tag) {
    add(new LexiconEntry(lemma, probability, null, tag));
  }

  /**
   * Add.
   *
   * @param lemma       the lemma
   * @param probability the probability
   */
  default void add(@NonNull String lemma, double probability) {
    add(new LexiconEntry(lemma, probability, null, null));
  }

  /**
   * Add.
   *
   * @param entry the entry
   */
  void add(LexiconEntry entry);

  /**
   * Add all.
   *
   * @param entries the entries
   */
  default void addAll(Iterable<LexiconEntry> entries) {
    if (entries != null) {
      entries.forEach(this::add);
    }
  }


  /**
   * Find list.
   *
   * @param source the source
   * @return the list
   */
  List<HString> match(HString source);


  /**
   * Gets entries.
   *
   * @param hString the h string
   * @return the entries
   */
  List<LexiconEntry> getEntries(HString hString);

}//END OF Lexicon
