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

import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.HString;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A consumer represents a set of nodes and edges in an NFA
 */
interface TransitionFunction extends Serializable {

  /**
   * Determines if the consumer matches on a given token.
   *
   * @param input the token to check
   * @return True if the consumer matches, i.e. can consume, the given token
   */
  int matches(HString input);

  int nonMatch(HString input);

  /**
   * Construct an NFA for the consumer.
   *
   * @return An NFA representing the consumer.
   */
  NFA construct();

  final class ParentMatcher implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    final TransitionFunction child;

    public ParentMatcher(TransitionFunction child) {
      this.child = child;
    }

    @Override
    public int matches(HString input) {
      return input.getParent().map(child::matches).orElse(0);
    }

    @Override
    public int nonMatch(HString input) {
      return input.getParent().map(child::nonMatch).orElse(1);
    }

    @Override
    public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
    }

    @Override
    public String toString() {
      return "/> " + child.toString();
    }
  }

  final class AnnotationMatcher implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    final AnnotationType type;
    final TransitionFunction child;

    public AnnotationMatcher(AnnotationType type, TransitionFunction child) {
      this.child = child;
      this.type = type;
    }

    @Override
    public int matches(HString input) {
      int max = 0;
      for (Annotation a : input.getStartingHere(type)) {
        max = Math.max(child.matches(a), max);
      }
      return max;
    }

    @Override
    public int nonMatch(HString input) {
      int min = input.tokenLength();
      for (Annotation a : input.getStartingHere(type)) {
        min = Math.min(child.nonMatch(a), min);
      }
      return min;
    }

    @Override
    public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
    }

    @Override
    public String toString() {
      return "${" + type.name() + "} " + child.toString();
    }
  }

  final class PredicateMatcher implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    final String pattern;
    final Predicate<? super HString> predicate;

    public PredicateMatcher(String pattern, SerializablePredicate<? super HString> predicate) {
      this.pattern = pattern;
      this.predicate = predicate;
    }

    @Override
    public int matches(HString input) {
      return predicate.test(input) ? input.tokenLength() : 0;
    }

    @Override
    public int nonMatch(HString input) {
      return predicate.test(input) ? 0 : input.tokenLength();
    }

    @Override
    public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
    }

    @Override
    public String toString() {
      return pattern;
    }
  }

  final class LookAhead implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;

    final TransitionFunction child;
    final TransitionFunction lookAhead;
    final boolean negativeLookAhead;

    public LookAhead(TransitionFunction child, TransitionFunction lookAhead, boolean negativeLookAhead) {
      this.child = child;
      this.lookAhead = lookAhead;
      this.negativeLookAhead = negativeLookAhead;
    }

    @Override
    public int matches(HString input) {
      int m = child.matches(input);
      if (m > 0) {
        Annotation next = next(input, m);
        if (negativeLookAhead) {
          return lookAhead.nonMatch(next) > 0 ? m : 0;
        }
        return lookAhead.matches(next) > 0 ? m : 0;
      }
      return 0;
    }

    private Annotation next(HString input, int m) {
      Annotation lToken = input.tokens().get(input.tokenLength() - 1);
      while (m > 0) {
        m--;
        lToken = lToken.next();
      }
      return lToken;
    }

    @Override
    public int nonMatch(HString input) {
      int m = child.nonMatch(input);
      if (m > 0) {
        Annotation next = next(input, m);
        if (negativeLookAhead) {
          return lookAhead.matches(next) > 0 ? m : 0;
        }
        return lookAhead.nonMatch(next) > 0 ? m : 0;
      }
      return 0;
    }

    @Override
    public NFA construct() {
      NFA nEnd = new NFA();
      nEnd.start.connect(nEnd.end, this);
      return nEnd;
    }

    @Override
    public String toString() {
      return "(" + child.toString() + ") (?" + (negativeLookAhead ? "!> " : "> ") + lookAhead.toString() + ")";
    }

  }//END OF LookAhead

  final class LogicStatement implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    final Function<? super HString, Integer> matcher;
    final String pattern;

    public LogicStatement(String pattern, Function<? super HString, Integer> matcher) {
      this.pattern = pattern;
      this.matcher = matcher;
    }

    @Override
    public int matches(HString input) {
      return matcher.apply(input);
    }

    @Override
    public int nonMatch(HString input) {
      if (matcher.apply(input) > 0) {
        return 0;
      }
      return 1;
    }

    public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
    }

    @Override
    public String toString() {
      return "[" + pattern + "]";
    }
  }//END OF LogicStatement

  final class Not implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    TransitionFunction c1;

    public Not(TransitionFunction c1) {
      this.c1 = c1;
    }

    @Override
    public int matches(HString token) {
      return c1.nonMatch(token);
    }

    @Override
    public int nonMatch(HString input) {
      return c1.matches(input);
    }

    @Override
    public NFA construct() {
      NFA parent = new NFA();
      NFA child1 = c1.construct();
      child1.end.isAccept = false;
      parent.start.connect(child1.start);
      child1.start.connect(parent.end, this);
      return parent;
    }

    @Override
    public String toString() {
      return "^(" + c1 + ")";
    }

  }//END OF Alternation

  final class Alternation implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    TransitionFunction c1;
    TransitionFunction c2;

    public Alternation(TransitionFunction c1, TransitionFunction c2) {
      super();
      this.c1 = c1;
      this.c2 = c2;
    }

    @Override
    public int matches(HString token) {
      return Math.max(c1.matches(token), c2.matches(token));
    }

    @Override
    public int nonMatch(HString input) {
      int m1 = c1.nonMatch(input);
      int m2 = c2.nonMatch(input);
      if (m1 > 0 && m2 > 0) {
        return Math.max(m1, m2);
      }
      return 0;
    }

    @Override
    public NFA construct() {
      NFA parent = new NFA();
      NFA child1 = c1.construct();
      NFA child2 = c2.construct();

      child1.end.isAccept = false;
      child2.end.isAccept = false;

      parent.start.connect(child1.start);
      parent.start.connect(child2.start);

      child1.end.connect(parent.end);
      child2.end.connect(parent.end);

      return parent;
    }

    @Override
    public String toString() {
      return "(" + c1 + " | " + c2 + ")";
    }

  }//END OF Alternation

  final class Sequence implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    final TransitionFunction c1;
    final TransitionFunction c2;

    public Sequence(TransitionFunction c1, TransitionFunction c2) {
      this.c1 = c1;
      this.c2 = c2;
    }

    @Override
    public int matches(HString token) {
      return 1;
    }

    @Override
    public int nonMatch(HString input) {
      int i = c1.matches(input);
      if (i > 0) {
        Annotation next = input.lastToken().next();
        for (int j = 1; j < i; j++) {
          next = next.next();
        }
        if (next.isEmpty()) {
          return i;
        }
        return c2.nonMatch(next);
      }
      i = c1.nonMatch(input);
      Annotation next = input.lastToken().next();
      for (int j = 1; j < i; j++) {
        next = next.next();
      }
      return i + Math.max(c2.matches(next), c2.nonMatch(next));
    }

    @Override
    public NFA construct() {
      NFA base = new NFA();
      NFA nfa1 = c1.construct();
      NFA nfa2 = c2.construct();

      base.start.connect(nfa1.start);

      nfa1.end.isAccept = false;
      nfa1.end.connect(nfa2.start);

      nfa2.end.isAccept = false;
      nfa2.end.connect(base.end);

      return base;
    }

    @Override
    public String toString() {
      return c1 + " " + c2;
    }


  }//END OF Sequence

  final class OneOrMore implements TransitionFunction, Serializable {

    private static final long serialVersionUID = 1L;
    final TransitionFunction child;

    public OneOrMore(TransitionFunction child) {
      this.child = child;
    }

    @Override
    public int matches(HString token) {
      return child.matches(token);
    }

    @Override
    public int nonMatch(HString input) {
      return child.nonMatch(input);
    }

    @Override
    public NFA construct() {
      NFA nfa = new NFA();

      NFA first = child.construct();
      first.end.isAccept = false;
      first.end.connect(first.start);

      nfa.start.connect(first.start);
      first.end.connect(nfa.end);

      return nfa;
    }

    @Override
    public String toString() {
      return child.toString() + "+";
    }

  }//END OF OneOrMore

  final class KleeneStar implements TransitionFunction, Serializable {

    private static final long serialVersionUID = 1L;
    final TransitionFunction child;

    public KleeneStar(TransitionFunction child) {
      this.child = child;
    }

    @Override
    public int matches(HString input) {
      return child.matches(input);
    }


    @Override
    public int nonMatch(HString input) {
      return child.nonMatch(input);
    }

    public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end);

      NFA childNFA = child.construct();
      childNFA.end.isAccept = false;

      nfa.start.connect(childNFA.start);
      childNFA.end.connect(childNFA.start);
      childNFA.end.connect(nfa.end);

      return nfa;
    }

    @Override
    public String toString() {
      return child.toString() + "*";
    }

  }//END OF KleeneStar

  final class ZeroOrOne implements TransitionFunction, Serializable {

    private static final long serialVersionUID = 1L;
    final TransitionFunction child;

    public ZeroOrOne(TransitionFunction child) {
      this.child = child;
    }

    @Override
    public int matches(HString token) {
      return child.matches(token);
    }


    @Override
    public int nonMatch(HString input) {
      return child.nonMatch(input);
    }


    @Override
    public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end);

      NFA first = child.construct();
      first.end.isAccept = false;

      nfa.start.connect(first.start);
      first.end.connect(nfa.end);

      return nfa;
    }

    @Override
    public String toString() {
      return child.toString() + "?";
    }

  }//END OF ZeroOrOne

  final class Range implements TransitionFunction, Serializable {

    private static final long serialVersionUID = 1L;
    final TransitionFunction child;
    final int low;
    final int high;

    public Range(TransitionFunction child, int low, int high) {
      this.child = child;
      this.low = low;
      this.high = high;
    }

    @Override
    public int matches(HString token) {
      return child.matches(token);
    }


    @Override
    public int nonMatch(HString input) {
      return child.nonMatch(input);
    }


    @Override
    public NFA construct() {
      NFA nfa = new NFA();

      TransitionFunction lowT = child;
      for (int i = 1; i < low; i++) {
        lowT = new Sequence(lowT, child);
      }

      NFA lowNFA = lowT.construct();
      lowNFA.end.isAccept = false;
      nfa.start.connect(lowNFA.start);
      lowNFA.end.connect(nfa.end);

      if (high == Integer.MAX_VALUE) {
        NFA tmp = child.construct();
        tmp.end.isAccept = false;
        tmp.end.connect(tmp.start);
        tmp.end.connect(nfa.end);
        lowNFA.end.connect(tmp.start);
      } else if (high > low) {
        TransitionFunction highT = child;
        for (int i = 1; i < high; i++) {
          highT = new Sequence(highT, child);
          NFA tmp = highT.construct();
          tmp.end.isAccept = false;
          lowNFA.start.connect(tmp.start);
          tmp.end.connect(nfa.end);
          lowNFA = tmp;
        }
      }

      return nfa;
    }

    @Override
    public String toString() {
      return child.toString() + "{" + low + "," + (high == Integer.MAX_VALUE ? "*" : high) + "}";
    }

  }//END OF ZeroOrOne


}//END OF TransitionFunction
