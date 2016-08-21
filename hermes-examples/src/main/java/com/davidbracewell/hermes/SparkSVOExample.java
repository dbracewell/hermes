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

import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.collection.counter.HashMapCounter;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusFormats;
import com.davidbracewell.io.Resources;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class SparkSVOExample implements Serializable {
  private static final long serialVersionUID = 1L;


  public static void main(String[] args) throws Exception {
    Config.initialize("SparkExample");

    //Need to add the spark core jar file to the classpath for this to run
    //We will run it local, so we set the spark master to local[*]
    Config.setProperty("spark.master", "local[*]");

    //Build the corpus
    Corpus corpus = Corpus.builder()
      .distributed()
      .format(CorpusFormats.PLAIN_TEXT_OPL)
      //You can substitute the file for one you have. Here I am using a 1,000,000 sentence corpus from news articles with
      // one sentence (treated as a document) per line.
      .source(Resources.fromFile("/shared/data/corpora/en/Raw/eng_news_2005_1M-sentences.txt")).build()
      .repartition(100)
      .annotate(Types.DEPENDENCY);

    Counter<String> svoCounts = new HashMapCounter<>(
      corpus.stream().flatMap(Document::sentences)
        .flatMap(sentence -> {
          List<String> svo = new LinkedList<>();
          sentence.tokens().stream().filter(token -> token.getPOS().isVerb())
            .forEach(predicate -> {
              List<Annotation> nsubjs = predicate.children("nsubj");
              List<Annotation> dobjs = predicate.children("dobj");
              if (nsubjs.size() > 0 && dobjs.size() > 0) {
                for (Annotation nsubj : nsubjs) {
                  for (Annotation dobj : dobjs) {
                    svo.add(nsubj.toLowerCase() + "::" + predicate.toLowerCase() + "::" + dobj);
                  }
                }
              }
            });
          return svo;
        })
        .countByValue()
    );

    //Calculate term frequencies for the corpus. Note we are saying we want lemmatized versions, but have not
    //run the lemma annotator, instead it will just return the lowercase version of the content.
    svoCounts.filterByValue(v -> v >= 10).entries().forEach(entry -> System.out.println(entry.getKey() + " => " + entry.getValue()));
  }

}//END OF SparkSVOExample
