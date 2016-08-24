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
import com.davidbracewell.apollo.ml.sequence.ContextualIterator;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.hermes.HString;

import java.util.HashSet;
import java.util.Set;

import static com.davidbracewell.apollo.ml.sequence.Sequence.BOS;
import static com.davidbracewell.apollo.ml.sequence.Sequence.EOS;

/**
 * @author David B. Bracewell
 */
public class BigramSequenceFeaturizer implements SequenceFeaturizer<HString> {
   private static final long serialVersionUID = 1L;
   private final boolean includeWordClass;

   public BigramSequenceFeaturizer() {this(true);}

   public BigramSequenceFeaturizer(boolean includeWordClass) {this.includeWordClass = includeWordClass;}

   @Override
   public Set<Feature> apply(ContextualIterator<HString> iterator) {
      Set<Feature> features = new HashSet<>();

      iterator.getPrevious(1).ifPresent(v -> {
         features.add(Feature.TRUE("Word[-1,0]", v.toString(), iterator.getCurrent().toString()));
         features.add(Feature.TRUE("WordClass[-1,0]",
                                   WordClassFeaturizer.wordClass(v.toString()),
                                   WordClassFeaturizer.wordClass(iterator.getCurrent().toString())));
      });

      if (!iterator.getPrevious(1).isPresent()) {
         features.add(Feature.TRUE("Word[-1,0]", BOS, iterator.getCurrent().toString()));
         features.add(Feature.TRUE("WordClass[-1,0]",
                                   BOS,
                                   WordClassFeaturizer.wordClass(iterator.getCurrent().toString())));
      }


      iterator.getNext(1).ifPresent(v -> {
         features.add(Feature.TRUE("Word[0,1]", iterator.getCurrent().toString(), v.toString()));
         features.add(Feature.TRUE("WordClass[0,1]",
                                   WordClassFeaturizer.wordClass(iterator.getCurrent().toString()),
                                   WordClassFeaturizer.wordClass(v.toString())));
      });


      if (!iterator.getNext(1).isPresent()) {
         features.add(Feature.TRUE("Word[0,1]", iterator.getCurrent().toString(), EOS));
         features.add(Feature.TRUE("WordClass[0,1]",
                                   WordClassFeaturizer.wordClass(iterator.getCurrent().toString()),
                                   EOS));
      }


      return features;
   }


}//END OF BigramSequenceFeaturizer
