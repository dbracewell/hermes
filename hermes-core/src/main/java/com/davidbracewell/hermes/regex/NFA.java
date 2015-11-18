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


import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.HString;
import com.google.common.collect.ArrayListMultimap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of a non-deterministic finite state automata that works on a Text
 */
final class NFA implements Serializable {

  private static final long serialVersionUID = 1L;
  Node start = new Node(false);
  Node end = new Node(true);

  /**
   * Matches the
   *
   * @param input      the input
   * @param startIndex the start index
   * @return the int
   */
  public Match matches(HString input, int startIndex) {
    //The set of states that the NFA is in
    Set<State> states = new HashSet<>();

    ArrayListMultimap<String, HString> namedGroups = ArrayListMultimap.create();

    //Add the start state
    states.add(new State(startIndex, start));

    //All the accept states that it enters
    Set<State> accepts = new HashSet<>();

    List<Annotation> tokens = input.tokens();

    while (true) {
      Set<State> newStates = new HashSet<>(); //states after the next consumption

      for (State s : states) {
        if (s.node.accepts()) {
          accepts.add(s);
        }
        for (Node n : s.node.epsilons) {
          State next = new State(s.inputPosition, n);
          newStates.add(next);
        }
        if (s.inputPosition >= input.tokenLength()) {
          continue;
        }
        for (Transition t : s.node.transitions) {
          int len = t.transitionFunction.matches(tokens.get(s.inputPosition));
          if (len > 0) {
            State next;
            if (t.transitionFunction instanceof TransitionFunction.GroupMatcher) {
              next = new State(s.inputPosition + len, t.destination, Cast.<TransitionFunction.GroupMatcher>as(t.transitionFunction).name);
              namedGroups.put(Cast.<TransitionFunction.GroupMatcher>as(t.transitionFunction).name, HString.union(tokens.subList(s.inputPosition, s.inputPosition + len)));
            } else {
              next = new State(s.inputPosition + len, t.destination);
            }
            newStates.add(next);
          }
        }
      }
      states.clear();
      states = newStates;
      if (states.isEmpty()) {
        break;
      }
    }

    if (accepts.isEmpty()) {
      return new Match(-1, null);
    }

    int max = 0;
    for (State s : accepts) {
      max = Math.max(max, s.inputPosition);
    }

    if (max == startIndex) {
      max++;
    }

    return new Match(max,namedGroups);
  }

  static class State {
    final int inputPosition;
    final Node node;
    final String matchName;

    /**
     * Instantiates a new State.
     *
     * @param inputPosition the input position
     * @param node          the node
     */
    public State(int inputPosition, Node node) {
      this(inputPosition, node, null);
    }

    public State(int inputPosition, Node node, String matchName) {
      this.inputPosition = inputPosition;
      this.node = node;
      this.matchName = matchName;
    }

  }//END OF NFA$State

  static class Transition implements Serializable {

    private static final long serialVersionUID = 1L;
    final Node source;
    Node destination;
    final TransitionFunction transitionFunction;

    @Override
    public String toString() {
      return "[" + destination + ", " + transitionFunction + "]";
    }

    /**
     * Instantiates a new Transition.
     *
     * @param source             the source
     * @param destination        the destination
     * @param transitionFunction the consumer
     */
    public Transition(Node source, Node destination, TransitionFunction transitionFunction) {
      this.source = source;
      this.destination = destination;
      this.transitionFunction = transitionFunction;
    }

  }//END OF NFA$Transition

  static class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    boolean isAccept;
    final List<Transition> transitions = new ArrayList<>();
    final List<Node> epsilons = new ArrayList<>();

    /**
     * Instantiates a new Node.
     *
     * @param accept the accept
     */
    public Node(boolean accept) {
      this.isAccept = accept;
    }

    /**
     * Connect void.
     *
     * @param node               the node
     * @param transitionFunction the consumer
     */
    public void connect(Node node, TransitionFunction transitionFunction) {
      transitions.add(new Transition(this, node, transitionFunction));
    }

    /**
     * Connect void.
     *
     * @param node the node
     */
    public void connect(Node node) {
      epsilons.add(node);
    }

    /**
     * Accepts boolean.
     *
     * @return the boolean
     */
    public boolean accepts() {
      return isAccept;
    }

    @Override
    public String toString() {
      return super.toString() + "[" + accepts() + "]";
    }

  }//END OF NFA$Node

}//END OF NFA
