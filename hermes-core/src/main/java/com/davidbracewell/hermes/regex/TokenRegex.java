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
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.filter.HStringPredicates;
import com.davidbracewell.hermes.filter.StopWords;
import com.davidbracewell.hermes.lexicon.LexiconManager;
import com.davidbracewell.hermes.tag.POS;
import com.davidbracewell.hermes.tokenization.TokenType;
import com.davidbracewell.parsing.*;
import com.davidbracewell.parsing.expressions.*;
import com.davidbracewell.parsing.handlers.*;
import com.davidbracewell.string.StringPredicates;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * <p>A regular expression that matches over <code>Token</code>s on a <code>Text</code>.</p>
 * <p>The format of the regular expression is token matches and regular expression operators.
 * A token match is in the form of <code>"{Text}"</code> where <code>{Text}</code> is the
 * text to match. The content in the token match can be a regular expression, which is
 * denoted by "RE:{Text}". Valid operators on tokens are +, *, ?, and |.
 * </p>
 * <p>An example is as follows:
 * <code>"The" ("man"|"woman") ("is"|"was") "on" "the" "hill"</code>
 * would match
 * "The man is on the hill"
 * "The man was on the hill"
 * "The woman is on the hill"
 * "The woman was on the hill"
 * </p>
 *
 * @author David B. Bracewell
 */
