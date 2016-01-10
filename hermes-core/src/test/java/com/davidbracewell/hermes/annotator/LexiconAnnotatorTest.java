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
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.lexicon.LexiconManager;
import com.davidbracewell.io.Resources;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class LexiconAnnotatorTest {

  @Test
  public void testAnnotate() throws Exception {
    Config.initializeTest();
    Config.loadConfig(Resources.fromClasspath("com/davidbracewell/hermes/test.conf"));
    Document document = DocumentProvider.getAnnotatedDocument();
    LexiconManager.clear();
    LexiconAnnotator annotator = new LexiconAnnotator(
      Types.ENTITY,
      "testing.lexicon"
    );


    annotator.annotate(document);


    List<Annotation> entities = document.get(Types.ENTITY);
    assertFalse(entities.isEmpty());
    assertEquals("Alice", entities.get(0).toString());
    assertEquals("Alice", entities.get(1).toString());
    assertEquals("White Rabbit", entities.get(2).toString());
    assertEquals("think", entities.get(4).toString());
    assertEquals(0.7d, entities.get(4).get(Attrs.CONFIDENCE).asDoubleValue(), 0.0d);

    document = DocumentProvider.getAnnotatedDocument();
    annotator = new LexiconAnnotator(
      Types.ENTITY,
      "testing.lexicon2"
    );
    annotator.annotate(document);
    entities = document.get(Types.ENTITY);
    assertFalse(entities.isEmpty());
    assertEquals("Alice", entities.get(0).toString());
    assertEquals("Alice", entities.get(1).toString());
    assertEquals("White Rabbit", entities.get(2).toString());
    assertEquals("think", entities.get(4).toString());
  }
}