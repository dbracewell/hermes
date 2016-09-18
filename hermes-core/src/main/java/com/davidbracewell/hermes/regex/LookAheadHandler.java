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

import com.davidbracewell.parsing.CommonTypes;
import com.davidbracewell.parsing.ExpressionIterator;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.parsing.ParserToken;
import com.davidbracewell.parsing.expressions.BinaryOperatorExpression;
import com.davidbracewell.parsing.expressions.Expression;
import com.davidbracewell.parsing.expressions.MultivalueExpression;
import com.davidbracewell.parsing.handlers.PostfixOperatorHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class LookAheadHandler extends PostfixOperatorHandler {
   /**
    * Default constructor
    *
    * @param precedence The precedence of the handler
    */
   public LookAheadHandler(int precedence) {
      super(precedence);
   }

   @Override
   public Expression parse(ExpressionIterator expressionIterator, Expression left, ParserToken token) throws ParseException {
      List<Expression> results = new ArrayList<>();
      while (!expressionIterator.tokenStream().nonConsumingMatch(CommonTypes.CLOSEPARENS)) {
         results.add(expressionIterator.next());
      }
      expressionIterator.tokenStream().consume(CommonTypes.CLOSEPARENS);
      return new BinaryOperatorExpression(left, token, new MultivalueExpression(results, CommonTypes.OPENPARENS));
   }


}//END OF LookAheadHandler
