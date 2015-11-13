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

import com.davidbracewell.cache.Cache;
import com.davidbracewell.cache.CacheManager;
import com.davidbracewell.cache.CacheSpec;
import com.davidbracewell.cache.GuavaLoadingCache;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.logging.Logger;
import com.google.common.collect.Iterators;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author David B. Bracewell
 */
public final class LexiconManager implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(LexiconManager.class);
  private static final Cache<String, Lexicon> lexiconCache = CacheManager.getInstance().register(
    CacheSpec.<String, Lexicon>create()
      .engine(GuavaLoadingCache.class.getName())
      .maxSize(50)
      .loadingFunction(name -> createLexicon(name.toLowerCase()))
  );


  private LexiconManager() {
    throw new IllegalAccessError();
  }

  public static <T extends Lexicon> T getLexicon(@NonNull String name) {
    return Cast.as(lexiconCache.get(name));
  }

  private static Lexicon createLexicon(String name) {
    log.warn("'{0}' does not exists returning an empty lexicon.", name);
    return EmptyLexicon.INSTANCE;
  }

  public static void register(@NonNull String name, @NonNull Lexicon lexicon) {
    lexiconCache.put(name.toLowerCase(), lexicon);
  }

  private enum EmptyLexicon implements Lexicon {
    INSTANCE;

    @Override
    public List<HString> find(HString source) {
      return Collections.emptyList();
    }

    @Override
    public void add(LexiconEntry entry) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<String> iterator() {
      return Iterators.emptyIterator();
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Optional<String> getMatch(HString hString) {
      return Optional.empty();
    }
  }

}//END OF LexiconManager
