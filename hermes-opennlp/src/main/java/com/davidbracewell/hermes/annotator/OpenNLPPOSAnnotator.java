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
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.guava.common.collect.Maps;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.POS;
import com.davidbracewell.hermes.Types;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * The type Open nLPPOS annotator.
 *
 * @author David B. Bracewell
 */
public class OpenNLPPOSAnnotator extends SentenceLevelAnnotator {
  private static final long serialVersionUID = 1L;
  private volatile Map<Language, POSModel> posModels = Maps.newEnumMap(Language.class);

  @Override
  public void annotate(Annotation sentence) {
    POSTaggerME posTagger = new POSTaggerME(loadPOSTagger(sentence.getLanguage()));
    String[] tokens = sentence.tokens().stream().map(Object::toString).toArray(String[]::new);
    String[] tags = posTagger.tag(tokens);
    for (int i = 0; i < tokens.length; i++) {
      Annotation token = sentence.tokenAt(i);
      token.put(Types.PART_OF_SPEECH, POS.fromString(tags[i]));
    }
  }

  private POSModel loadPOSTagger(Language language) {
    if (!posModels.containsKey(language)) {
      synchronized (OpenNLPPOSAnnotator.class) {
        if (!posModels.containsKey(language)) {
          try {
            posModels.put(language, new POSModel(Config.get("opennlp", language, "part_of_speech", "model").asResource().inputStream()));
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
        }
      }
    }
    return posModels.get(language);
  }

  @Override
  public Set<AnnotatableType> satisfies() {
    return Collections.singleton(Types.PART_OF_SPEECH);
  }

  @Override
  public Set<AnnotatableType> furtherRequires() {
    return Collections.singleton(Types.TOKEN);
  }

  @Override
  public String getVersion() {
    return "1.6.0";
  }

}//END OF OpenNLPPOSAnnotator
