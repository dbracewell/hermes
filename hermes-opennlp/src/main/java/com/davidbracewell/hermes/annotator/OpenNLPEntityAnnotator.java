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
import com.davidbracewell.collection.Collect;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.tag.EntityType;
import com.davidbracewell.io.resource.Resource;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author David B. Bracewell
 */
public class OpenNLPEntityAnnotator implements Annotator, Serializable {
  private static final long serialVersionUID = 1L;

  public static final AnnotationType OPENNLP_ENTITY = AnnotationType.create("OPENNLP_ENTITY");

  private volatile Multimap<Language, TokenNameFinderModel> models = HashMultimap.create();

  @Override
  public void annotate(Document document) {
    for (Annotation sentence : document.sentences()) {
      List<Annotation> tokenList = sentence.tokens();
      String[] tokens = tokenList.stream().map(Object::toString).toArray(String[]::new);
      for (TokenNameFinderModel model : loadModels(document.getLanguage())) {
        NameFinderME finder = new NameFinderME(model);
        opennlp.tools.util.Span[] spans = finder.find(tokens);
        double[] probs = finder.probs(spans);
        for (int i = 0; i < spans.length; i++) {
          opennlp.tools.util.Span span = spans[i];
          document.createAnnotation(
              OPENNLP_ENTITY,
              tokenList.get(span.getStart()).union(tokenList.get(span.getEnd() - 1))
          ).putAll(
            Collect.map(
              Types.ENTITY_TYPE, EntityType.create(span.getType().toUpperCase()),
              Types.CONFIDENCE, probs[i]
            )
          );
        }
      }
    }
  }

  private Collection<TokenNameFinderModel> loadModels(Language language) {
    if (!models.containsKey(language)) {
      synchronized (OpenNLPEntityAnnotator.class) {
        if (!models.containsKey(language)) {
          for (Resource resource : Config.get("opennlp", language, "entity","models").asList(Resource.class)) {
            try {
              models.put(language, new TokenNameFinderModel(resource.inputStream()));
            } catch (IOException e) {
              throw Throwables.propagate(e);
            }
          }
        }
      }
    }
    return models.get(language);
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
