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
import com.davidbracewell.hermes.tokenization.Tokenizer;
import com.davidbracewell.hermes.tokenization.TokenizerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * A <code>BreakIterator</code> backed token annotator. The locale used for the break iterator is based on the language
 * of the document.
 * </p>
 *
 * @author David B. Bracewell
 */
public class DefaultTokenAnnotator implements Annotator, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public void annotate(Document document) {
    Tokenizer tokenizer = TokenizerFactory.create(document.getLanguage());
    for (Tokenizer.Token token : tokenizer.tokenize(document.toString())) {
      Annotation aToken = document.createAnnotation(Types.TOKEN, token.charStartIndex, token.charEndIndex, token.properties);
      aToken.put(Attrs.TOKEN_TYPE, token.type);
    }
  }

  @Override
  public Set<AnnotationType> satisfies() {
    return Collections.singleton(Types.TOKEN);
  }

}//END OF DefaultTokenAnnotator
