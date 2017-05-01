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

package com.davidbracewell.hermes;

import com.davidbracewell.Language;
import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.guava.common.cache.Cache;
import com.davidbracewell.guava.common.cache.CacheBuilder;
import com.davidbracewell.hermes.lexicon.Lexicon;
import com.davidbracewell.hermes.lexicon.LexiconSpec;
import com.davidbracewell.hermes.lexicon.TrieLexicon;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Logger;
import lombok.NonNull;

import java.util.concurrent.ExecutionException;

/**
 * @author David B. Bracewell
 */
public final class LanguageData {

   private LanguageData() {
      throw new IllegalAccessError();
   }

   private static final Logger log = Logger.getLogger(LanguageData.class);
   private static final Resource baseClasspath = Resources.fromClasspath("hermes/");
   private static final Cache<String, Lexicon> lexicons = CacheBuilder.from("maximumSize=500").build();
   private static final Cache<String, Embedding> embeddingCache = CacheBuilder.from("maximumSize=25").build();

   private static String lng2Folder(Language language) {
      return language.getCode().toLowerCase();
   }


   public static Lexicon loadSubjectiveLexicon(@NonNull Language language) {
      try {
         return lexicons.get(language.getCode() + "::Sentiment",
                             () -> {
                                try {
                                   return LexiconSpec
                                             .builder()
                                             .caseSensitive(false)
                                             .tagAttribute(Types.TAG)
                                             .hasConstraints(true)
                                             .resource(
                                                baseClasspath.getChild(lng2Folder(language))
                                                             .getChild("lexicon")
                                                             .getChild("subjective.dict"))
                                             .build()
                                             .create();
                                } catch (Exception e) {
                                   log.severe("Error Loading Sentiment lexicon: {0}", e);
                                   return new TrieLexicon(false, false, Types.TAG);
                                }
                             });
      } catch (ExecutionException e) {
         throw Throwables.propagate(e);
      }
   }


}//END OF LanguageData
