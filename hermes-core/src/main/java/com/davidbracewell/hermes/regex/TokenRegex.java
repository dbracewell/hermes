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
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.parsing.CommonTypes;
import com.davidbracewell.parsing.ExpressionIterator;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.parsing.expressions.*;
import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;


/**
 * @author David B. Bracewell
 */
public final class TokenRegex implements Serializable {
   private static final long serialVersionUID = 1L;

   private final NFA nfa;
   private final String pattern;

   private TokenRegex(TransitionFunction transitionFunction) {
      this.nfa = transitionFunction.construct();
      this.pattern = transitionFunction.toString();
   }


   /**
    * Compiles the regular expression
    *
    * @param pattern The token regex pattern
    * @return A compiled TokenRegex
    * @throws ParseException the parse exception
    */
   public static TokenRegex compile(String pattern) throws ParseException {
      ExpressionIterator p = QueryToPredicate.PARSER.parse(pattern);
      Expression exp;
      TransitionFunction top = null;
      while ((exp = p.next()) != null) {
         if (top == null) {
            top = consumerize(exp);
         } else {
            top = new TransitionFunction.Sequence(top, consumerize(exp));
         }
      }
      return new TokenRegex(top);
   }

   private static TransitionFunction handleMultivalue(MultivalueExpression exp) throws ParseException {
      if (exp.match(CommonTypes.OPENPARENS)) {
         Preconditions.checkState(!exp.expressions.isEmpty());
         return combine(exp.expressions);
      } else if (exp.match(CommonTypes.OPENBRACKET)) {
         Expression child = exp.expressions.get(0);
         SerializablePredicate<HString> p = QueryToPredicate.parse(child);
         return new TransitionFunction.LogicStatement(child.toString(), (HString a) -> p.test(a) ? a.tokenLength() : 0);
      } else if (exp.match(RegexTokenTypes.GROUP)) {
         return new TransitionFunction.GroupMatcher(combine(exp.expressions), Cast.<GroupExpression>as(exp).groupName);
      } else if (exp.match(RegexTokenTypes.ANNOTATION)) {
         AnnotationExpression ae = Cast.as(exp);
         return new TransitionFunction.AnnotationMatcher(ae.annotationType, combine(ae.expressions));
      } else if (exp.match(RegexTokenTypes.RELATIONGROUP)) {
         RelationGroupExpression ae = Cast.as(exp);
         Expression child = exp.expressions.get(0);
         SerializablePredicate<HString> p = QueryToPredicate.parse(child);
         return new TransitionFunction.RelationMatcher(ae.relationType, ae.relationValue, child.toString(),
                                                       (HString a) -> p.test(a) ? a.tokenLength() : 0);
      }
      throw new ParseException("Unknown expression: " + exp.toString());
   }

   private static TransitionFunction combine(Collection<Expression> expressions) throws ParseException {
      TransitionFunction c = null;
      for (Expression e : expressions) {
         TransitionFunction cprime = consumerize(e);
         if (c == null) {
            c = cprime;
         } else {
            c = new TransitionFunction.Sequence(c, cprime);
         }
      }
      return c;
   }

   private static TransitionFunction handlePostfix(PostfixOperatorExpression postfix) throws ParseException {
      if (postfix.operator.type.isInstance(CommonTypes.QUESTION)) {
         return new TransitionFunction.ZeroOrOne(consumerize(postfix.left));
      } else if (postfix.operator.type.isInstance(CommonTypes.PLUS)) {
         return new TransitionFunction.OneOrMore(consumerize(postfix.left));
      } else if (postfix.operator.type.isInstance(CommonTypes.MULTIPLY)) {
         return new TransitionFunction.KleeneStar(consumerize(postfix.left));
      } else if (postfix.operator.type.isInstance(RegexTokenTypes.RANGE)) {
         String text = postfix.operator.getText().replace("{", "").replace("}", "");
         String[] parts = text.split("\\s*,\\s*");

         int low;
         try {
            low = Integer.parseInt(parts[0]);
         } catch (Exception e) {
            throw new ParseException("Invalid range: " + postfix.operator.getText());
         }

         int high;
         if (parts[1].equals("*")) {
            high = Integer.MAX_VALUE;
         } else {
            try {
               high = Integer.parseInt(parts[1]);
            } catch (Exception e) {
               throw new ParseException("Invalid range: " + postfix.operator.getText());
            }
         }

         if (high < low) {
            throw new ParseException("Invalid range: " + postfix.operator.getText());
         }

         if (low == 0 && high == Integer.MAX_VALUE) {
            return new TransitionFunction.KleeneStar(consumerize(postfix.left));
         } else if (low == 0 && high == 1) {
            return new TransitionFunction.ZeroOrOne(consumerize(postfix.left));
         } else if (low == 1 && high == Integer.MAX_VALUE) {
            return new TransitionFunction.OneOrMore(consumerize(postfix.left));
         }
         return new TransitionFunction.Range(consumerize(postfix.left), low, high);
      }
      throw new ParseException("Error in regular expression");
   }

