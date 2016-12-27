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

package com.davidbracewell.hermes.keyword;

import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.collection.counter.Counters;
import com.davidbracewell.guava.common.collect.HashMultimap;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.attribute.POS;
import com.davidbracewell.hermes.filter.StopWords;
import lombok.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public class NPClusteringKeywordExtractor implements KeywordExtractor {
   private static final long serialVersionUID = 1L;

   @Override
   public Counter<String> extract(@NonNull HString hstring) {
      hstring.document().annotate(Types.PHRASE_CHUNK, Types.LEMMA);

      Counter<String> tf = Counters.newCounter(hstring.tokenStream()
                                                      .map(HString::getLemma)
                                                      .collect(Collectors.toList()));

      double n = hstring.tokenLength();

      List<HString> chunks = hstring.stream(Types.PHRASE_CHUNK)
                                    .flatMap(pc -> pc.split(a -> a.getPOS().isInstance(POS.PUNCTUATION)).stream())
                                    .map(pc -> pc.trim(StopWords.isStopWord()))
                                    .filter(pc -> !pc.isEmpty())
                                    .collect(Collectors.toList());

      Counter<String> npFreqs = Counters.newCounter(chunks.stream()
                                                          .map(HString::getLemma)
                                                          .collect(Collectors.toList()));


      Counter<String> npScores = Counters.newCounter();

      chunks.forEach(pc -> {
         String lemma = pc.getLemma();
         double npFreq = npFreqs.get(lemma);
         double termSum = pc.tokenStream().mapToDouble(token -> tf.get(token.getLemma())).sum();
         double score = n + (termSum / n) * npFreq;
         if (score > npScores.get(lemma)) {
            npScores.set(lemma, score);
         }
      });


      HashMultimap<String, String> clusters = HashMultimap.create();

      chunks.stream().filter(pc -> pc.tokenLength() == 1)
            .forEach(pc -> clusters.put(pc.getLemma(), pc.getLemma()));

      Set<String> notAdded = new HashSet<>();
      chunks.stream().filter(pc -> pc.tokenLength() > 1)
            .forEach(pc -> {
               boolean added = false;
               for (Annotation token : pc.tokens()) {
                  if (clusters.containsKey(token.getLemma())) {
                     added = true;
                     clusters.put(token.getLemma(), pc.getLemma());
                  }
               }
               if (!added) {
                  notAdded.add(pc.getLemma());
               }
            });


      Counter<String> clusterScores = Counters.newCounter();
      clusters.keySet().forEach(cluster -> {
         double totalNPScore = clusters.get(cluster).stream().mapToDouble(npScores::get).sum();
         double score = Math.log(1.0 + clusters.get(cluster).size()) * totalNPScore / clusters.get(cluster).size();
         clusterScores.set(cluster, score);
      });


      clusterScores.forEach((c, score) -> {
         String centroid = clusters.get(c)
                                   .stream()
                                   .max((p1, p2) -> -Double.compare(npScores.get(p1), npScores.get(p2)))
                                   .orElse(c);
         System.out.println(centroid + " [" + c + "] (" + clusters.get(c) + ") => " + score);
      });

      return npScores;
   }

}//END OF NPClusteringKeywordExtractor
