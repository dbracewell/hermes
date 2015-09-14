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
import com.davidbracewell.hermes.annotator.TrieLexiconAnnotator;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.Resources;

import static com.davidbracewell.hermes.Types.*;

/**
 * @author David B. Bracewell
 */
public class LexiconExample {

  public static void main(String[] args) throws Exception {
    Config.initialize("LexiconExample");


    TrieLexiconAnnotator annotator = new TrieLexiconAnnotator(
        false, Types.LEXICON_MATCH,
        Attrs.TAG,
        Resources.fromClasspath("com/davidbracewell/hermes/people.dict")
    );
    annotator.setPrefixMatch(true);
    Pipeline.setAnnotator(Types.LEXICON_MATCH, Language.ENGLISH, annotator);

    Pipeline pipeline = Pipeline.builder()
        .addAnnotations(TOKEN, SENTENCE, LEXICON_MATCH)
        .onComplete(document -> {
          document.get(Types.LEXICON_MATCH)
              .forEach(m -> System.out.println(
                  m +
                      " [" + m.get(Attrs.TAG) + "] " +
                      " [" + m.get(Attrs.CONFIDENCE) + "]"
              ));
        })
        .build();

    pipeline.process(
        Corpus.from(CorpusFormat.PLAIN_TEXT_OPL, Resources.fromClasspath("com/davidbracewell/hermes/example_docs.txt"))
    );
  }

}//END OF LexiconExample
