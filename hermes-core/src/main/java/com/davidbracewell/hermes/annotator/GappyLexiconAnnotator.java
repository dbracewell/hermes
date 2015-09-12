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

import com.davidbracewell.collection.Counter;
import com.davidbracewell.collection.Counters;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.lexicon.SimpleTagLexicon;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * <p>
 * A lexicon annotator that allows gaps to occur in multi-word expressions. For example, "old red car" and "old broke
 * car" would match the lexicon item "old car" with a distance of one.
 * </p>
 *
 * @author David B. Bracewell
 */
public class GappyLexiconAnnotator extends ViterbiAnnotator {
  private static final long serialVersionUID = 1L;
  final AnnotationType type;
  final SimpleTagLexicon lexicon;
  final HashMultimap<String, String[]> prefix = HashMultimap.create();
  final HashMultimap<String, String[]> suffix = HashMultimap.create();
  final Attribute tagAttribute;
  final int maxDistance;


  /**
   * Instantiates a new Gappy lexicon annotator.
   *
   * @param type          the type
   * @param tagAttribute  the tag attribute
   * @param caseSensitive the case sensitive
   * @param maxDistance   the max distance
   * @param lexiconFiles  the lexicon files
   */
  public GappyLexiconAnnotator(AnnotationType type, Attribute tagAttribute, boolean caseSensitive, int maxDistance, Resource... lexiconFiles) {
    this(7, type, tagAttribute, caseSensitive, maxDistance, Arrays.asList(lexiconFiles));
  }

  /**
   * Instantiates a new Gappy lexicon annotator.
   *
   * @param maxSpanSize   the max span size
   * @param type          the type
   * @param tagAttribute  the tag attribute
   * @param caseSensitive the case sensitive
   * @param maxDistance   the max distance
   * @param lexiconFiles  the lexicon files
   */
  public GappyLexiconAnnotator(int maxSpanSize, AnnotationType type, Attribute tagAttribute, boolean caseSensitive, int maxDistance, Resource... lexiconFiles) {
    this(maxSpanSize, type, tagAttribute, caseSensitive, maxDistance, Arrays.asList(lexiconFiles));
  }

  /**
   * Instantiates a new Gappy lexicon annotator.
   *
   * @param maxSpanSize   the max span size
   * @param type          the type
   * @param tagAttribute  the tag attribute
   * @param caseSensitive the case sensitive
   * @param maxDistance   the max distance
   * @param lexiconFiles  the lexicon files
   */
  public GappyLexiconAnnotator(int maxSpanSize, AnnotationType type, Attribute tagAttribute, boolean caseSensitive, int maxDistance, Collection<Resource> lexiconFiles) {
    super(maxSpanSize + maxDistance);
    this.maxDistance = maxDistance;
    this.type = type;
    this.tagAttribute = tagAttribute;
    this.lexicon = new SimpleTagLexicon(caseSensitive, tagAttribute, lexiconFiles);
    for (String item : this.lexicon.lexicalItems()) {
      String[] parts = item.split("\\s+");
      if (parts.length > 1) {
        prefix.put(parts[0], parts);
        suffix.put(parts[parts.length - 1], parts);
      }
    }
  }

  @Override
  protected void createAndAttachAnnotation(Document document, Match match) {
    if (match.matchedString != null) {
      Annotation annotation = document.createAnnotation(type, match.span);
      annotation.put(tagAttribute, lexicon.lookup(match.matchedString).get());
      annotation.put(Attrs.CONFIDENCE, lexicon.probability(match.matchedString));
    }
  }

  private double distance(List<Annotation> span, String[] candidate) {


    //Make sure the span contains at least all of the words in the candidate
    Counter<String> cCtr = Counters.newHashMapCounter(Arrays.asList(candidate));
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
  public Set<AnnotationType> provides() {
    return Collections.singleton(type);
  }

  @Override
  protected Tuple2<String, Double> scoreSpan(HString span) {
    if (lexicon.contains(span)) {
      return Tuple2.of(span.toString(), Math.pow(lexicon.probability(span) * span.tokenLength(), 2));
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
        double score = lexicon.probability(matchedString) / (0.1 + minDist);
        score = Math.abs(2.0 / (1.0 + score)) - 1.0;
        return Tuple2.of(matchedString, score + span.tokenLength());
      }

    }
    return Tuple2.of(null, 1d);
  }

}//END OF ViterbiLexiconAnnotator
