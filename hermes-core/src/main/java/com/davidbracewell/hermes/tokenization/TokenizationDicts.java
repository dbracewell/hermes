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

package com.davidbracewell.hermes.tokenization;

import com.davidbracewell.config.Config;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public class TokenizationDicts {

  private static volatile TokenizationDicts INSTANCE;

  private final Set<String> abbreviations;
  private final Set<String> prefixes;
  private final Set<String> tlds;


  public static TokenizationDicts getInstance() {
    if (INSTANCE == null) {
      synchronized (TokenizationDicts.class) {
        if (INSTANCE == null) {
          INSTANCE = new TokenizationDicts();
        }
      }
    }
    return INSTANCE;
  }


  public TokenType getType(String input) {
    if (abbreviations.contains(input)) {
      return TokenType.ACRONYM;
    } else if (prefixes.contains(input.toLowerCase())) {
      return TokenType.PREFIX;
    } else if (tlds.contains(input.toLowerCase())) {
      return TokenType.URL;
    }
    return TokenType.UNKNOWN;
  }


  private TokenizationDicts() {
    try {
      this.abbreviations = Config.get("hermes.DefaultTokenizer.abbreviations")
        .asResource().readLines().stream().map(StringUtils::trim).collect(Collectors.toSet());
      this.prefixes = Config.get("hermes.DefaultTokenizer.prefixes")
        .asResource().readLines().stream().map(StringUtils::trim).collect(Collectors.toSet());
      this.tlds = Config.get("hermes.DefaultTokenizer.tlds")
        .asResource().readLines().stream().map(StringUtils::trim).collect(Collectors.toSet());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }


}//END OF TokenizationDicts
