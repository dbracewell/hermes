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
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.tag.POS;
import com.davidbracewell.hermes.tag.StringTag;
import com.davidbracewell.parsing.*;
import com.davidbracewell.parsing.expressions.BinaryOperatorExpression;
import com.davidbracewell.parsing.expressions.Expression;
import com.davidbracewell.parsing.expressions.MultivalueExpression;
import com.davidbracewell.parsing.expressions.PostfixExpression;
import com.davidbracewell.parsing.handlers.*;
import com.davidbracewell.string.StringPredicates;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
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
public class TokenRegex {


  private static final Grammar RPGrammar = new Grammar() {
    {
      register(CommonTypes.OPENPARENS, new SequenceGroupHandler(CommonTypes.CLOSEPARENS));
      register(CommonTypes.OPENBRACKET, new GroupHandler(CommonTypes.CLOSEBRACKET));
      register(CommonTypes.PLUS, new PostfixOperatorHandler(8));
      register(CommonTypes.MULTIPLY, new PostfixOperatorHandler(8));
      register(CommonTypes.QUESTION, new PostfixOperatorHandler(8));
      register(RegexTokenTypes.PATTERNTOKEN, new ValueHandler());
      register(CommonTypes.PIPE, new BinaryOperatorHandler(6, true));
      register(RegexTokenTypes.RANGE, new PostfixOperatorHandler(8));
    }
  };
  private static final RegularExpressionLexer lexer = RegularExpressionLexer.builder()
    .add(RegexTokenTypes.PATTERNTOKEN, "<(\\\\.|[^<>])+>")
    .add(CommonTypes.OPENPARENS)
    .add(CommonTypes.CLOSEPARENS)
    .add(CommonTypes.OPENBRACKET)
    .add(CommonTypes.CLOSEBRACKET)
    .add(CommonTypes.PLUS)
    .add(CommonTypes.MULTIPLY)
    .add(CommonTypes.QUESTION)
    .add(CommonTypes.PIPE)
    .add(RegexTokenTypes.RANGE)
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

  private static TransitionFunction handleToken(Expression exp) throws ParseException {
    //unescape escaped characters
    String token = exp.toString().replaceAll("\\\\(.)", "$1");
    if (token.length() == 2) {
      throw new ParseException("Illegal Parse Token " + token);
    }
    token = token.substring(1, token.length() - 1);

    if (StringPredicates.HAS_LETTER_OR_DIGIT.test(token) || (token.startsWith("@") && token.length() > 1)) {
      switch (token.charAt(0)) {
        case '%':
          return new TransitionFunction.LexiconMatch(token.substring(1));
        case '#':
          Tag tag = Val.of(token.substring(1)).as(Tag.class);
          if (tag == null) {
            tag = Val.of("com.davidbracewell.hermes.tag." + token.substring(1)).as(Tag.class);
          }
          if (tag == null) {
            tag = Val.of(token.substring(1)).as(POS.class);
          }
          if (tag == null) {
            tag = new StringTag(token.substring(1));
          }
          return new TransitionFunction.TagMatch(tag);
        case '@':
          return new TransitionFunction.ContentRegexMatch(token.substring(1));
        case '$':
          int p = token.indexOf(":");
          if (p == -1) {
            throw new ParseException("Ill-formed attribute match: " + token);
          }
          Attribute attr = Attribute.create(token.substring(1, p));
          return new TransitionFunction.AttributeMatch(attr, token.substring(p + 1));
        default:
          return new TransitionFunction.ContentEquals(token);
      }
    }
    return new TransitionFunction.ContentEquals(token);
  }

  private static TransitionFunction handleAlternation(BinaryOperatorExpression boe) throws ParseException {
    return new TransitionFunction.Alternation(consumerize(boe.left), consumerize(boe.right));
  }

  private static TransitionFunction consumerize(Expression exp) throws ParseException {
    if (exp.isInstance(MultivalueExpression.class)) {
      return handleMultivalue(exp.as(MultivalueExpression.class));
    } else if (exp.isInstance(PostfixExpression.class)) {
      return handlePostfix(exp.as(PostfixExpression.class));
    } else if (exp.match(RegexTokenTypes.PATTERNTOKEN)) {
      return handleToken(exp);
    } else if (exp.match(CommonTypes.PIPE)) {
      return handleAlternation(exp.as(BinaryOperatorExpression.class));
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
      super(0);
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


}//END OF TokenRegex
