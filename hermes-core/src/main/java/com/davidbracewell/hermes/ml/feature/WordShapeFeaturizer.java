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

package com.davidbracewell.hermes.ml.feature;

import com.davidbracewell.apollo.ml.featurizer.PredicateFeaturizer;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.string.StringUtils;

/**
 * @author David B. Bracewell
 */
public class WordShapeFeaturizer extends PredicateFeaturizer<HString> {
   public static final WordShapeFeaturizer INSTANCE = new WordShapeFeaturizer();
   private static final long serialVersionUID = 1L;

   public WordShapeFeaturizer() {
      super("WordShape");
   }

   @Override
   public String extractPredicate(HString string) {
      if (StringUtils.isNullOrBlank(string)) {
         return StringUtils.EMPTY;
      }
      StringBuilder builder = new StringBuilder();
      for (int ci = 0; ci < string.length(); ci++) {
         char c = string.charAt(ci);
         if (Character.isUpperCase(c)) {
            builder.append("U");
         } else if (Character.isLowerCase(c)) {
            builder.append("L");
         } else if (Character.isDigit(c)) {
            builder.append("D");
         } else if (c == '.' || c == ',') {
            builder.append(".");
         } else if (c == ';' || c == ':' || c == '?' || c == '!') {
            builder.append(";");
         } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '=' || c == '|' || c == '_') {
            builder.append("-");
         } else if (c == '(' || c == '{' || c == '[' || c == '<') {
            builder.append("(");
         } else if (c == ')' || c == '}' || c == ']' || c == '>') {
            builder.append(")");
         } else {
            builder.append(c);
         }
      }
      return builder.toString();
   }
}//END OF WordShapeFeaturizer
