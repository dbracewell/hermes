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

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * Annotates a sentence at a time.
 * </p>
 *
 * @author David B. Bracewell
 */
public abstract class SentenceLevelAnnotator implements Annotator, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public final void annotate(Document document) {
    document.sentences().forEach(this::annotate);
  }

  /**
   * Annotates a single sentence.
   *
   * @param sentence The sentence to annotate
   */
  public abstract void annotate(Annotation sentence);


  @Override
  public Set<AnnotationType> requires() {
    return Collections.singleton(Types.SENTENCE);
  }
}//END OF SentenceLevelAnnotator
