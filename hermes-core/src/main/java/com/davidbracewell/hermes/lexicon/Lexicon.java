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


import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * <p>Defines a lexicon in which words/phrases are mapped to categories.</p>
 *
 * @author David B. Bracewell
 */
public interface Lexicon extends Predicate<HString>, Iterable<String> {

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

  final class LexiconLoader {
    private boolean isProbabilistic;
    private boolean hasConstraints;
    private boolean isCaseSensitive;
    private Attribute tagAttribute;


    public LexiconLoader probabilisitic() {
      isProbabilistic = true;
      return this;
    }

    public LexiconLoader nonProbabilisitic() {
      isProbabilistic = false;
      return this;
    }

    public LexiconLoader constrained() {
      this.hasConstraints = true;
      return this;
    }

    public LexiconLoader nonConstrained() {
      this.hasConstraints = false;
      return this;
    }

    public LexiconLoader caseSensitive() {
      isCaseSensitive = true;
      return this;
    }

    public LexiconLoader caseInsensitive() {
      isCaseSensitive = false;
      return this;
    }

    public LexiconLoader tagAttribute(Attribute attribute) {
      this.tagAttribute = attribute;
      return this;
    }

    public Lexicon load(@NonNull Resource resource) throws IOException {

      if (isProbabilistic && hasConstraints && tagAttribute != null) {

      } else if (isProbabilistic && hasConstraints) {

      } else if (isProbabilistic && tagAttribute != null) {

      } else if (hasConstraints && tagAttribute != null) {

      } else if (hasConstraints) {


      } else if (tagAttribute != null) {
        return TrieTagLexicon.read(resource, isCaseSensitive, tagAttribute);
      }

      return TrieLexicon.read(resource, isCaseSensitive);
    }


  }// END OF LexiconLoader

}//END OF Lexicon
