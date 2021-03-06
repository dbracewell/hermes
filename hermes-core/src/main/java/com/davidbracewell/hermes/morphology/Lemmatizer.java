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


import com.davidbracewell.collection.Trie;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.POS;
import com.davidbracewell.hermes.Types;
import lombok.NonNull;

import java.util.List;
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
    return allPossibleLemmas(string, POS.ANY).stream().findFirst().orElse(string.toLowerCase());
  }

  /**
   * Determines the best lemma for a string given a part of speech
   *
   * @param string       the string
   * @param partOfSpeech the part of speech
   * @return the lemmatized version of the string
   */
  default String lemmatize(@NonNull String string, @NonNull POS partOfSpeech) {
    return allPossibleLemmas(string, partOfSpeech).stream().findFirst().orElse(string.toLowerCase());
  }

  /**
   * Gets all lemmas.
   *
   * @param string       the string
   * @param partOfSpeech the part of speech
   * @return the all lemmas
   */
  List<String> allPossibleLemmas(String string, POS partOfSpeech);

  /**
   * Gets prefixed lemmas.
   *
   * @param string       the string
   * @param partOfSpeech the part of speech
   * @return the prefixed lemmas
   */
  Trie<String> allPossibleLemmasAndPrefixes(String string, POS partOfSpeech);

  boolean canLemmatize(String input, POS partOfSpeech);

  /**
   * Lemmatizes a token.
   *
   * @param fragment the fragment to lemmatize
   * @return the lemmatized version of the token
   */
  default String lemmatize(@NonNull HString fragment) {
    if (fragment.isInstance(Types.TOKEN)) {
      POS pos = fragment.getPOS();
      if (pos == null) {
        pos = POS.ANY;
      }
      return lemmatize(fragment.toString(), pos);
    }
    return fragment.tokens().stream()
                   .map(this::lemmatize)
                   .collect(Collectors.joining(fragment.getLanguage().usesWhitespace() ? " " : ""));
  }


}//END OF Lemmatizer
