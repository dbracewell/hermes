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
import com.davidbracewell.hermes.lexicon.SimpleTagLexicon;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.tuple.Tuple2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * The type Viterbi lexicon annotator.
 *
 * @author David B. Bracewell
 */
public class ViterbiLexiconAnnotator extends ViterbiAnnotator {

  final AnnotationType type;
  final SimpleTagLexicon lexicon;
  final Attribute tagAttribute;

  /**
   * Instantiates a new Viterbi lexicon annotator.
   *
   * @param type         the type
   * @param tagAttribute the tag attribute
   * @param lexiconFiles the lexicon files
   */
  public ViterbiLexiconAnnotator(AnnotationType type, Attribute tagAttribute, Collection<Resource> lexiconFiles) {
    this(7, type, tagAttribute, false, lexiconFiles);
  }

  /**
   * Instantiates a new Viterbi lexicon annotator.
   *
   * @param type         the type
   * @param tagAttribute the tag attribute
   * @param lexiconFiles the lexicon files
   */
  public ViterbiLexiconAnnotator(AnnotationType type, Attribute tagAttribute, Resource... lexiconFiles) {
    this(7, type, tagAttribute, false, lexiconFiles);
  }

  /**
   * Instantiates a new Viterbi lexicon annotator.
   *
   * @param maxSpanSize   the max span size
   * @param type          the type
   * @param tagAttribute  the tag attribute
   * @param caseSensitive the case sensitive
   * @param lexiconFiles  the lexicon files
   */
  public ViterbiLexiconAnnotator(int maxSpanSize, AnnotationType type, Attribute tagAttribute, boolean caseSensitive, Resource... lexiconFiles) {
    this(maxSpanSize, type, tagAttribute, caseSensitive, Arrays.asList(lexiconFiles));
  }

  /**
   * Instantiates a new Viterbi lexicon annotator.
   *
   * @param maxSpanSize   the max span size
   * @param type          the type
   * @param tagAttribute  the tag attribute
   * @param caseSensitive the case sensitive
   * @param lexiconFiles  the lexicon files
   */
  public ViterbiLexiconAnnotator(int maxSpanSize, AnnotationType type, Attribute tagAttribute, boolean caseSensitive, Collection<Resource> lexiconFiles) {
    super(maxSpanSize);
    this.type = type;
    this.tagAttribute = tagAttribute;
    this.lexicon = new SimpleTagLexicon(caseSensitive, tagAttribute, lexiconFiles);
  }

  @Override
  protected void createAndAttachAnnotation(Document document, Match match) {
    if (match.matchedString != null) {
      Annotation annotation = document.createAnnotation(type, match.span);
      annotation.putAttribute(tagAttribute, lexicon.lookup(match.matchedString).get());
      annotation.putAttribute(Attrs.CONFIDENCE, lexicon.probability(annotation));
    }
  }

  @Override
  public Set<AnnotationType> provides() {
    return Collections.singleton(type);
  }

  @Override
  protected Tuple2<String, Double> scoreSpan(HString span) {
    if (lexicon.contains(span)) {
      return Tuple2.of(span.toString(), Math.pow(span.tokenLength() * lexicon.probability(span.toString()), 2));
    }
    return Tuple2.of(null, 0d);
  }


}//END OF ViterbiLexiconAnnotator
