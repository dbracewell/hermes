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
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.AttributeType;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.logging.Logger;
import com.davidbracewell.reflection.BeanUtils;
import com.davidbracewell.string.StringPredicates;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public final class LexiconManager implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(LexiconManager.class);
  private static final Cache<String, Lexicon> lexiconCache = CacheManager.getInstance().register(
    CacheSpec.<String, Lexicon>create()
      .engine("Guava")
      .maxSize(500)
      .loadingFunction(name -> createLexicon(name.toLowerCase()))
  );


  public static void clear() {
    lexiconCache.clear();
  }

  public static void remove(String name) {
    lexiconCache.invalidate(name);
  }

  private LexiconManager() {
    throw new IllegalAccessError();
  }

  public static Lexicon getLexicon(@NonNull String name) {
    return lexiconCache.get(name);
  }

  private static Lexicon createLexicon(String name) {
    if (Config.getPropertiesMatching(StringPredicates.STARTS_WITH(name, true)).isEmpty()) {
      log.warn("'{0}' does not exists returning an empty lexicon.", name);
      return EmptyLexicon.INSTANCE;
    } else try {
      return BeanUtils.getNamedBean(name, LexiconSpec.class).create();
    } catch (Exception e) {
      log.severe(e);
      log.warn("'{0}' does not exists returning an empty lexicon.", name);
      return EmptyLexicon.INSTANCE;
    }
  }

  public static void register(@NonNull String name, @NonNull Lexicon lexicon) {
    lexiconCache.put(name.toLowerCase(), lexicon);
  }

  private enum EmptyLexicon implements Lexicon {
    INSTANCE;

    @Override
    public List<HString> match(HString source) {
      return Collections.emptyList();
    }

    @Override
    public List<LexiconEntry> getEntries(HString hString) {
      return Collections.emptyList();
    }

    @Override
    public void add(LexiconEntry entry) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<String> iterator() {
      return Collections.emptyIterator();
    }

    @Override
    public int size() {
      return 0;
    }

    public AttributeType getTagAttributeType() {
      return null;
    }

    @Override
    public int getMaxTokenLength() {
      return 0;
    }

    @Override
    public boolean isCaseSensitive() {
      return false;
    }
  }

}//END OF LexiconManager
