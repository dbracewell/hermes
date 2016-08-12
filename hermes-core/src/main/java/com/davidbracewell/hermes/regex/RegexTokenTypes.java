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


import com.davidbracewell.Regex;
import com.davidbracewell.parsing.HasLexicalPattern;
import com.davidbracewell.parsing.ParserTokenType;

import static com.davidbracewell.Re.quote;

/**
 * @author David B. Bracewell
 */
public enum RegexTokenTypes implements ParserTokenType, HasLexicalPattern {
  REGEX("/(\\\\.|[^/>])+?/i?"),
  PATTERNTOKEN("(\\(\\?[li]+\\))?(\\\\.|[^\\p{Z}\\Q@$%#/{}:\"()[]&|~+*?^\\E])+|(\"(\\\\.|[^\"])+?\")"),
  TAGMATCH("\\$(\\\\.|[^\\p{Z}\\Q@$%#/{}:\"()[]&|~+*?^\\E])+"),
  ATTRMATCH(
    "\\$(\\\\.|[^\\p{Z}\\Q@$%#/{}:\"()[]&|~+*?^\\E])+:((\\\\.|[^\\p{Z}\\Q@$%#/{}:\"()[]&|~+*?^\\E])+|(\"(\\\\.|[^\"])+?\"))"),
  LEXICON("\\%(\"[^\"]+\"|[^\\p{Z}\\Q@$%#/{}:\"()[]&|~+*?^\\E]+)"),
  ANNOTATION("\\{(\\\\.|[^\\p{Z}\\Q@$%#/{}:\"()[]&|~+*?^\\E])+"),
  PUNCTUATION(quote("${PUNCT}")),
  NUMBER(quote("${NUMBER}")),
  STOPWORD(quote("${STOPWORD}")),
  ANY("~(\\d+)?"),
  NOT(quote("^")),
  RANGE("\\{\\d+\\s*,\\s*(\\d+|\\*)\\}"),
  LOOKAHEADPOST(quote("(?>")),
  NEGLOOKAHEADPOST(quote("(?!>")),
  GROUP("\\(\\?<[A-Za-z_]+>"),
  PARENT("/>"),
  RELATION("@(\\\\.|[^\\p{Z}\\Q@$%#/{}:\"()[]&|~+*?^\\E])+(:(\\\\.|[^\\p{Z}\\Q@$%#/{}:\"()[]&|~+*?\\E])+)?"),
  RELATIONGROUP("\\{@(\\\\.|[^\\p{Z}\\Q@$%#/{}:\"()[]&|~+*?^\\E])+(:(\\\\.|[^\\p{Z}\\Q@$%#/{}:\"()[]&|~+*?^\\E])+)?");

  //@ followed by escaped char or (valid phrase followed by 0 or 1 (: followed by one or more escaped char or not resevered))

  private final String pattern;

  RegexTokenTypes(String pattern) {
    this.pattern = pattern;
  }

  RegexTokenTypes(Regex pattern) {
    this.pattern = pattern.getPattern();
  }

  @Override
  public boolean isInstance(ParserTokenType tokenType) {
    return this == tokenType;
  }

  @Override
  public String lexicalPattern() {
    return pattern;
  }
}//END OF TokenTypes

