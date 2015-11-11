package com.davidbracewell.hermes.lexicon;

/**
 * @author David B. Bracewell
 */
public interface MutableLexicon {

  void add(String lexicalItem);

  default void addAll(Iterable<String> lexicalItems) {
    if (lexicalItems != null) {
      lexicalItems.forEach(this::add);
    }
  }

  void remove(String lexicalItem);

}//END OF MutableLexicon
