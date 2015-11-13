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

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.lexicon.LexiconManager;
import com.davidbracewell.hermes.lexicon.TrieLexicon;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * <p>A lexicon annotator that uses a trie-backed lexicon allowing for prefix matches.</p>
 *
 * @author David B. Bracewell
 */
public class TrieLexiconAnnotator implements Annotator, Serializable {
  private static final long serialVersionUID = 1L;
  private final AnnotationType type;
  private final TrieLexicon lexicon;


  private TrieLexiconAnnotator(@NonNull AnnotationType type, @NonNull String lexiconName) {
    this.lexicon = Cast.as(LexiconManager.getLexicon(lexiconName));
    this.type = type;
  }

  @Override
  public void annotate(Document document) {
//    for (HString f : lexicon.findMatches(document)) {
//      document.createAnnotation(type, f);
//    }
  }

  @Override
  public Set<AnnotationType> satisfies() {
    return Collections.singleton(type);
  }


  /**
   * Sets prefix match.
   *
   * @param fuzzyMatch the fuzzy match
   */
  public void setPrefixMatch(boolean fuzzyMatch) {
    this.lexicon.setFuzzyMatch(fuzzyMatch);
  }

}//END OF TrieLexiconAnnotator
