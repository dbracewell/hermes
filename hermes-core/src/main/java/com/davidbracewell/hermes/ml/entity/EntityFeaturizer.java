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

package com.davidbracewell.hermes.ml.entity;

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.sequence.ContextualIterator;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Fragments;
import com.davidbracewell.hermes.StringAnalyzer;
import com.davidbracewell.string.StringPredicates;
import com.davidbracewell.string.StringUtils;

import java.util.*;

import static com.davidbracewell.apollo.ml.sequence.Sequence.BOS;
import static com.davidbracewell.apollo.ml.sequence.Sequence.EOS;
import static com.davidbracewell.collection.map.Maps.map;

/**
 * @author David B. Bracewell
 */
public class EntityFeaturizer implements SequenceFeaturizer<Annotation> {
   private static final long serialVersionUID = 1L;

   private void affixes(String word, int position, int length, Set<Feature> features) {
      if (word.length() >= length && !word.equals("!DIGIT") && !word.equals("!YEAR") && !word.equals(BOS) && !word
            .equals(EOS)) {
         for (int li = 0; li < length; li++) {
            features.add(
                  Feature.TRUE(
                        "suffix[" + position + "][" + (li + 1) + "]=" +
                              word.substring(Math.max(word.length() - li - 1, 0))
                              )
                        );
         }
         for (int li = 0; li < length; li++) {
            features.add(
                  Feature.TRUE(
                        "prefix[" + position + "][" + (li + 1) + "]=" +
                              word.substring(0, Math.min(li + 1, word.length()))
                              )
                        );
         }
      }
   }


   private String create(String name, int index) {
      return name + "[" + index + "]";
   }

   private Map<String, String> generateFeatures(Annotation annotation, int position) {
      if (annotation.isEmpty() && position == -1) {
         return map(create("W", position), BOS,
                    create("WShape", position), BOS,
                    create("T", position), BOS);
      } else if (annotation.isEmpty() && position == 1) {
         return map(create("W", position), EOS,
                    create("WShape", position), EOS,
                    create("T", position), EOS);
      } else if (annotation.isEmpty()) {
         return Collections.emptyMap();
      }

      Map<String, String> map = new HashMap<>();
      map.put(create("W", position), annotation.toLowerCase());
      map.put(create("WShape", position), StringAnalyzer.shape(annotation));
      map.put(create("T", position), annotation.getPOS().asString());
      String str = annotation.toString();

      if (StringPredicates.IS_UPPER_CASE.test(str)) {
         map.put(create("ALL_UPPER", position), StringUtils.EMPTY);
      }
      if (StringPredicates.HAS_CAPITAL_LETTER.test(str)) {
         map.put(create("HAS_UPPER", position), StringUtils.EMPTY);
      }
      if (StringPredicates.HAS_DIGIT.test(str)) {
         map.put(create("DIGIT", position), StringUtils.EMPTY);
      }
      if (StringPredicates.HAS_PUNCTUATION.test(str)) {
         map.put(create("HAS_PUNCT", position), StringUtils.EMPTY);
      }
      return map;
   }

   @Override
   public Set<Feature> apply(ContextualIterator<Annotation> itr) {
      Set<Feature> features = new HashSet<>();

      Map<String, String> p0 = generateFeatures(itr.getCurrent(), 0);
      Map<String, String> p1 = generateFeatures(itr.getPrevious(-1).orElse(Fragments.detachedEmptyAnnotation()), -1);
      Map<String, String> n1 = generateFeatures(itr.getPrevious(+1).orElse(Fragments.detachedEmptyAnnotation()), +1);

      p0.entrySet().stream().map(e -> Feature.TRUE(e.getKey(), e.getValue())).forEach(features::add);
      p1.entrySet().stream().map(e -> Feature.TRUE(e.getKey(), e.getValue())).forEach(features::add);
      n1.entrySet().stream().map(e -> Feature.TRUE(e.getKey(), e.getValue())).forEach(features::add);

//      features.add(Feature.TRUE("W[-1,0]", p1.get("W[-1]"), p0.get("W[0]")));
//      features.add(Feature.TRUE("WShape[-1,0]", p1.get("WShape[-1]"), p0.get("WShape[0]")));
//      features.add(Feature.TRUE("T[-1,0]", p1.get("T[-1]"), p0.get("T[0]")));
//
//      features.add(Feature.TRUE("W[0,+1]", p0.get("W[0]"), n1.get("W[1]")));
//      features.add(Feature.TRUE("WShape[0,+1]", p0.get("WShape[0]"), n1.get("WShape[1]")));
//      features.add(Feature.TRUE("T[0,+1]", p0.get("T[0]"), n1.get("T[1]")));

      if (itr.getIndex() - 2 >= 0) {
         Map<String, String> p2 = generateFeatures(itr.getPrevious(-2).orElse(Fragments.detachedEmptyAnnotation()), -2);
         p2.entrySet().stream().map(e -> Feature.TRUE(e.getKey(), e.getValue())).forEach(features::add);
//         features.add(Feature.TRUE("W[-2,-1]", p2.get("W[-2]"), p1.get("W[-1]")));
//         features.add(Feature.TRUE("WShape[-2,-1]", p2.get("WShape[-2]"), p1.get("WShape[-1]")));
//         features.add(Feature.TRUE("T[-2,-1]", p2.get("T[-2]"), p1.get("T[-1]")));
//
//         features.add(Feature.TRUE("W[-2,-1,0]", p2.get("W[-2]"), p1.get("W[-1]"), p0.get("W[0]")));
//         features.add(Feature.TRUE("WShape[-2,-1,0]", p2.get("WShape[-2]"), p1.get("WShape[-1]"), p0.get("WShape[0]")));
//         features.add(Feature.TRUE("T[-2,-1,0]", p2.get("T[-2]"), p1.get("T[-1]"), p0.get("T[0]")));
      }


      return features;
   }


}//END OF EntityFeaturizer
