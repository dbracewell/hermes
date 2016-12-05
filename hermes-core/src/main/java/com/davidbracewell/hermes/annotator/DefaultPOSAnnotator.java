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
import com.davidbracewell.config.Config;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.ml.pos.POSTagger;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author David B. Bracewell
 */
public class DefaultPOSAnnotator extends SentenceLevelAnnotator implements Serializable {
  private static final long serialVersionUID = 1L;
  private volatile ConcurrentMap<Language, POSTagger> taggers = new ConcurrentHashMap<>();


  private POSTagger loadModel(Language language) {
    if (!taggers.containsKey(language)) {
      synchronized (this) {
        if (!taggers.containsKey(language)) {
          try {
            Resource classPath = Resources.fromClasspath("hermes/models/" + language.getCode().toLowerCase() + "/pos.model.gz");
            if (classPath.exists()) {
              taggers.put(language, POSTagger.read(classPath));
            } else {
              taggers.put(language, POSTagger.read(Config.get("Attribute.PART_OF_SPEECH", language, "model").asResource()));
            }
          } catch (Exception e) {
            throw Throwables.propagate(e);
          }
        }
      }
    }
    return taggers.get(language);
  }

  @Override
  public void annotate(Annotation sentence) {
    loadModel(sentence.getLanguage()).tag(sentence);
  }

  @Override
  public Set<AnnotatableType> satisfies() {
    return Collections.singleton(Types.PART_OF_SPEECH);
  }

}//END OF DefaultPOSAnnotator
