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

import com.davidbracewell.DynamicEnum;
import com.davidbracewell.EnumValue;
import com.davidbracewell.string.StringUtils;

import java.io.ObjectStreamException;
import java.util.Collection;

/**
 * @author David B. Bracewell
 */
public final class TokenType extends EnumValue {

  private static final DynamicEnum<TokenType> index = new DynamicEnum<>();
  private static final long serialVersionUID = 1L;

  private TokenType(String name) {
    super(name);
  }

  /**
   * Creates an TokenType with the given name.
   *
   * @param name the name of the TokenType
   * @return the TokenType
   * @throws IllegalArgumentException If the name is invalid
   */
  public static TokenType create(String name) {
    if (StringUtils.isNullOrBlank(name)) {
      throw new IllegalArgumentException(name + " is invalid");
    }
    return index.register(new TokenType(name));
  }

  /**
   * Determine if a name is an existing TokenType
   *
   * @param name the name
   * @return True if it exists, otherwise False
   */
  public static boolean isDefined(String name) {
    return index.isDefined(name);
  }


  /**
   * Gets the TokenType associated with a string.
   *
   * @param name the name as a string
   * @return the TokenType for the string
   * @throws IllegalArgumentException if the name is not a valid TokenType
   */
  public static TokenType valueOf(String name) {
    return index.valueOf(name);
  }

  /**
   * The current collection of known TokenType
   *
   * @return All known TokenType names
   */
  public static Collection<TokenType> values() {
    return index.values();
  }


  private Object readResolve() throws ObjectStreamException {
    if (isDefined(name())) {
      return index.valueOf(name());
    }
    return index.register(this);
  }


  public static final TokenType ALPHA_NUMERIC = create("ALPHA_NUMERIC");
  public static final TokenType PUNCTUATION = create("PUNCTUATION");
  public static final TokenType CHINESE_JAPANESE = create("CHINESE_JAPANESE");
  public static final TokenType EMAIL = create("EMAIL");
  public static final TokenType NUMBER = create("NUMBER");
  public static final TokenType MONEY = create("MONEY");
  public static final TokenType UNKNOWN = create("UNKNOWN");
  public static final TokenType CONTRACTION = create("CONTRACTION");
  public static final TokenType PERSON_TITLE = create("PERSON_TITLE");
  public static final TokenType ACRONYM = create("ACRONYM");
  public static final TokenType SGML = create("SGML");
  public static final TokenType COMPANY = create("COMPANY");
  public static final TokenType PROTOCOL = create("PROTOCOL");
  public static final TokenType URL = create("URL");
  public static final TokenType HYPHEN = create("HYPHEN");
  public static final TokenType EMOTICON = create("EMOTICON");
  public static final TokenType HASH_TAG = create("HASH_TAG");

}//END OF TokenType
