package com.davidbracewell.hermes.lexicon;

import com.davidbracewell.Tag;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.HString;
import lombok.Value;

import java.io.Serializable;

/**
 * @author David B. Bracewell
 */
@Value
public class LexiconEntry implements Serializable {
  private static final long serialVersionUID = 1L;
  String lemma;
  double probability;
  SerializablePredicate<HString> constraint;
  Tag tag;
}// END OF LexiconEntry
