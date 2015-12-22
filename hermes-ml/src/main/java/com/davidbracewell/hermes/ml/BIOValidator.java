package com.davidbracewell.hermes.ml;

import com.davidbracewell.apollo.ml.sequence.SequenceValidator;

/**
 * @author David B. Bracewell
 */
public class BIOValidator implements SequenceValidator {
  private static final long serialVersionUID = 1L;

  @Override
  public boolean isValid(String label, String previousLabel) {
    if (label.startsWith("I-")) {
      if (previousLabel == null) {
        return false;
      }
      if (previousLabel.startsWith("O")) {
        return false;
      }
      if (previousLabel.startsWith("I-") && !label.equals(previousLabel)) {
        return false;
      }
      if (previousLabel.startsWith("B-") && !label.substring(2).equals(previousLabel.substring(2))) {
        return false;
      }
    }
    return true;
  }
}// END OF BIOValidator
