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
import com.davidbracewell.hermes.annotator.RegexAnnotator;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.DocumentFormats;
import com.davidbracewell.io.Resources;

import static com.davidbracewell.hermes.Types.SENTENCE;
import static com.davidbracewell.hermes.Types.TOKEN;

/**
 * @author David B. Bracewell
 */
public class CustomAnnotator {

  public static void main(String[] args) throws Exception {
    Config.initialize("CustomAnnotator");

    AnnotationType animalMention = AnnotationType.create("ANIMAL_MENTION");
    Pipeline.setAnnotator(animalMention, Language.ENGLISH, new RegexAnnotator("(fox|dog)", animalMention));

    AnnotationType verbs = AnnotationType.create("VERBS");
    Pipeline.setAnnotator(verbs, Language.ENGLISH, new RegexAnnotator("(is|jumps?|come)", verbs));

    Corpus.builder().source(DocumentFormats.PLAIN_TEXT_OPL, Resources.fromString(
      "The quick brown fox jumps over the lazy dog.\n" +
        "Now is the time for all good men to come to aid of their country.\n"
    )).build().forEach(document -> {
      Pipeline.process(document, TOKEN, SENTENCE, animalMention, verbs);
      document.get(animalMention).forEach(a -> System.out.println("ANIMAL: " + a));
      document.get(verbs).forEach(a -> System.out.println("VERB: " + a));
    });

  }

}//END OF CustomAnnotator
