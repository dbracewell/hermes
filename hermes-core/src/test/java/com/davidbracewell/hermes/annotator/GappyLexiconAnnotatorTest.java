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
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Pipeline;
import com.davidbracewell.hermes.Types;
import org.junit.Test;

/**
 * @author David B. Bracewell
 */
public class GappyLexiconAnnotatorTest {

  @Test
  public void testMatch() {
    Config.initializeTest();
    Document document = DocumentProvider.getDocument();
    Pipeline.process(document, Types.TOKEN, Types.SENTENCE);
//
//    GappyLexiconAnnotator gappyLexiconAnnotator = new GappyLexiconAnnotator(
//      Types.LEXICON_MATCH,
//      Attrs.TAG,
//      false,
//      5,
//      Resources.fromString(
//        "get tired,SLEEPY\n" +
//          "get very tired,VERY_SLEEPY\n" +
//          "feel sleepy,SLEEPY\n" +
//          "she peeped,ACTION\n" +
//          "sitting on the bank,ACTION\n" +
//          "sitting the bank,ACTION\n" +
//          "rabbit took a watch,ACTION\n"
//      )
//    );
//
//    gappyLexiconAnnotator.annotate(document);
//    List<Annotation> annotationList = document.get(Types.LEXICON_MATCH);
//
//    assertEquals(5, annotationList.size());
//    assertEquals("get very tired", annotationList.get(0).toLowerCase());
//    assertEquals("VERY_SLEEPY", annotationList.get(0).getTag().get().name());
//    assertEquals("sitting by her sister on the bank", annotationList.get(1).toLowerCase());
//    assertEquals("she had peeped", annotationList.get(2).toLowerCase());
//    assertEquals("feel very sleepy", annotationList.get(3).toLowerCase());
//    assertEquals("rabbit actually took a watch", annotationList.get(4).toLowerCase());

  }

}