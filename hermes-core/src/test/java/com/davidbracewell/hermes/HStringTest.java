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

package com.davidbracewell.hermes;

import com.davidbracewell.Language;
import com.davidbracewell.collection.Counter;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.filter.StopWords;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class HStringTest {

  @Before
  public void setUp() throws Exception {
    Config.initializeTest();
  }

  @Test
  public void testCharNGrams() {
    HString hString = Fragments.string("abcdef");
    List<HString> unigrams = hString.charNGrams(1);
    assertEquals(6, unigrams.size());
    List<HString> bigrams = hString.charNGrams(2);
    assertEquals(5, bigrams.size());
    List<HString> trigrams = hString.charNGrams(3);
    assertEquals(4, trigrams.size());

    unigrams = hString.charNGrams(1, c -> c != 'a');
    assertEquals(5, unigrams.size());
    bigrams = hString.charNGrams(2, c -> c != 'a');
    assertEquals(4, bigrams.size());
    trigrams = hString.charNGrams(3, c -> c != 'a');
    assertEquals(3, trigrams.size());
  }

  @Test
  public void testStringFunctions() {
    HString hString = Fragments.string("abcdef");
    assertTrue(hString.contentEqual("abcdef"));
    assertTrue(hString.contentEqualIgnoreCase("ABCDEF"));

    assertEquals("abcdef", hString.toLowerCase());
    assertEquals("ABCDEF", hString.toUpperCase());
    assertEquals("gbcdef", hString.replace("a", "g"));
    assertEquals("gbcdgf", hString.replaceAll("[aieou]", "g"));
    assertEquals("gbcdef", hString.replaceFirst("[aieou]", "g"));

    Matcher m = hString.matcher("[aieou]");
    assertTrue(m.find());
    assertEquals("a", m.group());

    List<HString> patterns = hString.findAllPatterns(Pattern.compile("[aieou]"));
    assertEquals(2, patterns.size(), 0d);
    assertTrue(patterns.get(0).contentEqual("a"));
    assertTrue(patterns.get(1).contentEqual("e"));

    patterns = hString.findAll("a");
    assertEquals(1, patterns.size(), 0d);
    assertTrue(patterns.get(0).contentEqual("a"));

    assertTrue(hString.find("z").isEmpty());
    assertTrue(hString.find("a").start() == 0);

    assertEquals(0, hString.indexOf("a"));
    assertEquals(-1, hString.indexOf("x"));
    assertEquals(-1, hString.indexOf("a", 1));

    assertFalse(hString.isAnnotation());
    assertFalse(hString.asAnnotation().isPresent());

    assertFalse(hString.isDocument());
  }

  @Test
  public void testCounts() {
    Document document = DocumentFactory.getInstance().create("Once upon a time there lived a princess who was stuck in time.");
    Pipeline.process(document, Types.TOKEN);
    Counter<String> counts = document.countLemmas(Types.TOKEN);
    assertEquals(2, counts.get("time"), 0d);
    assertEquals(2d, counts.get("a"), 0d);

    counts = document.count(Types.TOKEN);
    assertEquals(2, counts.get("time"), 0d);
    assertEquals(2d, counts.get("a"), 0d);

    counts = document.count(Types.TOKEN, HString::toLowerCase);
    assertEquals(2, counts.get("time"), 0d);
    assertEquals(2d, counts.get("a"), 0d);

    counts = document.count(Types.TOKEN, StopWords.getInstance(Language.ENGLISH), HString::toLowerCase);
    assertEquals(0d, counts.get("a"), 0d);

    List<HString> patterns = document.findAllPatterns(Pattern.compile("\\ba\\s+\\w+\\b"));
    assertEquals(2, patterns.size(), 0d);
    assertTrue(patterns.get(0).contentEqual("a time"));
    assertTrue(patterns.get(1).contentEqual("a princess"));

    patterns = document.findAll("a time");
    assertEquals(1, patterns.size(), 0d);
    assertTrue(patterns.get(0).contentEqual("a time"));

    assertTrue(document.find("z").isEmpty());
    assertTrue(document.find("c").start() == 0);

    assertTrue(document.tokenAt(0).isAnnotation());
    assertTrue(document.tokenAt(0).matches("(?i)once"));
    assertTrue(document.tokenAt(0).asAnnotation().isPresent());

    assertTrue(document.isDocument());
  }

  @Test
  public void testTokenPatterns() {
    Document document = DocumentFactory.getInstance().create("Once upon a time there lived a princess who was stuck in time.");
    Pipeline.process(document, Types.TOKEN, Types.SENTENCE);
    List<HString> patterns = document.findAllPatterns(Pattern.compile("\\ba\\s+\\w+\\b"));
    assertEquals(2, patterns.size(), 0d);
    assertTrue(patterns.get(0).contentEqual("a time"));
    assertTrue(patterns.get(1).contentEqual("a princess"));

    patterns = document.findAll("a time");
    assertEquals(1, patterns.size(), 0d);
    assertTrue(patterns.get(0).contentEqual("a time"));

    assertTrue(document.find("z").isEmpty());
    assertTrue(document.find("c").start() == 0);

    assertTrue(document.tokenAt(0).startsWith("O"));
    assertTrue(document.tokenAt(0).endsWith("ce"));

    assertTrue(document.first(Types.SENTENCE).encloses(document.tokenAt(0)));
    assertTrue(document.first(Types.SENTENCE).overlaps(document.tokenAt(0)));

    assertTrue(document.tokenAt(0).isAnnotation());
    assertTrue(document.tokenAt(0).matches("(?i)once"));
    assertTrue(document.tokenAt(0).asAnnotation().isPresent());

    assertTrue(document.isDocument());
  }

  @Test
  public void testTokenNgrams() {
    Document document = DocumentFactory.getInstance().create("Once upon a time there lived a princess who was stuck in time.");
    Pipeline.process(document, Types.TOKEN);

    List<HString> ngrams = document.ngrams(1, Types.TOKEN);
    assertEquals(14, ngrams.size());

    ngrams = document.ngrams(1, Types.TOKEN, true);
    assertEquals(5, ngrams.size());

    ngrams = document.ngrams(1, Types.TOKEN, hString -> hString.contentEqual("a"));
    assertEquals(2, ngrams.size());

    ngrams = document.tokenNGrams(1);
    assertEquals(14, ngrams.size());

    ngrams = document.tokenNGrams(1, true);
    assertEquals(5, ngrams.size());

    ngrams = document.tokenNGrams(1, hString -> hString.contentEqual("a"));
    assertEquals(2, ngrams.size());

    ngrams = document.tokenNGrams(2);
    assertEquals(13, ngrams.size());
  }


}