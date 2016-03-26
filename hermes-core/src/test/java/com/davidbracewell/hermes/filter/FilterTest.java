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

package com.davidbracewell.hermes.filter;

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.tag.POS;
import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


/**
 * @author David B. Bracewell
 */
public class FilterTest {

  @Test
  public void stopWordTest() {
    Config.initializeTest();
    Document document = DocumentFactory.getInstance().create("This is a sample document. 100 is a number and ( is a punctuation.");
    Pipeline.process(document, Types.TOKEN);
    List<Annotation> results = document.get(Types.TOKEN).stream()
      .filter(StopWords.isNotStopWord())
      .collect(Collectors.toList());
    assertEquals(4, results.size());
  }

  @Test
  public void vowelTest() {
    Config.initializeTest();
    Document document = DocumentFactory.getInstance().create("This is a sample document. 100 is a number and ( is a punctuation.");
    Pipeline.process(document, Types.TOKEN);
    List<Annotation> results = document.get(Types.TOKEN).stream()
      .filter(EnglishVowelFilter.INSTANCE)
      .collect(Collectors.toList());
    assertEquals(12, results.size());
  }

  @Test
  public void languageTest() {
    Config.initializeTest();
    Document document = DocumentFactory.getInstance().create("This is a sample document. 100 is a number and ( is a punctuation.", Language.ENGLISH);
    assertTrue(new LanguageFilter(Language.ENGLISH).test(document));
    assertFalse(new LanguageFilter(Language.SPANISH).test(document));
  }

  @Test
  public void predicates() {
    Config.initializeTest();
    Document document = DocumentFactory.getInstance().create("This is a sample document. 100 is a number and ( is a punctuation.");
    Pipeline.process(document, Types.TOKEN);
    document.tokens().get(0).put(Attrs.PART_OF_SPEECH, POS.PRONOUN);
    document.tokens().get(0).put(Attrs.CONFIDENCE, 0.9);

    List<Annotation> results = document.get(Types.TOKEN).stream()
      .filter(HStringPredicates.contentMatch("This"))
      .collect(Collectors.toList());
    assertEquals(1, results.size());

    results = document.get(Types.TOKEN).stream()
      .filter(HStringPredicates.contentMatch("is", true))
      .collect(Collectors.toList());
    assertEquals(3, results.size());

    results = document.get(Types.TOKEN).stream()
      .filter(HStringPredicates.contentRegexMatch("[^\\w]", true))
      .collect(Collectors.toList());
    assertEquals(3, results.size());

    results = document.get(Types.TOKEN).stream()
      .filter(HStringPredicates.contentRegexMatch(Pattern.compile("[^\\w]")))
      .collect(Collectors.toList());
    assertEquals(3, results.size());

    results = document.get(Types.TOKEN).stream()
      .filter(HStringPredicates.contentRegexMatch("^this$", false))
      .collect(Collectors.toList());
    assertEquals(1, results.size());

    results = document.get(Types.TOKEN).stream()
      .filter(HStringPredicates.hasTagInstance("PRONOUN"))
      .collect(Collectors.toList());
    assertEquals(1, results.size());

    results = document.get(Types.TOKEN).stream()
      .filter(HStringPredicates.hasTagInstance(POS.PRONOUN))
      .collect(Collectors.toList());
    assertEquals(1, results.size());

    results = document.get(Types.TOKEN).stream()
      .filter(HStringPredicates.hasAttribute(Attrs.PART_OF_SPEECH))
      .collect(Collectors.toList());
    assertEquals(1, results.size());

    results = document.get(Types.TOKEN).stream()
      .filter(HStringPredicates.attributeMatch(Attrs.PART_OF_SPEECH, "PRONOUN"))
      .collect(Collectors.toList());
    assertEquals(1, results.size());

    results = document.get(Types.TOKEN).stream()
      .filter(HStringPredicates.attributeMatch(Attrs.CONFIDENCE, 0.9))
      .collect(Collectors.toList());
    assertEquals(1, results.size());

  }

}//END OF FilterTest
