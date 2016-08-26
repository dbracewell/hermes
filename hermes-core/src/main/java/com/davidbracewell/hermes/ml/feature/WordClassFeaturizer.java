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

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.Featurizer;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.string.StringUtils;

import java.util.Collections;
import java.util.Set;

import static com.davidbracewell.collection.set.Sets.asSet;
import static com.davidbracewell.collection.set.Sets.linkedHashSet;

/**
 * @author David B. Bracewell
 */
public class WordClassFeaturizer implements Featurizer<HString> {
   private static final long serialVersionUID = 1L;
   private static final Set<String> WORD_FEATURES = Collections
         .unmodifiableSet(linkedHashSet("WordClass=ALL_UPPER_CASE",
                                        "WordClass=ALL_DIGIT",
                                        "WordClass=ALL_SYMBOL",
                                        "WordClass=ALL_UPPER_DIGIT",
                                        "WordClass=ALL_UPPER_SYMBOL",
                                        "WordClass=ALL_DIGIT_SYMBOL",
                                        "WordClass=ALL_UPPER_DIGIT_SYMBOL",
                                        "WordClass=INITIAL_UPPER",
                                        "WordClass=ALL_LETTER",
                                        "WordClass=ALL_LETTER_NUMBER",
                                        "WordClass=ALL_LOWER_CASE")
                         );

   @Override
   public Set<Feature> apply(HString string) {
      if (string == null || string.isEmpty()) {
         return Collections.singleton(Feature.TRUE("WordClass", "NO"));
      }
      return Collections.singleton(Feature.TRUE(wordClass(string.toString())));
   }

   public static String wordClass(String string) {
      if (StringUtils.isNullOrBlank(string)) {
         return Feature.TRUE("WordClass", "NO").getName();
      }


      Set<String> features = asSet(WORD_FEATURES);


      for (int i = 0; i < string.length(); i++) {
         char c = string.charAt(i);

         if (i == 0 && !Character.isUpperCase(c)) {
            features.remove("WordClass=INITIAL_UPPER");
         }

         if (Character.isUpperCase(c)) {
            features.remove("WordClass=ALL_LOWER_CASE");
            features.remove("WordClass=ALL_DIGIT");
            features.remove("WordClass=ALL_SYMBOL");
            features.remove("WordClass=ALL_DIGIT_SYMBOL");
         } else if (Character.isDigit(c) || c == ',' || c == '.') {
            features.remove("WordClass=ALL_LOWER_CASE");
            features.remove("WordClass=ALL_UPPER_CASE");
            features.remove("WordClass=ALL_SYMBOL");
            features.remove("WordClass=ALL_UPPER_SYMBOL");
            features.remove("WordClass=ALL_LETTER");
         } else if (Character.isLowerCase(c)) {
            features.remove("WordClass=ALL_UPPER_CASE");
            features.remove("WordClass=ALL_DIGIT");
            features.remove("WordClass=ALL_SYMBOL");
            features.remove("WordClass=ALL_UPPER_DIGIT");
            features.remove("WordClass=ALL_UPPER_SYMBOL");
            features.remove("WordClass=ALL_DIGIT_SYMBOL");
            features.remove("WordClass=ALL_UPPER_DIGIT_SYMBOL");
         } else {
            features.remove("WordClass=ALL_UPPER_CASE");
            features.remove("WordClass=ALL_DIGIT");
            features.remove("WordClass=ALL_LETTER");
            features.remove("WordClass=ALL_LETTER_NUMBER");
            features.remove("WordClass=ALL_DIGIT_SYMBOL");
         }
      }

      for (String feature : WORD_FEATURES) {
         if (features.contains(feature)) {
            return feature;
         }
      }

      return Feature.TRUE("WordClass", "NO").getName();
   }

}//END OF WordClassFeaturizer
