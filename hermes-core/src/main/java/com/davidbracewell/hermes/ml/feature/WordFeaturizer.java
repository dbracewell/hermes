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

import java.util.Collections;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class WordFeaturizer implements Featurizer<HString> {
   private static final long serialVersionUID = 1L;
   private final boolean lowerCase;

   public WordFeaturizer() {this(true);}

   public WordFeaturizer(boolean lowerCase) {this.lowerCase = lowerCase;}

   @Override
   public Set<Feature> apply(HString s) {
      if (s == null || s.isEmpty()) {
         return Collections.singleton(Feature.TRUE("Word", "--NULL--"));
      }
      if (lowerCase) {
         return Collections.singleton(Feature.TRUE("Word", s.toLowerCase()));
      }
      return Collections.singleton(Feature.TRUE("Word", s.toString()));
   }

}//END OF WordFeaturizer