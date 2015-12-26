package com.davidbracewell.hermes.ml.feature;

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.Featurizer;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.stream.Streams;
import lombok.NonNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Bag of annotation.
 *
 * @author David B. Bracewell
 */
public class BagOfAnnotation implements Featurizer<HString> {

  private final AnnotationType type;
  private final SerializableFunction<HString, String> toStringFunction;
  private final SerializablePredicate<HString> predicate;
  private final boolean binary;

  /**
   * Instantiates a new Bag of annotation.
   *
   * @param type             the type
   * @param toStringFunction the to string function
   * @param predicate        the predicate
   * @param binary           the binary
   */
  public BagOfAnnotation(@NonNull AnnotationType type, @NonNull SerializableFunction<HString, String> toStringFunction, @NonNull SerializablePredicate<HString> predicate, boolean binary) {
    this.type = type;
    this.toStringFunction = toStringFunction;
    this.predicate = predicate;
    this.binary = binary;
  }

  /**
   * Binary featurizer.
   *
   * @param type             the type
   * @param toStringFunction the to string function
   * @param predicate        the predicate
   * @return the featurizer
   */
  public static Featurizer<HString> binary(@NonNull AnnotationType type, @NonNull SerializableFunction<HString, String> toStringFunction, @NonNull SerializablePredicate<HString> predicate) {
    return new BagOfAnnotation(type, toStringFunction, predicate, true);
  }

  /**
   * Frequency featurizer.
   *
   * @param type             the type
   * @param toStringFunction the to string function
   * @param predicate        the predicate
   * @return the featurizer
   */
  public static Featurizer<HString> frequency(@NonNull AnnotationType type, @NonNull SerializableFunction<HString, String> toStringFunction, @NonNull SerializablePredicate<HString> predicate) {
    return new BagOfAnnotation(type, toStringFunction, predicate, false);
  }


  @Override
  public Set<Feature> apply(HString hString) {
    MStream<String> stream = Streams.of(
      hString.get(type).stream()
        .filter(predicate)
        .map(toStringFunction)
    );

    if (binary) {
      stream = stream.distinct();
    }

    return stream.countByValue().entrySet().stream()
      .map(e -> Feature.real(e.getKey(), e.getValue()))
      .collect(Collectors.toSet());
  }


}// END OF BagOfAnnotation
