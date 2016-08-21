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

package com.davidbracewell.hermes.lexicon;

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AttributeType;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.Types;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * The type Base lexicon.
 *
 * @author David B. Bracewell
 */
public abstract class BaseLexicon implements Lexicon, Serializable {
  private static final long serialVersionUID = 1L;
  private final boolean caseSensitive;
  private final boolean probabilistic;
  private final AttributeType tagAttributeType;
  private int longestLemma = 0;

  /**
   * Instantiates a new Base lexicon.
   *
   * @param caseSensitive the case sensitive
   * @param probabilistic the probabilistic
   * @param tagAttributeType  the tag attribute
   */
  public BaseLexicon(boolean caseSensitive, boolean probabilistic, AttributeType tagAttributeType) {
    this.caseSensitive = caseSensitive;
    this.probabilistic = probabilistic;
    this.tagAttributeType = tagAttributeType;
  }


  public AttributeType getTagAttributeType() {
    return tagAttributeType;
  }

  /**
   * Is case sensitive boolean.
   *
   * @return the boolean
   */
  @Override
  public final boolean isCaseSensitive() {
    return caseSensitive;
  }


  protected void ensureLongestLemma(String lemma) {
    this.longestLemma = Math.max(this.longestLemma, lemma.split("\\s+").length);
  }

  /**
   * Normalize string.
   *
   * @param sequence the sequence
   * @return the string
   */
  protected String normalize(CharSequence sequence) {
    if (isCaseSensitive()) {
      return sequence.toString();
    }
    return sequence.toString().toLowerCase();
  }

  public List<HString> match(@NonNull HString source) {
    if (longestLemma < 3) {
      this.longestLemma = 3;
    }
    if (probabilistic) {
      return viterbi(source);
    } else {
      return longestMatchFirst(source);
    }
  }

  /**
   * Viterbi list.
   *
   * @param source the source
   * @return the list
   */
  protected List<HString> viterbi(@NonNull HString source) {
    List<Annotation> tokens = source.tokens();
    int n = tokens.size();
    int maxLen = longestLemma + 1;
    LexiconMatch[] matches = new LexiconMatch[n + 1];
    double[] best = new double[n + 1];
    best[0] = 0;
    for (int end = 1; end <= n; end++) {
      matches[end] = new LexiconMatch(tokens.get(end - 1), 0d, "", null);
      for (int start = end - 1; start >= 0 && start >= (end - maxLen); start--) {
        HString span = HString.union(tokens.subList(start, end));
        LexiconEntry entry = getEntries(span).stream().findFirst().orElse(new LexiconEntry("", 0, null, null));
        LexiconMatch score = new LexiconMatch(span, entry.getProbability(), entry.getLemma(), entry.getTag());
        double segmentScore = score.getScore() + best[start];
        if (segmentScore >= best[end]) {
          best[end] = segmentScore;
          matches[end] = score;
        }
      }
    }
    int i = n;
    List<HString> results = new LinkedList<>();
    while (i > 0) {
      LexiconMatch match = matches[i];
      if (match.getScore() > 0) {
        results.add(createFragment(match));
      }
      i = i - matches[i].getSpan().tokenLength();
    }

    Collections.reverse(results);
    return results;
  }

  private HString createFragment(LexiconMatch match) {
    HString tmp = match.getSpan().document().substring(match.getSpan().start(), match.getSpan().end());
    tmp.put(Types.CONFIDENCE, match.getScore());
    tmp.put(Types.MATCHED_STRING, match.getMatchedString());
    if (tagAttributeType != null) {
      tmp.put(tagAttributeType, match.getTag());
    }
    return tmp;
  }

  /**
   * Longest match first list.
   *
   * @param source the source
   * @return the list
   */
  protected List<HString> longestMatchFirst(@NonNull HString source) {
    List<HString> results = new LinkedList<>();
    List<Annotation> tokens = source.tokens();
    Predicate<HString> prefix = (this instanceof PrefixSearchable)
                                ? Cast.<PrefixSearchable>as(this)::isPrefixMatch
                                : h -> true;

    for (int i = 0; i < tokens.size(); ) {
      Annotation token = tokens.get(i);
      if (prefix.test(token)) {

        LexiconMatch bestMatch = null;
        for (int j = i + 1; j < tokens.size() && j < (i + 1 + longestLemma); j++) {
          HString temp = HString.union(tokens.subList(i, j));
          List<LexiconEntry> entries = getEntries(temp);
          if (entries.size() > 0) {
            bestMatch = new LexiconMatch(temp, entries.get(0));
          }
          if (!prefix.test(temp)) {
            break;
          }
        }

        if (bestMatch != null) {
          results.add(createFragment(bestMatch));
          i += bestMatch.getSpan().tokenLength();
        } else {
          i++;
        }

      } else if (test(token)) {
        results.add(createFragment(new LexiconMatch(token, getEntries(token).get(0))));
        i++;
      } else {
        i++;
      }
    }

    return results;
  }

  @Override
  public int getMaxTokenLength() {
    return longestLemma;
  }

}//END OF BaseLexicon
