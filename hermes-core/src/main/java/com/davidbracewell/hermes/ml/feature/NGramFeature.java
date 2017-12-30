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
import com.davidbracewell.apollo.ml.featurizer.Featurizer;
import com.davidbracewell.cache.Cached;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.extraction.AbstractNGramExtractor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public class NGramFeature implements Featurizer<HString> {
   private static final long serialVersionUID = 1L;
   final Builder spec;

   protected NGramFeature(Builder spec) {
      this.spec = spec;
   }

   public static Builder builder() {
      return new Builder();
   }

   @Override
   @Cached(keyMaker = HStringKeyMaker.class)
   public List<Feature> apply(HString hString) {
      return spec.countTuples(hString).entries().stream()
                 .map(e -> Feature.real("NGRAM(" + e.getKey().stream()
                                                    .map(Object::toString)
                                                    .collect(Collectors.joining(" ")) + ")",
                                        e.getValue()))
                 .collect(Collectors.toList());
   }

   public static class Builder extends AbstractNGramExtractor<Builder> {
      private static final long serialVersionUID = 1L;

      public NGramFeature build() {
         return new NGramFeature(this);
      }

   }

}//END OF NGramFeature
