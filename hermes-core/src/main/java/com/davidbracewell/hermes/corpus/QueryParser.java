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

package com.davidbracewell.hermes.corpus;

import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.parsing.*;
import com.davidbracewell.parsing.expressions.BinaryOperatorExpression;
import com.davidbracewell.parsing.expressions.Expression;
import com.davidbracewell.parsing.expressions.PrefixExpression;
import com.davidbracewell.parsing.expressions.ValueExpression;
import com.davidbracewell.parsing.handlers.BinaryOperatorHandler;
import com.davidbracewell.parsing.handlers.GroupHandler;
import com.davidbracewell.parsing.handlers.PrefixHandler;
import com.davidbracewell.parsing.handlers.PrefixOperatorHandler;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class QueryParser {

  public enum Operator implements ParserTokenType, HasLexicalPattern {
    AND("([Aa][Nn][Dd]|&)"),
    OR("([Oo][Rr]|\\|)");
    private final String lexicalPattern;

    Operator(String lexicalPattern) {
      this.lexicalPattern = lexicalPattern;
    }

    @Override
    public boolean isInstance(ParserTokenType tokenType) {
      return tokenType != null && tokenType.equals(this);
    }

    @Override
    public String lexicalPattern() {
      return lexicalPattern;
    }
  }

  private enum Types implements ParserTokenType, HasLexicalPattern {
    NOT("-"),
    FIELD("\\[[^\\]]+\\]:");
    private final String lexicalPattern;

    Types(String lexicalPattern) {
      this.lexicalPattern = lexicalPattern;
    }

    @Override
    public boolean isInstance(ParserTokenType tokenType) {
      return tokenType != null && tokenType.equals(this);
    }

    @Override
    public String lexicalPattern() {
      return lexicalPattern;
    }
  }

  private static class WordHandler extends PrefixHandler {

    private final Operator defaultOperator;

    public WordHandler(int precedence, Operator defaultOperator) {
      super(precedence);
      this.defaultOperator = defaultOperator;
    }

    @Override
    public Expression parse(Parser parser, ParserToken token) throws ParseException {
      if (
        parser.hasNext() &&
          !parser.tokenStream().lookAheadType(0).equals(Operator.AND) &&
          !parser.tokenStream().lookAheadType(0).equals(Operator.OR) &&
          !parser.tokenStream().lookAheadType(0).equals(CommonTypes.CLOSEPARENS)
        ) {
        return new BinaryOperatorExpression(
          new ValueExpression(token.text, token.type),
          new ParserToken(defaultOperator.toString(), defaultOperator),
          parser.next()
        );
      }
      return new ValueExpression(token.text, token.getType());
    }

  }

  private final Lexer lexer;
  private final Grammar grammar;
  private final Operator defaultOperator;

  public QueryParser() {
    this(Operator.OR);
  }

  public QueryParser(@NonNull Operator defaultOperator) {
    this.lexer = RegularExpressionLexer.builder()
      .add(CommonTypes.OPENPARENS)
      .add(CommonTypes.CLOSEPARENS)
      .add(Operator.AND)
      .add(Operator.OR)
      .add(Types.NOT)
      .add(Types.FIELD)
      .add(CommonTypes.WORD, "(\"([^\"]|\\\\\")*\"|[^\\s\\|\\&\\)\\(]+)")
      .build();
    this.grammar = new Grammar() {{
      register(CommonTypes.OPENPARENS, new GroupHandler(CommonTypes.CLOSEPARENS));
      register(Types.NOT, new PrefixOperatorHandler(100));
      register(CommonTypes.WORD, new WordHandler(5, defaultOperator));
      register(Types.FIELD, new PrefixOperatorHandler(20));
      register(Operator.AND, new BinaryOperatorHandler(10, true));
      register(Operator.OR, new BinaryOperatorHandler(10, true));
    }};
    this.defaultOperator = defaultOperator;
  }


  public SerializablePredicate<HString> parse(String query) throws ParseException {
    Parser parser = new Parser(grammar, lexer.lex(query));
    List<SerializablePredicate<HString>> predicates = new ArrayList<>();
    while (parser.hasNext()) {
      Expression expression = parser.next();
      predicates.add(generate(expression));
    }
    if (predicates.isEmpty()) {
      return d -> true;
    }
    SerializablePredicate<HString> finalPredicate = predicates.get(0);
    for (int i = 1; i < predicates.size(); i++) {
      finalPredicate = defaultOperator == Operator.AND ? and(finalPredicate, predicates.get(i)) : or(finalPredicate, predicates.get(i));
    }
    return finalPredicate;
  }

  private SerializablePredicate<HString> generate(Expression e) {
    if (e.isInstance(ValueExpression.class)) {
      return s -> s.contains(e.as(ValueExpression.class).value);
    } else if (e.isInstance(PrefixExpression.class)) {
      PrefixExpression pe = e.as(PrefixExpression.class);
      if (pe.operator.getType().isInstance(Types.NOT)) {
        return negate(generate(pe.right));
      } else if (pe.operator.getType().isInstance(Types.FIELD)) {
        return generate(pe.right);
      }
      return generate(pe.right);
    }
    BinaryOperatorExpression boe = e.as(BinaryOperatorExpression.class);
    SerializablePredicate<HString> left = generate(boe.left);
    SerializablePredicate<HString> right = generate(boe.right);
    return boe.operator.getType().isInstance(Operator.AND) ? and(left, right) : or(left, right);
  }

  SerializablePredicate<HString> negate(SerializablePredicate<HString> p) {
    return (hString -> !p.test(hString));
  }

  SerializablePredicate<HString> and(SerializablePredicate<HString> l, SerializablePredicate<HString> r) {
    return (hString -> l.test(hString) && r.test(hString));
  }

  SerializablePredicate<HString> or(SerializablePredicate<HString> l, SerializablePredicate<HString> r) {
    return (hString -> l.test(hString) || r.test(hString));
  }


}//END OF QueryParser
