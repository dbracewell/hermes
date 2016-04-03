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

package com.davidbracewell.hermes.morphology;

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.tag.POS;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class EnglishLemmatizerTest {

  @Test
  public void testLemmatize() throws Exception {
    Config.initializeTest();

    Lemmatizer lemmatizer = EnglishLemmatizer.getInstance();
    assertEquals("walk", lemmatizer.lemmatize("walking", POS.VERB));
    assertEquals("walk", lemmatizer.lemmatize("walked", POS.VERB));
    assertEquals("walk", lemmatizer.lemmatize("walks", POS.VERB));

    assertEquals("be", lemmatizer.lemmatize("is", POS.VERB));
    assertEquals("be", lemmatizer.lemmatize("was", POS.VERB));
    assertEquals("be", lemmatizer.lemmatize("were", POS.VERB));

    assertEquals("troll", lemmatizer.lemmatize("trolling", POS.VERB));

    assertEquals("pan", lemmatizer.lemmatize("pans", POS.NOUN));
    assertEquals("clothes", lemmatizer.lemmatize("clothes", POS.NOUN));
    assertEquals("tax", lemmatizer.lemmatize("taxes", POS.NOUN));

    assertEquals("knife", lemmatizer.lemmatize("knives", POS.NOUN));
    assertEquals("life", lemmatizer.lemmatize("lives", POS.NOUN));

    //Walking may be correct if we do not the pos
    assertEquals("walking", lemmatizer.lemmatize("walking", POS.ANY));

    //Walking may be correct if we do not the pos
    assertEquals("walking", lemmatizer.lemmatize("walking"));

    Document document = DocumentFactory.getInstance().create("I was walking to the shore.", Language.ENGLISH);
    Pipeline.process(document, Types.TOKEN, Types.SENTENCE);
    assertEquals("walking", lemmatizer.lemmatize(document.find("walking")));

    document.find("was").first(Types.TOKEN).put(Attrs.PART_OF_SPEECH, POS.VBD);
    document.find("walking").first(Types.TOKEN).put(Attrs.PART_OF_SPEECH, POS.VBG);
    document.getAnnotationSet().setIsCompleted(Attrs.PART_OF_SPEECH, true, "");
    Pipeline.process(document, Attrs.LEMMA);
    assertEquals("be walk", document.find("was walking").getLemma());
  }
}

