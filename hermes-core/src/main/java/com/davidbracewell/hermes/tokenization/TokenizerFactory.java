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

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author David B. Bracewell
 */
public class TokenizerFactory implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Map<Language, Tokenizer> cache = new ConcurrentHashMap<>();

  public static Tokenizer create(@NonNull Language language) {
    if (!cache.containsKey(language)) {
      if (Config.hasProperty("hermes.Tokenizer", language)) {
        cache.put(language, Config.get("hermes.Tokenizer", language).as(Tokenizer.class));
      } else {
        cache.put(language, new BreakIteratorTokenizer(language.asLocale()));
      }
    }
    return cache.get(language);
  }

}//END OF TokenizerFactory
