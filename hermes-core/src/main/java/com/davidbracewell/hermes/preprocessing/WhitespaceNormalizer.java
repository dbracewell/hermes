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

package com.davidbracewell.hermes.preprocessing;

import com.davidbracewell.Language;
import com.davidbracewell.string.StringUtils;

/**
 * @author David B. Bracewell
 */
public class WhitespaceNormalizer extends TextNormalizer {
  private static final long serialVersionUID = 1L;

  @Override
  public String performNormalization(String input, Language inputLanguage) {
    String normSpaces = input.replaceAll("\\p{Zs}+", " ");
    normSpaces = normSpaces.replaceAll("\r(?!\n)", "\n");
    normSpaces = normSpaces.replaceAll("(?<!(\\p{P}\\p{Z}?))\n", " ");
    normSpaces = normSpaces.replaceAll("\r?\n(" + StringUtils.MULTIPLE_WHITESPACE + "\r?\n)+", "\n");
    return StringUtils.trim(normSpaces);
  }

}//END OF WhitespacePreprocessor
