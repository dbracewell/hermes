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

package com.davidbracewell.hermes.morphology;


import com.davidbracewell.collection.Streams;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.tag.POS;
import com.google.common.collect.Iterables;
import lombok.NonNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Defines the interface for lemmatizing tokens.</p>
 *
 * @author David B. Bracewell
 */
public interface Lemmatizer {

  /**
   * Determines the best lemma for a string
   *
   * @param string the string to lemmatize
   * @return the lemmatized version of the string
   */
  default String lemmatize(@NonNull String string) {
    return Iterables.getFirst(getBaseForms(string), string);
  }

  /**
   * Determines the best lemma for a string given a part of speech
   *
   * @param string       the string
   * @param partOfSpeech the part of speech
   * @return the lemmatized version of the string
   */
  default String lemmatize(@NonNull String string, @NonNull POS partOfSpeech) {
    return Iterables.getFirst(getBaseForms(string, partOfSpeech), string);
  }

  /**
   * Lemmatizes a token.
   *
   * @param fragment the fragment to lemmatize
   * @return the lemmatized version of the token
   */
  default String lemmatize(@NonNull HString fragment) {
    return Iterables.getFirst(getBaseForms(fragment), fragment.toString());
  }

  /**
   * lemmatizes the given token (string) without consideration of the part of speech
   *
   * @param string The token
   * @return The lemmatized version
   */
  default Iterable<String> getBaseForms(@NonNull String string) {
    return getBaseForms(string, POS.ANY);
  }

  /**
   * lemmatizes the given token (string)
   *
   * @param string       The token
   * @param partOfSpeech the part of speech
   * @return The lemmatized version
   */
  Iterable<String> getBaseForms(String string, POS partOfSpeech);

  /**
   * lemmatizes the given token.
   *
   * @param fragment The fragment
   * @return The lemmatized version
   */
  default Iterable<String> getBaseForms(@NonNull HString fragment) {
    return fragment.tokens().stream()
      .flatMap(token -> Streams.from(getBaseForms(token.toString(), token.get(Attrs.PART_OF_SPEECH).as(POS.class, POS.ANY).getUniversalTag())))
      .collect(Collectors.toList());
  }


  /**
   * Gets prefix base form.
   *
   * @param string       the string
   * @param partOfSpeech the part of speech
   * @return the prefix base form
   */
  Set<String> getPrefixBaseForms(String string, POS partOfSpeech);

  /**
   * Is lemma.
   *
   * @param word the word
   * @return the boolean
   */
  boolean isLemma(String word);


}//END OF Lemmatizer
