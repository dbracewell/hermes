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

package com.davidbracewell.hermes.annotator;

import com.davidbracewell.Language;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Hermes;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.ml.BIOTagger;
import com.davidbracewell.logging.Loggable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author David B. Bracewell
 */
public class DefaultMlEntityAnnotator extends SentenceLevelAnnotator implements Serializable, Loggable {
   private static final long serialVersionUID = 1L;
   private volatile ConcurrentMap<Language, BIOTagger> taggers = new ConcurrentHashMap<>();

   @Override
   public void annotate(Annotation sentence) {
      BIOTagger tagger = Hermes.loadModel(this,
                                          sentence.getLanguage(),
                                          "Annotation.ML_ENTITY",
                                          "ner.model.gz",
                                          () -> taggers.get(sentence.getLanguage()),
                                          newTagger -> taggers.put(sentence.getLanguage(), newTagger)
                                         );
      if (tagger != null) {
         tagger.tag(sentence);
      }
   }


   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.ML_ENTITY);
   }

   @Override
   protected Set<AnnotatableType> furtherRequires() {
      return Collections.singleton(Types.PART_OF_SPEECH);
   }

}//END OF MLEntityAnnotator
