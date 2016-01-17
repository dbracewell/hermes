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

package com.davidbracewell.hermes.wordnet;

import com.davidbracewell.collection.trie.PatriciaTrie;
import com.davidbracewell.config.Config;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author David B. Bracewell
 */
public class WordNetLemmatizer {
  private static WordNetLemmatizer INSTANCE = null;
  private final Multimap<WordNetPOS, DetachmentRule> rules = ArrayListMultimap.create();
  private final Multimap<Tuple2<WordNetPOS, String>, String> exceptions = HashMultimap.create();
  private final PatriciaTrie<Boolean> lemmas;

  /**
   * Instantiates a new English lemmatizer.
   */
  protected WordNetLemmatizer() {
    rules.put(WordNetPOS.NOUN, new DetachmentRule("s", ""));
    rules.put(WordNetPOS.NOUN, new DetachmentRule("ses", "s"));
    rules.put(WordNetPOS.NOUN, new DetachmentRule("xes", "x"));
    rules.put(WordNetPOS.NOUN, new DetachmentRule("zes", "z"));
    rules.put(WordNetPOS.NOUN, new DetachmentRule("ies", "y"));
    rules.put(WordNetPOS.NOUN, new DetachmentRule("shes", "sh"));
    rules.put(WordNetPOS.NOUN, new DetachmentRule("ches", "ch"));
    rules.put(WordNetPOS.NOUN, new DetachmentRule("men", "man"));
    loadException(WordNetPOS.NOUN);

    rules.put(WordNetPOS.VERB, new DetachmentRule("s", ""));
    rules.put(WordNetPOS.VERB, new DetachmentRule("ies", "y"));
    rules.put(WordNetPOS.VERB, new DetachmentRule("es", "s"));
    rules.put(WordNetPOS.VERB, new DetachmentRule("es", ""));
    rules.put(WordNetPOS.VERB, new DetachmentRule("ed", "e"));
    rules.put(WordNetPOS.VERB, new DetachmentRule("ed", ""));
    rules.put(WordNetPOS.VERB, new DetachmentRule("ing", "e"));
    rules.put(WordNetPOS.VERB, new DetachmentRule("ing", ""));
    loadException(WordNetPOS.VERB);

    rules.put(WordNetPOS.ADJECTIVE, new DetachmentRule("er", ""));
    rules.put(WordNetPOS.ADJECTIVE, new DetachmentRule("est", ""));
    rules.put(WordNetPOS.ADJECTIVE, new DetachmentRule("er", "e"));
    rules.put(WordNetPOS.ADJECTIVE, new DetachmentRule("est", "e"));
    loadException(WordNetPOS.ADJECTIVE);

    loadException(WordNetPOS.ADVERB);

    this.lemmas = new PatriciaTrie<>();
    try (MStream<String> stream = Config.get(WordNetLemmatizer.class, "dictionary").asResource().lines()) {
      stream.forEach(line -> {
        if (!Strings.isNullOrEmpty(line) && !line.trim().startsWith("#")) {
          lemmas.put(line.trim().toLowerCase(), true);
        }
      });
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static WordNetLemmatizer getInstance() {
    if (INSTANCE == null) {
      synchronized (WordNetLemmatizer.class) {
        if (INSTANCE != null) {
          return INSTANCE;
        }
        INSTANCE = new WordNetLemmatizer();
      }
    }
    return INSTANCE;
  }

  private void loadException(WordNetPOS tag) {
    try {
      for (String line :
        Config.get(WordNetLemmatizer.class, "exceptions").asResource()
          .getChild(tag.getShortForm().toLowerCase() + ".exc").readLines()) {
        if (!Strings.isNullOrEmpty(line)) {
          String[] parts = line.split("\\s+");
          Tuple2<WordNetPOS, String> key = Tuple2.of(tag, parts[0].replaceAll("_", " "));
          for (int i = 1; i < parts.length; i++) {
            exceptions.put(key, parts[i]);
          }
        }
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }


  public Set<String> getPrefixBaseForm(String string, WordNetPOS partOfSpeech) {
    Set<String> lemmaSet = Sets.newHashSet();
    for (String lemma : doLemmatization(string, partOfSpeech)) {
      lemmaSet.add(lemma);
      lemmaSet.addAll(lemmas.prefixMap(string + "_").keySet());
    }
    return lemmaSet;
  }

  public boolean isLemma(String word) {
    return lemmas.containsKey(word.toLowerCase());
  }

  private String normalize(String input) {
    return input.toLowerCase().trim().replaceAll("\\-+", " - ").replaceAll("\\s+", "_");
  }

  private Collection<String> singletTokenLemmatizer(String string, WordNetPOS... tags) {
    Set<String> tokenLemmas = Sets.newHashSet();
    for (WordNetPOS tag : tags) {
      fill(string, tag, tokenLemmas);
    }

    if (tokenLemmas.isEmpty()) {
      return Sets.newHashSet(string);
    }

    return tokenLemmas;
  }

  private Collection<String> doLemmatization(String string, WordNetPOS... tags) {
    String normalized = normalize(string);
    if (normalized.contains("_")) {

      //Get all possible lemmas
      List<Collection<String>> phraseLemmas = new ArrayList<>();
      for (String token : normalized.split("_")) {
        Collection<String> tl = singletTokenLemmatizer(token, tags);
        tl.add(token);
        phraseLemmas.add(tl);
      }

      List<StringBuilder> phrases = new LinkedList<>();
      for (String token : phraseLemmas.get(0)) {
        if (this.lemmas.prefixMap(token).isEmpty()) {
          continue;
        }
        phrases.add(new StringBuilder(token));
      }

      if (phrases.isEmpty()) {
        Collections.emptySet();
      }

      for (int i = 1; i < phraseLemmas.size(); i++) {
        List<StringBuilder> newBuilders = new ArrayList<>();
        for (String token : phraseLemmas.get(i)) {
          for (StringBuilder builder : phrases) {
            if (token.equals("-")) {
              newBuilders.add(new StringBuilder(builder));
              builder.append("-");
            } else {
              if (builder.charAt(builder.length() - 1) != '-') {
                builder.append("_");
              }
              builder.append(token);
            }
            if (this.lemmas.prefixMap(builder.toString()).size() > 0 || this.lemmas.containsKey(builder.toString())) {
              newBuilders.add(new StringBuilder(builder));
            }
          }
        }

        phrases = newBuilders;
      }

      Set<String> finalLemmas = new HashSet<>();
      for (StringBuilder phrase : phrases) {
        if (isLemma(phrase.toString())) {
          finalLemmas.add(phrase.toString().replace("_", " "));
        }
      }

      if (finalLemmas.isEmpty()) {
        finalLemmas.add(string);
      }

      return finalLemmas;
    } else {
      return singletTokenLemmatizer(normalized, tags);
    }
  }

  public Iterable<String> getBaseForm(String string) {
    Preconditions.checkNotNull(string);
    return doLemmatization(string, WordNetPOS.NOUN, WordNetPOS.ADJECTIVE, WordNetPOS.VERB);
  }

  public Iterable<String> getBaseForm(String string, WordNetPOS partOfSpeech) {
    Preconditions.checkNotNull(string);
    Preconditions.checkNotNull(partOfSpeech);
    return doLemmatization(string, partOfSpeech);
  }

  private void fill(String string, WordNetPOS partOfSpeech, Set<String> set) {
    Tuple2<WordNetPOS, String> key = Tuple2.of(partOfSpeech, string.toLowerCase());
    if (exceptions.containsKey(key)) {
      set.addAll(exceptions.get(key));
    }
    for (DetachmentRule rule : rules.get(partOfSpeech)) {
      String output = rule.apply(string);
      if (output != null && lemmas.containsKey(output.toLowerCase())) {
        set.add(output);
      }
    }
    if (lemmas.containsKey(string)) {
      set.add(string);
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
  }


}//END OF WordNetLemmatizer
