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
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Pipeline;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.attribute.Entities;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class EntityAnnotatorTest {


  @Test
  public void testAnnotate() throws Exception {
    Config.initializeTest();
    Document document = DocumentProvider.getAnnotatedEmoticonDocument();
    Pipeline.process(document, Types.ENTITY);
    List<Annotation> entities = document.get(Types.ENTITY);

    assertEquals(";-)", entities.get(0).toString());
    assertEquals(Entities.EMOTICON, entities.get(0).getTag().get());

    assertEquals("http://www.somevideo.com/video.html", entities.get(1).toString());
    assertEquals(Entities.URL, entities.get(1).getTag().get());

    assertEquals("$100", entities.get(2).toString());
    assertEquals(Entities.MONEY, entities.get(2).getTag().get());

  }
}