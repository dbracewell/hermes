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
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
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
   * Loader lexicon loader.
   *
   * @return the lexicon loader
   */
  static LexiconLoader loader() {
    return new LexiconLoader();
  }

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
  Optional<String> getMatch(HString hString);

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
  double getProbability(HString hString);

  Attribute getTagAttribute();

  default double getProbability(String lemma) {
    return getProbability(Fragments.string(lemma));
  }

  default Optional<Tag> getTag(String lemma) {
    return getTag(Fragments.string(lemma));
  }

  Optional<Tag> getTag(HString hString);

  /**
   * Gets probability.
   *
   * @param hString the h string
   * @param tag     the tag
   * @return the probability
   */
  double getProbability(HString hString, Tag tag);

  int getMaxTokenLength();

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

  /**
   * The type Lexicon loader.
   */
  final class LexiconLoader {
    private boolean isProbabilistic;
    private boolean hasConstraints;
    private boolean isCaseSensitive;
    private Attribute tagAttribute;
    private Tag defaultTag;
    private boolean useResourceNameAsTag;

    /**
     * Probabilisitic lexicon loader.
     *
     * @return the lexicon loader
     */
    public LexiconLoader probabilisitic() {
      isProbabilistic = true;
      return this;
    }

    /**
     * Non probabilisitic lexicon loader.
     *
     * @return the lexicon loader
     */
    public LexiconLoader nonProbabilisitic() {
      isProbabilistic = false;
      return this;
    }

    /**
     * Constrained lexicon loader.
     *
     * @return the lexicon loader
     */
    public LexiconLoader constrained() {
      this.hasConstraints = true;
      return this;
    }

    /**
     * Non constrained lexicon loader.
     *
     * @return the lexicon loader
     */
    public LexiconLoader nonConstrained() {
      this.hasConstraints = false;
      return this;
    }

    /**
     * Case sensitive lexicon loader.
     *
     * @return the lexicon loader
     */
    public LexiconLoader caseSensitive() {
      isCaseSensitive = true;
      return this;
    }

    /**
     * Case insensitive lexicon loader.
     *
     * @return the lexicon loader
     */
    public LexiconLoader caseInsensitive() {
      isCaseSensitive = false;
      return this;
    }

    /**
     * Tag attribute lexicon loader.
     *
     * @param attribute the attribute
     * @return the lexicon loader
     */
    public LexiconLoader tagAttribute(Attribute attribute) {
      this.tagAttribute = attribute;
      return this;
    }

    /**
     * Use resource name as tag lexicon loader.
     *
     * @return the lexicon loader
     */
    public LexiconLoader useResourceNameAsTag() {
      this.useResourceNameAsTag = true;
      this.defaultTag = null;
      return this;
    }

    /**
     * Default tag lexicon loader.
     *
     * @param tag the tag
     * @return the lexicon loader
     */
    public LexiconLoader defaultTag(Tag tag) {
      this.defaultTag = tag;
      this.useResourceNameAsTag = false;
      return this;
    }

    /**
     * Load lexicon.
     *
     * @param resource the resource
     * @return the lexicon
     * @throws IOException the io exception
     */
    public Lexicon load(@NonNull Resource resource) throws IOException {

//      Tag dTag = defaultTag;
//      if (useResourceNameAsTag && tagAttribute != null) {
//        String tagV = resource.baseName().replaceFirst("\\.*$", "");
//        dTag = tagAttribute.getValueType().convert(tagV);
//      }
//
//      if (isProbabilistic && hasConstraints && tagAttribute != null) {
//
//      } else if (isProbabilistic && hasConstraints) {
//
//      } else if (isProbabilistic && tagAttribute != null) {
//
//      } else if (isProbabilistic) {
//        return ProbabilisticTrieLexicon.read(resource, isCaseSensitive);
//      } else if (hasConstraints && tagAttribute != null) {
//
//      } else if (hasConstraints) {
//
//
//      } else if (tagAttribute != null) {
//        return TrieTagLexicon.read(resource, isCaseSensitive, tagAttribute, dTag);
//      }
//
//      return TrieLexicon.read(resource, isCaseSensitive);

      return null;
    }


  }// END OF LexiconLoader

}//END OF Lexicon
