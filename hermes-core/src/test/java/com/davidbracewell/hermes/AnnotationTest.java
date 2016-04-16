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

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.annotator.DocumentProvider;
import com.davidbracewell.hermes.tag.Entities;
import com.davidbracewell.hermes.tokenization.TokenType;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.structured.StructuredFormat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.davidbracewell.hermes.Types.*;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class AnnotationTest {

  @Before
  public void setUp() throws Exception {
    Config.initializeTest();
  }

  @Test
  public void putAttributeTest() {
    Document document = DocumentProvider.getAnnotatedDocument();
    List<Annotation> tokens = document.tokens();

    TokenType type = tokens.get(0).get(TOKEN_TYPE).cast();
    tokens.get(0).putIfAbsent(TOKEN_TYPE, TokenType.create("REALLYSTRANGETYPE"));
    assertEquals(type, tokens.get(0).get(TOKEN_TYPE).get());

    tokens.get(0).putIfAbsent(TOKEN_TYPE, () -> TokenType.create("REALLYSTRANGETYPE"));
    assertEquals(type, tokens.get(0).get(TOKEN_TYPE).get());

    tokens.get(0).putIfAbsent(ENTITY_TYPE, () -> TokenType.create("REALLYSTRANGETYPE"));
    assertEquals(TokenType.create("REALLYSTRANGETYPE"), tokens.get(0).get(ENTITY_TYPE).get());

    tokens.get(0).putIfAbsent(CATEGORY, TokenType.create("REALLYSTRANGETYPE"));
    assertEquals(TokenType.create("REALLYSTRANGETYPE"), tokens.get(0).get(CATEGORY).get());

  }

  @Test
  public void sentenceTest() {
    Document document = DocumentProvider.getAnnotatedDocument();
    for (Annotation token : document.tokens()) {
      assertEquals(1, token.sentences().size(), 0d);
      assertEquals(token.sentences().get(0), token.last(SENTENCE));
      assertEquals(token.sentences().get(0), token.first(SENTENCE));
      assertEquals(token, token.firstToken());
      assertEquals(token, token.lastToken());
    }
  }

  @Test
  public void nextPreviousTest() {
    Document document = DocumentFactory.getInstance().fromTokens(Arrays.asList("This", "is", "simple"));
    List<Annotation> tokens = document.tokens();
    assertTrue(tokens.get(0).previous().isEmpty());
    assertTrue(tokens.get(0).previous().isDetached());
    assertEquals("is", tokens.get(0).next().toString());
    assertEquals("This", tokens.get(1).previous().toString());
    assertEquals("simple", tokens.get(1).next().toString());
    assertEquals("is", tokens.get(2).previous().toString());
    assertTrue(tokens.get(2).next().isEmpty());
  }

  @Test
  public void parentChildTest() {
    Document document = DocumentFactory.getInstance().fromTokens(Arrays.asList("This", "is", "simple"));
    List<Annotation> tokens = document.tokens();
    tokens.get(0).add(new Relation(DEPENDENCY, "nsubj", tokens.get(2).getId()));
    tokens.get(1).add(new Relation(DEPENDENCY, "cop", tokens.get(2).getId()));
    assertTrue(tokens.get(0).parent().isPresent());
    assertEquals("simple", tokens.get(0).parent().get().toString());
    assertEquals(1, tokens.get(0).get(DEPENDENCY).size(), 0d);
    assertEquals("simple", tokens.get(0).targets(DEPENDENCY, "nsubj").get(0).toString());
    assertEquals("simple", tokens.get(0).targets(DEPENDENCY).get(0).toString());
    assertTrue(tokens.get(1).parent().isPresent());
    assertEquals("simple", tokens.get(1).parent().get().toString());
    assertEquals(2, tokens.get(2).children().size());

    Pipeline.process(document, SENTENCE);
    assertEquals(2, tokens.get(2).children().size());
    tokens.get(0).remove(tokens.get(0).get(DEPENDENCY).get(0));
    assertEquals(1, tokens.get(2).children().size());

    Annotation sentence = document.first(SENTENCE);
    assertFalse(sentence.isEmpty());
    assertEquals(sentence, tokens.get(0).first(SENTENCE));
    assertEquals(sentence, tokens.get(1).first(SENTENCE));
    assertEquals(sentence, tokens.get(2).first(SENTENCE));
  }

  @Test
  public void createTest() {
    Annotation a = new Annotation(Fragments.string("test"), ENTITY);
    a.put(ENTITY_TYPE, Entities.EMAIL);

    assertNotNull(a);
    assertNull(a.document());
    assertEquals(ENTITY, a.getType());
    assertTrue(a.isInstance(ENTITY));

    assertTrue(a.isAnnotation());
    assertNotNull(a.asAnnotation().orElse(null));

    assertTrue(a.isInstanceOfTag(Entities.INTERNET));
    assertTrue(a.isInstanceOfTag("INTERNET"));


  }

  @Test(expected = RuntimeException.class)
  public void testBadJson() {
    Document.fromJson("{\"id\":\"2a6de221-8d5f-4dbf-b21f-6c746be1a4a0\",\"content\":\"11\\t\\\"It has much more commercial potential.\\\"\",\"attributes\":{\"LANGUAGE\":\"ENGLISH\"},\"completed\":{\"ENTITY\":\"hermes.annotator.EntityAnnotator::1.0\",\"OPENNLP_ENTITY\":\"hermes.annotator.OpenNLPEntityAnnotator::1.6.0\",\"SENTENCE\":\"hermes.annotator.OpenNLPSentenceAnnotator::1.6.0\",\"TOKEN\":\"hermes.annotator.DefaultTokenAnnotator::1.0\",\"PHRASE_CHUNK\":\"hermes.annotator.OpenNLPPhraseChunkAnnotator::1.6.0\",\"PART_OF_SPEECH\":\"hermes.annotator.OpenNLPPOSAnnotator::1.6.0\",\"TOKEN_TYPE_ENTITY\":\"hermes.annotator.TokenTypeEntityAnnotator::1.0\"},\"annotations\":[{\"type\":\"TOKEN\",\"start\":0,\"end\":2,\"id\":0,\"attributes\":{\"PART_OF_SPEECH\":\"CD\",\"TOKEN_TYPE\":\"NUMBER\"}},{\"type\":\"TOKEN_TYPE_ENTITY\",\"start\":0,\"end\":2,\"id\":11,\"attributes\":{\"ENTITY_TYPE\":\"NUMBER\",\"CONFIDENCE\":1.0}},{\"type\":\"PHRASE_CHUNK\",\"start\":0,\"end\":2,\"id\":12,\"attributes\":{\"PART_OF_SPEECH\":\"NP\"}},{\"type\":\"SENTENCE\",\"start\":0,\"end\":43,\"id\":10,\"attributes\":{\"INDEX\":0}},{\"type\":\"TOKEN\",\"start\":3,\"end\":4,\"id\":1,\"attributes\":{\"PART_OF_SPEECH\":\"OPEN_QUOTE\",\"TOKEN_TYPE\":\"PUNCTUATION\"}},{\"type\":\"TOKEN\",\"start\":4,\"end\":6,\"id\":2,\"attributes\":{\"PART_OF_SPEECH\":\"PRP\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"PHRASE_CHUNK\",\"start\":4,\"end\":6,\"id\":13,\"attributes\":{\"PART_OF_SPEECH\":\"NP\"}},{\"type\":\"TOKEN\",\"start\":7,\"end\":10,\"id\":3,\"attributes\":{\"PART_OF_SPEECH\":\"VBZ\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"PHRASE_CHUNK\",\"start\":7,\"end\":10,\"id\":14,\"attributes\":{\"PART_OF_SPEECH\":\"VP\"}},{\"type\":\"TOKEN\",\"start\":11,\"end\":15,\"id\":4,\"attributes\":{\"PART_OF_SPEECH\":\"RB\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"PHRASE_CHUNK\",\"start\":11,\"end\":41,\"id\":15,\"attributes\":{\"PART_OF_SPEECH\":\"NP\"}},{\"type\":\"TOKEN\",\"start\":16,\"end\":20,\"id\":5,\"attributes\":{\"PART_OF_SPEECH\":\"JJR\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"TOKEN\",\"start\":21,\"end\":31,\"id\":6,\"attributes\":{\"PART_OF_SPEECH\":\"JJ\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"TOKEN\",\"start\":32,\"end\":41,\"id\":7,\"attributes\":{\"PART_OF_SPEECH\":\"NN\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"TOKEN\",\"start\":41,\"end\":42,\"id\":8,\"attributes\":{\"PART_OF_SPEECH\":\"PUNCTUATION\",\"TOKEN_TYPE\":\"PUNCTUATION\"}},{\"type\":\"TOKEN\",\"start\":42,\"end\":43,\"id\":9,\"error\":[\"error\"],\"attributes\":{\"PART_OF_SPEECH\":\"CLOSE_QUOTE\",\"TOKEN_TYPE\":\"PUNCTUATION\"}}]}");
  }

  @Test(expected = IOException.class)
  public void testBadJson2() throws IOException {
    Document.read(StructuredFormat.JSON,
      Resources.fromString(
        "{\"id\":\"2a6de221-8d5f-4dbf-b21f-6c746be1a4a0\",\"content\":\"11\\t\\\"It has much more commercial potential.\\\"\",\"attributes\":{\"LANGUAGE\":\"ENGLISH\"},\"completed\":{\"ENTITY\":\"hermes.annotator.EntityAnnotator::1.0\",\"OPENNLP_ENTITY\":\"hermes.annotator.OpenNLPEntityAnnotator::1.6.0\",\"SENTENCE\":\"hermes.annotator.OpenNLPSentenceAnnotator::1.6.0\",\"TOKEN\":\"hermes.annotator.DefaultTokenAnnotator::1.0\",\"PHRASE_CHUNK\":\"hermes.annotator.OpenNLPPhraseChunkAnnotator::1.6.0\",\"PART_OF_SPEECH\":\"hermes.annotator.OpenNLPPOSAnnotator::1.6.0\",\"TOKEN_TYPE_ENTITY\":\"hermes.annotator.TokenTypeEntityAnnotator::1.0\"},\"annotations\":[{\"type\":\"TOKEN\",\"start\":0,\"end\":2,\"id\":0,\"attributes\":{\"PART_OF_SPEECH\":\"CD\",\"TOKEN_TYPE\":\"NUMBER\"}},{\"type\":\"TOKEN_TYPE_ENTITY\",\"start\":0,\"end\":2,\"id\":11,\"attributes\":{\"ENTITY_TYPE\":\"NUMBER\",\"CONFIDENCE\":1.0}},{\"type\":\"PHRASE_CHUNK\",\"start\":0,\"end\":2,\"id\":12,\"attributes\":{\"PART_OF_SPEECH\":\"NP\"}},{\"type\":\"SENTENCE\",\"start\":0,\"end\":43,\"id\":10,\"attributes\":{\"INDEX\":0}},{\"type\":\"TOKEN\",\"start\":3,\"end\":4,\"id\":1,\"attributes\":{\"PART_OF_SPEECH\":\"OPEN_QUOTE\",\"TOKEN_TYPE\":\"PUNCTUATION\"}},{\"type\":\"TOKEN\",\"start\":4,\"end\":6,\"id\":2,\"attributes\":{\"PART_OF_SPEECH\":\"PRP\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"PHRASE_CHUNK\",\"start\":4,\"end\":6,\"id\":13,\"attributes\":{\"PART_OF_SPEECH\":\"NP\"}},{\"type\":\"TOKEN\",\"start\":7,\"end\":10,\"id\":3,\"attributes\":{\"PART_OF_SPEECH\":\"VBZ\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"PHRASE_CHUNK\",\"start\":7,\"end\":10,\"id\":14,\"attributes\":{\"PART_OF_SPEECH\":\"VP\"}},{\"type\":\"TOKEN\",\"start\":11,\"end\":15,\"id\":4,\"attributes\":{\"PART_OF_SPEECH\":\"RB\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"PHRASE_CHUNK\",\"start\":11,\"end\":41,\"id\":15,\"attributes\":{\"PART_OF_SPEECH\":\"NP\"}},{\"type\":\"TOKEN\",\"start\":16,\"end\":20,\"id\":5,\"attributes\":{\"PART_OF_SPEECH\":\"JJR\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"TOKEN\",\"start\":21,\"end\":31,\"id\":6,\"attributes\":{\"PART_OF_SPEECH\":\"JJ\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"TOKEN\",\"start\":32,\"end\":41,\"id\":7,\"attributes\":{\"PART_OF_SPEECH\":\"NN\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"TOKEN\",\"start\":41,\"end\":42,\"id\":8,\"attributes\":{\"PART_OF_SPEECH\":\"PUNCTUATION\",\"TOKEN_TYPE\":\"PUNCTUATION\"}},{\"type\":\"TOKEN\",\"start\":42,\"end\":43,\"id\":9,\"error\":[\"error\"],\"attributes\":{\"PART_OF_SPEECH\":\"CLOSE_QUOTE\",\"TOKEN_TYPE\":\"PUNCTUATION\"}}]}"
      )
    );
  }

}
