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
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.filter.HStringPredicates;
import com.davidbracewell.hermes.filter.StopWords;
import com.davidbracewell.hermes.lexicon.LexiconManager;
import com.davidbracewell.hermes.tag.POS;
import com.davidbracewell.hermes.tag.RelationType;
import com.davidbracewell.hermes.tokenization.TokenType;
import com.davidbracewell.parsing.*;
import com.davidbracewell.parsing.expressions.*;
import com.davidbracewell.parsing.handlers.BinaryOperatorHandler;
import com.davidbracewell.parsing.handlers.PostfixOperatorHandler;
import com.davidbracewell.parsing.handlers.PrefixOperatorHandler;
import com.davidbracewell.parsing.handlers.ValueHandler;
import com.davidbracewell.string.StringPredicates;
import com.davidbracewell.string.StringUtils;

import java.util.List;

/**
 * @author David B. Bracewell
 */
public final class QueryToPredicate {

  protected static final Grammar RPGrammar = new Grammar() {
    {
      register(CommonTypes.OPENPARENS, new SequenceGroupHandler(CommonTypes.CLOSEPARENS));
      register(CommonTypes.PLUS, new PostfixOperatorHandler(8));
      register(CommonTypes.MULTIPLY, new PostfixOperatorHandler(8));
      register(CommonTypes.QUESTION, new PostfixOperatorHandler(8));
      register(RegexTokenTypes.TAGMATCH, new ValueHandler());
      register(RegexTokenTypes.RELATION, new ValueHandler());
      register(RegexTokenTypes.PATTERNTOKEN, new ValueHandler());
      register(RegexTokenTypes.REGEX, new ValueHandler());
      register(RegexTokenTypes.ATTRMATCH, new ValueHandler());
      register(RegexTokenTypes.LEXICON, new ValueHandler());
      register(RegexTokenTypes.PUNCTUATION, new ValueHandler());
      register(RegexTokenTypes.NUMBER, new ValueHandler());
      register(RegexTokenTypes.ANY, new ValueHandler());
      register(RegexTokenTypes.STOPWORD, new ValueHandler());
      register(RegexTokenTypes.ANNOTATION, new AnnotationHandler(7));
      register(RegexTokenTypes.NOT, new PrefixOperatorHandler(12));
      register(CommonTypes.OPENBRACKET, new LogicGroupHandler(12));
      register(CommonTypes.PIPE, new BinaryOperatorHandler(7, false));
      register(CommonTypes.AMPERSAND, new BinaryOperatorHandler(7, false));
      register(RegexTokenTypes.LOOKAHEADPOST, new LookAheadHandler(7));
      register(RegexTokenTypes.LOOKAHEADPOST, new LookAheadPrefixHandler(7));
      register(RegexTokenTypes.NEGLOOKAHEADPOST, new LookAheadHandler(7));
      register(RegexTokenTypes.NEGLOOKAHEADPOST, new LookAheadPrefixHandler(7));
      register(RegexTokenTypes.RELATIONGROUP, new RelationGroupHandler(7));
      register(RegexTokenTypes.GROUP, new GroupHandler(7));
      register(RegexTokenTypes.PARENT, new PrefixOperatorHandler(1));
      register(RegexTokenTypes.RANGE, new PostfixOperatorHandler(8));
    }
  };

  protected static final RegularExpressionLexer lexer = RegularExpressionLexer.builder()
    .add(RegexTokenTypes.ATTRMATCH)
    .add(RegexTokenTypes.RELATIONGROUP)
    .add(RegexTokenTypes.PATTERNTOKEN)
    .add(RegexTokenTypes.REGEX)
    .add(RegexTokenTypes.LOOKAHEADPOST)
    .add(RegexTokenTypes.NEGLOOKAHEADPOST)
    .add(RegexTokenTypes.GROUP)
    .add(CommonTypes.OPENPARENS, "\\((?!\\?)")
    .add(CommonTypes.CLOSEPARENS)
    .add(CommonTypes.OPENBRACKET)
    .add(CommonTypes.CLOSEBRACKET)
    .add(CommonTypes.CLOSEBRACE)
    .add(CommonTypes.PLUS)
    .add(CommonTypes.MULTIPLY)
    .add(CommonTypes.QUESTION)
    .add(CommonTypes.PIPE)
    .add(RegexTokenTypes.LEXICON)
    .add(RegexTokenTypes.RANGE)
    .add(RegexTokenTypes.TAGMATCH)
    .add(RegexTokenTypes.ANNOTATION)
    .add(RegexTokenTypes.RELATION)
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

  private QueryToPredicate() {
    throw new IllegalAccessError();
  }

  public static SerializablePredicate<HString> parse(String query) throws ParseException {
    Parser p = new Parser(
      RPGrammar,
      lexer.lex(query)
    );
    Expression exp;
    SerializablePredicate<HString> top = null;
    while ((exp = p.next()) != null) {
      SerializablePredicate<HString> next = parse(exp);
      if (top == null) {
        top = next;
      } else {
        top = top.and(next);
      }
    }
    return top;
  }

