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

import com.davidbracewell.collection.trie.PatriciaTrie;
import com.davidbracewell.hermes.tag.POS;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.structured.csv.CSVReader;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * The type English lemmatizer.
 *
 * @author David B. Bracewell
 */
public class EnglishLemmatizer implements Lemmatizer, Serializable {
  private static final long serialVersionUID = -6093027604295026727L;
  private static EnglishLemmatizer INSTANCE = null;
  private final Multimap<POS, DetachmentRule> rules = ArrayListMultimap.create();
  private final Multimap<Tuple2<POS, String>, String> exceptions = LinkedHashMultimap.create();
  private final PatriciaTrie<Set<POS>> lemmas;

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

    this.lemmas = new PatriciaTrie<>();
    try (CSVReader reader = CSV.builder().delimiter('\t').reader(Resources.fromClasspath("com/davidbracewell/hermes/morphology/en/lemmas.dict.gz"))) {
      for (List<String> row : reader) {
        if (row.size() >= 2) {
          String lemma = row.get(0).toLowerCase();
          POS pos = POS.fromString(row.get(1).toUpperCase());
          if (!lemmas.containsKey(lemma)) {
            lemmas.put(lemma, new HashSet<>());
          }
          lemmas.get(lemma).add(pos);
        }
      }
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

  private Iterable<String> doLemmatization(String string, POS... tags) {
    Set<String> tokenLemmas = new LinkedHashSet<>();
    for (POS tag : tags) {
      fill(string, tag, tokenLemmas);
    }
    if (tokenLemmas.isEmpty()) {
      string = string.toLowerCase();
      for (POS tag : tags) {
        fill(string, tag, tokenLemmas);
      }
    }
    if (tokenLemmas.isEmpty()) {
      return Collections.singleton(string);
    }

    return tokenLemmas;
  }

  private void fill(String string, POS partOfSpeech, Set<String> set) {
    if (partOfSpeech.isVerb()) {
      if (string.equalsIgnoreCase("'s") || string.equalsIgnoreCase("'re")) {
        set.add("be");
        return;
      } else if (string.equals("'ll")) {
        set.add("will");
        return;
      } else if (string.equals("'ve")) {
        set.add("will");
        return;
      }
    } else if (partOfSpeech.isAdverb()) {
      if (string.equalsIgnoreCase("n't")) {
        set.add("not");
        return;
      }
    } else if (string.equalsIgnoreCase("'d")) {
      set.add("would");
      return;
    }


    Tuple2<POS, String> key = Tuple2.of(partOfSpeech.getUniversalTag(), string.toLowerCase());
    if (exceptions.containsKey(key)) {
      set.addAll(exceptions.get(key));
    }
    for (DetachmentRule rule : rules.get(partOfSpeech.getUniversalTag())) {
      String output = rule.apply(string);
      if (output != null && lemmas.containsKey(output) && lemmas.get(output).contains(partOfSpeech.getUniversalTag())) {
        set.add(output);
      }
    }
  }

  @Override
  public String lemmatize(@NonNull String string, @NonNull POS partOfSpeech) {
    if (partOfSpeech == POS.ANY) {
      return Iterables.getFirst(doLemmatization(string, POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB), string).toLowerCase();
    } else if (partOfSpeech.isInstance(POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB)) {
      return Iterables.getFirst(doLemmatization(string, partOfSpeech), string).toLowerCase();
    }
    return string.toLowerCase();
  }

  private void loadException(POS tag) {
    try {
      for (String line :
        Resources.fromClasspath("com/davidbracewell/hermes/morphology/en").getChild(tag.asString().toLowerCase() + ".exc").readLines()) {
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
