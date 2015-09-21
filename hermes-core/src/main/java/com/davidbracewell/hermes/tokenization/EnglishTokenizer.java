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

import com.davidbracewell.collection.Collect;
import com.davidbracewell.collection.trie.PatriciaTrie;
import com.davidbracewell.io.Resources;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public class EnglishTokenizer implements Tokenizer, Serializable {
  private static final long serialVersionUID = 1L;
  private final Set<String> abbreviations;
  private final Set<String> tlds;
  public final PatriciaTrie<String> emoticons;

  public EnglishTokenizer() {
    try {
      this.abbreviations = Resources.fromClasspath("com/davidbracewell/hermes/tokenization/abbreviations.txt")
        .readLines().stream()
        .map(line -> line.trim().toLowerCase())
        .collect(Collectors.toSet());
      this.tlds = Resources.fromClasspath("com/davidbracewell/hermes/tokenization/tlds.txt")
        .readLines().stream()
        .map(line -> line.trim().toLowerCase())
        .collect(Collectors.toSet());

      this.emoticons = new PatriciaTrie<>();
      Resources.fromClasspath("com/davidbracewell/hermes/tokenization/emoticons.txt").forEach(
        line -> emoticons.put(line.trim().toLowerCase(), line)
      );

    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }


  @Override
  public Iterable<Token> tokenize(@NonNull Reader reader) {
    return Collect.asIterable(new TokenIterator(reader));
  }

  private class TokenIterator implements Iterator<Token> {
    private int lastIndex = 0;
    private final LinkedList<Token> buffer = Lists.newLinkedList();
    private final com.davidbracewell.hermes.tokenization.StandardTokenizer tokenizer;

    public TokenIterator(Reader reader) {
      this.tokenizer = new StandardTokenizer(reader);
    }

    @Override
    public boolean hasNext() {
      return peek(0) != null;
    }

    @Override
    public Token next() {
      if (peek(0) == null) {
        throw new NoSuchElementException();
      }

      Token token = buffer.remove();

      if (token.type.isInstance(TokenType.URL)) {
        token = checkURL(token);
      } else if (peekIsType(0, TokenType.PUNCTUATION) &&
        (token.type.isInstance(TokenType.ACRONYM) || abbreviations.contains(token.text.toLowerCase()))) {
        token = mergeAbbreviationAndAcronym(token);
      } else if (token.type.isInstance(TokenType.PUNCTUATION, TokenType.HYPHEN)) {
        token = handleEmoticon(token);
      }

      if (token.type.isInstance(TokenType.HYPHEN)) {
        token = mergeMultiHyphens(token);
      }

      token.index = lastIndex;
      lastIndex++;
      return token;
    }


    private Token consume(int number) {
      Token token = null;
      while (number >= 0) {
        token = consume();
        number--;
      }
      return token;
    }

    private Token consume() {
      peek(0);
      return buffer.isEmpty() ? null : buffer.remove();
    }

    private boolean peekIsType(int distance, TokenType type, TokenType... types) {
      Token peeked = peek(distance);
      if (peeked == null) {
        return false;
      }
      return peeked.type.isInstance(type, types);
    }

    private Token peek(int distance) {
      while (buffer.size() <= distance) {
        try {
          Token token = tokenizer.next();
          if (token == null) {
            return null;
          }
          buffer.add(token);
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }
      }
      return buffer.get(distance);
    }

    private Token checkURL(Token n) {
      //Ensure that the TLD is valid
      int dot = n.text.lastIndexOf('.');
      String tld = n.text.substring(dot + 1);

      if (!tlds.contains(tld.toLowerCase())) {
        Token nn = peek(0);
        if (nn != null && nn.charStartIndex == n.charEndIndex) {
          consume();
          buffer.addFirst(new Token( //Add the bad tld
              tld + nn.text,
              TokenType.ALPHA_NUMERIC,
              n.charStartIndex + dot + 1,
              nn.charEndIndex,
              n.index
            )
          );
        } else {
          buffer.addFirst(new Token( //Add the bad tld
              tld,
              TokenType.ALPHA_NUMERIC,
              n.charStartIndex + dot + 1,
              n.charEndIndex,
              n.index
            )
          );
        }

        buffer.addFirst(new Token( //Add the dot to the buffer
            n.text.substring(dot, dot + 1),
            TokenType.PUNCTUATION,
            n.charStartIndex + dot,
            n.charStartIndex + dot + 1,
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
      return n;
    }

    private Token mergeMultiHyphens(Token n) {
      String text = n.text;
      int end = n.charEndIndex;
      while (peekIsType(0, TokenType.HYPHEN)) {
        Token nn = consume();
        end = nn.charEndIndex;
        text += nn.text;
      }
      if (end != n.charEndIndex) {
        return new Token(text, TokenType.HYPHEN, n.charStartIndex, end, 0);
      }
      return n;
    }

    private Token handleEmoticon(Token n) {
      String emo = n.text;
      String emoLower = n.text.toLowerCase();
      if (emoticons.prefixMap(emoLower).isEmpty()) {
        return n;
      }
      Token nn;
      int end = n.charEndIndex;
      int peek = 0;
      while ((nn = peek(peek)) != null) {
        String tempLower = emoLower + nn.text.toLowerCase();
        if (emoticons.prefixMap(tempLower).size() > 0) {
          end = nn.charEndIndex;
          emo = emo + nn.text;
          emoLower = tempLower;
          peek++;
        } else if (emoticons.containsKey(tempLower)) {
          consume(peek);
          lastIndex = n.index;
          return new Token(emo, TokenType.EMOTICON, n.charStartIndex, end, n.index);
        } else if (emoLower.length() > 1 && emoticons.containsKey(emoLower.substring(0, emoLower.length() - 1))) {
          Token last = consume(peek - 1);
          lastIndex = n.index;
          return new Token(emo, TokenType.EMOTICON, n.charStartIndex, last.charEndIndex, n.index);
        } else {
          return n;
        }
      }

      return n;
    }

    private Token mergeAbbreviationAndAcronym(Token n) {
      Token nn = peek(0);
      if (nn == null) {
        return n;
      }
      if (nn.text.equals(".")) {
        Token token = new Token(
          n.text + nn.text,
          TokenType.ACRONYM,
          n.charStartIndex,
          nn.charEndIndex,
          n.index
        );
        consume();
        return token;
      }
      return n;
    }

  }

}//END OF EnglishTokenizer
