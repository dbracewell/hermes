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

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.*;
import com.davidbracewell.io.Resources;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class ViterbiLexiconAnnotatorTest {

  @Test
  public void testMatch() {
    Config.initializeTest();
    Document document = DocumentProvider.getDocument();
    Pipeline.process(document, Types.TOKEN, Types.SENTENCE);

    ViterbiLexiconAnnotator viterbiLexiconAnnotator = new ViterbiLexiconAnnotator(
      7,
      Types.LEXICON_MATCH,
      Attrs.TAG,
      false,
      Resources.fromString(
        "get tired,SLEEPY\n" +
          "get very tired,VERY_SLEEPY\n" +
          "feel very sleepy,SLEEPY\n" +
          "she had peeped,ACTION\n" +
          "sitting by her sister on the bank,ACTION\n" +
          "sitting the bank,ACTION\n" +
          "rabbit actually took a watch,ACTION\n"
      )
    );

    viterbiLexiconAnnotator.annotate(document);
    List<Annotation> annotationList = document.get(Types.LEXICON_MATCH);
    assertEquals(5, annotationList.size());
    assertEquals("get very tired", annotationList.get(0).toLowerCase());
    assertEquals("VERY_SLEEPY", annotationList.get(0).getTag().get().name());
    assertEquals("she had peeped", annotationList.get(2).toLowerCase());
    assertEquals("feel very sleepy", annotationList.get(3).toLowerCase());
    assertEquals("rabbit actually took a watch", annotationList.get(4).toLowerCase());

  }

}