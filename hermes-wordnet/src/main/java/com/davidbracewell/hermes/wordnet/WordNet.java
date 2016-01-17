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

import com.davidbracewell.Language;
import com.davidbracewell.SystemInfo;
import com.davidbracewell.cache.Cache;
import com.davidbracewell.cache.CacheManager;
import com.davidbracewell.cache.CacheSpec;
import com.davidbracewell.collection.Counter;
import com.davidbracewell.collection.Counters;
import com.davidbracewell.collection.Sorting;
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.wordnet.io.WordNetDB;
import com.davidbracewell.hermes.wordnet.io.WordNetLoader;
import com.davidbracewell.hermes.wordnet.io.WordNetPropertyLoader;
import com.davidbracewell.hermes.wordnet.io.properties.InformationContentLoader;
import com.davidbracewell.hermes.wordnet.properties.PropertyName;
import com.davidbracewell.io.Resources;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.*;

import java.util.*;

/**
 * The type Word net.
 *
 * @author David B. Bracewell
 */
public class WordNet {

  private static volatile WordNet INSTANCE;
  private final double[] maxDepths = {-1, -1, -1, -1, -1};
  private final WordNetDB db;

  private final Cache<Synset, ListMultimap<Synset, Synset>> shortestPathCache = CacheManager.getInstance()
    .register(
      new CacheSpec<Synset, ListMultimap<Synset, Synset>>()
        .engine("Guava")
        .maxSize(25000)
        .concurrencyLevel(SystemInfo.NUMBER_OF_PROCESSORS)
        .name("WordNetDistanceCache")
        .expiresAfterAccess("20m")
        .loadingFunction(input -> input == null ? null : dijkstra_path(input))
    );

  private WordNet() {
    db = Config.get(WordNet.class, "db").as(WordNetDB.class);
    for (WordNetLoader loader : Config.get(WordNet.class, "loaders").asList(WordNetLoader.class)) {
      loader.load(db);
    }
    if (Config.hasProperty(WordNet.class, "properties")) {
      for (WordNetPropertyLoader loader : Config.get(WordNet.class, "properties").asList(WordNetPropertyLoader.class)) {
        loader.load(db);
      }
    }
  }

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static WordNet getInstance() {
    if (INSTANCE == null) {
      synchronized (WordNet.class) {
        if (INSTANCE == null) {
          INSTANCE = new WordNet();
        }
      }
    }
    return INSTANCE;
  }


  private ListMultimap<Synset, Synset> dijkstra_path(Synset source) {
    Counter<Synset> dist = Counters.newHashMapCounter();
    Map<Synset, Synset> previous = new HashMap<>();
    Set<Synset> visited = Sets.newHashSet(source);

    for (Synset other : getSynsets()) {
      if (!other.equals(source)) {
        dist.set(other, Integer.MAX_VALUE);
        previous.put(other, null);
      }
    }

    MinMaxPriorityQueue<Tuple2<Synset, Double>> queue = MinMaxPriorityQueue
      .orderedBy(Cast.<Comparator<? super Tuple2<Synset, Double>>>as(Sorting.mapEntryComparator(false, true)))
      .create();
    queue.add(Tuple2.of(source, 0d));

    while (!queue.isEmpty()) {
      Tuple2<Synset, Double> next = queue.remove();

      Synset synset = next.getV1();
      visited.add(synset);

      Iterable<Synset> neighbors = Iterables.concat(
        synset.getRelatedSynsets(WordNetRelation.HYPERNYM),
        synset.getRelatedSynsets(WordNetRelation.HYPERNYM_INSTANCE),
        synset.getRelatedSynsets(WordNetRelation.HYPONYM),
        synset.getRelatedSynsets(WordNetRelation.HYPONYM_INSTANCE)
      );

      for (Synset neighbor : neighbors) {
        double alt = dist.get(synset);
        if (alt != Integer.MAX_VALUE && (alt + 1) < dist.get(neighbor)) {
          dist.set(neighbor, alt + 1);
          previous.put(neighbor, synset);
        }
        if (!visited.contains(neighbor)) {
          queue.add(Tuple2.of(neighbor, alt));
        }
      }
    }

    ListMultimap<Synset, Synset> path = ArrayListMultimap.create();
    for (Synset other : getSynsets()) {
      if (other.equals(source) || dist.get(other) == Integer.MAX_VALUE) continue;

      Deque<Synset> stack = Lists.newLinkedList();
      Synset u = other;
      while (u != null && previous.containsKey(u)) {
        stack.push(u);
        u = previous.get(u);
      }
      while (!stack.isEmpty()) {
        Synset to = stack.pop();
        path.put(other, to);
      }
    }

    return path;
  }

