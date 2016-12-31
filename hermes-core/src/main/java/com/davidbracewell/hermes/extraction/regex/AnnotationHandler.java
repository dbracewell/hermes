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

package com.davidbracewell.hermes.extraction.regex;

import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.parsing.CommonTypes;
import com.davidbracewell.parsing.ExpressionIterator;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.parsing.ParserToken;
import com.davidbracewell.parsing.expressions.Expression;
import com.davidbracewell.parsing.expressions.ValueExpression;
import com.davidbracewell.parsing.handlers.PrefixHandler;
import com.davidbracewell.string.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
class AnnotationHandler extends PrefixHandler {

   @Override
   public Expression parse(ExpressionIterator expressionIterator, ParserToken token) throws ParseException {
      List<Expression> results = new ArrayList<>();
      while (!expressionIterator.tokenStream().nonConsumingMatch(CommonTypes.CLOSEBRACE)) {
         Expression next = expressionIterator.next();
         if (next == null) {
            throw new ParseException("Premature end of expression: " + token.getText());
         }
         results.add(next);
      }
      if (results.size() == 0) {
         results.add(new ValueExpression("~", RegexTokenTypes.ANY));
      }
      expressionIterator.tokenStream().consume(CommonTypes.CLOSEBRACE);
      AnnotationType type = AnnotationType.create(StringUtils.unescape(token.getText().substring(1), '\\'));
      return new AnnotationExpression(results, token.type, type);
   }
}
