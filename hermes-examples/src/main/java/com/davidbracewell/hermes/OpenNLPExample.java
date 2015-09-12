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
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.Resources;

import static com.davidbracewell.hermes.Types.*;

/**
 * @author David B. Bracewell
 */
public class OpenNLPExample {

  public static void main(String[] args) throws Exception {
    Config.initialize("Sandbox");

    //Load the OpenNLP default.conf file to setup the OPENNLP_ENTITY type
    Config.loadPackageConfig("com.davidbracewell.hermes.opennlp");

    //Create a prefix where the models are stored
    Config.setProperty("data.cp", "/data/models");

    //Set config properties for the token annotator to use OpenNLP
    Config.setProperty("Annotation.TOKEN.annotator", OpenNLPTokenAnnotator.class.getName());
    //Set where the model is located
    Config.setProperty("opennlp.tokenizer.model.ENGLISH", "${data.cp}/en/en-token.bin");

    //Set config properties for the sentence annotator to use OpenNLP
    Config.setProperty("Annotation.SENTENCE.annotator", OpenNLPSentenceAnnotator.class.getName());
    //Set where the model is located
    Config.setProperty("opennlp.sentence.model.ENGLISH", "${data.cp}/en/opennlp-sent.bin");

    //Set config properties for the part of speech annotator to use OpenNLP
    Config.setProperty("Annotation.PART_OF_SPEECH.annotator", OpenNLPPOSAnnotator.class.getName());
    //Set where the model is located
    Config.setProperty("opennlp.part_of_speech.model.ENGLISH", "${data.cp}/en/opennlp-pos.bin");


    Config.setProperty("data.cp", "/data/models");
    //Set where the model is located
    Config.setProperty("opennlp.entity.models.ENGLISH", "${data.cp}/en/ner/en-ner-person.bin,${data.cp}/en/ner/en-ner-location.bin,${data.cp}/en/ner/en-ner-organization.bin,${data.cp}/en/ner/en-ner-date.bin, ${data.cp}/en/ner/en-ner-money.bin, ${data.cp}/en/ner/en-ner-percentage.bin, ${data.cp}/en/ner/en-ner-time.bin");


    //Create a pipeline to do tokenization, sentence segmentation, and part of speech tagging
    Pipeline pipeline = Pipeline.builder()
      .addAnnotations(TOKEN, SENTENCE, PART_OF_SPEECH, OpenNLPEntityAnnotator.OPENNLP_ENTITY)
      .onComplete(document -> document.sentences().forEach
          (
            sentence -> {
              System.out.println(sentence.toPOSString());
              sentence.getOverlapping(ENTITY).forEach
                (
                  entity -> System.out.println
                    (
                      entity + "/" +
                        entity.get(Attrs.ENTITY_TYPE) +
                        " [" + entity.get(Attrs.CONFIDENCE) + "]"
                    )
                );
              System.out.println();
            }
          )
      )
      .build();

    //load in the example docs as one big document and process it.
    pipeline.process(
      Corpus.from(CorpusFormat.PLAIN_TEXT, Resources.fromClasspath("com/davidbracewell/hermes/example_docs.txt"))
    );
  }

}//END OF Sandbox