  /**
   * Gets max depth.
   *
   * @param partOfSpeech the part of speech
   * @return the max depth
   */
  public double getMaxDepth(WordNetPOS partOfSpeech) {
    Preconditions.checkNotNull(partOfSpeech);
    if (maxDepths[partOfSpeech.ordinal()] == -1) {
      synchronized (maxDepths) {
        if (maxDepths[partOfSpeech.ordinal()] == -1) {
          double max = 0d;
          for (Synset synset : getSynsets()) {
            if (synset.getPOS() == partOfSpeech) {
              max = Math.max(max, depth(synset) - 1);
            }
          }
          maxDepths[partOfSpeech.ordinal()] = max;
        }
      }
    }
    return maxDepths[partOfSpeech.ordinal()];
  }


  /**
   * Gets relation.
   *
   * @param from the from
   * @param to   the to
   * @return the relation
   */
  public WordNetRelation getRelation(Sense from, Sense to) {
    if (from == null || to == null) {
      return null;
    }
    return db.getRelation(from, to);
  }

  /**
   * Contains lemma.
   *
   * @param lemma the lemma
   * @return the boolean
   */
  public boolean containsLemma(String lemma) {
    return !Strings.isNullOrEmpty(lemma) && db.containsLemma(lemma.toLowerCase());
  }

  /**
   * Gets lemmas.
   *
   * @return the lemmas in the network.
   */
  public Set<String> getLemmas() {
    return Collections.unmodifiableSet(db.getLemmas());
  }

  /**
   * Gets senses.
   *
   * @return All senses present in the network
   */
  public Collection<Sense> getSenses() {
    return Collections.unmodifiableCollection(db.getSenses());
  }

  /**
   * Gets synsets.
   *
   * @return All synsets present in the network
   */
  public Collection<Synset> getSynsets() {
    return Collections.unmodifiableCollection(db.getSynsets());
  }

  /**
   * Returns the height of the node in the ontology.
   *
   * @param node The node to check
   * @return The height
   */
  public int depth(Synset node) {
    Preconditions.checkNotNull(node);
    //TODO : implement
    return -1;
  }


  /**
   * Gets the hypernyms of the given WordNetNode.
   *
   * @param node The WordNet node
   * @return The hypernyms
   */
  public Set<Synset> getHypernyms(Synset node) {
    return getRelatedSynsets(node, WordNetRelation.HYPERNYM);
  }

  /**
   * Gets the first hypernym of the given WordNetNode.
   *
   * @param node The WordNet node
   * @return The first hypernym
   */
  public Synset getHypernym(Synset node) {
    return Iterables.getFirst(getHypernyms(node), null);
  }

  /**
   * Gets the hyponyms of the given WordNetNode.
   *
   * @param node The WordNet node
   * @return The hyponyms
   */
  public Set<Synset> getHyponyms(Synset node) {
    return getRelatedSynsets(node, WordNetRelation.HYPONYM);
  }

  /**
   * Gets the first hyponym of the given WordNetNode.
   *
   * @param node The WordNet node
   * @return The first hyponym
   */
  public Synset getHyponym(Synset node) {
    return Iterables.getFirst(getHyponyms(node), null);
  }

  /**
   * Gets the semantic relations associated with the given WordNetNode.
   *
   * @param node     The WordNet node
   * @param relation The desired relation
   * @return A set of synset representing the synsets with the given relation to the given node
   */
  public Set<Synset> getRelatedSynsets(Synset node, final WordNetRelation relation) {
    if (node == null) {
      return Collections.emptySet();
    }
    Set<Synset> synsets = new HashSet<>();
    for (Map.Entry<String, WordNetRelation> entry : db.getRelations(node).entrySet()) {
      if (entry.getValue() == relation) {
        synsets.add(db.getSynsetFromId(entry.getKey()));
      }
    }
    return synsets;
  }

  /**
   * Gets the semantic relations associated with the given synset.
   *
   * @param synset The WordNet synset
   * @return A set of synset representing the relation with to the given synset
   */
  public HashMultimap<WordNetRelation, Synset> getRelatedSynsets(final Synset synset) {
    if (synset == null) {
      return HashMultimap.create();
    }
    HashMultimap<WordNetRelation, Synset> map = HashMultimap.create();
    for (Map.Entry<String, WordNetRelation> entry : db.getRelations(synset).entrySet()) {
      map.put(entry.getValue(), getSynset(entry.getKey()));
    }
    return map;
  }

  /**
   * Gets the lexical relations associated with the given sense.
   *
   * @param sense    The WordNet sense
   * @param relation The desired relation
   * @return A set of senses representing the sense with the given relation to the given sense
   */
  public Set<Sense> getRelatedSenses(final Sense sense, final WordNetRelation relation) {
    if (sense == null) {
      return Collections.emptySet();
    }
    Set<Sense> senses = new HashSet<>();

    for (Map.Entry<Sense, WordNetRelation> entry : db.getRelations(sense).entrySet()) {
      if (entry.getValue() == relation) {
        senses.add(entry.getKey());
      }
    }
    return senses;
  }

