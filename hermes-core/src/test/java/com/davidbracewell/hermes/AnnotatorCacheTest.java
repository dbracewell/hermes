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

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.annotator.Annotator;
import com.davidbracewell.hermes.annotator.DefaultTokenAnnotator;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class AnnotatorCacheTest {

  @Before
  public void setUp() throws Exception {
    Config.initializeTest();
  }

  @Test
  public void testSetAnnotator() throws Exception {
    AnnotatorCache.getInstance().setAnnotator(Types.TOKEN, Language.CHINESE, new DefaultTokenAnnotator());
    assertEquals(DefaultTokenAnnotator.class, AnnotatorCache.getInstance().get(Types.TOKEN, Language.CHINESE).getClass());
  }

  @Test
  public void testClear() throws Exception {
    AnnotatorCache.getInstance().setAnnotator(Types.TOKEN, Language.CHINESE, new DummyTokenAnnotator());
    assertEquals(DummyTokenAnnotator.class, AnnotatorCache.getInstance().get(Types.TOKEN, Language.CHINESE).getClass());
    AnnotatorCache.getInstance().clear();
    assertNotEquals(DummyTokenAnnotator.class, AnnotatorCache.getInstance().get(Types.TOKEN, Language.CHINESE).getClass());
  }

  @Test
  public void testRemove() throws Exception {
    AnnotatorCache.getInstance().setAnnotator(Types.TOKEN, Language.CHINESE, new DummyTokenAnnotator());
    assertEquals(DummyTokenAnnotator.class, AnnotatorCache.getInstance().get(Types.TOKEN, Language.CHINESE).getClass());
    AnnotatorCache.getInstance().remove(Types.TOKEN, Language.CHINESE);
    assertNotEquals(DummyTokenAnnotator.class, AnnotatorCache.getInstance().get(Types.TOKEN, Language.CHINESE).getClass());
  }

  private static class DummyTokenAnnotator implements Annotator {

    @Override
    public void annotate(Document document) {

    }

    @Override
    public Set<Annotatable> satisfies() {
      return Collections.singleton(Types.TOKEN);
    }
  }

}