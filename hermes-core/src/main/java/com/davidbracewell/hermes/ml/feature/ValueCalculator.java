package com.davidbracewell.hermes.ml.feature;

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.function.SerializableFunction;
import lombok.NonNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The enum Value calculator.
 *
 * @author David B. Bracewell
 */
public enum ValueCalculator implements SerializableFunction<Counter<String>, Set<Feature>> {
   /**
    * The Binary.
    */
   Binary {
      @Override
      public <T> Counter<T> adjust(Counter<T> counter) {
         return counter.adjustValuesSelf(d -> 1);
      }
   },
   /**
    * The Frequency.
    */
   Frequency {
      @Override
      public <T> Counter<T> adjust(Counter<T> counter) {
         return counter;
      }
   },
   /**
    * The L 1 norm.
    */
   L1_NORM {
      @Override
      public <T> Counter<T> adjust(Counter<T> counter) {
         return counter.divideBySum();
      }
   };


   /**
    * Adjust counter.
    *
    * @param <T>     the type parameter
    * @param counter the counter
    * @return the counter
    */
   public abstract <T> Counter<T> adjust(Counter<T> counter);

   @Override
   public Set<Feature> apply(@NonNull Counter<String> stringCounter) {
      return adjust(stringCounter).entries().stream()
                                  .map(entry -> Feature.real(entry.getKey(), entry.getValue()))
                                  .collect(Collectors.toSet());
   }

}// END OF ValueCalculator
