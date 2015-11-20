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
import com.davidbracewell.hermes.tag.Entities;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.structured.StructuredFormat;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class AnnotationTest {


  @Test
  public void createTest() {
    Config.initializeTest();
    Annotation a = new Annotation(Fragments.string("test"), Types.ENTITY);
    a.put(Attrs.ENTITY_TYPE, Entities.EMAIL);

    assertNotNull(a);
    assertNull(a.document());
    assertEquals(Types.ENTITY, a.getType());
    assertTrue(a.isInstance(Types.ENTITY));

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
  public void testBadJson2() throws IOException{
    Document.read(StructuredFormat.JSON,
      Resources.fromString(
        "{\"id\":\"2a6de221-8d5f-4dbf-b21f-6c746be1a4a0\",\"content\":\"11\\t\\\"It has much more commercial potential.\\\"\",\"attributes\":{\"LANGUAGE\":\"ENGLISH\"},\"completed\":{\"ENTITY\":\"hermes.annotator.EntityAnnotator::1.0\",\"OPENNLP_ENTITY\":\"hermes.annotator.OpenNLPEntityAnnotator::1.6.0\",\"SENTENCE\":\"hermes.annotator.OpenNLPSentenceAnnotator::1.6.0\",\"TOKEN\":\"hermes.annotator.DefaultTokenAnnotator::1.0\",\"PHRASE_CHUNK\":\"hermes.annotator.OpenNLPPhraseChunkAnnotator::1.6.0\",\"PART_OF_SPEECH\":\"hermes.annotator.OpenNLPPOSAnnotator::1.6.0\",\"TOKEN_TYPE_ENTITY\":\"hermes.annotator.TokenTypeEntityAnnotator::1.0\"},\"annotations\":[{\"type\":\"TOKEN\",\"start\":0,\"end\":2,\"id\":0,\"attributes\":{\"PART_OF_SPEECH\":\"CD\",\"TOKEN_TYPE\":\"NUMBER\"}},{\"type\":\"TOKEN_TYPE_ENTITY\",\"start\":0,\"end\":2,\"id\":11,\"attributes\":{\"ENTITY_TYPE\":\"NUMBER\",\"CONFIDENCE\":1.0}},{\"type\":\"PHRASE_CHUNK\",\"start\":0,\"end\":2,\"id\":12,\"attributes\":{\"PART_OF_SPEECH\":\"NP\"}},{\"type\":\"SENTENCE\",\"start\":0,\"end\":43,\"id\":10,\"attributes\":{\"INDEX\":0}},{\"type\":\"TOKEN\",\"start\":3,\"end\":4,\"id\":1,\"attributes\":{\"PART_OF_SPEECH\":\"OPEN_QUOTE\",\"TOKEN_TYPE\":\"PUNCTUATION\"}},{\"type\":\"TOKEN\",\"start\":4,\"end\":6,\"id\":2,\"attributes\":{\"PART_OF_SPEECH\":\"PRP\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"PHRASE_CHUNK\",\"start\":4,\"end\":6,\"id\":13,\"attributes\":{\"PART_OF_SPEECH\":\"NP\"}},{\"type\":\"TOKEN\",\"start\":7,\"end\":10,\"id\":3,\"attributes\":{\"PART_OF_SPEECH\":\"VBZ\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"PHRASE_CHUNK\",\"start\":7,\"end\":10,\"id\":14,\"attributes\":{\"PART_OF_SPEECH\":\"VP\"}},{\"type\":\"TOKEN\",\"start\":11,\"end\":15,\"id\":4,\"attributes\":{\"PART_OF_SPEECH\":\"RB\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"PHRASE_CHUNK\",\"start\":11,\"end\":41,\"id\":15,\"attributes\":{\"PART_OF_SPEECH\":\"NP\"}},{\"type\":\"TOKEN\",\"start\":16,\"end\":20,\"id\":5,\"attributes\":{\"PART_OF_SPEECH\":\"JJR\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"TOKEN\",\"start\":21,\"end\":31,\"id\":6,\"attributes\":{\"PART_OF_SPEECH\":\"JJ\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"TOKEN\",\"start\":32,\"end\":41,\"id\":7,\"attributes\":{\"PART_OF_SPEECH\":\"NN\",\"TOKEN_TYPE\":\"ALPHA_NUMERIC\"}},{\"type\":\"TOKEN\",\"start\":41,\"end\":42,\"id\":8,\"attributes\":{\"PART_OF_SPEECH\":\"PUNCTUATION\",\"TOKEN_TYPE\":\"PUNCTUATION\"}},{\"type\":\"TOKEN\",\"start\":42,\"end\":43,\"id\":9,\"error\":[\"error\"],\"attributes\":{\"PART_OF_SPEECH\":\"CLOSE_QUOTE\",\"TOKEN_TYPE\":\"PUNCTUATION\"}}]}"
      )
    );
  }

}