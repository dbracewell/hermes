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

import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.morphology.Lemmatizers;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

/**
 * <p>Sets the lemma attribute of each token in the document</p>
 *
 * @author David B. Bracewell
 */
public class LemmaAnnotator implements Annotator {
  private static final long serialVersionUID = 1L;

  @Override
  public void annotate(Document document) {
    document.tokens().stream()
        .forEach(token -> {
          String lemma = Lemmatizers.getLemmatizer(token.getLanguage()).lemmatize(token);
          token.put(Attrs.LEMMA, lemma.toLowerCase());
        });
  }

  @Override
  public Set<AnnotationType> satisfies() {
    return Collections.singleton(Types.LEMMA);
  }

  @Override
  public Set<AnnotationType> requires() {
    return Sets.newHashSet(Types.TOKEN, Types.PART_OF_SPEECH);
  }

}//END OF LemmaAnnotator

