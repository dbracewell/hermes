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


import com.davidbracewell.guava.common.collect.ArrayListMultimap;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.tuple.Tuple2;
import com.davidbracewell.tuple.Tuples;

import java.io.Serializable;
import java.util.*;

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


    //Add the start state
    states.add(new State(startIndex, start));

    //All the accept states that it enters
    NavigableSet<State> accepts = new TreeSet<>();

    List<Annotation> tokens = input.tokens();


    while (!states.isEmpty()) {
      Set<State> newStates = new HashSet<>(); //states after the next consumption

      for (State s : states) {

        if (s.node.accepts()) {
          if (s.stack.isEmpty() || (s.stack.size() == 1 && s.node.consumes && s.node.name.equals(s.stack.peek()))) {
            accepts.add(s);
          }
        }

        Deque<Tuple2<String, Integer>> currentStack = s.stack;
        if (s.node.emits) {
          currentStack.push(Tuples.$(s.node.name, s.inputPosition));
        }

        for (Node n : s.node.epsilons) {
          if (s.node.consumes) {
            State next = new State(s.inputPosition, n, currentStack, s.namedGroups);
            Tuple2<String, Integer> ng = next.stack.pop();
            next.namedGroups.put(ng.getKey(), HString.union(tokens.subList(ng.v2, s.inputPosition)));
            newStates.add(next);
          }

          State next = new State(s.inputPosition, n, currentStack, s.namedGroups);
          newStates.add(next);
        }

        if (s.inputPosition >= input.tokenLength()) {
          continue;
        }

        for (Transition t : s.node.transitions) {
          int len = t.transitionFunction.matches(tokens.get(s.inputPosition));
          if (len > 0) {
            State next = new State(s.inputPosition + len, t.destination, currentStack, s.namedGroups);
            newStates.add(next);
          }
        }
      }
      states.clear();
      states = newStates;
    }

    if (accepts.isEmpty()) {
      return new Match(-1, null);
    }


    State last = accepts.last();
    int max = last.inputPosition;

    State temp = accepts.last();
    while (temp != null && temp.inputPosition >= max) {
      temp = accepts.lower(temp);
    }

    if (max == startIndex) {
      max++;
    }

    return new Match(max, last.namedGroups);
  }

  static class State implements Comparable<State> {
    final int inputPosition;
    final Node node;
    final Deque<Tuple2<String, Integer>> stack;
    final ArrayListMultimap<String, HString> namedGroups = ArrayListMultimap.create();

    /**
     * Instantiates a new State.
     *
     * @param inputPosition the input position
     * @param node          the node
     */
    public State(int inputPosition, Node node) {
      this(inputPosition, node, new LinkedList<>(), ArrayListMultimap.create());
    }

    public State(int inputPosition, Node node, Deque<Tuple2<String, Integer>> currentStack, ArrayListMultimap<String, HString> namedGroups) {
      this.inputPosition = inputPosition;
      this.node = node;
      this.stack = new LinkedList<>(currentStack);
      this.namedGroups.putAll(namedGroups);
    }

    private int score() {
      return inputPosition + namedGroups.size() * namedGroups.values().stream().mapToInt(HString::tokenLength).sum();
    }

    @Override
    public int compareTo(State o) {
      return Integer.compare(score(), o.score());
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
    String name = null;
    boolean emits = false;
    boolean consumes = false;
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
