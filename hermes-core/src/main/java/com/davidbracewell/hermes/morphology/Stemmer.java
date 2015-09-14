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

package com.davidbracewell.hermes.morphology;


import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

/**
 * <p>Defines the interface for stemming tokens.</p>
 *
 * @author David B. Bracewell
 */
public interface Stemmer {

  /**
   * Stem the given token (string) without consideration of the part of speech
   *
   * @param string The token
   * @return The stemmed version
   */
  String stem(String string);

  /**
   * Stems the given text object.
   *
   * @param text The text to stem
   * @return The stemmed version
   */
  default String stem(@NonNull HString text) {
    if (text.isEmpty()) {
      return StringUtils.EMPTY;
    }
    if (text.tokenLength() == 1) {
      return stem(text.first(Types.TOKEN).toString());
    }
    StringBuilder builder = new StringBuilder(stem(text.first(Types.TOKEN).toString()));
    for (int i = 1; i < text.tokenLength(); i++) {
      if (text.getLanguage().usesWhitespace()) {
        builder.append(" ");
      }
      builder.append(stem(text.tokenAt(i).toString()));
    }
    return builder.toString();
  }


}//END OF Stemmer
