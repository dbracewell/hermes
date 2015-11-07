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

package com.davidbracewell.hermes.regex;


import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.HString;

import java.util.List;

/**
 * A token equivalent of <code>java.util.regex.Matcher</code>
 *
 * @author David B. Bracewell
 */
public class TokenMatcher {

  final NFA automaton;
  final HString input;

  private int start = 0;
  private int end = 0;
  private int last = 0;
  private final List<Annotation> tokens;

  TokenMatcher(NFA automaton, HString input) {
    this.automaton = automaton;
    this.input = input;
    this.tokens = input.tokens();
  }

  TokenMatcher(NFA automaton, HString input, int start) {
    this.automaton = automaton;
    this.input = input;
    this.last = start;
    this.tokens = input.tokens();
  }

  /**
   * Find boolean.
   *
   * @return True if the pattern finds a next match
   */
  public boolean find() {
    start = last;
    for (; start < tokens.size(); start++) {
      end = automaton.matches(input, start);
      if (end != -1) {
        last = end;
        return true;
      }
    }
    start = -1;
    end = -1;
    return false;
  }

  /**
   * Start int.
   *
   * @return The start token of the match
   */
  public int start() {
    if (start >= 0) {
      return tokens.get(start).start();
    }
    return -1;
  }

  /**
   * End int.
   *
   * @return The end end token of the match
   */
  public int end() {
    if (end >= 0) {
      return tokens.get(end - 1).end();
    }
    return -1;
  }

  /**
   * Group span.
   *
   * @return the span
   */
  public HString group() {
    return input.document().substring(start(), end());
  }


}//END OF TokenMatcher
