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

import com.davidbracewell.collection.Collect;
import com.davidbracewell.hermes.Annotatable;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.tag.Entities;
import com.davidbracewell.hermes.tag.EntityType;
import com.davidbracewell.hermes.tokenization.TokenType;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class TokenTypeEntityAnnotator extends SentenceLevelAnnotator {
  private static final long serialVersionUID = 1L;

  private static Map<TokenType, EntityType> mapping = ImmutableMap.<TokenType, EntityType>builder()
    .put(TokenType.EMAIL, Entities.EMAIL)
    .put(TokenType.URL, Entities.URL)
    .put(TokenType.MONEY, Entities.MONEY)
    .put(TokenType.NUMBER, Entities.NUMBER)
    .put(TokenType.EMOTICON, Entities.EMOTICON)
    .put(TokenType.COMPANY, Entities.ORGANIZATION)
    .put(TokenType.HASH_TAG, Entities.HASH_TAG)
    .put(TokenType.REPLY, Entities.REPLY)
    .put(TokenType.TIME, Entities.TIME)
    .build();

  @Override
  public void annotate(Annotation sentence) {
    sentence.tokens().forEach(token -> {
      TokenType type = token.get(Attrs.TOKEN_TYPE).as(TokenType.class, TokenType.UNKNOWN);
      if (mapping.containsKey(type)) {
        sentence.document().createAnnotation(
          Types.TOKEN_TYPE_ENTITY,
          token,
          Collect.map(Attrs.ENTITY_TYPE, mapping.get(type), Attrs.CONFIDENCE, 1.0)
        );
      }
    });
  }

  @Override
  public Set<Annotatable> satisfies() {
    return Collections.singleton(Types.TOKEN_TYPE_ENTITY);
  }

  @Override
  protected Set<Annotatable> furtherRequires() {
    return Collections.singleton(Types.TOKEN);
  }

}//END OF TokenTypeEntityAnnotator
