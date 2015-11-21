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

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.annotators.MaltParserAnnotator;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.DocumentFormats;
import com.davidbracewell.io.Resources;

import java.util.stream.Collectors;

import static com.davidbracewell.hermes.Types.*;

/**
 * @author David B. Bracewell
 */
public class OpenNLPExample {

  public static void main(String[] args) throws Exception {
    Config.initialize("OpenNLPExample");

    //Load the OpenNLP English defaults
    Config.loadConfig(Resources.fromClasspath("com/davidbracewell/hermes/opennlp/opennlp-english.conf"));
    //Create a prefix where the models are stored
    Config.setProperty("data.cp", "/data/models");

    Config.setProperty("Annotation.DEPENDENCY.annotator", MaltParserAnnotator.class.getName());
    Config.setProperty("MaltParser.ENGLISH.model", "/home/ik/Downloads/engmalt.linear-1.7.mco");
    Corpus corpus = Corpus.builder()
      .format(DocumentFormats.PLAIN_TEXT_OPL)
      .source(Resources.fromClasspath("com/davidbracewell/hermes/example_docs.txt"))
      .build()
      .annotate(TOKEN, SENTENCE, PART_OF_SPEECH, ENTITY, LEMMA, STEM, DEPENDENCY);


//    List<Document> docs = corpus.stream().collect();
//
//    docs.get(2).tokens().forEach(token ->
//      System.out.println(token + " : " + token.getRelations() + " : " + token.sentences())
//    );

    corpus.forEach(document -> {
      document.sentences().forEach(sentence -> {
        Annotation root = sentence.tokens().stream().filter(a -> !a.getParent().isPresent()).findFirst().get();
        System.out.println(root + " : " + root.getChildren());
        System.out.println(sentence.tokens().stream().filter(t -> t.getParent().filter(p -> p == root).isPresent()).collect(Collectors.toList()));
        System.out.println();
      });
    });


  }

}//END OF Sandbox
