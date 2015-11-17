package com.davidbracewell.hermes;

import com.davidbracewell.hermes.tag.RelationType;
import lombok.Value;

import java.io.Serializable;

/**
 * @author David B. Bracewell
 */
@Value
public class Relation implements Serializable {
  private final RelationType type;
  private final String value;
  private final long target;
}// END OF Relation
