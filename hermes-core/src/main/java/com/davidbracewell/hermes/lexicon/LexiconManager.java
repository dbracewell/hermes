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

import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.lexicon.spi.LexiconSupplier;
import com.davidbracewell.logging.Logger;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentMap;

/**
 * @author David B. Bracewell
 */
public final class LexiconManager implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(LexiconManager.class);
  private static final ConcurrentMap<String, Lexicon> lexicons = Maps.newConcurrentMap();

  private static final Map<Class<?>, LexiconSupplier> lexiconSupplierMap;

  static {
    ImmutableMap.Builder<Class<?>, LexiconSupplier> builder = ImmutableMap.builder();
    for (LexiconSupplier supplier : ServiceLoader.load(LexiconSupplier.class)) {
      builder.put(supplier.getLexiconClass(), supplier);
    }
    lexiconSupplierMap = builder.build();
  }


  private LexiconManager() {
    throw new IllegalAccessError();
  }

  public static <T extends Lexicon> T getLexicon(@NonNull String name) {
    String norm = name.toLowerCase();
    if (!lexicons.containsKey(norm)) {
      synchronized (lexicons) {
        if (!lexicons.containsKey(norm)) {
          lexicons.put(norm, createLexicon(norm));
        }
      }
    }
    return Cast.as(lexicons.get(norm));
  }

  private static Lexicon createLexicon(String name) {
    Class<?> clazz = Config.get(name, "class").asClass();
    if (clazz != null) {
      try {
        return lexiconSupplierMap.get(clazz).get(name);
      } catch (IOException e) {
        log.severe(e);
        throw Throwables.propagate(e);
      }
    }
    log.warn("'{0}' does not exists returning an empty lexicon.", name);
    return EmptyLexicon.INSTANCE;
  }

  public static void register(@NonNull String name, @NonNull Lexicon lexicon) {
    lexicons.put(name.toLowerCase(), lexicon);
  }

  private enum EmptyLexicon implements Lexicon {
    INSTANCE;

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
