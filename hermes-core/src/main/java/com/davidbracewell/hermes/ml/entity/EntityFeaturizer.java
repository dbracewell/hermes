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
import com.davidbracewell.hermes.ml.feature.WordShapeFeaturizer;
import com.davidbracewell.string.StringUtils;

import java.util.HashSet;
import java.util.Set;

import static com.davidbracewell.apollo.ml.sequence.Sequence.BOS;
import static com.davidbracewell.apollo.ml.sequence.Sequence.EOS;

/**
 * @author David B. Bracewell
 */
public class EntityFeaturizer implements SequenceFeaturizer<Annotation> {
   private static final long serialVersionUID = 1L;

   private void affixes(String word, int position, int length, Set<Feature> features) {
      if (word.length() >= length && !word.equals("!DIGIT") && !word.equals("!YEAR") && !word.equals(BOS) && !word
            .equals(EOS)) {
         for (int li = 0; li < length; li++) {
            features.add(Feature.TRUE("suffix[" + position + "][" + (li + 1) + "]=" +
                                            word.substring(Math.max(word.length() - li - 1, 0)))
                        );
         }
         for (int li = 0; li < length; li++) {
            features.add(Feature.TRUE("prefix[" + position + "][" + (li + 1) + "]=" +
                                            word.substring(0, Math.min(li + 1, word.length())))
                        );
         }
      }
   }


   private static Feature p(String name) {
      return Feature.TRUE(name);
   }

   @Override
   public Set<Feature> apply(ContextualIterator<Annotation> itr) {
      Set<Feature> features = new HashSet<>();

      String current = itr.getCurrent().toString();
      if (current.length() == 2) {
         features.add(p("2d"));
      } else if (current.length() == 4) {
         features.add(p("4d"));
      }

      if (current.length() == 2 && Character.isUpperCase(current.charAt(0)) && current.charAt(1) == '.') {
         features.add(p("cp"));
      }

      features.add(p(StringUtils.compactRepeatedChars(current)));
      features.add(p(WordShapeFeaturizer.INSTANCE.extractPredicate(itr.getCurrent())));
      features.add(p(StringUtils.compactRepeatedChars(WordShapeFeaturizer.INSTANCE.extractPredicate(itr.getCurrent()))));
      affixes(current, 0, 4, features);

      return features;
   }


}//END OF EntityFeaturizer
