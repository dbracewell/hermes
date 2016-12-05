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

package com.davidbracewell.hermes.corpus;

import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.config.Config;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.guava.common.collect.Multimap;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.tuple.*;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class CorpusTest {

  @Test
  public void inMemory() {
    Config.initializeTest();
    Corpus corpus = Corpus.builder()
      .inMemory()
      .add(DocumentFactory.getInstance().create("This is the first document."))
      .add(DocumentFactory.getInstance().create("This is the second document."))
      .add(DocumentFactory.getInstance().create("This is the third document."))
      .build();
    assertEquals(3, corpus.size());
    assertEquals("This is the first document.", corpus.stream().first().get().toString());
    corpus.annotate(Types.TOKEN);
    Counter<String> cntr = corpus.terms();
    assertEquals(3, cntr.get("the"), 0d);
    assertEquals(3, cntr.get("document"), 0d);
    assertEquals(3, cntr.get("This"), 0d);
    assertEquals(3, cntr.get("is"), 0d);
    assertEquals(3, cntr.get("."), 0d);
    assertEquals(1, cntr.get("first"), 0d);
    assertEquals(1, cntr.get("second"), 0d);
    assertEquals(1, cntr.get("third"), 0d);
  }


  @Test
  public void searchTest() {
    Config.initializeTest();
    Corpus corpus = Corpus.builder()
      .inMemory()
      .add(DocumentFactory.getInstance().create("This is the first document."))
      .add(DocumentFactory.getInstance().create("This is the second document."))
      .add(DocumentFactory.getInstance().create("This is the third document."))
      .add(DocumentFactory.getInstance().create("This is the first long document."))
      .build();
    assertFalse(corpus.isEmpty());
    corpus.annotate(Types.TOKEN);
    try {
      assertEquals(2, corpus.query("first").size(), 0d);
    } catch (ParseException e) {
      throw Throwables.propagate(e);
    }
  }

  @Test
  public void sampleTest() {
    Config.initializeTest();
    Corpus corpus = Corpus.builder()
      .inMemory()
      .add(DocumentFactory.getInstance().create("This is the first document."))
      .add(DocumentFactory.getInstance().create("This is the second document."))
      .add(DocumentFactory.getInstance().create("This is the third document."))
      .add(DocumentFactory.getInstance().create("This is the first long document."))
      .build()
      .cache();
    assertFalse(corpus.isEmpty());
    Corpus sample = corpus.sample(2);
    assertEquals(2, sample.stream().distinct().count());
  }


  @Test
  public void groupByTest() {
    Config.initializeTest();
    Corpus corpus = Corpus.builder()
      .inMemory()
      .add(DocumentFactory.getInstance().create("This is the first document."))
      .add(DocumentFactory.getInstance().create("This is the second document."))
      .add(DocumentFactory.getInstance().create("This is the third document."))
      .add(DocumentFactory.getInstance().create("This is the first long document."))
      .build();
    Multimap<String, Document> map = corpus.groupBy(d -> d.toString());
    assertEquals(4, map.size(), 0d);
  }


  @Test
  public void mStreamTest() {
    Config.initializeTest();
    Corpus corpus = Corpus.builder()
      .source(CorpusFormats.PLAIN_TEXT, Resources.fromClasspath("com/davidbracewell/hermes/docs/txt"))
      .build()
      .filter(document -> true);

    corpus = corpus.annotate(Types.TOKEN)
      .filter(document -> document.tokenLength() > 2);

    assertEquals(3d, corpus.size(), 0d);

  }

  @Test
  public void resourceTest() {
    Config.initializeTest();
    Corpus corpus = Corpus.builder()
      .source(CorpusFormats.PLAIN_TEXT, Resources.fromClasspath("com/davidbracewell/hermes/docs/txt"))
      .build();

    assertFalse(corpus.isEmpty());
    assertEquals(3, corpus.size());
    corpus = corpus.annotate(Types.TOKEN);
    Counter<String> cntr = corpus.terms();
    assertEquals(3, cntr.get("the"), 0d);
    assertEquals(3, cntr.get("document"), 0d);
    assertEquals(3, cntr.get("This"), 0d);
    assertEquals(3, cntr.get("is"), 0d);
    assertEquals(3, cntr.get("."), 0d);
    assertEquals(1, cntr.get("first"), 0d);
    assertEquals(1, cntr.get("second"), 0d);
    assertEquals(1, cntr.get("third"), 0d);
    assertEquals(1, corpus.filter(d -> d.contains("third")).size(), 0d);

    Counter<String> ngrams = corpus.ngrams(NGramSpec.create().order(1)).mapKeys(tuple -> tuple.get(0).toString());
    assertEquals(cntr, ngrams);

    Counter<Tuple> bigrams = corpus.ngrams(NGramSpec.create().order(2));
    assertEquals(3d, bigrams.get(Tuple2.of("This", "is")), 0d);
    assertEquals(1d, bigrams.get(Tuple2.of("the", "first")), 0d);

    Counter<Tuple> trigrams = corpus.ngrams(NGramSpec.create().order(3));
    assertEquals(3d, trigrams.get(Tuple3.of("This", "is", "the")), 0d);

    Counter<Tuple> quadgrams = corpus.ngrams(NGramSpec.create().order(4));
    assertEquals(1d, quadgrams.get(Tuple4.of("This", "is", "the", "first")), 0d);

    Counter<Tuple> fivegrams = corpus.ngrams(NGramSpec.create().order(5));
    assertEquals(1d, fivegrams.get(NTuple.of("This", "is", "the", "first", "document")), 0d);


    cntr = corpus.documentFrequencies(false);
    assertEquals(3, cntr.get("the"), 0d);
    assertEquals(3, cntr.get("document"), 0d);
    assertEquals(3, cntr.get("This"), 0d);
    assertEquals(3, cntr.get("is"), 0d);
    assertEquals(3, cntr.get("."), 0d);
    assertEquals(1, cntr.get("first"), 0d);
    assertEquals(1, cntr.get("second"), 0d);
    assertEquals(1, cntr.get("third"), 0d);
  }

  @Test
  public void distributed() {
    Config.initializeTest();
    Config.setProperty("spark.master", "local");
    Corpus corpus = Corpus.builder()
      .distributed()
      .add(DocumentFactory.getInstance().create("This is the first document."))
      .add(DocumentFactory.getInstance().create("This is the second document."))
      .add(DocumentFactory.getInstance().create("This is the third document."))
      .build()
      .cache();
    assertEquals(3, corpus.size());
    assertEquals("This is the first document.", corpus.stream().first().get().toString());
    corpus = corpus.annotate(Types.TOKEN);
    Counter<String> cntr = corpus.terms();
    assertEquals(3, cntr.get("the"), 0d);
    assertEquals(3, cntr.get("document"), 0d);
    assertEquals(3, cntr.get("This"), 0d);
    assertEquals(3, cntr.get("is"), 0d);
    assertEquals(3, cntr.get("."), 0d);
    assertEquals(1, cntr.get("first"), 0d);
    assertEquals(1, cntr.get("second"), 0d);
    assertEquals(1, cntr.get("third"), 0d);
    assertEquals(1, corpus.filter(d -> d.contains("third")).size(), 0d);


    Resource r = Resources.temporaryDirectory();
    r.delete();
    try {
      corpus.write(r);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

  }

  @Test
  public void unionTest() {
    Corpus c1 = Corpus.builder()
      .source(CorpusFormats.PLAIN_TEXT, Resources.fromClasspath("com/davidbracewell/hermes/docs/txt"))
      .build();
    Corpus c2 = Corpus.builder()
      .inMemory()
      .add(DocumentFactory.getInstance().create("This is the first document."))
      .add(DocumentFactory.getInstance().create("This is the second document."))
      .add(DocumentFactory.getInstance().create("This is the third document."))
      .add(DocumentFactory.getInstance().create("This is the first long document."))
      .build();

    assertEquals(7, c1.union(c2).size(), 0d);
  }

}