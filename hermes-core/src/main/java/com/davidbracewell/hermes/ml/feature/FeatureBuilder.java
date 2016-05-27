package com.davidbracewell.hermes.ml.feature;

import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.Types;
import lombok.Data;

/**
 * @author David B. Bracewell
 */
@Data
public abstract class FeatureBuilder {

  private AnnotationType annotationType = Types.TOKEN;
  private SerializableFunction<HString, String> toStringFunction = HString::toString;
  private SerializablePredicate<HString> filter = hString -> false;


}// END OF FeatureBuilder
