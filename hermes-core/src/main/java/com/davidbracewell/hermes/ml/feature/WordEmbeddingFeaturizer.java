package com.davidbracewell.hermes.ml.feature;

import com.davidbracewell.apollo.linalg.Vector;
import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.Featurizer;
import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.collection.Streams;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.LanguageData;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Word embedding featurizer.
 *
 * @author David B. Bracewell
 */
public class WordEmbeddingFeaturizer implements Featurizer<HString> {
   private static final long serialVersionUID = 1L;

   @Override
   public Set<Feature> apply(HString hString) {
      Embedding embedding = LanguageData.getDefaultEmbeddingModel(hString.getLanguage());
      Vector vector = null;
      if (embedding.contains(hString.toString())) {
         vector = embedding.getVector(hString.toString());
      } else if (embedding.contains(hString.toLowerCase())) {
         vector = embedding.getVector(hString.toLowerCase());
      } else if (embedding.contains(hString.getLemma())) {
         vector = embedding.getVector(hString.getLemma());
      }

      if (vector == null) {
         return Collections.emptySet();
      }

      return Streams.asStream(vector.nonZeroIterator())
                    .map(e -> Feature.real("embedding-dimension-" + e.getIndex(), e.getValue()))
                    .collect(Collectors.toSet());
   }

}// END OF WordEmbeddingFeaturizer
