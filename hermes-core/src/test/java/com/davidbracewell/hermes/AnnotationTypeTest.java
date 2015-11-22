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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class AnnotationTypeTest {

  @Test
  public void testIsInstance() throws Exception {
    AnnotationType TEST_PARENT = AnnotationType.create("TEST_PARENT");
    AnnotationType TEST_CHILD1 = AnnotationType.create("TEST_CHILD1", TEST_PARENT);
    AnnotationType TEST_CHILD2 = AnnotationType.create("TEST_CHILD2", TEST_PARENT);

    assertTrue(TEST_PARENT.isInstance(AnnotationType.ROOT));

    assertTrue(TEST_CHILD1.isInstance(TEST_PARENT));

    assertFalse(TEST_CHILD1.isInstance(TEST_CHILD2));
    assertFalse(TEST_PARENT.isInstance(TEST_CHILD2));

  }
}