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


import com.davidbracewell.hermes.*;
import com.davidbracewell.tuple.Tuple2;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>An abstract base annotator that uses the Viterbi algorithm to find text items in a document. Child classes
 * implement the <code>scoreSpan</code> and <code>createAndAttachAnnotation</code> methods to score individual spans
 * and attach to the document. Child implementations may also override <code>combineScore</code> to change how scores
 * are combined, by default theyocker  are multiplied.</p>
 *
 * @author David B. Bracewell
 */
public abstract class ViterbiAnnotator extends SentenceLevelAnnotator {

  private final int maxSpanSize;

  /**
   * Default constructor
   *
   * @param maxSpanSize The maximum length that an identified span will be
   */
  protected ViterbiAnnotator(int maxSpanSize) {
    this.maxSpanSize = maxSpanSize;
  }

  @Override
  public final void annotate(Annotation sentence) {
    List<Annotation> tokens = sentence.tokens();
    int n = tokens.size();
    int maxLen = maxSpanSize > 0 ? maxSpanSize : n;
    Match[] matches = new Match[n + 1];
    double[] best = new double[n + 1];
    best[0] = 1.0;


    for (int i = 1; i <= n; i++) {
      for (int j = i - 1; j >= 0 && j >= (i - maxLen); j--) {
        int w = i - j;
        HString span = HString.union(tokens.subList(j, i));
        Tuple2<String, Double> score = scoreSpan(span);
        double segmentScore = combineScore(best[i - w], score.v2);
        if (segmentScore >= best[i]) {
          best[i] = segmentScore;
          matches[i] = new Match(span, score.v1, score.v2);
        }
      }
    }
    int i = n;
    while (i > 0) {
      createAndAttachAnnotation(sentence.document(), matches[i]);
      i = i - matches[i].span.tokenLength();
    }
  }

  /**
   * Combines the score of a possible span with that of the spans up to this point to determine the optimal
   * segmentation.
   *
   * @param currentScore The score of the sentence so far
   * @param spanScore    The score of the span under consideration
   * @return The combination of the current and span scores
   */
  protected double combineScore(double currentScore, double spanScore) {
    return currentScore + spanScore;
  }

  /**
   * Given an possible span determines if an annotation should be created and if so creates and attaches it.
   *
   * @param span The span to check
   */
  protected abstract void createAndAttachAnnotation(Document document, Match span);

  /**
   * Scores the given span.
   *
   * @param span The span
   * @return The score of the span
   */
  protected abstract Tuple2<String, Double> scoreSpan(HString span);

  /**
   * Class for holding lexicon matches
   */
  protected static class Match {
    public final HString span;
    public final double score;
    public final String matchedString;

    public Match(HString span, String matchedString, double score) {
      this.span = span;
      this.matchedString = matchedString;
      this.score = score;
    }

  }//END OF Match

  @Override
  public Set<AnnotationType> requires() {
    return Collections.singleton(Types.TOKEN);
  }
}//END OF ViterbiAnnotator
