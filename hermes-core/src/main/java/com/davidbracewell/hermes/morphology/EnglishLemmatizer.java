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

import com.davidbracewell.collection.Collect;
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
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
      reader.forEach(row -> {
        if (row.size() >= 2) {
          String lemma = row.get(0).replace('_', ' ');
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

  public static void main(String[] args) {
    EnglishLemmatizer lt = EnglishLemmatizer.getInstance();
    System.out.println(lt.lemmatize("United States of America"));
    System.out.println(lt.lemmatize("states"));
    System.out.println(lt.contains("United States of America"));
  }

  @Override
  public boolean contains(String string, POS... tags) {
    return doLemmatization(string, false, tags).size() > 0;
  }

  private Set<String> doLemmatization(String word, boolean includeSelf, POS... tags) {
    Set<String> tokenLemmas = new LinkedHashSet<>();
    if (tags == null || tags.length == 0) {
      tags = ALL_POS;
    }

    //Try as is
    for (POS tag : tags) {
      fill(word, tag, tokenLemmas);
    }

    //Try lower case
    if (tokenLemmas.isEmpty()) {
      word = word.toLowerCase();
      for (POS tag : tags) {
        fill(word, tag, tokenLemmas);
      }
    }

    //If all else fails and we should include the word return it
    if (tokenLemmas.isEmpty() && includeSelf) {
      return Collections.singleton(word);
    }

    return tokenLemmas;
  }

  private boolean contains(String string, POS pos) {
    return lemmas.containsKey(string) && (pos == POS.ANY || lemmas.get(string).contains(pos.getUniversalTag()));
  }

  private void fill(String word, POS partOfSpeech, Set<String> set) {
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
  public boolean prefixMatch(@NonNull String word) {
    String lower = word.toLowerCase();
    if (lemmas.containsKey(word) || lemmas.prefixMap(word).size() > 0 || lemmas.containsKey(lower) || lemmas.prefixMap(lower).size() > 0) {
      return true;
    }
    return getAllLemmas(word, POS.ANY).size() > 1;
  }

  @Override
  public List<String> getAllLemmas(@NonNull String word, @NonNull POS partOfSpeech) {
    List<String> lemmas = null;
    if (partOfSpeech == POS.ANY) {
      lemmas = Lists.newArrayList(doLemmatization(word, true, POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB));
    } else if (partOfSpeech.isInstance(POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB)) {
      lemmas = Lists.newArrayList(doLemmatization(word, true, partOfSpeech));
    }
    if (lemmas == null || lemmas.isEmpty()) {
      lemmas = Collections.emptyList();
    }
    return lemmas;
  }

  @Override
  public Set<String> getPrefixedLemmas(@NonNull String string, @NonNull POS partOfSpeech) {
    Set<String> lemmaSet = new LinkedHashSet<>();
    for (String lemma : doLemmatization(string, false, partOfSpeech)) {
      lemmaSet.add(lemma);
      lemmaSet.addAll(lemmas.prefixMap(lemma + " ").keySet());
    }
    return lemmaSet;
  }

  @Override
  public String lemmatize(@NonNull String string, @NonNull POS partOfSpeech) {
    if (partOfSpeech == POS.ANY) {
      return Collect.from(doLemmatization(string, true, ALL_POS)).findFirst().orElse(string).toLowerCase();
    } else if (partOfSpeech.isInstance(ALL_POS)) {
      return Collect.from(doLemmatization(string, true, partOfSpeech)).findFirst().orElse(string).toLowerCase();
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
