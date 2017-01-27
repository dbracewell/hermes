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

package com.davidbracewell.hermes.lexicon.generation;

import com.davidbracewell.Tag;
import com.davidbracewell.apollo.affinity.Similarity;
import com.davidbracewell.apollo.linalg.DenseVector;
import com.davidbracewell.apollo.linalg.Vector;
import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.collection.counter.MultiCounter;
import com.davidbracewell.collection.counter.MultiCounters;
import com.davidbracewell.guava.common.base.Preconditions;
import com.davidbracewell.hermes.StringTag;
import com.davidbracewell.io.Resources;
import com.davidbracewell.string.StringUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.eclipse.collections.api.multimap.Multimap;
import org.eclipse.collections.impl.multimap.set.UnifiedSetMultimap;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Distributional lexicon generator.
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public class DistributionalLexiconGenerator<T extends Tag> implements LexiconGenerator<T> {

   private final Embedding wordEmbeddings;
   @Getter
   @Setter
   private double threshold = 0.4;
   @Getter
   @Setter
   private int maximumTermCount = 100;
   private final UnifiedSetMultimap<T, String> seedTerms = UnifiedSetMultimap.newMultimap();
   private final UnifiedSetMultimap<T, String> negativeSeedTerms = UnifiedSetMultimap.newMultimap();

   /**
    * Instantiates a new Distributional lexicon generator.
    *
    * @param wordEmbeddings the word embeddings
    */
   public DistributionalLexiconGenerator(@NonNull Embedding wordEmbeddings) {
      this.wordEmbeddings = wordEmbeddings;
   }

   /**
    * Instantiates a new Distributional lexicon generator.
    *
    * @param wordEmbeddings the word embeddings
    * @param seedTerms      the seed terms
    */
   public DistributionalLexiconGenerator(@NonNull Embedding wordEmbeddings, @NonNull Multimap<T, String> seedTerms) {
      this.wordEmbeddings = wordEmbeddings;
      this.seedTerms.putAll(seedTerms);
   }

   /**
    * Instantiates a new Distributional lexicon generator.
    *
    * @param wordEmbeddings the word embeddings
    * @param seedTerms      the seed terms
    * @param threshold      the threshold
    */
   public DistributionalLexiconGenerator(@NonNull Embedding wordEmbeddings, @NonNull Multimap<T, String> seedTerms, double threshold) {
      this.wordEmbeddings = wordEmbeddings;
      this.seedTerms.putAll(seedTerms);
      this.threshold = threshold;
   }

   @Override
   public Multimap<T, String> generate() {
      UnifiedSetMultimap<T, String> lexicon = UnifiedSetMultimap.newMultimap();
      if (seedTerms.size() > 0) {
         Map<T, Vector> vectors = new HashMap<>();
         Map<T, Vector> negVectors = new HashMap<>();
         seedTerms.keySet().forEach(tag -> {
            Vector v = new DenseVector(wordEmbeddings.getDimension());
            seedTerms.get(tag).stream()
                     .filter(wordEmbeddings::contains)
                     .forEach(s -> v.addSelf(wordEmbeddings.getVector(s)));
            v.mapDivideSelf(seedTerms.size());
            vectors.put(tag, v);

            Vector negV = new DenseVector(wordEmbeddings.getDimension());
            negativeSeedTerms.get(tag)
                             .stream()
                             .filter(wordEmbeddings::contains)
                             .forEach(s -> negV.addSelf(wordEmbeddings.getVector(s)));
            negVectors.put(tag, negV);
         });
         lexicon.putAll(seedTerms);
         MultiCounter<String, T> scores = MultiCounters.newMultiCounter();
         vectors.forEach((tag, vector) -> {
            wordEmbeddings.nearest(vector, maximumTermCount * 10)
                          .stream()
                          .filter(slv -> !seedTerms.containsValue(slv.getLabel()))
                          .forEach(slv -> {
                             double neg = 0;
                             if (negVectors.get(tag).magnitude() > 0) {
                                neg = Similarity.Cosine.calculate(negVectors.get(tag), slv);
                             }
                             scores.set(slv.getLabel(), tag, slv.getScore() - neg);
                          });
         });
         MultiCounter<T, String> selection = MultiCounters.newMultiCounter();
         scores.firstKeys().forEach(k -> {
            if (!seedTerms.containsValue(k)) {
               T best = scores.get(k).filterByValue(d -> d >= threshold).max();
               selection.set(best, k, scores.get(k, best));
            }
         });
         selection.firstKeys().forEach(k -> lexicon.putAll(k, selection.get(k).topN(maximumTermCount).items()));
      }
      return lexicon;
   }


   public boolean addSeed(@NonNull T tag, String phrase) {
      Preconditions.checkArgument(StringUtils.isNotNullOrBlank(phrase), "Phrase must not be null or blank");
      if (seedTerms.containsValue(phrase)) {
         return false;
      }
      seedTerms.put(tag, phrase);
      return true;
   }

   public boolean addNegativeSeed(@NonNull T tag, String phrase) {
      Preconditions.checkArgument(StringUtils.isNotNullOrBlank(phrase), "Phrase must not be null or blank");
      negativeSeedTerms.put(tag, phrase);
      return true;
   }

   public static void main(String[] args) throws Exception {
      DistributionalLexiconGenerator<StringTag> dlg
         = new DistributionalLexiconGenerator<>(
                                                  Resources.fromFile("/home/ik/prj/o360-hermes/w2v.bin").readObject()
      );
      dlg.setThreshold(0.3);

      dlg.addSeed(new StringTag("POSITIVE"), "good");
      dlg.addSeed(new StringTag("POSITIVE"), "great");
      dlg.addNegativeSeed(new StringTag("POSITIVE"), "bad");
      dlg.addNegativeSeed(new StringTag("POSITIVE"), "terrible");

      dlg.addSeed(new StringTag("NEGATIVE"), "bad");
      dlg.addSeed(new StringTag("NEGATIVE"), "terrible");
      dlg.addNegativeSeed(new StringTag("NEGATIVE"), "good");
      dlg.addNegativeSeed(new StringTag("NEGATIVE"), "great");


      dlg.addSeed(new StringTag("SCENT"), "scent");
      dlg.addSeed(new StringTag("SCENT"), "smell");
      dlg.addSeed(new StringTag("SCENT"), "aroma");
      dlg.addSeed(new StringTag("SCENT"), "fragrance");

      dlg.addSeed(new StringTag("FAMILY"), "family");
      dlg.addSeed(new StringTag("FAMILY"), "wife");
      dlg.addSeed(new StringTag("FAMILY"), "son");
      dlg.addSeed(new StringTag("FAMILY"), "father");
      dlg.addSeed(new StringTag("FAMILY"), "husband");
      dlg.addSeed(new StringTag("FAMILY"), "daughter");
      dlg.addSeed(new StringTag("FAMILY"), "sister");
      dlg.addSeed(new StringTag("FAMILY"), "brother");

      dlg.addSeed(new StringTag("CHEST"), "chest");
      dlg.addSeed(new StringTag("CHEST"), "breast");

      Multimap<StringTag, String> r = dlg.generate();
      r.forEachKey(k -> System.out.println(k + " : " + r.get(k)));
   }


}//END OF DistributionalLexiconGenerator
