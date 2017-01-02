package com.davidbracewell.hermes.lexicon;

import com.davidbracewell.Tag;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.HString;
import lombok.Value;

import java.io.Serializable;

/**
 * The type Lexicon entry.
 *
 * @author David B. Bracewell
 */
@Value
public class LexiconEntry implements Serializable, Comparable<LexiconEntry> {
   private static final long serialVersionUID = 1L;
   String lemma;
   double probability;
   SerializablePredicate<HString> constraint;
   Tag tag;


   @Override
   public int compareTo(LexiconEntry o) {
      int d = Double.compare(getProbability(), o.getProbability());
      if (d == 0) {
         d = Double.compare(getLemma().length(), o.getLemma().length());
      }
      return -d;
   }
}// END OF LexiconEntry
