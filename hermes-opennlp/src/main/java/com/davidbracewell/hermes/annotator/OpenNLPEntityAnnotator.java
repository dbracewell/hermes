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

package com.davidbracewell.hermes.annotator;

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.guava.common.collect.HashMultimap;
import com.davidbracewell.guava.common.collect.Multimap;
import com.davidbracewell.hermes.*;
import com.davidbracewell.io.resource.Resource;
import lombok.SneakyThrows;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

import java.io.Serializable;
import java.util.*;

import static com.davidbracewell.collection.map.Maps.map;

/**
 * @author David B. Bracewell
 */
public class OpenNLPEntityAnnotator implements Annotator, Serializable {
   private static final long serialVersionUID = 1L;

   public static final AnnotationType OPENNLP_ENTITY = AnnotationType.create("OPENNLP_ENTITY");

   private volatile Multimap<Language, TokenNameFinderModel> models = HashMultimap.create();

   @Override
   public void annotate(Document document) {
      Collection<TokenNameFinderModel> models = loadModels(document.getLanguage());
      for (Annotation sentence : document.sentences()) {
         List<Annotation> tokenList = sentence.tokens();
         String[] tokens = tokenList.stream().map(Object::toString).toArray(String[]::new);
         for (TokenNameFinderModel model : models) {
            NameFinderME finder = new NameFinderME(model);
            opennlp.tools.util.Span[] spans = finder.find(tokens);
            double[] probs = finder.probs(spans);
            for (int i = 0; i < spans.length; i++) {
               opennlp.tools.util.Span span = spans[i];
               document.createAnnotation(OPENNLP_ENTITY,
                                         tokenList.get(span.getStart()).union(tokenList.get(span.getEnd() - 1)))
                       .putAll(map(Types.ENTITY_TYPE, EntityType.create(span.getType().toUpperCase()),
                                   Types.CONFIDENCE, probs[i]));
            }
         }
      }
   }

   @SneakyThrows
   private Collection<TokenNameFinderModel> loadModels(Language language) {
      if (!models.containsKey(language)) {
         synchronized (OpenNLPEntityAnnotator.class) {
            if (!models.containsKey(language)) {
               for (Resource resource : Config.get("opennlp", language, "entity", "models").asList(Resource.class)) {
                  models.put(language, new TokenNameFinderModel(resource.inputStream()));
               }
            }
         }
      }
      return Collections.unmodifiableCollection(models.get(language));
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(OPENNLP_ENTITY);
   }

   @Override
   public Set<AnnotatableType> requires() {
      return new HashSet<>(Arrays.asList(Types.SENTENCE, Types.TOKEN));
   }

   @Override
   public String getVersion() {
      return "1.6.0";
   }
}//END OF OpenNLPEntityAnnotator
