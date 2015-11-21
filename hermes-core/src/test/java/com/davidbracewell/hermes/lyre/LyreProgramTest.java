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

package com.davidbracewell.hermes.lyre;

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.annotator.DocumentProvider;
import com.davidbracewell.hermes.tag.RelationType;
import com.davidbracewell.io.Resources;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class LyreProgramTest {

  @Test
  public void testExecute() throws Exception {
    Config.initializeTest();
    LyreProgram p = LyreProgram.read(Resources.fromClasspath("com/davidbracewell/hermes/lyre/example.yaml"));
    Document doc = DocumentProvider.getDocument();
    Pipeline.process(doc, Types.TOKEN, Types.SENTENCE);
    p.execute(doc);
    List<Annotation> entities = doc.get(Types.ENTITY);
    assertEquals(5, entities.size());

    assertEquals("Alice", entities.get(0).toString());
    assertEquals("Alice", entities.get(1).toString());
    assertEquals("White Rabbit", entities.get(2).toString());
    assertEquals("Alice", entities.get(3).toString());
    assertEquals("Alice", entities.get(4).toString());


    assertEquals(1, entities.get(2).getRelations().size());
    assertEquals(RelationType.create("ATTRIBUTE"), entities.get(2).getRelations().stream().findFirst().get().getType());
    assertEquals("EYES", entities.get(2).getRelations().stream().findFirst().get().getValue());

  }
}