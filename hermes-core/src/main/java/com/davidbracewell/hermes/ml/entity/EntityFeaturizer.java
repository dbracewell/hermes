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

import com.davidbracewell.Language;
import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.sequence.Context;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.hermes.Annotation;

import java.util.HashSet;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class EntityFeaturizer implements SequenceFeaturizer<Annotation> {
   private static final long serialVersionUID = 1L;


   private static Feature p(String name) {
      return Feature.TRUE(name);
   }

   @Override
   public Set<Feature> apply(Context<Annotation> itr) {
      Set<Feature> features = new HashSet<>();
      Language language = Language.fromString(itr.getCurrent().toString());
      if (language != Language.UNKNOWN) {
         features.add(p("IsLanguageName"));
      }
      return features;
   }


}//END OF EntityFeaturizer
