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

import com.davidbracewell.apollo.ml.PredicateFeaturizer;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.string.StringUtils;

import java.util.Collections;
import java.util.Set;

import static com.davidbracewell.collection.set.Sets.asSet;
import static com.davidbracewell.collection.set.Sets.linkedHashSet;

/**
 * @author David B. Bracewell
 */
public class WordClassFeaturizer extends PredicateFeaturizer<HString> {
   private static final long serialVersionUID = 1L;
   private static final Set<String> WORD_FEATURES = Collections
         .unmodifiableSet(linkedHashSet("ALL_UPPER_CASE",
                                        "ALL_DIGIT",
                                        "ALL_SYMBOL",
                                        "ALL_UPPER_DIGIT",
                                        "ALL_UPPER_SYMBOL",
                                        "ALL_DIGIT_SYMBOL",
                                        "ALL_UPPER_DIGIT_SYMBOL",
                                        "INITIAL_UPPER",
                                        "ALL_LETTER",
                                        "ALL_LETTER_NUMBER",
                                        "ALL_LOWER_CASE")
                         );


   public WordClassFeaturizer() {
      super("WordClass");
   }

   @Override
   public String extractPredicate(HString string) {
      if (StringUtils.isNullOrBlank(string)) {
         return "No";
      }

      Set<String> features = asSet(WORD_FEATURES);
      for (int i = 0; i < string.length(); i++) {
         char c = string.charAt(i);

         if (i == 0 && !Character.isUpperCase(c)) {
            features.remove("INITIAL_UPPER");
         }

         if (Character.isUpperCase(c)) {
            features.remove("ALL_LOWER_CASE");
            features.remove("ALL_DIGIT");
            features.remove("ALL_SYMBOL");
            features.remove("ALL_DIGIT_SYMBOL");
         } else if (Character.isDigit(c) || c == ',' || c == '.') {
            features.remove("ALL_LOWER_CASE");
            features.remove("ALL_UPPER_CASE");
            features.remove("ALL_SYMBOL");
            features.remove("ALL_UPPER_SYMBOL");
            features.remove("ALL_LETTER");
         } else if (Character.isLowerCase(c)) {
            features.remove("ALL_UPPER_CASE");
            features.remove("ALL_DIGIT");
            features.remove("ALL_SYMBOL");
            features.remove("ALL_UPPER_DIGIT");
            features.remove("ALL_UPPER_SYMBOL");
            features.remove("ALL_DIGIT_SYMBOL");
            features.remove("ALL_UPPER_DIGIT_SYMBOL");
         } else {
            features.remove("ALL_UPPER_CASE");
            features.remove("ALL_DIGIT");
            features.remove("ALL_LETTER");
            features.remove("ALL_LETTER_NUMBER");
            features.remove("ALL_DIGIT_SYMBOL");
         }
      }

      for (String feature : WORD_FEATURES) {
         if (features.contains(feature)) {
            return feature;
         }
      }

      return "No";
   }

}//END OF WordClassFeaturizer
