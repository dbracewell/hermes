package com.davidbracewell.hermes.ml.feature;

import com.davidbracewell.apollo.linear.NDArray;
import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.apollo.ml.featurizer.Featurizer;
import com.davidbracewell.collection.Streams;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.LanguageData;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Word embedding featurizer.
 *
 * @author David B. Bracewell
 */
public class WordEmbeddingFeaturizer implements Featurizer<HString> {
   private static final long serialVersionUID = 1L;

   @Override
   public List<Feature> apply(HString hString) {
      Embedding embedding = LanguageData.getDefaultEmbedding(hString.getLanguage());
      NDArray vector = null;

      if (embedding.contains(hString.toString())) {
         vector = embedding.get(hString.toString());
      } else if (embedding.contains(hString.toLowerCase())) {
         vector = embedding.get(hString.toLowerCase());
      } else if (embedding.contains(hString.getLemma())) {
         vector = embedding.get(hString.getLemma());
      } else if (embedding.contains(hString.getPOS().getUniversalTag().name())) {
         vector = embedding.get(hString.getPOS().getUniversalTag().name());
      }

      if (vector == null) {
         return Collections.emptyList();
      }

      return Streams.asStream(vector.sparseIterator())
                    .map(e -> Feature.real("embedding-dimension-" + e.getIndex(), e.getValue()))
                    .collect(Collectors.toList());
   }

}// END OF WordEmbeddingFeaturizer
