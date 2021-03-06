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

package com.davidbracewell.hermes.filter;

import com.davidbracewell.config.Config;
import com.davidbracewell.guava.common.base.CharMatcher;
import com.davidbracewell.guava.common.base.Strings;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.POS;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.tokenization.TokenType;
import com.davidbracewell.logging.Logger;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.string.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * The type English stop words.
 *
 * @author David B. Bracewell
 */
public class EnglishStopWords extends StopWords {
  private static final long serialVersionUID = 1L;
  private static volatile StopWords INSTANCE;
  private final Set<String> stopWords = new HashSet<>();

  private EnglishStopWords() {
    if (Config.hasProperty("hermes.StopWords.ENGLISH", "dict")) {
      try (MStream<String> stream = Config.get("hermes.StopWords.ENGLISH", "dict").asResource().lines()) {
        stream.forEach(line -> {
          line = CharMatcher.WHITESPACE.trimFrom(line);
          if (!Strings.isNullOrEmpty(line) && !line.startsWith("#")) {
            stopWords.add(line);
          }
        });
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    } else {
      Logger.getLogger(EnglishStopWords.class).severe("No dictionary defined for English stop words.");
    }
  }

  /**
   * Gets the instance.
   *
   * @return the instance
   */
  public static StopWords getInstance() {
    if (INSTANCE == null) {
      synchronized (EnglishStopWords.class) {
        if (INSTANCE == null) {
          INSTANCE = new EnglishStopWords();
        }
      }
    }
    return INSTANCE;
  }

  @Override
  protected boolean isTokenStopWord(Annotation token) {
    TokenType tokenType = token.get(Types.TOKEN_TYPE).as(TokenType.class, TokenType.UNKNOWN);
    if( tokenType.equals(TokenType.CHINESE_JAPANESE)){
      return true;
    }
    if (tokenType.equals(TokenType.URL)) {
      return true;
    }
    if (tokenType.equals(TokenType.EMOTICON)) {
      return true;
    }
    if (tokenType.equals(TokenType.EMAIL)) {
      return true;
    }
    if (tokenType.equals(TokenType.PUNCTUATION)) {
      return true;
    }
    if (tokenType.equals(TokenType.SGML)) {
      return true;
    }
    if (tokenType.equals(TokenType.PROTOCOL)) {
      return true;
    }
    if (token.contains(Types.PART_OF_SPEECH)) {
      POS tag = token.get(Types.PART_OF_SPEECH).as(POS.class);
      if (tag != null) {
        if (tag.isInstance(POS.ADJECTIVE, POS.ADVERB, POS.NOUN, POS.VERB)) {
          return isStopWord(token.toString()) || isStopWord(token.getLemma());
        }
        return true;
      }
    }
    return isStopWord(token.toString()) || isStopWord(token.getLemma());
  }

  @Override
  public boolean isStopWord(String word) {
    return Strings.isNullOrEmpty(word) ||
      stopWords.contains(word.toLowerCase()) ||
      !StringUtils.hasLetter(word);
  }


}//END OF EnglishStopWords
