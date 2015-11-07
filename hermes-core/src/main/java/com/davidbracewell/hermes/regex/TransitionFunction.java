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

import com.davidbracewell.Tag;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.lexicon.Lexicon;
import com.davidbracewell.hermes.lexicon.LexiconManager;
import com.davidbracewell.string.StringPredicates;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * A consumer represents a set of nodes and edges in an NFA
 */
interface TransitionFunction extends Serializable {

  /**
   * Determines if the consumer matches on a given token.
   *
   * @param token the token to check
   * @return True if the consumer matches, i.e. can consume, the given token
   */
  int matches(Annotation token);

  /**
   * Construct an NFA for the consumer.
   *
   * @return An NFA representing the consumer.
   */
  NFA construct();


  final class ContentEquals implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    final Predicate<CharSequence> matcher;

    public ContentEquals(String token) {
      if (token.startsWith("(?i)")) {
        matcher = StringPredicates.MATCHES(token.substring(4), false);
      } else {
        matcher = StringPredicates.MATCHES(token);
      }
    }

    @Override
    public int matches(Annotation input) {
      return matcher.test(input) ? 1 : 0;
    }

    public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
    }

    @Override
    public String toString() {
      return "\"" + matcher.toString() + "\"";
    }

  }//END OF ContentEquals

  final class LexiconMatch implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    final Lexicon lexicon;
    final String lexiconName;

    public LexiconMatch(String token) {
      this.lexicon = LexiconManager.getLexicon(token);
      this.lexiconName = token;
    }

    @Override
    public int matches(Annotation input) {
      if (lexicon == null) {
        return 0;
      }
      return lexicon.contains(input) ? 1 : 0;
    }

    public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
    }

    @Override
    public String toString() {
      return "\"%" + lexiconName + "\"";
    }

  }//END OF ContentEquals

  final class ContentRegexMatch implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    final Predicate<CharSequence> matcher;

    public ContentRegexMatch(String token) {
      matcher = StringPredicates.REGEX_MATCH(token);
    }

    @Override
    public int matches(Annotation input) {
      return matcher.test(input) ? 1 : 0;
    }

    public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
    }

    @Override
    public String toString() {
      return "\"@" + matcher.toString() + "\"";
    }

  }//END OF ContentRegexMatch

  final class TagMatch implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    final Tag tag;

    public TagMatch(Tag tag) {
      this.tag = tag;
    }

    @Override
    public int matches(Annotation input) {
      return input.getTag().filter(t -> t.isInstance(tag)).isPresent() ? 1 : 0;
    }

    public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
    }

    @Override
    public String toString() {
      String tagClass = tag.getClass().getName().replaceFirst("\\$\\d*$", "");
      return "\"#" + tagClass + "." + tag.name() + "\"";
    }

  }//END OF TagMatch

  final class AttributeMatch implements TransitionFunction, Serializable {
    private static final long serialVersionUID = 1L;
    final Attribute attribute;
    final String value;

    public AttributeMatch(Attribute attribute, String value) {
      this.attribute = attribute;
      this.value = value;
    }

    @Override
    public int matches(Annotation input) {
      if (input.contains(attribute)) {
        Val v = input.get(attribute);
        if (v.equals(value)) {
          return 1;
        }

        if (v.getWrappedClass() != String.class) {
          Object o = Val.of(value).as(v.getWrappedClass());
          return v.equals(o) ? 1 : 0;
        }

        return 0;
      }
      return 0;
    }

    public NFA construct() {
      NFA nfa = new NFA();
      nfa.start.connect(nfa.end, this);
      return nfa;
    }

    @Override
    public String toString() {
      return "\"$" + attribute.name() + ":" + value + "\"";
    }

  }//END OF TagMatch

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
    public int matches(Annotation token) {
      return Math.max(c1.matches(token), c2.matches(token));
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
    public int matches(Annotation token) {
      return 1;
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
    public int matches(Annotation token) {
      return child.matches(token);
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
    public int matches(Annotation input) {
      return child.matches(input);
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
    public int matches(Annotation token) {
      return child.matches(token);
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
    public int matches(Annotation token) {
      return child.matches(token);
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
