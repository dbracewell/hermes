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
import com.ibm.icu.text.Transliterator;

import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * Adds a <code>TRANSLITERATION</code> attribute to tokens using the ICU4J {@link Transliterator} class.
 * </p>
 *
 * @author David B. Bracewell
 */
public class TransliterationAnnotator implements Annotator {

  private final String ID;

  /**
   * <p>
   * Instantiates a new Transliteration annotator with a given id.
   * The valid ids can be obtained via {@link Transliterator#getAvailableIDs()}
   * </p>
   *
   * @param id the id
   */
  public TransliterationAnnotator(String id) {
    ID = id;
  }

  @Override
  public void annotate(Document document) {
    Transliterator transliterator = Transliterator.getInstance(ID);
    document.tokens().forEach(token ->
        token.put(Attrs.TRANSLITERATION, transliterator.transform(token.toString()))
    );
  }

  @Override
  public Set<AnnotationType> satisfies() {
    return Collections.singleton(Types.TRANSLITERATION);
  }


  @Override
  public Set<AnnotationType> requires() {
    return Collections.singleton(Types.TOKEN);
  }

}//END OF TransliterationAnnotator
