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

package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.annotator.DocumentProvider;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.DocumentFormats;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class FormatsTests {
  @Before
  public void setUp() throws Exception {
    Config.initializeTest();
  }

  @Test
  public void testXMLReadWrite() throws Exception {
    Resource writeTo = Resources.temporaryDirectory();
    Corpus c = Corpus.builder().add(DocumentProvider.getAnnotatedDocument())
      .build()
      .write("XML", writeTo);
    assertEquals(1d, c.size(), 0d);
  }

  @Test
  public void testJSONReadWrite() throws Exception {
    Resource writeTo = Resources.fromString();
    Corpus c = Corpus.builder()
      .add(DocumentProvider.getAnnotatedDocument())
      .build()
      .write("json_opl", writeTo);
    assertEquals(1d, c.size(), 0d);
  }

  @Test
  public void testTXTReadWrite() throws Exception {
    Resource writeTo = Resources.fromString();
    Corpus c = Corpus.builder()
      .add(DocumentProvider.getAnnotatedDocument())
      .build()
      .write("text_opl", writeTo);

    assertTrue(writeTo.readToString().length() > 0);
  }

  @Test
  public void testCSVRead() throws Exception {
    Config.setProperty("CSVCorpus.hasHeader", "true");
    Cast.<CSVCorpus>as(DocumentFormats.forName("CSV")).clearFields();
    Corpus c = Corpus.builder()
      .format("CSV")
      .source(Resources.fromClasspath("com/davidbracewell/hermes/docs/dsv/test.csv"))
      .build();
    List<Document> documents = new ArrayList<>();
    c.forEach(documents::add);
    assertEquals("This is the first document.", documents.get(0).toString());
    assertEquals("This is the second document.", documents.get(1).toString());
  }

  @Test
  public void testTSVRead() throws Exception {
    Config.setProperty("TSVCorpus.hasHeader", "false");
    Config.setProperty("TSVCorpus.fields", "ID,CONTENT");
    Corpus c = Corpus.builder()
      .format("TSV")
      .source(Resources.fromClasspath("com/davidbracewell/hermes/docs/dsv/test.tsv"))
      .build();
    List<Document> documents = new ArrayList<>();
    c.forEach(documents::add);
    assertEquals("This is the first document.", documents.get(0).toString());
    assertEquals("This is the second document.", documents.get(1).toString());
  }

  @Test
  public void testCONLL() throws Exception {
    Config.setProperty("CONLL.fields", "WORD,POS,CHUNK");
    Corpus c = Corpus.builder()
      .format("CONLL")
      .source(Resources.fromClasspath("com/davidbracewell/hermes/docs/conll/test.conll"))
      .build();
    List<Document> documents = new ArrayList<>();
    c.forEach(documents::add);
    assertTrue(documents.get(0).getAnnotationSet().isCompleted(Types.TOKEN));
    assertTrue(documents.get(0).getAnnotationSet().isCompleted(Types.PART_OF_SPEECH));
    assertTrue(documents.get(0).getAnnotationSet().isCompleted(Types.PHRASE_CHUNK));
  }

}