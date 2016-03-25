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
import com.davidbracewell.hermes.corpus.CorpusFormats;
import com.davidbracewell.io.Resources;

import static com.davidbracewell.hermes.Types.*;

/**
 * @author David B. Bracewell
 */
public class OpenNLPExample {

  public static void main(String[] args) throws Exception {
    //Initializes configuration settings
    Hermes.initializeApplication(args);

    //Lets us the OpenNLP models instead of Herme's
    //Load the OpenNLP English defaults (May need to edit this file or override to point to your models)
    Config.loadConfig(Resources.fromClasspath("com/davidbracewell/hermes/opennlp/opennlp-english.conf"));

    //Create a prefix where the models are stored
    //The directory structure can be seen by looking in hermes-opennlp/src/main/resources/com/davidbracewell/hermes/opennlp/opennlp-english.conf
    Config.setProperty("data.cp", "/shared/data/models"); //This is the root

    Corpus corpus = Corpus.builder()
      .format(CorpusFormats.PLAIN_TEXT_OPL)
      .source(Resources.fromClasspath("com/davidbracewell/hermes/example_docs.txt"))
      .build()
      .annotate(TOKEN, SENTENCE, PART_OF_SPEECH, ENTITY);

    corpus.forEach(document -> document.sentences().forEach(sentence -> System.out.println(sentence.toPOSString())));

    //Write the corpus to a resource in one json per line format (the resource just prints to standard out)
    //The thing to check out is the "completed" section, which shows the annotations that have been completed on
    //the document and a description of the provider (should be OpenNLPxxxAnnotator!)
    corpus.write(Resources.fromStdout());

    //Other libraries (e.g. Stanford's CoreNLP, ClearNLP, Gate, etc.) can be wrapped and used as the default annotators.


  }

}//END OF Sandbox
