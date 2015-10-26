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

import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * Annotates all sentences in the document in parallel.
 * </p>
 *
 * @author David B. Bracewell
 */
public abstract class SentenceLevelAnnotator implements Annotator, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public final void annotate(Document document) {
    document.sentences().stream().forEach(this::annotate);
  }

  /**
   * Annotates a single sentence.
   *
   * @param sentence The sentence to annotate
   */
  public abstract void annotate(Annotation sentence);


  @Override
  public final Set<AnnotationType> requires() {
    return Sets.union(Sets.newHashSet(Types.SENTENCE, Types.TOKEN), furtherRequires());
  }

  /**
   * The annotation types beyond sentence and token that are also required. By default will return an empty Set.
   *
   * @return The annotations beyond sentence and token that are required for this annotator to perform annotation
   */
  protected Set<AnnotationType> furtherRequires() {
    return Collections.emptySet();
  }

}//END OF SentenceLevelAnnotator
