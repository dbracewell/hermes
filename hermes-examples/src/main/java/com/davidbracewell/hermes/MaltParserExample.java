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
public class MaltParserExample {
  public static void main(String[] args) throws Exception {
    Config.initialize("MaltParserExample", args);

    Corpus corpus = Corpus.builder()
      .format(DocumentFormats.PLAIN_TEXT_OPL)
      .source(Resources.fromClasspath("com/davidbracewell/hermes/example_docs.txt"))
      .build()
      .annotate(TOKEN, SENTENCE, PART_OF_SPEECH, ENTITY, LEMMA, STEM, DEPENDENCY);

    corpus.forEach(document -> document.sentences()
      .forEach(sentence -> {
        sentence.tokens().forEach(token ->
          //Dependency relations are stored as relations on the tokens.
          //For convenience there is a method to get the first (which should be the only) dependency relation associated
          //with a token. It returns an optional in case there is no relation (e.g. the root of the tree)
          System.out.println(token + "[" + token.getLemma() + "]" + "/" + token.getPOS().asString() +
            " : " + token.dependencyRelation().map(r -> r.getKey() + "=>" + r.getValue()).orElse(""))
        );
        System.out.println();
      })
    );
  }
}//END OF MaltParserExample
