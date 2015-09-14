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

package com.davidbracewell.hermes.annotator;

import com.davidbracewell.Language;
import com.davidbracewell.collection.Collect;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.*;
import com.davidbracewell.logging.Logger;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author David B. Bracewell
 */
public class OpenNLPSentenceAnnotator implements Annotator {

  private static final Logger log = Logger.getLogger(OpenNLPSentenceAnnotator.class);
  private volatile Map<Language, SentenceModel> sentenceDetectors = Maps.newEnumMap(Language.class);

  @Override
  public void annotate(Document document) {
    loadSentenceDetector(document.getLanguage());
    String content = document.toString();

    SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceDetectors.get(document.getLanguage()));
    Span[] sentences = sentenceDetector.sentPosDetect(content);
    for (int index = 0, i = 0; i < sentences.length; i++) {
      int start = sentences[i].getStart();
      int end = sentences[i].getEnd();

      while (start < content.length() && StringUtils.WHITESPACE.matches(content.charAt(start))) {
        start++;
      }

      while (end > 0 && end < content.length() && StringUtils.WHITESPACE.matches(content.charAt(end - 1))) {
        end--;
      }


      if (end <= start || start >= content.length()) {
        continue;
      }


      HString sentenceFragment = document.substring(start, end);

      int pos;
      while ((pos = findEOS(sentenceFragment)) != sentenceFragment.end()) {
        //There is a disagreement between what that tokenizer thinks is an aberviation or non-end of sentence
        //period and what the sentence segmenter says. Err on the side of the tokenizer.
        document.createAnnotation(Types.SENTENCE, start, pos, Collect.map(Attrs.INDEX, index));
        index++;

        start = pos;
        while (start < content.length() && StringUtils.WHITESPACE.matches(content.charAt(start))) {
          start++;
        }
        sentenceFragment = document.substring(start, end);
      }

      document.createAnnotation(Types.SENTENCE, sentenceFragment.start(), sentenceFragment.end(), Collect.map(Attrs.INDEX, index));
      index++;
    }
  }

  private int findEOS(HString fragment) {
//    for (Annotation token : fragment.tokens()) {
//      if (token.span().length() == 1 && Character.getType((int) token.content().charAt(0)) == Character.END_PUNCTUATION
//          && (TokenType.ALPHA_NUMERIC == token.previousOfType(Types.TOKEN).getAttribute(Attrs.TOKEN_TYPE))
//          && (TokenType.ALPHA_NUMERIC == token.nextOfType(Types.TOKEN).getAttribute(Attrs.TOKEN_TYPE))
//          ) {
//        return token.span().end;
//      }
//    }
//    return fragment.span().end;
    return fragment.end();
  }

  private void loadSentenceDetector(Language language) {
    if (!sentenceDetectors.containsKey(language)) {
      synchronized (OpenNLPSentenceAnnotator.class) {
        if (!sentenceDetectors.containsKey(language)) {
          Stopwatch sw = Stopwatch.createStarted();
          SentenceModel model = null;
          try {
            model = new SentenceModel(Config.get("opennlp.sentence", language, "model").asResource().openInputStream());
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
          sw.stop();
          if (log.isLoggable(Level.FINE)) {
            log.fine("Loaded sentence model [{0}] for {1} in {2}.",
                Config.get(OpenNLPSentenceAnnotator.class, language, "model").asString(), language, sw
            );
          }
          sentenceDetectors.put(language, model);
        }
      }
    }
  }

  @Override
  public Set<AnnotationType> satisfies() {
    return Collections.singleton(Types.SENTENCE);
  }

  @Override
  public Set<AnnotationType> requires() {
    return Collections.singleton(Types.TOKEN);
  }

  @Override
  public String getVersion() {
    return "1.6.0";
  }

}//END OF OpenNLPSentenceAnnotator
