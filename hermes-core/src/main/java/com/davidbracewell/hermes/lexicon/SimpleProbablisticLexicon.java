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

import com.davidbracewell.io.resource.Resource;

import java.util.*;

/**
 * @author David B. Bracewell
 */
public class SimpleProbablisticLexicon extends ProbabilisticLexicon {
  private static final long serialVersionUID = 5222754361194149560L;

  private final Map<String, Double> lexicon;

  /**
   * Instantiates a new Tag lexicon.
   *
   * @param caseSensitive the case sensitive
   */
  public SimpleProbablisticLexicon(boolean caseSensitive, Resource... lexicons) {
    this(caseSensitive, Arrays.asList(lexicons));
  }

  public SimpleProbablisticLexicon(boolean caseSensitive, Collection<Resource> lexicons) {
    super(caseSensitive);
    this.lexicon = loadProbabilisticLexicon(lexicons);
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
  protected double probabilityImpl(String lexicalItem) {
    Double d = lexicon.get(lexicalItem);
    return d == null ? 0d : d;
  }

  @Override
  public int size() {
    return lexicon.size();
  }

}//END OF SimpleProbablisticLexicon
