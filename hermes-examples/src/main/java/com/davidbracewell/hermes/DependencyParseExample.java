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

import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusFormats;
import com.davidbracewell.io.Resources;
import com.davidbracewell.tuple.Tuple2;

import java.util.Optional;

import static com.davidbracewell.hermes.Types.DEPENDENCY;
import static com.davidbracewell.hermes.Types.PHRASE_CHUNK;

/**
 * @author David B. Bracewell
 */
public class DependencyParseExample {
  public static void main(String[] args) throws Exception {
    //Initializes configuration settings
    Hermes.initializeApplication(args);

    //In order to run this example you will need to download a MaltParser model (http://www.maltparser.org/mco/mco.html)
    //By default the model is expected to be located in /shared/data/models/en/engmalt.linear-1.7.mco
    //This can be changed by uncommenting the following line and putting the location in the ""
    //Config.setProperty("Annotation.DEPENDENCY.model.ENGLISH", "");


    //We will construct a corpus made up of the sample documents.
    Corpus corpus = Corpus.builder()
      .format(CorpusFormats.PLAIN_TEXT_OPL)
      .source(Resources.fromClasspath("com/davidbracewell/hermes/example_docs.txt"))
      .build()
      //Annotate for Dependency, which will also annotate for Token, Sentence, and Part of Speech
      .annotate(DEPENDENCY, PHRASE_CHUNK);

    corpus.forEach(document -> document.sentences()
      .forEach(sentence -> {
        //Output the sentence with pos information
        System.out.println(sentence.toPOSString());
        sentence.tokens().forEach(token -> {
          //Dependency relations are stored as relations on the tokens.
          //For convenience there is a method to get the first (which should be the only) dependency relation associated
          //with a token. It returns an optional in case there is no relation (e.g. the root of the tree)
          Optional<Tuple2<String, Annotation>> depRel = token.dependencyRelation();
          if (depRel.isPresent()) {
            System.out.println(depRel.get().v1 + "(" + token + ", " + depRel.get().v2 + ")");
          } else {
            System.out.println("root(" + token + ")");
          }
        });
        System.out.println("-----------------------------");

        //Dependency information can also propagate to other annotations.
        //Here we will loop over phrase chunks and get the dependency relation.
        //Note the target of the dependency relation is still at the token level, which is why we use first(PHRASE_CHUNK)
        sentence.get(PHRASE_CHUNK).forEach(chunk -> {
          Optional<Tuple2<String, Annotation>> depRel = chunk.dependencyRelation();
          if (depRel.isPresent()) {
            System.out.println(depRel.get().v1 + "(" + chunk + ", " + depRel.get().v2.first(PHRASE_CHUNK) + ")");
          } else {
            System.out.println("root(" + chunk + ")");
          }
        });
        System.out.println("===========================");

      })
    );
  }
}//END OF MaltParserExample
