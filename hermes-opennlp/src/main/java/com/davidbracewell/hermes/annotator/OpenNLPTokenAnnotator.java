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
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.davidbracewell.collection.map.Maps.map;

/**
 * The type Open nLP token annotator.
 *
 * @author David B. Bracewell
 */
public class OpenNLPTokenAnnotator implements Annotator, Serializable {
  private static final long serialVersionUID = 1L;
  private volatile Map<Language, TokenizerModel> tokenModels = Maps.newEnumMap(Language.class);


  @Override
  public void annotate(Document document) {
    TokenizerME tokenizer = new TokenizerME(loadTokenizer(document.getLanguage()));
    Span[] spans = tokenizer.tokenizePos(document.toString());
    for (int i = 0; i < spans.length; i++) {
      document.createAnnotation(Types.TOKEN, spans[i].getStart(), spans[i].getEnd(), map(Types.INDEX, i));
    }
  }

  @Override
  public Set<AnnotatableType> satisfies() {
    return Collections.singleton(Types.TOKEN);
  }


  private TokenizerModel loadTokenizer(Language language) {
    if (!tokenModels.containsKey(language)) {
      synchronized (OpenNLPTokenAnnotator.class) {
        if (!tokenModels.containsKey(language)) {
          try {
            tokenModels.put(language,
                            new TokenizerModel(Config.get("opennlp", language, "tokenizer", "model")
                                                     .asResource()
                                                     .inputStream())
            );
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
        }
      }
    }
    return tokenModels.get(language);
  }

  @Override
  public String getVersion() {
    return "1.6.0";
  }

}//END OF OpenNLPTokenAnnotator
