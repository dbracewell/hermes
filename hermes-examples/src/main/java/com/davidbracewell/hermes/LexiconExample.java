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

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.annotator.LexiconAnnotator;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.DocumentFormats;
import com.davidbracewell.hermes.lexicon.Lexicon;
import com.davidbracewell.hermes.lexicon.LexiconSpec;
import com.davidbracewell.io.Resources;

/**
 * @author David B. Bracewell
 */
public class LexiconExample {

  public static void main(String[] args) throws Exception {
    Config.initialize("LexiconExample");

    //Load the config that defines the lexicon and annotator
    Config.loadConfig(Resources.fromClasspath("com/davidbracewell/hermes/example.conf"));
    Corpus.builder()
      .format(DocumentFormats.PLAIN_TEXT_OPL)
      .source(Resources.fromClasspath("com/davidbracewell/hermes/example_docs.txt"))
      .build()
      .annotate(Types.TOKEN, Types.SENTENCE, Types.ENTITY)
      .forEach(document -> document.get(Types.ENTITY).forEach(entity -> System.out.println(entity + "/" + entity.getTag().get())));
    System.out.println();

    //Alternatively we can do everything in code if we are not working in a distributed environment
    Lexicon lexicon = LexiconSpec.builder()
      .caseSensitive(false)
      .hasConstraints(false)
      .probabilistic(false)
      .tagAttribute(Attrs.ENTITY_TYPE)
      .resource(Resources.fromClasspath("com/davidbracewell/hermes/people.dict"))
      .build().create();

    //Register a lexicon annotator using the lexicon we created above to provide ENTITY annotations
    Pipeline.setAnnotator(Types.ENTITY, Language.ENGLISH, new LexiconAnnotator(Types.ENTITY, lexicon));

    Corpus.builder()
      .format(DocumentFormats.PLAIN_TEXT_OPL)
      .source(Resources.fromClasspath("com/davidbracewell/hermes/example_docs.txt"))
      .build()
      .annotate(Types.TOKEN, Types.SENTENCE, Types.ENTITY)
      .forEach(document -> document.get(Types.ENTITY).forEach(entity -> System.out.println(entity + "/" + entity.getTag().get())));


  }

}//END OF LexiconExample