  protected static SerializablePredicate<HString> parse(Expression exp) throws ParseException {
    if (exp.isInstance(ValueExpression.class)) {
      return valueExpressionToPredicate(exp);
    } else if (exp.isInstance(PrefixExpression.class)) {
      PrefixExpression pe = Cast.as(exp);
      SerializablePredicate<HString> child = parse(pe.right);

      if (exp.match(RegexTokenTypes.PARENT)) {
        return hString -> hString.asAnnotation().map(a -> a.parent().filter(child).isPresent()).orElse(false);
      }
      if (exp.match(RegexTokenTypes.NOT)) {
        return child.negate();
      }


    } else if (exp.isInstance(BinaryOperatorExpression.class)) {
      BinaryOperatorExpression boe = Cast.as(exp);
      SerializablePredicate<HString> left = parse(boe.left);
      SerializablePredicate<HString> right = parse(boe.right);
      if (boe.match(CommonTypes.PIPE)) {
        return left.or(right);
      } else if (boe.match(CommonTypes.AMPERSAND)) {
        return left.and(right);
      } else if (exp.match(RegexTokenTypes.LOOKAHEADPOST) || exp.match(RegexTokenTypes.NEGLOOKAHEADPOST)) {
        TransitionFunction tf = TokenRegex.consumerize(exp);
        return hString -> tf.matches(hString) > 0;
      }
    } else if (exp.isInstance(MultivalueExpression.class) && exp.match(CommonTypes.OPENPARENS)) {
      MultivalueExpression mve = Cast.as(exp);
      SerializablePredicate<HString> predicate = parse(mve.expressions.get(0));
      for (int i = 1; i < mve.expressions.size(); i++) {
        predicate = predicate.and(parse(mve.expressions.get(i)));
      }
      return predicate;
    } else if (exp.isInstance(MultivalueExpression.class) && exp.match(RegexTokenTypes.ANNOTATION)) {
      AnnotationExpression mve = Cast.as(exp);
      SerializablePredicate<HString> predicate = parse(mve.expressions.get(0));
      for (int i = 1; i < mve.expressions.size(); i++) {
        predicate = predicate.and(parse(mve.expressions.get(i)));
      }
      final SerializablePredicate<HString> fPredicate = predicate;
      return hString -> hString.getStartingHere(mve.annotationType).stream().anyMatch(fPredicate);
    } else if (exp.match(RegexTokenTypes.RELATIONGROUP)) {
      RelationGroupExpression ae = Cast.as(exp);
      Expression child = ae.expressions.get(0);
      SerializablePredicate<HString> p = QueryToPredicate.parse(child);
      TransitionFunction tf = new TransitionFunction.RelationMatcher(ae.relationType, ae.relationValue, child.toString(), (HString a) -> p.test(a) ? a.tokenLength() : 0);
      return h -> tf.matches(h) > 0;
    }

    throw new ParseException("Unknown expression: " + exp.toString());
  }

  protected static SerializablePredicate<HString> valueExpressionToPredicate(Expression exp) throws ParseException {

    if (exp.match(RegexTokenTypes.NUMBER)) {
      return a -> StringPredicates.IS_DIGIT.test(a) || a.getPOS().isInstance(POS.NUMBER) || TokenType.NUMBER.equals(a.get(Attrs.TOKEN_TYPE).cast());
    }

    if (exp.match(RegexTokenTypes.LEXICON)) {
      String lexiconName = StringUtils.unescape(exp.toString().substring(1).replaceFirst("^\"", "").replaceFirst("\"$", ""), '\\');
      return a -> LexiconManager.getLexicon(lexiconName).test(a);
    }


    if (exp.match(RegexTokenTypes.REGEX)) {
      String pattern = exp.toString();
      boolean isCaseSensitive = !pattern.endsWith("/i");
      pattern = pattern.substring(1, pattern.length() - (!isCaseSensitive ? 2 : 1)).replaceAll("\\\\(.)", "\\1");
      return HStringPredicates.contentRegexMatch(StringUtils.unescape(pattern, '\\'), isCaseSensitive);
    }

    if (exp.match(RegexTokenTypes.PATTERNTOKEN)) {
      String pattern = StringUtils.split(exp.toString(), ':').get(0);
      boolean isCaseSensitive = !pattern.startsWith("(?i)");
      if (!isCaseSensitive) pattern = pattern.substring(4);
      return HStringPredicates.contentMatch(StringUtils.unescape(pattern, '\\'), isCaseSensitive);
    }

    if (exp.match(RegexTokenTypes.TAGMATCH)) {
      return HStringPredicates.hasTagInstance(StringUtils.unescape(exp.toString().substring(1), '\\'));
    }

    if (exp.match(RegexTokenTypes.ATTRMATCH)) {
      List<String> parts = StringUtils.split(exp.toString().substring(1), ':');
      Attribute attrName = Attribute.create(StringUtils.unescape(parts.get(0), '\\'));
      Object attrValue = attrName.getValueType().convert(StringUtils.unescape(parts.get(1), '\\'));
      return HStringPredicates.attributeMatch(attrName, attrValue);
    }

    if (exp.match(RegexTokenTypes.PUNCTUATION)) {
      return h -> StringPredicates.IS_PUNCTUATION.test(h);
    }

    if (exp.match(RegexTokenTypes.STOPWORD)) {
      return a -> StopWords.getInstance(a.getLanguage()).isStopWord(a);
    }

    if (exp.match(RegexTokenTypes.ANY)) {
      String s = exp.toString();
      if (s.length() == 1) {
        return a -> true;
      }
    }

    if (exp.match(RegexTokenTypes.RELATION)) {
      List<String> parts = StringUtils.split(exp.toString().substring(1), ':');
      RelationType relation = RelationType.create(StringUtils.unescape(parts.get(0), '\\'));
      String value = parts.size() > 1 ? StringUtils.unescape(parts.get(1), '\\') : null;
      return h -> h.asAnnotation().filter(a -> {
        if (value == null) {
          return a.targets(relation).size() > 0;
        }
        return a.targets(relation, value).size() > 0;
      }).map(a -> true).orElse(false);
    }

    throw new ParseException("Unknown expression: " + exp.toString());
  }


}//END OF QueryToPredicate