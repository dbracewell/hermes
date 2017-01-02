/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.davidbracewell.hermes.morphology;

import com.davidbracewell.collection.Streams;
import com.davidbracewell.collection.Trie;
import com.davidbracewell.guava.common.base.Function;
import com.davidbracewell.guava.common.base.Strings;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.guava.common.collect.ArrayListMultimap;
import com.davidbracewell.guava.common.collect.LinkedHashMultimap;
import com.davidbracewell.guava.common.collect.Maps;
import com.davidbracewell.guava.common.collect.Multimap;
import com.davidbracewell.hermes.attribute.POS;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.CSVReader;
import com.davidbracewell.io.Resources;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The type English lemmatizer.
 *
 * @author David B. Bracewell
 */
public class EnglishLemmatizer implements Lemmatizer, Serializable {
   private static final long serialVersionUID = -6093027604295026727L;
   private static final POS[] ALL_POS = {POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB};
   private static EnglishLemmatizer INSTANCE = null;
   private final Multimap<POS, DetachmentRule> rules = ArrayListMultimap.create();
   private final Multimap<Tuple2<POS, String>, String> exceptions = LinkedHashMultimap.create();
   private final Trie<Set<POS>> lemmas;

   /**
    * Instantiates a new English lemmatizer.
    */
   protected EnglishLemmatizer() {
      rules.put(POS.NOUN, new DetachmentRule("s", ""));
      rules.put(POS.NOUN, new DetachmentRule("ses", "s"));
      rules.put(POS.NOUN, new DetachmentRule("xes", "x"));
      rules.put(POS.NOUN, new DetachmentRule("zes", "z"));
      rules.put(POS.NOUN, new DetachmentRule("ies", "y"));
      rules.put(POS.NOUN, new DetachmentRule("shes", "sh"));
      rules.put(POS.NOUN, new DetachmentRule("ches", "ch"));
      rules.put(POS.NOUN, new DetachmentRule("men", "man"));
      loadException(POS.NOUN);

      rules.put(POS.VERB, new DetachmentRule("s", ""));
      rules.put(POS.VERB, new DetachmentRule("ies", "y"));
      rules.put(POS.VERB, new DetachmentRule("es", "s"));
      rules.put(POS.VERB, new DetachmentRule("es", ""));
      rules.put(POS.VERB, new DetachmentRule("ed", "e"));
      rules.put(POS.VERB, new DetachmentRule("ed", ""));
      rules.put(POS.VERB, new DetachmentRule("ing", "e"));
      rules.put(POS.VERB, new DetachmentRule("ing", ""));
      loadException(POS.VERB);

      rules.put(POS.ADJECTIVE, new DetachmentRule("er", ""));
      rules.put(POS.ADJECTIVE, new DetachmentRule("est", ""));
      rules.put(POS.ADJECTIVE, new DetachmentRule("er", "e"));
      rules.put(POS.ADJECTIVE, new DetachmentRule("est", "e"));
      loadException(POS.ADJECTIVE);

      loadException(POS.ADVERB);

      this.lemmas = new Trie<>();
      try (CSVReader reader = CSV.builder()
                                 .delimiter('\t')
                                 .reader(Resources.fromClasspath(
                                       "com/davidbracewell/hermes/morphology/en/lemmas.dict.gz"))) {
         reader.forEach(row -> {
            if (row.size() >= 2) {
               String lemma = row.get(0).replace('_', ' ').toLowerCase();
               POS pos = POS.fromString(row.get(1).toUpperCase());
               if (!lemmas.containsKey(lemma)) {
                  lemmas.put(lemma, new HashSet<>());
               }
               lemmas.get(lemma).add(pos);
            }
         });
      } catch (Exception e) {
         throw Throwables.propagate(e);
      }
   }

   /**
    * Gets instance.
    *
    * @return the instance
    */
   public static EnglishLemmatizer getInstance() {
      if (INSTANCE == null) {
         synchronized (EnglishLemmatizer.class) {
            if (INSTANCE != null) {
               return INSTANCE;
            }
            INSTANCE = new EnglishLemmatizer();
         }
      }
      return INSTANCE;
   }

