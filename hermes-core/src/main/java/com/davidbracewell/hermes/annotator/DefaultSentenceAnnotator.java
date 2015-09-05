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

import com.davidbracewell.collection.Collect;
import com.davidbracewell.hermes.*;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.text.BreakIterator;
import java.util.Collections;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class DefaultSentenceAnnotator implements Annotator, Serializable{
  private static final long serialVersionUID = 1L;

  private static CharMatcher BAD_EOS = CharMatcher.INVISIBLE.and(CharMatcher.WHITESPACE).and(CharMatcher.BREAKING_WHITESPACE);
  private final Set<String> noSentenceBreak = ImmutableSet.<String>builder()
      .add("Mr.")
      .add("Mrs.")
      .add("Dr.")
      .add("Gen.")
      .build();

  @Override
  public void annotate(Document doc) {
    int index = 0;
    BreakIterator iterator = BreakIterator.getSentenceInstance(doc.getLanguage().asLocale());
    iterator.setText(doc.toString());
    for (int end = iterator.next(), start = 0; end != BreakIterator.DONE; end = iterator.next()) {
      //Trim whitespace the front and end of the entence
      while (start < doc.length() && BAD_EOS.matches(doc.charAt(start))) {
        start++;
      }
      while (end > 0 && BAD_EOS.matches(doc.charAt(end - 1))) {
        end--;
      }

      //Make sure it is still a valid sentence
      if (end <= start || start >= doc.length()) {
        start = end;
        continue;
      }

      if (!noSentenceBreak.contains(doc.substring(start, end))) {
        doc.createAnnotation(Types.SENTENCE,
            start,
            end,
            Collect.map(Attrs.INDEX, index)
        );
        index++;
        start = end;
      }

    }

  }

  public Set<AnnotationType> provides() {
    return Collections.singleton(Types.SENTENCE);
  }

  public Set<AnnotationType> requires() {
    return Collections.emptySet();
  }


}//END OF DefaultSentenceAnnotator
