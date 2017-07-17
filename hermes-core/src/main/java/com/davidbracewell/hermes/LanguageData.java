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
import com.davidbracewell.apollo.affinity.Similarity;
import com.davidbracewell.apollo.linalg.store.DefaultVectorStore;
import com.davidbracewell.apollo.ml.EncoderPair;
import com.davidbracewell.apollo.ml.NoOptEncoder;
import com.davidbracewell.apollo.ml.NoOptLabelEncoder;
import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.guava.common.cache.Cache;
import com.davidbracewell.guava.common.cache.CacheBuilder;
import com.davidbracewell.guava.common.cache.CacheLoader;
import com.davidbracewell.guava.common.cache.LoadingCache;
import com.davidbracewell.hermes.lexicon.Lexicon;
import com.davidbracewell.hermes.lexicon.LexiconSpec;
import com.davidbracewell.hermes.lexicon.TrieLexicon;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Loggable;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

import java.util.concurrent.ExecutionException;

/**
 * The type Language data.
 *
 * @author David B. Bracewell
 */
public final class LanguageData implements Loggable {

   private static final Resource baseClasspath = Resources.fromClasspath("hermes/");
   private static final LoadingCache<Language, Embedding> embeddingCache = CacheBuilder
                                                                              .from("maximumSize=25")
                                                                              .build(new EmbeddingLoader());
   private static final Cache<String, Lexicon> lexicons = CacheBuilder
                                                             .from("maximumSize=500")
                                                             .build();


   private LanguageData() {
      throw new IllegalAccessError();
   }

   /**
    * Gets default embedding model.
    *
    * @param language the language
    * @return the default embedding model
    */
   public static Embedding getDefaultEmbedding(@NonNull Language language) {
      try {
         return embeddingCache.get(language);
      } catch (ExecutionException e) {
         throw Throwables.propagate(e);
      }
   }

   public static Lexicon getLexicon(@NonNull Language language, @NonNull String lexiconName) {
      return getLexicon(language, lexiconName, LexiconSpec.builder().caseSensitive(false).probabilistic(false).build());
   }

   public static Lexicon getLexicon(@NonNull Language language, @NonNull String lexiconName, @NonNull LexiconSpec spec) {
      String name = language.getCode() + "::" + lexiconName;
      try {
         return lexicons.get(name, () -> {
            for (Resource lexicon : new Resource[]{
               baseClasspath.getChild(lng2Folder(language)).getChild("lexicons").getChild(lexiconName),
               baseClasspath.getChild("lexicons").getChild(lexiconName)
            }) {
               if (lexicon.exists()) {
                  spec.setResource(lexicon);
                  return spec.create();
               }
            }
            return new TrieLexicon(true, true, null);
         });
      } catch (ExecutionException e) {
         throw Throwables.propagate(e);
      }
   }

   private static String lng2Folder(Language language) {
      if (language == Language.UNKNOWN) {
         return StringUtils.EMPTY;
      }
      return language
                .getCode()
                .toLowerCase();
   }


   private static class EmbeddingLoader extends CacheLoader<Language, Embedding> {

      @Override
      public Embedding load(Language language) throws Exception {
         Resource loc = Hermes.findModel(language, "embedding", StringUtils.EMPTY);
         if (loc != null && loc.exists()) {
            return Embedding.read(loc);
         }
         return new Embedding(new EncoderPair(new NoOptLabelEncoder(), new NoOptEncoder()),
                              new DefaultVectorStore<>(100, Similarity.Cosine));
      }
   }


}//END OF LanguageData
