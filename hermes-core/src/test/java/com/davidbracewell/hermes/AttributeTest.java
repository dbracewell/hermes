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

import com.davidbracewell.collection.Collect;
import com.davidbracewell.config.Config;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class AttributeTest {

  @Before
  public void setUp() throws Exception {
    Config.initializeTest();
  }

  @Test
  public void testCreate() throws Exception {
    assertEquals(Double.class, Attrs.CONFIDENCE.getValueType().getType());
    assertEquals(Attrs.CONFIDENCE, Attribute.create("CONFIDENCE", Double.class));
    assertEquals(Attrs.CONFIDENCE, Attribute.create("CONFIDENCE"));
    assertNotNull(Attribute.create("DUMMY", String.class));
    assertTrue(Attribute.isDefined("dummy"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadCreate() throws Exception {
    Attribute a = Attrs.CONFIDENCE;
    Attribute.create("CONFIDENCE", Integer.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadCreate2() throws Exception {
    Attribute.create("", Integer.class);
  }

  @Test
  public void checkValues() {
    Attribute dummy = Attribute.create("DUMMY", String.class);
    assertFalse(Attribute.values().isEmpty());
    assertTrue(Attribute.values().contains(dummy));
    assertEquals(dummy, Attribute.valueOf("dummy"));
  }

  @Test
  public void writeIgnoreTest() {
    Config.setProperty("Attribute.ignoreTypeChecks", "true");
    Config.setProperty("Attribute.ignoreTypeErrors", "true");
    Document document = DocumentFactory.getInstance().create("This is a test.");
    Pipeline.process(document, Types.TOKEN, Types.SENTENCE);
    //Set token type to wrong value type
    document.tokenAt(0).put(Attrs.TOKEN_TYPE, 34);
    String json = document.toJson();

    //Reading back in token type is ignored for the first token, because it is not a valid type
    document = Document.fromJson(json);
    assertNull(document.tokenAt(0).get(Attrs.TOKEN_TYPE).get());
  }

  @Test(expected = IllegalArgumentException.class)
  public void writeNoIgnoreTest() {
    Config.setProperty("Attribute.ignoreTypeChecks", "false");
    Config.setProperty("Attribute.ignoreTypeErrors", "false");
    Document document = DocumentFactory.getInstance().create("This is a test.");
    Pipeline.process(document, Types.TOKEN, Types.SENTENCE);
    document.tokenAt(0).put(Attrs.TOKEN_TYPE, 34);
    document.toJson();
  }

  @Test
  public void writeNoIgnoreCheckIgnoreErrorsTest() {
    Config.setProperty("Attribute.ignoreTypeChecks", "false");
    Config.setProperty("Attribute.ignoreTypeErrors", "true");
    Document document = DocumentFactory.getInstance().create("This is a test.");
    Pipeline.process(document, Types.TOKEN, Types.SENTENCE);
    //Set token type to wrong value type
    document.tokenAt(0).put(Attrs.TOKEN_TYPE, 34);
    String json = document.toJson();

    //Reading back in token type is ignored for the first token, because it is not a valid type
    document = Document.fromJson(json);
    assertNull(document.tokenAt(0).get(Attrs.TOKEN_TYPE).get());
  }

  @Test
  public void testCollectionAttributes() {
    Config.setProperty("Attribute.ignoreTypeChecks", "false");
    Config.setProperty("Attribute.ignoreTypeErrors", "true");
    Document document = DocumentFactory.getInstance().create("This is a test.");

    Config.setProperty("Attribute.LIST.type", "List");
    Config.setProperty("Attribute.LIST.elementType", "String");
    Attribute listAttribute = Attribute.create("LIST", List.class);

    Config.setProperty("Attribute.MAP.type", "map");
    Config.setProperty("Attribute.MAP.keyType", "String");
    Config.setProperty("Attribute.MAP.valueType", "String");
    Attribute mapAttribute = Attribute.create("MAP", Map.class);


    Pipeline.process(document, Types.TOKEN, Types.SENTENCE);
    //Set token type to wrong value type
    document.tokenAt(0).put(listAttribute, Arrays.asList("One", "Two", "Three"));
    document.tokenAt(1).put(mapAttribute, Collect.map("A", "B", "C", "D"));
    String json = document.toJson();

    //Reading back in token type is ignored for the first token, because it is not a valid type
    document = Document.fromJson(json);
    assertEquals(Arrays.asList("One", "Two", "Three"), document.tokenAt(0).getAttributeAsList(listAttribute));
    assertEquals(Collect.map("A", "B", "C", "D"), document.tokenAt(1).getAttributeAsMap(mapAttribute));

  }

}