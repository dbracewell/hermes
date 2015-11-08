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

import com.davidbracewell.logging.Logger;
import com.google.common.collect.Maps;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;

/**
 * @author David B. Bracewell
 */
public final class LexiconManager implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(LexiconManager.class);
  private static final ConcurrentMap<String, Lexicon> lexicons = Maps.newConcurrentMap();


  private static final Lexicon EMPTY = new SetLexicon(false, Collections.emptySet());

  private LexiconManager() {
    throw new IllegalAccessError();
  }


  public static Lexicon getLexicon(@NonNull String name) {
    String norm = name.toLowerCase();
    if (lexicons.containsKey(norm)) {
      return lexicons.get(norm);
    }
    log.warn("'{0}' does not exists returning an empty lexicon.", name);
    lexicons.put(norm, EMPTY);
    return EMPTY;
  }

  public static void register(@NonNull String name, @NonNull Lexicon lexicon) {
    lexicons.put(name.toLowerCase(), lexicon);
  }


}//END OF LexiconManager