   private static TransitionFunction handlePrefix(PrefixExpression exp) throws ParseException {
      if (exp.match(RegexTokenTypes.PARENT)) {
         return new TransitionFunction.ParentMatcher(consumerize(exp.right));
      }
      if (exp.match(RegexTokenTypes.NOT)) {
         if (exp.right.match(RegexTokenTypes.LOOKAHEADPOST) || exp.right.match(RegexTokenTypes.NEGLOOKAHEADPOST)) {
            throw new ParseException("Cannot negate a lookahead.");
         }
         return new TransitionFunction.Not(consumerize(exp.right));
      }

      throw new ParseException("Unknown expression: " + exp.toString());
   }

   protected static TransitionFunction consumerize(Expression exp) throws ParseException {
      //Handle Sequences
      if (exp.isInstance(MultivalueExpression.class)) {
         return handleMultivalue(exp.as(MultivalueExpression.class));
      }

      //Handle +, ?, *, {n,m}
      if (exp.isInstance(PostfixOperatorExpression.class)) {
         return handlePostfix(exp.as(PostfixOperatorExpression.class));
      }

      //Handle Parent, Annotation, Not
      if (exp.isInstance(PrefixExpression.class)) {
         return handlePrefix(exp.as(PrefixExpression.class));
      }

      if (exp.match(CommonTypes.PIPE)) {
         BinaryOperatorExpression boe = Cast.as(exp);
         return new TransitionFunction.Alternation(consumerize(boe.left), consumerize(boe.right));
      }

      if (exp.match(RegexTokenTypes.LOOKAHEADPOST)) {
         BinaryOperatorExpression boe = Cast.as(exp);
         return new TransitionFunction.LookAhead(consumerize(boe.left), consumerize(boe.right), false);
      }

      if (exp.match(RegexTokenTypes.NEGLOOKAHEADPOST)) {
         BinaryOperatorExpression boe = Cast.as(exp);
         return new TransitionFunction.LookAhead(consumerize(boe.left), consumerize(boe.right), true);
      }

      if (exp.match(RegexTokenTypes.ANY)) {
         String s = exp.toString();
         int high = Integer.MAX_VALUE;
         if (s.length() > 1) {
            high = Integer.parseInt(s.substring(1));
         }
         return new TransitionFunction.Range(new TransitionFunction.PredicateMatcher("/.*/", a -> true), 0, high);
      }


      if (exp.isInstance(ValueExpression.class)) {
         return new TransitionFunction.PredicateMatcher(exp.toString(),
                                                        QueryToPredicate.valueExpressionToPredicate(exp));
      }


      throw new IllegalArgumentException("Unknown expression: " + exp.toString());
   }

   /**
    * Creates a <code>TokenMatcher</code> to match against the given text.
    *
    * @param text The text to run the TokenRegex against
    * @return A TokenMatcher
    */
   public TokenMatcher matcher(HString text) {
      return new TokenMatcher(nfa, text);
   }

   /**
    * Creates a <code>TokenMatcher</code> to match against the given text.
    *
    * @param text  The text to run the TokenRegex against
    * @param start Which token to start the TokenRegex on
    * @return A TokenMatcher
    */
   public TokenMatcher matcher(HString text, int start) {
      return new TokenMatcher(nfa, text, start);
   }


   /**
    * Matches boolean.
    *
    * @param text the text
    * @return the boolean
    */
   public boolean matches(HString text) {
      return new TokenMatcher(nfa, text).find();
   }

   /**
    * Match first optional.
    *
    * @param text the text
    * @return the optional
    */
   public Optional<HString> matchFirst(HString text) {
      TokenMatcher matcher = new TokenMatcher(nfa, text);
      if (matcher.find()) {
         return Optional.of(matcher.group());
      }
      return Optional.empty();
   }

   /**
    * Pattern string.
    *
    * @return The token regex pattern as a string
    */
   public String pattern() {
      return pattern;
   }


   @Override
   public String toString() {
      return pattern;
   }
}//END OF TokenRegex