   private static Pattern WHITESPACE = Pattern.compile(StringUtils.MULTIPLE_WHITESPACE);

   private Set<String> doLemmatization(String word, boolean includeSelf, POS... tags) {
      Set<String> tokenLemmas = new LinkedHashSet<>();

      if (tags == null || tags.length == 0 || tags[0] == POS.ANY) {
         tags = ALL_POS;
      }

      word = word.toLowerCase();
      for (POS tag : tags) {
         fill(word, tag, tokenLemmas);
      }

      if (tokenLemmas.isEmpty() && word.contains("-")) {
         String noHyphen = word.replace('-', ' ');
         for (POS tag : tags) {
            fill(noHyphen, tag, tokenLemmas);
         }
      }

      if (tokenLemmas.isEmpty() && word.contains(" ")) {
         String withHyphen = word.replace(' ', '-');
         for (POS tag : tags) {
            fill(withHyphen, tag, tokenLemmas);
         }
      }

      if (tokenLemmas.isEmpty() && WHITESPACE.matcher(word).find()) {
         tokenLemmas.addAll(phraseLemmas(word, tags));
      }

      //If all else fails and we should include the word return it
      if (tokenLemmas.isEmpty() && includeSelf) {
         return Collections.singleton(word.toLowerCase());
      }

      return tokenLemmas;
   }

   private boolean contains(String string, POS pos) {
      return lemmas.containsKey(string) && (pos == POS.ANY || lemmas.get(string).contains(pos.getUniversalTag()));
   }

   private void fill(String word, POS partOfSpeech, Set<String> set) {
      word = word.toLowerCase();
      //Word is already a lemma with the given part of speech
      if (contains(word, partOfSpeech.getUniversalTag())) {
         set.add(word);
         return;
      }

      if (partOfSpeech.isVerb()) {
         if (word.equalsIgnoreCase("'s") || word.equalsIgnoreCase("'re")) {
            set.add("be");
            return;
         } else if (word.equals("'ll")) {
            set.add("will");
            return;
         } else if (word.equals("'ve")) {
            set.add("will");
            return;
         }
      } else if (partOfSpeech.isAdverb()) {
         if (word.equalsIgnoreCase("n't")) {
            set.add("not");
            return;
         }
      } else if (word.equalsIgnoreCase("'d")) {
         set.add("would");
         return;
      }

      //Apply the exceptions
      Tuple2<POS, String> key = Tuple2.of(partOfSpeech.getUniversalTag(), word.toLowerCase());
      if (exceptions.containsKey(key)) {
         set.addAll(exceptions.get(key));
      }

      //Apply the rules
      for (DetachmentRule rule : rules.get(partOfSpeech.getUniversalTag())) {
         String output = rule.apply(word);
         if (contains(output, partOfSpeech.getUniversalTag())) {
            set.add(output);
         }
      }
   }

   @Override
   public List<String> allPossibleLemmas(@NonNull String word, @NonNull POS partOfSpeech) {
      List<String> lemmaList = null;
      if (partOfSpeech == POS.ANY) {
         lemmaList = new ArrayList<>(doLemmatization(word, true, POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB));
      } else if (partOfSpeech.isInstance(POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB)) {
         lemmaList = new ArrayList<>(doLemmatization(word, true, partOfSpeech));
      }
      if (lemmaList == null || lemmaList.isEmpty()) {
         lemmaList = Collections.singletonList(word.toLowerCase());
      }
      return lemmaList;
   }

   @Override
   public Trie<String> allPossibleLemmasAndPrefixes(@NonNull String string, @NonNull POS partOfSpeech) {
      Trie<String> lemmaSet = new Trie<>();
      for (String lemma : doLemmatization(string, true, partOfSpeech)) {
         lemmaSet.putAll(Maps.asMap(lemmas.prefix(lemma + " ").keySet(), k -> k));
         if (lemmas.containsKey(lemma)) {
            lemmaSet.put(lemma, lemma);
         }
      }
      return lemmaSet;
   }

   private Set<String> allAndSelf(String word) {
      Set<String> lemmas = new HashSet<>(doLemmatization(word, true, POS.ANY));
      lemmas.add(word);
      return lemmas;
   }

