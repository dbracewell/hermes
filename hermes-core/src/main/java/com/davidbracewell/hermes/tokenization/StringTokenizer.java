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


import com.davidbracewell.collection.NormalizedStringMap;

import java.util.Objects;

/**
 * <p>Low level tokenization of strings</p>
 *
 * @author David B. Bracewell
 */
public interface StringTokenizer {


  /**
   * Next string tokenizer . token.
   *
   * @return the string tokenizer . token
   * @throws Exception the exception
   */
  public Token next() throws Exception;


  /**
   * An internal token
   */
  public static class Token {
    /**
     * The Text.
     */
    public final String text;
    /**
     * The Type.
     */
    public TokenType type;

    public final int charStartIndex;

    public final int charEndIndex;
    /**
     * The Index.
     */
    public int index;
    /**
     * The Properties.
     */
    public final NormalizedStringMap<String> properties = new NormalizedStringMap<>();

    /**
     * Default constructor
     *
     * @param text The text covered by the token
     * @param type The type of token
     * @param startChar The first character offset
     * @param endChar The last character offset
     * @param index The token index
     */
    public Token(String text, TokenType type, int startChar, int endChar, int index) {
      this.text = text;
      this.type = type;
      this.charStartIndex = startChar;
      this.charEndIndex = endChar;
      this.index = index;
    }


    @Override
    public int hashCode() {
      return Objects.hash(text, type, charStartIndex, charEndIndex, index);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final Token other = (Token) obj;
      return Objects.equals(this.text, other.text) &&
          Objects.equals(this.type, other.type) &&
          Objects.equals(this.charStartIndex, other.charStartIndex) &&
          Objects.equals(this.charEndIndex, other.charEndIndex) &&
          Objects.equals(this.index, other.index);
    }

    @Override
    public String toString() {
      return "Token{" +
          "text='" + text + '\'' +
          ", charOffset=[" + charStartIndex + ", " + charEndIndex + ") " +
          ", type=" + type +
          ", index=" + index +
          '}';
    }
  }//END OF StringTokenizer$Token


}//END OF StringTokenizer
