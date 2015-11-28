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
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.DocumentFormats;
import com.davidbracewell.io.Resources;

import static com.davidbracewell.hermes.Types.*;

/**
 * @author David B. Bracewell
 */
public class OpenNLPExample {

  public static void main(String[] args) throws Exception {
    Config.initialize("OpenNLPExample");

    //Load the OpenNLP English defaults (May need to edit this file or override to point to your models)
    Config.loadConfig(Resources.fromClasspath("com/davidbracewell/hermes/opennlp/opennlp-english.conf"));
    //Create a prefix where the models are stored
    Config.setProperty("data.cp", "/data/models"); //This is the root

    Corpus corpus = Corpus.builder()
      .format(DocumentFormats.PLAIN_TEXT)
      .source(Resources.fromClasspath("com/davidbracewell/hermes/example_docs.txt"))
      .build()
      .annotate(TOKEN, SENTENCE, PART_OF_SPEECH, ENTITY, LEMMA, STEM);

    corpus.forEach(document -> document.sentences().forEach(sentence -> System.out.println(sentence.toPOSString())));

    //Write the corpus to a resource in one json per line format (the resource just prints to standard out)
    corpus.write(Resources.fromStdout());

  }

}//END OF Sandbox
