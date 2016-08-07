package com.davidbracewell.hermes;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.util.Optional;

/**
 * The type Relation.
 *
 * @author David B. Bracewell
 */
@Data
public class Relation implements Serializable {
  private static final long serialVersionUID = 1L;
  private final RelationType type;
  @Getter
  @Setter
  private String value;
  private final long target;

  public Relation(RelationType type, String value, long target) {
    this.type = type;
    this.value = value;
    this.target = target;
  }

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
