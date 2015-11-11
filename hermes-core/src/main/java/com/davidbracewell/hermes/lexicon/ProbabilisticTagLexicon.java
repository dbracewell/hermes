package com.davidbracewell.hermes.lexicon;

import com.davidbracewell.Tag;
import com.davidbracewell.hermes.HString;

/**
 * @author David B. Bracewell
 */
public interface ProbabilisticTagLexicon extends Lexicon {


  double getProbability(HString hString, Tag tag);


}// END OF ProbabilisticTagLexicon
