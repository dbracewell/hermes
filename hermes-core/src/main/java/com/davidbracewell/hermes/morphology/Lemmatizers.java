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

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.tag.POS;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * <p>Factory class for creating/retrieving lemmatizers for a given language</p>
 *
 * @author David B. Bracewell
 */
public final class Lemmatizers {

  private static volatile Map<Language, Lemmatizer> lemmatizerMap = Maps.newConcurrentMap();

  /**
   * Gets the Lemmatizer for the given language as defined in the config option
   * <code>iknowledge.latte.morphology.Lemmatizer.LANGUAGE</code>. if no lemmatizer is specified a no-op lemmatizer is
   * returned.
   *
   * @param language The language
   * @return The Lemmatizer for the language
   */
  public static synchronized Lemmatizer getLemmatizer(@NonNull Language language) {
    if( !lemmatizerMap.containsKey(language)) {
      if (Config.hasProperty("hermes.Lemmatizer", language)) {
        Lemmatizer lemmatizer = Config.get("hermes.Lemmatizer", language).as(Lemmatizer.class);
        lemmatizerMap.put(language, lemmatizer);
      } else {
        lemmatizerMap.put(language, NoOptLemmatizer.INSTANCE);
      }
    }
    return lemmatizerMap.get(language);
  }

  /**
   * A stemmer implementation that returns the input
   */
  private enum NoOptLemmatizer implements Lemmatizer {
    INSTANCE;


    @Override
    public String lemmatize(String string) {
      return string;
    }

    @Override
    public String lemmatize(String string, POS partOfSpeech) {
      return string;
    }

    @Override
    public String lemmatize(HString token) {
      return token == null ? null : token.toString();
    }

    @Override
    public Iterable<String> getBaseForms(String string) {
      Preconditions.checkNotNull(string);
      return Collections.singletonList(string);
    }

    @Override
    public Iterable<String> getBaseForms(String string, POS partOfSpeech) {
      Preconditions.checkNotNull(string);
      return Collections.singletonList(string);
    }

    @Override
    public Iterable<String> getBaseForms(HString token) {
      Preconditions.checkNotNull(token);
      return Collections.singletonList(token.toString());
    }

    @Override
    public Set<String> getPrefixBaseForms(String string, POS partOfSpeech) {
      Preconditions.checkNotNull(string);
      return Collections.singleton(string);
    }

    @Override
    public boolean isLemma(String word) {
      return true;
    }

  }//END OF Stemmer$NoOptStemmer

}//END OF Stemmers
