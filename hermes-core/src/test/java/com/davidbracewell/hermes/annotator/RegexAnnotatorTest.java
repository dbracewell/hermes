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
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class RegexAnnotatorTest {

  @Test
  public void testAnnotate() throws Exception {
    Config.initializeTest();
    Document document = DocumentProvider.getAnnotatedDocument();
    RegexAnnotator annotator = new RegexAnnotator("(?i)alice", Types.ENTITY);
    annotator.annotate(document);
    assertEquals(4.0, document.get(Types.ENTITY).size(), 0d);
    annotator = new RegexAnnotator("(?i)alice", "PERSON");
    annotator.annotate(document);
    assertEquals(4.0, document.get(AnnotationType.create("PERSON")).size(), 0d);

  }
}