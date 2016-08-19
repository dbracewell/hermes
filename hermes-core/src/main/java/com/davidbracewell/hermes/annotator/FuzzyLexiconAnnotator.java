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

import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.collection.counter.HashMapCounter;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.lexicon.Lexicon;
import com.davidbracewell.hermes.lexicon.LexiconEntry;
import com.davidbracewell.hermes.lexicon.LexiconManager;
import com.davidbracewell.hermes.lexicon.LexiconMatch;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * A lexicon annotator that allows gaps to occur in multi-word expressions. For example, "old red car" and "old broke
 * car" would match the lexicon item "old car" with a distance of one.
 * </p>
 *
 * @author David B. Bracewell
 */
public class FuzzyLexiconAnnotator extends ViterbiAnnotator {
  private static final long serialVersionUID = 1L;
  private final AnnotationType type;
  private final Lexicon lexicon;
  private final HashMultimap<String, String[]> prefix = HashMultimap.create();
  private final HashMultimap<String, String[]> suffix = HashMultimap.create();
  private final int maxDistance;


  /**
   * Instantiates a new Gappy lexicon annotator.
   *
   * @param type        the type
   * @param lexicon     the lexicon
   * @param maxDistance the max distance
   */
  public FuzzyLexiconAnnotator(@NonNull AnnotationType type, @NonNull Lexicon lexicon, int maxDistance) {
    super(lexicon.getMaxTokenLength() + maxDistance);
    Preconditions.checkArgument(maxDistance >= 0, "Maximum fuzzy distance must be > 0");
    this.type = type;
    this.lexicon = lexicon;
    this.maxDistance = maxDistance;
    for (String item : this.lexicon) {
      String[] parts = item.split("\\s+");
      if (parts.length > 1) {
        prefix.put(parts[0], parts);
        suffix.put(parts[parts.length - 1], parts);
      }
    }
  }

  /**
   * Instantiates a new Fuzzy lexicon annotator.
   *
   * @param type        the type
   * @param lexiconName the lexicon name
   * @param maxDistance the max distance
   */
  public FuzzyLexiconAnnotator(@NonNull AnnotationType type, @NonNull String lexiconName, int maxDistance) {
    this(type, LexiconManager.getLexicon(lexiconName), maxDistance);
  }


  @Override
  protected void createAndAttachAnnotation(Document document, LexiconMatch match) {
    if (!StringUtils.isNullOrBlank(match.getMatchedString())) {
      Annotation annotation = document.createAnnotation(type, match.getSpan());
      if (lexicon.getTagAttributeType() != null) {
        annotation.put(lexicon.getTagAttributeType(), match.getTag());
      }
      annotation.put(Types.CONFIDENCE, match.getScore());
      annotation.put(Types.MATCHED_STRING, match.getMatchedString());
    }
  }

  private double distance(List<Annotation> span, String[] candidate) {
    //Make sure the span contains at least all of the words in the candidate
    Counter<String> cCtr = new HashMapCounter<>(Arrays.asList(candidate));
    for (Annotation a : span) {
      if (cCtr.contains(a.toString())) {
        cCtr.decrement(a.toString());
      } else if (!lexicon.isCaseSensitive() && cCtr.contains(a.toString().toLowerCase())) {
        cCtr.decrement(a.toString().toLowerCase());
      } else if (cCtr.contains(a.getLemma())) {
        cCtr.decrement(a.getLemma());
      } else if (!lexicon.isCaseSensitive() && cCtr.contains(a.getLemma().toLowerCase())) {
        cCtr.decrement(a.getLemma().toLowerCase());
      }
    }
    if (cCtr.sum() > 0) {
      return Double.POSITIVE_INFINITY;
    }


    double[] row0 = new double[candidate.length + 1];
    double[] row1 = new double[candidate.length + 1];
    for (int i = 0; i < row0.length; i++) {
      row0[i] = i;
    }

    for (int i = 0; i < span.size(); i++) {
      row1[0] = i + 1;
      for (int j = 0; j < candidate.length; j++) {
        double cost =
          (StringUtils.safeEquals(candidate[j], span.get(i).toString(), lexicon.isCaseSensitive()) ||
            StringUtils.safeEquals(candidate[j], span.get(i).getLemma(), lexicon.isCaseSensitive())) ? 0d : 1d;

        if (cost == 1 && StringUtils.isPunctuation(span.get(j).toString())) {
          cost = row0.length;
        }

        row1[j + 1] = Math.min(row1[j] + cost, Math.min(row0[j + 1] + cost, row0[j] + cost));
      }
      if (row1[candidate.length] > maxDistance) {
        return Double.POSITIVE_INFINITY;
      }
      System.arraycopy(row1, 0, row0, 0, row0.length);
    }

    return row0[candidate.length];
  }

  private Set<String[]> getCandidates(String prefixStr, String suffixStr) {
    return Sets.intersection(prefix.get(prefixStr), suffix.get(suffixStr));
  }

  @Override
  public Set<AnnotatableType> satisfies() {
    return Collections.singleton(type);
  }

  @Override
  protected LexiconEntry scoreSpan(HString span) {
    LexiconEntry entry = lexicon.getEntries(span).stream().findFirst().orElse(null);

    if (entry != null) {
      return entry;
    }

    if (span.tokenLength() > 2) {
      List<Annotation> tokens = span.tokens();
      int TL = tokens.size() - 1;
      Set<String[]> candidates;

      if (lexicon.isCaseSensitive()) {
        candidates = Sets.union(
          getCandidates(tokens.get(0).toString(), tokens.get(TL).toString()),
          getCandidates(tokens.get(0).getLemma(), tokens.get(TL).getLemma())
        );
      } else {
        candidates = Sets.union(
          getCandidates(tokens.get(0).toString().toLowerCase(), tokens.get(TL).toString().toLowerCase()),
          getCandidates(tokens.get(0).getLemma().toLowerCase(), tokens.get(TL).getLemma().toLowerCase())
        );
      }

      String[] bestCandidate = null;
      double minDist = Double.POSITIVE_INFINITY;
      for (String[] candidate : candidates) {
        if (candidate.length < tokens.size()) {
          double d = distance(tokens, candidate);
          if (d < minDist) {
            minDist = d;
            bestCandidate = candidate;
          }
        }
      }

      if (minDist <= maxDistance && bestCandidate != null) {
        String matchedString = Joiner.on(' ').join(bestCandidate);
        double score = lexicon.getProbability(Fragments.string(matchedString)) / (0.1 + minDist);
        return new LexiconEntry(matchedString, score, null, lexicon.getTag(matchedString).orElse(null));
      }

    }
    return new LexiconEntry(StringUtils.EMPTY, 0, null, null);
  }

}//END OF FuzzyLexiconAnnotator