   private boolean hasPOS(String lemma, POS... tags) {
      if (tags == null || tags.length == 0 || tags[0] == POS.ANY) {
         return this.lemmas.containsKey(lemma);
      }
      for (POS pos : this.lemmas.getOrDefault(lemma, Collections.emptySet())) {
         if (pos.isInstance(tags)) {
            return true;
         }
      }
      return false;
   }

   private Set<String> phraseLemmas(String phrase, POS... tags) {
      String[] words = phrase.split("\\s+");
      if (words.length == 0) {
         return Collections.emptySet();
      }
      if (tags == null || tags.length == 0 || tags[0] == POS.ANY) {
         tags = ALL_POS;
      }
      Trie<String> prefixes = allPossibleLemmasAndPrefixes(words[0], POS.ANY);
      Set<String> lemmas = allAndSelf(words[0]);
      for (int i = 1; i < words.length; i++) {
         Set<String> nextSet = new HashSet<>();
         for (String previous : lemmas) {
            for (String next : allAndSelf(words[i])) {
               String subPhrase = previous + " " + next;
               if (prefixes.containsKey(subPhrase)) {
                  nextSet.add(subPhrase);
               } else if (prefixes.prefix(subPhrase).size() > 0) {
                  nextSet.add(subPhrase);
               }
            }
         }
         if (nextSet.isEmpty()) {
            return Collections.emptySet();
         }
         lemmas = nextSet;
      }

      final POS[] target = tags;
      lemmas = lemmas.stream().filter(lemma -> hasPOS(lemma, target)).collect(Collectors.toSet());
      return lemmas;
   }


   @Override
   public boolean canLemmatize(String input, POS partOfSpeech) {
      return partOfSpeech.isInstance(POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB) && doLemmatization(input,
                                                                                                       false,
                                                                                                       partOfSpeech
                                                                                                      ).size() > 0;
   }

   @Override
   public String lemmatize(@NonNull String string, @NonNull POS partOfSpeech) {
      if (partOfSpeech == POS.ANY) {
         return Streams.asStream(doLemmatization(string, true, ALL_POS)).findFirst().orElse(string).toLowerCase();
      } else if (partOfSpeech.isInstance(ALL_POS)) {
         return Streams.asStream(doLemmatization(string, true, partOfSpeech)).findFirst().orElse(string).toLowerCase();
      }
      return string.toLowerCase();
   }

   private void loadException(POS tag) {
      try {
         for (String line :
               Resources.fromClasspath("com/davidbracewell/hermes/morphology/en")
                        .getChild(tag.asString().toLowerCase() + ".exc")
                        .readLines()) {
            if (!Strings.isNullOrEmpty(line)) {
               String[] parts = line.split("\\s+");
               Tuple2<POS, String> key = Tuple2.of(tag.getUniversalTag(), parts[0].replaceAll("_", " "));
               for (int i = 1; i < parts.length; i++) {
                  exceptions.put(key, parts[i]);
               }
            }
         }
      } catch (IOException e) {
         throw Throwables.propagate(e);
      }
   }

   private static class DetachmentRule implements Serializable, Function<String, String> {
      private static final long serialVersionUID = 2748362312310767937L;
      /**
       * The Ending.
       */
      public final String ending;
      /**
       * The Replacement.
       */
      public final String replacement;

      private DetachmentRule(String ending, String replacement) {
         this.ending = ending;
         this.replacement = replacement;
      }


      @Override
      public String apply(String input) {
         if (input == null) {
            return null;
         }
         if (input.endsWith(ending)) {
            int end = input.length() - ending.length();
            if (end == 0) {
               return replacement;
            }
            return input.substring(0, end) + replacement;
         }
         return input;
      }

      /**
       * Unapply string.
       *
       * @param input the input
       * @return the string
       */

      public String unapply(String input) {
         if (input == null) {
            return null;
         }
         if (input.endsWith(replacement)) {
            int end = input.length() - replacement.length();
            if (end == 0) {
               return ending;
            }
            return input.substring(0, end) + ending;
         }
         return input;
      }
   }

}//END OF EnglishLemmatizer