  /**
   * Gets the lexical relations associated with the given sense.
   *
   * @param sense The WordNet sense
   * @return A set of senses representing the sense with to the given sense
   */
  public HashMultimap<WordNetRelation, Sense> getRelatedSenses(final Sense sense) {
    if (sense == null) {
      return HashMultimap.create();
    }
    HashMultimap<WordNetRelation, Sense> map = HashMultimap.create();
    for (Map.Entry<Sense, WordNetRelation> entry : db.getRelations(sense).entrySet()) {
      map.put(entry.getValue(), entry.getKey());
    }
    return map;
  }

  /**
   * Gets the siblings of the given Synset, i.e. the synsets with which the given synset shares a hypernym.
   *
   * @param synset The synset
   * @return A set of siblings
   */
  public Set<Synset> getSiblings(Synset synset) {
    if (synset == null) {
      return Collections.emptySet();
    }
    Set<Synset> siblings = Sets.newHashSet();
    for (Synset hypernym : getHypernyms(synset)) {
      siblings.addAll(getHyponyms(hypernym));
    }
    siblings.remove(synset);
    return siblings;
  }

  /**
   * Gets the synset associated with the id
   *
   * @param id The sense
   * @return The synset or null
   */
  public Synset getSynset(String id) {
    return db.getSynsetFromId(id);
  }

  public List<Sense> getSenses(String surfaceForm) {
    return getSenses(surfaceForm, WordNetPOS.ANY, Language.ENGLISH);
  }

  /**
   * Gets senses.
   *
   * @param surfaceForm the surface form
   * @param language    the language
   * @return the senses
   */
  public List<Sense> getSenses(String surfaceForm, Language language) {
    return getSenses(surfaceForm, WordNetPOS.ANY, language);
  }

  private List<Sense> getSenses(Predicate<Sense> predicate, Iterable<String> lemmas) {
    List<Sense> senses = Lists.newArrayList();
    List<String> lemmaList = Lists.newArrayList(lemmas);

    for (String lemma : lemmaList) {
      senses.addAll(Collections2.filter(db.getSenses(lemma), Predicates.and(predicate, new SenseFormPredicate(lemma))));
    }

    if (senses.isEmpty()) {
      for (String lemma : lemmaList) {
        senses.addAll(Collections2.filter(db.getSenses(lemma), Predicates.and(predicate, new SenseFormPredicate(lemma.toLowerCase()))));
      }
    }

    Collections.sort(senses);
    return senses;
  }

