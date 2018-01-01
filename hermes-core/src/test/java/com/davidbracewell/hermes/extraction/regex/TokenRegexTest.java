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

package com.davidbracewell.hermes.extraction.regex;

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.lexicon.LexiconManager;
import com.davidbracewell.hermes.lexicon.TrieLexicon;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class TokenRegexTest {

   private Document document;

   @Before
   public void setUp() throws Exception {
      Config.initializeTest();
      Config.setProperty("com.davidbracewell.hermes.annotator.DefaultEntityAnnotator.subTypes", "TOKEN_TYPE_ENTITY");
      document = DocumentFactory.getInstance().create("John met Sally by the seashore at 12:30pm yesterday.");
      Pipeline.process(document, Types.TOKEN, Types.SENTENCE, Types.ENTITY);


      //Add basic POS
      document.tokenAt(0).put(Types.PART_OF_SPEECH, POS.NOUN);
      document.tokenAt(1).put(Types.PART_OF_SPEECH, POS.VERB);
      document.tokenAt(2).put(Types.PART_OF_SPEECH, POS.NOUN);
      document.tokenAt(3).put(Types.PART_OF_SPEECH, POS.ADPOSITION);
      document.tokenAt(4).put(Types.PART_OF_SPEECH, POS.DETERMINER);
      document.tokenAt(5).put(Types.PART_OF_SPEECH, POS.NOUN);
      document.tokenAt(6).put(Types.PART_OF_SPEECH, POS.ADPOSITION);
      document.tokenAt(7).put(Types.PART_OF_SPEECH, POS.NUMBER);
      document.tokenAt(8).put(Types.PART_OF_SPEECH, POS.NOUN);
      document.tokenAt(9).put(Types.PART_OF_SPEECH, POS.PUNCTUATION);

      //Add some dependencies
      document.tokenAt(0).add(new Relation(Types.DEPENDENCY, "nsubj", document.tokenAt(1).getId()));
      document.tokenAt(2).add(new Relation(Types.DEPENDENCY, "dobj", document.tokenAt(1).getId()));

      //Create some more entities
      document.annotationBuilder()
              .type(Types.ENTITY)
              .bounds(document.tokenAt(0))
              .attribute(Types.ENTITY_TYPE, Entities.PERSON)
              .createAttached();

      document.annotationBuilder()
              .type(Types.ENTITY)
              .bounds(document.tokenAt(2))
              .attribute(Types.ENTITY_TYPE, Entities.PERSON)
              .createAttached();

      document.annotationBuilder()
              .type(Types.ENTITY)
              .bounds(document.tokenAt(5))
              .attribute(Types.ENTITY_TYPE, Entities.LOCATION)
              .createAttached();

      TrieLexicon lexicon = new TrieLexicon(false, false, AttributeType.create("DUMMY_TAG"));
      lexicon.add("seashore", new StringTag("BY_THE_SEA"));

      LexiconManager.register("testing.lexicon", lexicon);

   }

   @Test
   public void testAlternation() throws Exception {
      TokenMatcher matcher = TokenRegex.compile("John | Sally").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("^ (John | Sally) ").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("by", matcher.group().toString());
   }

   @Test
   public void testParent() throws Exception {
      TokenMatcher matcher = TokenRegex.compile("(/> $VERB)").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("^(/> $VERB)").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("by", matcher.group().toString());

   }

   @Test
   public void testNot() throws Exception {
      TokenMatcher matcher = TokenRegex.compile("^ /^[A-za-z]/").matcher(document);
      assertTrue(matcher.find());
      assertEquals("12:30pm", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals(".", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("^^ /^[A-za-z]/").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());

      matcher = TokenRegex.compile("^ (John met) ").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met Sally", matcher.group().toString());

      matcher = TokenRegex.compile("^ (by the) ").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John met", matcher.group().toString());

      matcher = TokenRegex.compile("^ {ENTITY}+ ").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());

      matcher = TokenRegex.compile("^ {ENTITY}* ").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());

      matcher = TokenRegex.compile("^ {ENTITY}? ").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());

      matcher = TokenRegex.compile("^ {ENTITY}{1,4} ").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());
   }

   @Test
   public void testRelations() throws Exception {
      TokenMatcher matcher = TokenRegex.compile("@DEPENDENCY:nsubj").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("[@DEPENDENCY:dobj]").matcher(document);
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("@DEPENDENCY").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("^@DEPENDENCY").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("by", matcher.group().toString());

      matcher = TokenRegex.compile("[@DEPENDENCY]").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("{@DEPENDENCY $VERB}").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("^{@DEPENDENCY $VERB}").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("by", matcher.group().toString());
   }

   @Test
   public void testLexiconMatch() throws Exception {
      TokenMatcher matcher = TokenRegex.compile("%testing.lexicon").matcher(document);
      assertTrue(matcher.find());
      assertEquals("seashore", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("[%testing.lexicon]").matcher(document);
      assertTrue(matcher.find());
      assertEquals("seashore", matcher.group().toString());
      assertFalse(matcher.find());
   }

   @Test
   public void testLogic() throws Exception {
      TokenMatcher matcher = TokenRegex.compile("[ ($NOUN & /^[JS]/) | (/\\d+/ (?> yesterday)) | {ENTITY $PERSON} ]")
                                       .matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("12:30pm", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("[ ^{ENTITY} ]").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("by", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("the", matcher.group().toString());

      matcher = TokenRegex.compile("^[ {ENTITY} ]").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("by", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("the", matcher.group().toString());
   }

   @Test
   public void testAnnotationMatch() throws Exception {
      TokenMatcher matcher = TokenRegex.compile("{ENTITY ~}").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("seashore", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("12:30pm", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("{ENTITY $PERSON}").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("{ENTITY}{1,2}").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("seashore", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("12:30pm", matcher.group().toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("^{ENTITY}").matcher(document);
      assertTrue(matcher.find());
      assertEquals("met", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("by", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("the", matcher.group().toString());
   }

   @Test
   public void testAttributeMatch() throws Exception {
      TokenMatcher matcher = TokenRegex.compile("$TOKEN_TYPE:TIME").matcher(document);
      assertTrue(matcher.find());
      assertEquals("12:30pm", matcher.group().toString());
      assertFalse(matcher.find());
   }

   @Test
   public void testTagMatch() throws Exception {
      TokenMatcher matcher = TokenRegex.compile("$NOUN+ $VERB+ $NOUN+").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John met Sally", matcher.group().toString());
      assertFalse(matcher.find());
   }

   @Test
   public void testGroups() throws Exception {
      TokenMatcher matcher = TokenRegex.compile("(?<PERSON> $NOUN+) (?<ACTION> $VERB+) (?<PERSON> $NOUN+)").matcher(
         document);
      assertTrue(matcher.find());
      assertEquals(1.0, matcher.group("ACTION").size(), 0d);
      assertEquals("met", matcher.group("ACTION").get(0).toString());
      assertEquals(2.0, matcher.group("PERSON").size(), 0d);
      assertEquals("John", matcher.group("PERSON").get(0).toString());
      assertEquals("Sally", matcher.group("PERSON").get(1).toString());
      assertFalse(matcher.find());

      matcher = TokenRegex.compile("^(?<PERSON> $NOUN+) (?<ACTION> $VERB+) (?<PERSON> $NOUN+)").matcher(document);
      assertFalse(matcher.find());
   }

   @Test
   public void testNamed() throws Exception {
      TokenMatcher matcher = TokenRegex.compile("(${NUMBER} | ${STOPWORD} | ${PUNCT})").matcher(document);
      assertTrue(matcher.find());
      assertEquals("by", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("the", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("at", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("12:30pm", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals(".", matcher.group().toString());
      assertFalse(matcher.find());
   }

   @Test
   public void testLookAhead() throws Exception {
      //Lookahead
      TokenMatcher matcher = TokenRegex.compile("( /^john$/i | /^sally$/i ) (?> ($VERB | $ADPOSITION))").matcher(
         document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertFalse(matcher.find());

      //Negative lookahead
      matcher = TokenRegex.compile("( /^john$/i | /^sally$/i ) (?!> $NUMBER)").matcher(document);
      assertTrue(matcher.find());
      assertEquals("John", matcher.group().toString());
      assertTrue(matcher.find());
      assertEquals("Sally", matcher.group().toString());
      assertFalse(matcher.find());
   }
}