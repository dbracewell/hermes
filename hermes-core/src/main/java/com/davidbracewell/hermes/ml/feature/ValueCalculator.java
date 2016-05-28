package com.davidbracewell.hermes.ml.feature;

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.collection.Counter;
import com.davidbracewell.function.SerializableFunction;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public enum ValueCalculator implements SerializableFunction<Counter<String>, Set<Feature>> {

  Binary {
    @Override
    public Set<Feature> apply(Counter<String> stringCounter) {
      return stringCounter.items().stream().map(Feature::TRUE).collect(Collectors.toSet());
    }
  },
  Frequency {
    @Override
    public Set<Feature> apply(Counter<String> stringCounter) {
      return stringCounter.entries().stream()
        .map(entry -> Feature.real(entry.getKey(), entry.getValue()))
        .collect(Collectors.toSet());
    }
  },
  L1_NORM {
    @Override
    public Set<Feature> apply(Counter<String> stringCounter) {
      return stringCounter.divideBySum().entries().stream()
        .map(entry -> Feature.real(entry.getKey(), entry.getValue()))
        .collect(Collectors.toSet());
    }
  }


}// END OF ValueCalculator
