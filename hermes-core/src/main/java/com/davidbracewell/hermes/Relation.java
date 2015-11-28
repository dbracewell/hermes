package com.davidbracewell.hermes;

import com.davidbracewell.hermes.tag.RelationType;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.Optional;

/**
 * The type Relation.
 *
 * @author David B. Bracewell
 */
@Value
public class Relation implements Serializable {
  private static final long serialVersionUID = 1L;
  private final RelationType type;
  private final String value;
  private final long target;


  /**
   * Gets target.
   *
   * @param hString the h string
   * @return the target
   */
  public Optional<Annotation> getTarget(@NonNull HString hString) {
    return hString.document().getAnnotation(target);
  }

}// END OF Relation