public final class TokenRegex implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final Grammar RPGrammar = new Grammar() {
    {
      register(CommonTypes.OPENPARENS, new SequenceGroupHandler(CommonTypes.CLOSEPARENS));
      register(CommonTypes.PLUS, new PostfixOperatorHandler(8));
      register(CommonTypes.MULTIPLY, new PostfixOperatorHandler(8));
      register(CommonTypes.QUESTION, new PostfixOperatorHandler(8));
      register(RegexTokenTypes.TAGMATCH, new ValueHandler());
      register(RegexTokenTypes.PATTERNTOKEN, new ValueHandler());
      register(RegexTokenTypes.ATTRMATCH, new ValueHandler());
      register(RegexTokenTypes.LEXICON, new ValueHandler());
      register(RegexTokenTypes.PUNCTUATION, new ValueHandler());
      register(RegexTokenTypes.NUMBER, new ValueHandler());
      register(RegexTokenTypes.ANY, new ValueHandler());
      register(RegexTokenTypes.STOPWORD, new ValueHandler());
      register(RegexTokenTypes.ANNOTATION, new PrefixOperatorHandler(12));
      register(RegexTokenTypes.NOT, new PrefixOperatorHandler(12));
      register(CommonTypes.OPENBRACKET, new LogicGroupHandler(14));
      register(CommonTypes.PIPE, new BinaryOperatorHandler(7, false));
      register(CommonTypes.AMPERSAND, new BinaryOperatorHandler(7, false));
      register(RegexTokenTypes.PARENT, new PrefixOperatorHandler(1));
      register(RegexTokenTypes.RANGE, new PostfixOperatorHandler(8));
    }
  };

  private static final RegularExpressionLexer lexer = RegularExpressionLexer.builder()
    .add(RegexTokenTypes.ATTRMATCH)
    .add(RegexTokenTypes.PATTERNTOKEN, "<(\\\\.|[^<>])+>")
    .add(CommonTypes.OPENPARENS)
    .add(CommonTypes.CLOSEPARENS)
    .add(CommonTypes.OPENBRACKET)
    .add(CommonTypes.CLOSEBRACKET)
    .add(CommonTypes.PLUS)
    .add(CommonTypes.MULTIPLY)
    .add(CommonTypes.QUESTION)
    .add(CommonTypes.PIPE)
    .add(RegexTokenTypes.LEXICON)
    .add(RegexTokenTypes.RANGE)
    .add(RegexTokenTypes.TAGMATCH)
    .add(RegexTokenTypes.ANNOTATION)
    .add(RegexTokenTypes.PUNCTUATION)
    .add(RegexTokenTypes.NUMBER)
    .add(RegexTokenTypes.NOT)
    .add(RegexTokenTypes.ANY)
    .add(RegexTokenTypes.STOPWORD)
    .add(CommonTypes.OPENBRACKET)
    .add(CommonTypes.CLOSEBRACKET)
    .add(CommonTypes.AMPERSAND)
    .add(RegexTokenTypes.PARENT)
    .build();
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
   * @throws com.davidbracewell.parsing.ParseException Something went wrong parsing the regular expression
   */
  public static TokenRegex compile(String pattern) throws ParseException {
    Parser p = new Parser(
      RPGrammar,
      lexer.lex(pattern)
    );
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
      List<Expression> expressions = exp.expressions;
      Preconditions.checkState(!expressions.isEmpty());
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
    throw new ParseException("Unknown expression: " + exp.toString());
  }

  private static SerializableFunction<Annotation, Integer> toFunction(Expression exp) throws ParseException {
    if (exp.isInstance(ValueExpression.class)) {
      TransitionFunction tf = consumerize(exp);
      return a -> tf.matches(a);
    } else if (exp.match(RegexTokenTypes.NOT)) {
      TransitionFunction tf = consumerize(exp.as(PrefixExpression.class).right);
      return a -> tf.nonMatch(a);
    } else if (exp.match(CommonTypes.PIPE)) {
      BinaryOperatorExpression boe = exp.as(BinaryOperatorExpression.class);
      TransitionFunction tf1 = consumerize(boe.left);
      TransitionFunction tf2 = consumerize(boe.right);
      return a -> Math.max(tf1.matches(a), tf2.matches(a));
    } else if (exp.match(CommonTypes.AMPERSAND)) {
      BinaryOperatorExpression boe = exp.as(BinaryOperatorExpression.class);
      TransitionFunction tf1 = consumerize(boe.left);
      TransitionFunction tf2 = consumerize(boe.right);
      return a -> {
        int i = tf1.matches(a);
        int j = tf2.matches(a);
        if (i > 0 && j > 0) {
          return Math.max(i, j);
        }
        return 0;
      };
    } else if (exp.match(RegexTokenTypes.ANNOTATION)) {
      TransitionFunction tf = consumerize(exp);
      return a -> tf.matches(a);
    }
    throw new ParseException("Unknown expression: " + exp.toString());
  }


  private static SerializablePredicate<? super Annotation> valueExpressionToPredicate(Expression exp) throws ParseException {

    if (exp.match(RegexTokenTypes.NUMBER)) {
      return a -> StringPredicates.IS_DIGIT.test(a) || a.getPOS().isInstance(POS.NUMBER) || TokenType.NUMBER.equals(a.get(Attrs.TOKEN_TYPE).cast());
    }

    if (exp.match(RegexTokenTypes.LEXICON)) {
      String lexiconName = exp.toString().substring(1).replaceFirst("^\"", "").replaceFirst("\"$", "");
      return a -> LexiconManager.getLexicon(lexiconName).contains(a);
    }

    if (exp.match(RegexTokenTypes.PATTERNTOKEN)) {
      String pattern = exp.toString();
      pattern = pattern.substring(1, pattern.length() - 1);
      boolean isRegex = pattern.startsWith("@") && pattern.length() > 1;
      if (isRegex) pattern = pattern.substring(1);
      boolean isCaseSensitive = !pattern.startsWith("(?i)");
      if (!isCaseSensitive) pattern = pattern.substring(4);

      if (isRegex) {
        return HStringPredicates.contentRegexMatch(pattern, isCaseSensitive);
      }
      return HStringPredicates.contentMatch(pattern, isCaseSensitive);
    }

    if (exp.match(RegexTokenTypes.TAGMATCH)) {
      return HStringPredicates.hasTagInstance(exp.toString().substring(1));
    }

    if (exp.match(RegexTokenTypes.ATTRMATCH)) {
      List<String> parts = StringUtils.split(exp.toString().substring(1), ':');
      Attribute attrName = Attribute.create(parts.get(0));
      Object attrValue = attrName.getValueType().convert(parts.get(1));
      return HStringPredicates.attributeMatch(attrName, attrValue);
    }

    if (exp.match(RegexTokenTypes.PUNCTUATION)) {
      return StringPredicates.IS_PUNCTUATION;
    }

    if (exp.match(RegexTokenTypes.STOPWORD)) {
      return a -> StopWords.getInstance(a.getLanguage()).isStopWord(a);
    }

    throw new ParseException("Unknown expression: " + exp.toString());
  }

  private static TransitionFunction handlePostfix(PostfixExpression postfix) throws ParseException {
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
      return new TransitionFunction.Not(consumerize(exp.right));
    }

    if (exp.match(RegexTokenTypes.ANNOTATION)) {
      String typeName = exp.operator.getText().substring(2).replaceFirst("\\}$", "");
      return new TransitionFunction.AnnotationMatcher(AnnotationType.create(typeName), consumerize(exp.right));
    }

    throw new ParseException("Unknown expression: " + exp.toString());
  }

  private static TransitionFunction consumerize(Expression exp) throws ParseException {

    //Handle Sequences
    if (exp.isInstance(MultivalueExpression.class)) {
      return handleMultivalue(exp.as(MultivalueExpression.class));
    }

    //Handle +, ?, *, {n,m}
    if (exp.isInstance(PostfixExpression.class)) {
      return handlePostfix(exp.as(PostfixExpression.class));
    }

    //Handle Parent, Annotation, Not
    if (exp.isInstance(PrefixExpression.class)) {
      return handlePrefix(exp.as(PrefixExpression.class));
    }

    if (exp.match(CommonTypes.PIPE)) {
      BinaryOperatorExpression boe = Cast.as(exp);
      return new TransitionFunction.Alternation(consumerize(boe.left), consumerize(boe.right));
    }

    if (exp.match(RegexTokenTypes.ANY)) {
      String s = exp.toString();
      int high = Integer.MAX_VALUE;
      if (s.length() > 1) {
        high = Integer.parseInt(s.substring(1));
      }
      return new TransitionFunction.Range(new TransitionFunction.PredicateMatcher("<.*>", a -> true), 0, high);
    }


    if (exp.isInstance(ValueExpression.class)) {
      return new TransitionFunction.PredicateMatcher(exp.toString(), valueExpressionToPredicate(exp));
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
   * @return The token regex pattern as a string
   */
  public String pattern() {
    return pattern;
  }

  private static class SequenceGroupHandler extends PrefixHandler {

    private final ParserTokenType closeGroupType;

    /**
     * Default Constructor
     *
     * @param closeGroupType The token type that indicates the end of the group
     */
    public SequenceGroupHandler(ParserTokenType closeGroupType) {
      super(100);
      this.closeGroupType = closeGroupType;
    }

    @Override
    public Expression parse(Parser parser, ParserToken token) throws ParseException {
      List<Expression> results = new ArrayList<>();
      while (!parser.tokenStream().nonConsumingMatch(closeGroupType)) {
        results.add(parser.next());
      }
      parser.tokenStream().consume(closeGroupType);
      return new MultivalueExpression(results, token.type);
    }

  }

  private static class LogicGroupHandler extends PrefixHandler {

    /**
     * Default constructor
     *
     * @param precedence The precedence of the handler
     */
    public LogicGroupHandler(int precedence) {
      super(precedence);
    }

    @Override
    public Expression parse(Parser parser, ParserToken token) throws ParseException {
      Expression exp = parser.next();
      parser.tokenStream().consume(CommonTypes.CLOSEBRACKET);
      return new MultivalueExpression(Collections.singletonList(exp), token.type);
    }
  }

}//END OF TokenRegex
