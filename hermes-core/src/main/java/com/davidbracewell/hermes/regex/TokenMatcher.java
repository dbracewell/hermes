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


import com.clearspring.analytics.util.Preconditions;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.HString;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A token equivalent of <code>java.util.regex.Matcher</code>
 *
 * @author David B. Bracewell
 */
public class TokenMatcher {

  /**
   * The Automaton.
   */
  final NFA automaton;
  /**
   * The Input.
   */
  final HString input;

  private int start = 0;
  private Match match = null;
  private int last = 0;
  private final List<Annotation> tokens;

  /**
   * Instantiates a new Token matcher.
   *
   * @param automaton the automaton
   * @param input     the input
   */
  TokenMatcher(NFA automaton, HString input) {
    this.automaton = automaton;
    this.input = input;
    this.tokens = input.tokens();
  }

  /**
   * Instantiates a new Token matcher.
   *
   * @param automaton the automaton
   * @param input     the input
   * @param start     the start
   */
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
      match = automaton.matches(input, start);
      if (match.getEndLocation() != -1) {
        last = match.getEndLocation();
        return true;
      }
    }
    start = -1;
    match = new Match(-1, null);
    return false;
  }

  /**
   * Start int.
   *
   * @return The start token of the match
   */
  public int start() {
    Preconditions.checkState(match != null, "Have not called find()");
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
    Preconditions.checkState(match != null, "Have not called find()");
    if (match.getEndLocation() >= 0) {
      return tokens.get(match.getEndLocation() - 1).end();
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

  /**
   * Group list.
   *
   * @param groupName the group name
   * @return the list
   */
  public List<HString> group(String groupName) {
    return Collections.unmodifiableList(match.getCaptures().get(groupName));
  }

  /**
   * Group names set.
   *
   * @return the set
   */
  public Set<String> groupNames() {
    return Collections.unmodifiableSet(match.getCaptures().keySet());
  }


}//END OF TokenMatcher
