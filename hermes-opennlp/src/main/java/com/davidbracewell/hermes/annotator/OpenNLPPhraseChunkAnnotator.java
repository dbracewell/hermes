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
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.attribute.POS;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.Span;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author David B. Bracewell
 */
public class OpenNLPPhraseChunkAnnotator implements Annotator, Serializable {
  private static final long serialVersionUID = 1L;
  private volatile Map<Language, ChunkerModel> chunkerModels = Maps.newEnumMap(Language.class);

  @Override
  public void annotate(Document document) {
    ChunkerME chunker = new ChunkerME(loadChunker(document.getLanguage()));
    for (Annotation sentence : document.sentences()) {
      List<Annotation> tokenList = sentence.tokens();
      String[] tokens = new String[tokenList.size()];
      String[] pos = new String[tokenList.size()];
      for (int i = 0; i < tokenList.size(); i++) {
        tokens[i] = tokenList.get(i).toString();
        pos[i] = tokenList.get(i).get(Types.PART_OF_SPEECH).as(POS.class).asString();
      }
      Span[] chunks = chunker.chunkAsSpans(tokens, pos);
      for (Span span : chunks) {
        sentence.document().createAnnotation(
          Types.PHRASE_CHUNK,
          tokenList.get(span.getStart()).union(tokenList.get(span.getEnd() - 1))
        ).put(Types.PART_OF_SPEECH, POS.valueOf(span.getType()));
      }
    }
  }

  private ChunkerModel loadChunker(Language language) {
    if (!chunkerModels.containsKey(language)) {
      synchronized (OpenNLPPhraseChunkAnnotator.class) {
        if (!chunkerModels.containsKey(language)) {
          try {
            chunkerModels.put(language, new ChunkerModel(Config.get("opennlp", language, "phrase_chunk", "model").asResource().inputStream()));
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
        }
      }
    }
    return chunkerModels.get(language);
  }


  @Override
  public Set<AnnotatableType> satisfies() {
    return Collections.singleton(Types.PHRASE_CHUNK);
  }

  @Override
  public Set<AnnotatableType> requires() {
    return new HashSet<>(Arrays.asList(Types.SENTENCE, Types.TOKEN, Types.PART_OF_SPEECH));
  }

  @Override
  public String getVersion() {
    return "1.6.0";
  }
}//END OF OpenNLPPhraseChunkAnnotator
