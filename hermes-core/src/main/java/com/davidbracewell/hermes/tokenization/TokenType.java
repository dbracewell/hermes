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
import com.google.common.collect.Sets;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public final class TokenType extends EnumValue implements Comparable<TokenType> {
   private static final long serialVersionUID = 1L;

   private static final Set<TokenType> values = Sets.newConcurrentHashSet();

   private TokenType(String name) {
      super(name);
   }

   /**
    * <p>Creates a new or retrieves an existing instance of TokenType with the given name.</p>
    *
    * @return The instance of TokenType corresponding th the give name.
    */
   public static TokenType create(@NonNull String name) {
      TokenType toReturn = DynamicEnum.register(new TokenType(name));
      values.add(toReturn);
      return toReturn;
   }

   /**
    * <p>Retrieves all currently known values of TokenType.</p>
    *
    * @return An unmodifiable collection of currently known values for TokenType.
    */
   public static Collection<TokenType> values() {
      return Collections.unmodifiableSet(values);
   }

   /**
    * <p>Returns the constant of TokenType with the specified name.The normalized version of the specified name will
    * be matched allowing for case and space variations.</p>
    *
    * @return The constant of TokenType with the specified name
    * @throws IllegalArgumentException if the specified name is not a member of TokenType.
    */
   public static TokenType valueOf(@NonNull String name) {
      return DynamicEnum.valueOf(TokenType.class, name);
   }

   @Override
   public int compareTo(@NonNull TokenType o) {
      return this.canonicalName().compareTo(o.canonicalName());
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
   public static final TokenType REPLY = create("REPLY");
   public static final TokenType TIME = create("TIME");

}//END OF TokenType
