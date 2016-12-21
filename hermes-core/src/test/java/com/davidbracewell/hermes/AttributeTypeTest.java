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

import com.davidbracewell.DynamicEnum;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.attribute.AttributeType;
import com.davidbracewell.hermes.attribute.AttributeValueType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.davidbracewell.collection.map.Maps.map;
import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class AttributeTypeTest {

   @Before
   public void setUp() throws Exception {
      Config.initializeTest();
   }

   @Test
   public void testCreate() throws Exception {
      assertEquals(AttributeValueType.DOUBLE, Types.CONFIDENCE.getValueType());
      assertEquals(Types.CONFIDENCE, AttributeType.create("CONFIDENCE", AttributeValueType.DOUBLE));
      assertEquals(Types.CONFIDENCE, AttributeType.create("CONFIDENCE"));
      assertNotNull(AttributeType.create("DUMMY", AttributeValueType.STRING));
      assertTrue(DynamicEnum.isDefined(AttributeType.class, "dummy"));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadCreate() throws Exception {
      AttributeType a = Types.CONFIDENCE;
      AttributeType.create("CONFIDENCE", AttributeValueType.INTEGER);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testBadCreate2() throws Exception {
      AttributeType.create("", AttributeValueType.INTEGER);
   }

   @Test
   public void checkValues() {
      AttributeType dummy = AttributeType.create("DUMMY", AttributeValueType.STRING);
      assertFalse(AttributeType.values().isEmpty());
      assertTrue(AttributeType.values().contains(dummy));
      assertEquals(dummy, AttributeType.valueOf("dummy"));
   }

   @Test
   public void testCollectionAttributes() {
      Document document = DocumentFactory.getInstance().create("This is a test.");
      AttributeType listAttributeType = AttributeType.create("LIST", AttributeValueType.STRING);
      AttributeType mapAttributeType = AttributeType.create("MAP");

      Pipeline.process(document, Types.TOKEN, Types.SENTENCE);
      //Set token type to wrong value type
      document.tokenAt(0).put(listAttributeType, Arrays.asList("One", "Two", "Three"));
      document.tokenAt(1).put(mapAttributeType, map("A", "B", "C", "D"));
      String json = document.toJson();

      //Reading back in token type is ignored for the first token, because it is not a valid type
      document = Document.fromJson(json);

      assertEquals(Arrays.asList("One", "Two", "Three"), document.tokenAt(0).getAttributeAsList(listAttributeType));
      assertEquals(map("A", "B", "C", "D"), document.tokenAt(1).getAttributeAsMap(mapAttributeType));

   }

}
