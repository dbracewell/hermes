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

import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.lexicon.TrieLexicon;
import com.davidbracewell.io.resource.Resource;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * The type Trie lexicon annotator.
 *
 * @author David B. Bracewell
 */
public class TrieLexiconAnnotator implements Annotator {

  private final AnnotationType type;
  private final TrieLexicon lexicon;


  /**
   * Instantiates a new Trie lexicon annotator.
   *
   * @param type         the type
   * @param tagAttribute the tag attribute
   * @param lexiconFiles the lexicon files
   */
  public TrieLexiconAnnotator(AnnotationType type, Attribute tagAttribute, Resource... lexiconFiles) {
    this(false, false, type, tagAttribute, Arrays.asList(lexiconFiles));
  }


  /**
   * Instantiates a new Trie lexicon annotator.
   *
   * @param caseSensitive the case sensitive
   * @param type          the type
   * @param tagAttribute  the tag attribute
   * @param lexiconFiles  the lexicon files
   */
  public TrieLexiconAnnotator(boolean caseSensitive, AnnotationType type, Attribute tagAttribute, Resource... lexiconFiles) {
    this(caseSensitive, false, type, tagAttribute, Arrays.asList(lexiconFiles));
  }


  /**
   * Instantiates a new Trie lexicon annotator.
   *
   * @param type         the type
   * @param tagAttribute the tag attribute
   * @param lexiconFiles the lexicon files
   */
  public TrieLexiconAnnotator(AnnotationType type, Attribute tagAttribute, Collection<Resource> lexiconFiles) {
    this(false, false, type, tagAttribute, lexiconFiles);
  }


  /**
   * Instantiates a new Trie lexicon annotator.
   *
   * @param caseSensitive the case sensitive
   * @param type          the type
   * @param tagAttribute  the tag attribute
   * @param lexiconFiles  the lexicon files
   * @parm fuzzy
   */
  public TrieLexiconAnnotator(boolean caseSensitive, boolean fuzzy, AnnotationType type, Attribute tagAttribute, Collection<Resource> lexiconFiles) {
    this.type = Preconditions.checkNotNull(type);
    this.lexicon = new TrieLexicon(caseSensitive, tagAttribute, lexiconFiles);
    this.lexicon.setFuzzyMatch(fuzzy);
  }

  @Override
  public void annotate(Document document) {
    for (HString f : lexicon.findMatches(document)) {
      document.createAnnotation(type, f);
    }
  }

  @Override
  public Set<AnnotationType> provides() {
    return Collections.singleton(type);
  }


  public void setFuzzyMatch(boolean fuzzyMatch) {
    this.lexicon.setFuzzyMatch(fuzzyMatch);
  }

}//END OF TrieLexiconAnnotator
