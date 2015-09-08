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

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.tag.Tag;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.tuple.Tuple2;

import java.util.*;

/**
 * @author David B. Bracewell
 */
public class SimpleTagLexicon extends TagLexicon {
  private static final long serialVersionUID = 1L;
  private final Map<String, Tuple2<Tag, Double>> lexicon;

  public SimpleTagLexicon(boolean caseSensitive, Attribute tagAttribute, Resource... lexicons) {
    this(caseSensitive, tagAttribute, Arrays.asList(lexicons));
  }

  public SimpleTagLexicon(boolean caseSensitive, Attribute tagAttribute, Collection<Resource> lexicons) {
    super(caseSensitive);
    this.lexicon = loadLexicon(lexicons, Cast.<Class<? extends Tag>>as(tagAttribute.getValueType().getType()));
  }

  @Override
  protected boolean containsImpl(String nonNullLexicalItem) {
    return lexicon.containsKey(nonNullLexicalItem);
  }

  @Override
  public Set<String> lexicalItems() {
    return Collections.unmodifiableSet(lexicon.keySet());
  }

  @Override
  protected Optional<Tag> lookupImpl(String lexicalItem) {
    Tuple2<Tag, Double> tagValue = lexicon.get(lexicalItem);
    if (tagValue == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(tagValue.getKey());
  }

  @Override
  protected double probabilityImpl(String lexicalItem) {
    Tuple2<Tag, Double> tagValue = lexicon.get(lexicalItem);
    if (tagValue == null) {
      return 0d;
    }
    return tagValue.getValue() == null ? 0d : tagValue.getValue();
  }

  @Override
  public int size() {
    return lexicon.size();
  }

}//END OF TagLexicon
