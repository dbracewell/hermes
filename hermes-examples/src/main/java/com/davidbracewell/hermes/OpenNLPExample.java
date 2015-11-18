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
import com.davidbracewell.hermes.annotator.OpenNLPEntityAnnotator;
import com.davidbracewell.hermes.annotator.OpenNLPPOSAnnotator;
import com.davidbracewell.hermes.annotator.OpenNLPSentenceAnnotator;
import com.davidbracewell.hermes.annotator.OpenNLPTokenAnnotator;
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

    //Load the OpenNLP English defaults
    Config.loadConfig(Resources.fromClasspath("com/davidbracewell/hermes/opennlp/opennlp-english.conf"));
    //Create a prefix where the models are stored
    Config.setProperty("data.cp", "/data/models");

    //Create a pipeline to do tokenization, sentence segmentation, and part of speech tagging
    Pipeline pipeline = Pipeline.builder()
      .addAnnotations(TOKEN, SENTENCE, PART_OF_SPEECH, OpenNLPEntityAnnotator.OPENNLP_ENTITY, Types.LEMMA, Types.STEM)
      .onComplete(document -> document.sentences().forEach
          (
            sentence -> {
              System.out.println(sentence.toPOSString());
              sentence.tokens().forEach(token -> System.out.print(token.getLemma() + " "));
              System.out.println();
              sentence.tokens().forEach(token -> System.out.print(token.getStem() + " "));
              System.out.println();
            }
          )
      )
      .returnCorpus(false)
      .build();


    Corpus corpus = Corpus.builder()
      .format(DocumentFormats.PLAIN_TEXT)
      .source(Resources.fromClasspath("com/davidbracewell/hermes/example_docs.txt"))
      .build();

    //load in the example docs as one big document and process it.
    pipeline.process(corpus);
  }

}//END OF Sandbox
