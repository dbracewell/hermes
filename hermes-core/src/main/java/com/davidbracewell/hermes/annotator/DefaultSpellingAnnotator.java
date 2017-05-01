package com.davidbracewell.hermes.annotator;

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.lexicon.TrieWordList;
import com.davidbracewell.io.Resources;
import com.davidbracewell.string.StringPredicates;
import com.google.common.base.Throwables;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.davidbracewell.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
public class DefaultSpellingAnnotator implements Annotator {
   private TrieWordList dictionary = null;
   private int maxCost;

   @Override
   public void annotate(Document document) {
      TrieWordList dictionary = loadDictionary();
      document.tokenStream()
              .filter(StringPredicates.HAS_LETTER)
              .forEach(token -> {
                 if (!dictionary.contains(token)) {
                    Map<String, Integer> suggestions = dictionary.suggest(token.toString(), maxCost, 5);
                    suggestions.entrySet()
                               .stream()
                               .map(e -> {
                                  double diff = Math.pow(token.length() - e.getKey().length(), 2);
                                  double fudge = token.charAt(0) != e.getKey().charAt(0) ? 0.5 : 0;
                                  return $(e.getKey(), e.getValue() + diff + fudge);
                               })
                               .sorted(Map.Entry.comparingByValue())
                               .findFirst()
                               .map(Map.Entry::getKey)
                               .ifPresent(correction -> token.put(Types.SPELLING_CORRECTION, correction));
                 }
              });
   }

   private TrieWordList loadDictionary() {
      if (dictionary == null) {
         synchronized (this) {
            if (dictionary == null) {
               try {
                  if (Config.hasProperty("DefaultSpellingAnnotator", "dictionary")) {
                     dictionary = TrieWordList.read(Config
                                                       .get("DefaultSpellingAnnotator", "dictionary")
                                                       .asResource(), true);
                  } else {
                     dictionary = TrieWordList.read(Resources.fromString(), true);
                  }
                  this.maxCost = Config.get("DefaultSpellingAnnotator", "maxCost")
                                       .asIntegerValue(2);
               } catch (Exception e) {
                  throw Throwables.propagate(e);
               }
            }
         }
      }
      return dictionary;
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.SPELLING_CORRECTION);
   }

   @Override
   public Set<AnnotatableType> requires() {
      return Collections.singleton(Types.TOKEN);
   }

}//END OF DefaultSpellingAnnotator