  /**
   * Gets senses.
   *
   * @param surfaceForm the surface form
   * @param POS         the part of speech tag
   * @param language    the language
   * @return the senses
   */
  public List<Sense> getSenses(String surfaceForm, WordNetPOS POS, Language language) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(surfaceForm));
    Preconditions.checkNotNull(POS);
    Preconditions.checkNotNull(language);
    return getSenses(new SenseEnum(-1, POS, language), WordNetLemmatizer.getInstance().getBaseForm(surfaceForm, POS));
  }

  /**
   * Gets senses.
   *
   * @param surfaceForm     the surface form
   * @param partOfSpeechTag the part of speech tag
   * @param senseNum        the sense num
   * @return the senses
   */
  public List<Sense> getSenses(String surfaceForm, WordNetPOS partOfSpeechTag, int senseNum) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(surfaceForm));
    Preconditions.checkNotNull(partOfSpeechTag);
    List<Sense> senses = Lists.newArrayList();

    for (Sense sense : db.getSenses(surfaceForm.toLowerCase())) {
      if ((partOfSpeechTag == WordNetPOS.ANY || sense.getPOS() == partOfSpeechTag) && sense.getSenseNumber() == senseNum) {
        senses.add(sense);
      }
    }
    if (senses.isEmpty()) {
      for (Sense sense : db.getSenses(surfaceForm)) {
        if ((partOfSpeechTag == WordNetPOS.ANY || sense.getPOS() == partOfSpeechTag) && sense.getSenseNumber() == senseNum) {
          senses.add(sense);
        }
      }
    }
    Collections.sort(senses);
    return senses;
  }

  /**
   * Gets the sense for the associated information
   *
   * @param lemma    The lemma
   * @param POS      The part of speech
   * @param senseNum The sense number
   * @param language The language
   * @return The sense
   */
  public Sense getSense(String lemma, WordNetPOS POS, int senseNum, Language language) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(lemma));
    Preconditions.checkNotNull(POS);
    Preconditions.checkNotNull(language);
    for (Sense sense : db.getSenses(lemma.toLowerCase())) {
      if ((POS == WordNetPOS.ANY || sense.getPOS() == POS) && sense.getSenseNumber() == senseNum && sense.getLanguage() == language) {
        return sense;
      }
    }
    for (Sense sense : db.getSenses(lemma)) {
      if ((POS == WordNetPOS.ANY || sense.getPOS() == POS) && sense.getSenseNumber() == senseNum && sense.getLanguage() == language) {
        return sense;
      }
    }
    return null;
  }

  /**
   * Gets the node that is least common subsumer (the synset with maximum height that is a parent to both nodes.)
   *
   * @param synset1 The first node
   * @param synset2 The second node
   * @return The least common subsumer or null
   */
  public Synset getLeastCommonSubsumer(Synset synset1, Synset synset2) {
    Preconditions.checkNotNull(synset1);
    Preconditions.checkNotNull(synset2);

    if (synset1.equals(synset2)) {
      return synset1;
    }

    List<Synset> path = shortestPath(synset1, synset2);
    if (path.isEmpty()) {
      return null;
    }

    int node1Height = depth(synset1);
    int node2Height = depth(synset2);
    int minHeight = Math.min(node1Height, node2Height);
    int maxHeight = Integer.MIN_VALUE;
    Synset lcs = null;
    for (Synset s : path) {
      if (s.equals(synset1) || s.equals(synset2)) {
        continue;
      }
      int height = depth(s);
      if (height < minHeight && height > maxHeight) {
        maxHeight = height;
        lcs = s;
      }
    }
    if (lcs == null) {
      if (node1Height < node2Height) {
        return synset1;
      }
      return synset2;
    }
    return lcs;
  }

  /**
   * Gets the shortest path between synset.
   *
   * @param synset1 The first synset
   * @param synset2 The second synset
   * @return The path
   */
  public List<Synset> shortestPath(Synset synset1, Synset synset2) {
    Preconditions.checkNotNull(synset1);
    Preconditions.checkNotNull(synset2);
    return Collections.unmodifiableList(shortestPathCache.get(synset1).get(synset2));
  }


  /**
   * Calculates the distance between synsets.
   *
   * @param synset1 Synset 1
   * @param synset2 Synset 2
   * @return The distance
   */
  public double distance(Synset synset1, Synset synset2) {
    Preconditions.checkNotNull(synset1);
    Preconditions.checkNotNull(synset2);
    if (synset1.equals(synset2)) {
      return 0d;
    }
    List<Synset> path = shortestPath(synset1, synset2);
    return path.isEmpty() ? Double.POSITIVE_INFINITY : path.size() - 1;
  }


  /**
   * Gets the root synsets in the network
   *
   * @return The set of root synsets
   */
  public Set<Synset> getRoots() {
    return Collections.unmodifiableSet(db.getRoots());
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws Exception the exception
   */
  public static void main(String[] args) throws Exception {
    Config.initialize("WordNet");
//    System.out.println(WordNetLemmatizer.getInstance().getBaseForm("running up"));
    System.out.println(WordNetLemmatizer.getInstance().getBaseForm("hand-washing"));
    InformationContentLoader loader = new InformationContentLoader(Resources.fromFile("/shared/data/ic-bnc-add1.dat"), "INFORMATION_CONTENT");
    loader.load(WordNet.getInstance().db);

    Synset cat = WordNet.getInstance().getSense("cat", WordNetPOS.NOUN, 1, Language.ENGLISH).getSynset();
    Synset dog = WordNet.getInstance().getSense("dog", WordNetPOS.NOUN, 1, Language.ENGLISH).getSynset();
    System.out.println(cat + " : " + cat.getProperty(PropertyName.INFO_CONTENT).get("value"));
    System.out.println(dog + " : " + dog.getProperty(PropertyName.INFO_CONTENT).get("value"));


    WordNet.getInstance().shortestPath(cat, dog).forEach(synset -> System.out.println(synset.getSenses()));

  }

  private static class SenseFormPredicate implements Predicate<Sense> {
    private final String lemma;

    private SenseFormPredicate(String lemma) {
      this.lemma = lemma;
    }

    @Override
    public boolean apply(Sense sense) {
      return sense != null && sense.getLemma().equals(lemma);
    }
  }

  private static class SenseEnum implements Predicate<Sense> {

    private final int senseNum;
    private final WordNetPOS pos;
    private final Language language;

    private SenseEnum(int senseNum, WordNetPOS pos, Language language) {
      this.senseNum = senseNum;
      this.pos = pos;
      this.language = language;
    }

    @Override
    public boolean apply(Sense sense) {
      if (sense == null) {
        return false;
      }
      if (senseNum != -1 && sense.getLexicalId() != senseNum) {
        return false;
      }
      if (pos != null && sense.getPOS() != pos) {
        return false;
      }
      if (language != null && sense.getLanguage() != language) {
        return false;
      }
      return true;
    }

  }


}//END OF WordNetGraph
