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

package com.davidbracewell.hermes.tokenization;

import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

/**
 * @author David B. Bracewell
 */
public class DefaultTokenizer implements StringTokenizer {

  private final LinkedList<Token> buffer = Lists.newLinkedList();
  private final com.davidbracewell.hermes.tokenization.StandardTokenizer tokenizer;
  private int lastIndex = -1;

  public DefaultTokenizer(Reader reader) {
    tokenizer = new com.davidbracewell.hermes.tokenization.StandardTokenizer(Preconditions.checkNotNull(reader));
  }

  private Token mergeAbbreviationAndAcronym(Token n) throws IOException {
    Token nn = tokenizer.next();
    if (nn == null) {
      return n;
    }
    if (nn.type == TokenType.PUNCTUATION) {
      Token token = new Token(
        n.text + nn.text,
        TokenType.ACRONYM,
        n.charStartIndex,
        nn.charEndIndex,
        n.index
      );
      return token;
    }
    buffer.add(nn);
    return n;
  }

  private Token mergePrefixHyphens(Token n) throws IOException {


    Token hyphen = tokenizer.next();

    if (hyphen == null) {
      return n;
    }

    if (hyphen.type == TokenType.HYPHEN) {
      //We have a hypen so lets join it
      Token suffix = tokenizer.next();
      if (suffix != null) {
        n = new Token(
          n.text + hyphen.text + suffix.text,
          TokenType.ALPHA_NUMERIC,
          n.charStartIndex,
          suffix.charEndIndex,
          n.index
        );
      }

    } else {

      buffer.add(hyphen);

    }

    return n;
  }

  private Token mergeMultiHyphens(Token n) throws IOException {
    Token nn;

    while ((nn = tokenizer.next()) != null) {

      if (nn.type == TokenType.HYPHEN) {

        n = new Token(
          n.text + nn.text,
          TokenType.HYPHEN,
          n.charStartIndex,
          nn.charEndIndex,
          n.index
        );

      } else {

        buffer.add(nn);
        return n;

      }
    }

    return n;
  }

  @Override
  public Token next() throws Exception {
    Token n;

    if (!buffer.isEmpty()) {
      n = buffer.remove();
    } else {
      n = tokenizer.next();
    }
    if (n == null) {
      return null;
    }
    if (StringUtils.isNullOrBlank(n.text)) {
      return next();
    }

    //n.index += indexOffset;
    n.index = lastIndex + 1;

    if (n.type == TokenType.URL) {
      //Ensure that the TLD is valid
      int dot = n.text.lastIndexOf('.');
      String tld = n.text.substring(dot + 1);

      if (TokenizationDicts.getInstance().getType(tld) != TokenType.URL) {

        buffer.add(new Token( //Add the dot to the buffer
            n.text.substring(dot, dot + 1),
            TokenType.PUNCTUATION,
            n.charStartIndex + dot,
            n.charStartIndex + dot + 1,
            n.index
          )
        );

        buffer.add(new Token( //Add the bad tld
            tld,
            TokenType.ALPHA_NUMERIC,
            n.charStartIndex + dot + 1,
            n.charEndIndex,
            n.index
          )
        );

        n = new Token( //Change the token to the first part of the bad url
          n.text.substring(0, dot),
          TokenType.ALPHA_NUMERIC,
          n.charStartIndex,
          n.charStartIndex + dot,
          n.index
        );

      }
    }


    TokenType dictType = TokenizationDicts.getInstance().getType(n.text);
    if (dictType.equals(TokenType.ACRONYM)) {
      n = mergeAbbreviationAndAcronym(n);
    } else if (dictType.equals(TokenType.PREFIX)) {
      n = mergePrefixHyphens(n);
    }

    if (n.type.equals(TokenType.HYPHEN)) {
      n = mergeMultiHyphens(n);
    }

    lastIndex = n.index;

    return n;
  }

}//END OF DefaultTokenizer
