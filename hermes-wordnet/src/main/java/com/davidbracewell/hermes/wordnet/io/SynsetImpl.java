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

package com.davidbracewell.hermes.wordnet.io;

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.attribute.POS;
import com.davidbracewell.hermes.wordnet.*;
import com.davidbracewell.hermes.wordnet.properties.Property;
import com.davidbracewell.hermes.wordnet.properties.PropertyName;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;

import java.io.Serializable;
import java.util.*;

/**
 * The type Synset.
 *
 * @author David B. Bracewell
 */
public class SynsetImpl implements Synset, Serializable, Comparable<Synset> {

  private static final long serialVersionUID = -2919580932965749780L;
  private String id;
  private boolean isAdjectiveSatelite;
  private LexicographerFile lexicographerFile;
  private WordNetPOS partOfSpeech;
  private String gloss;
  private Sense[] senses;
  private int depth = -1;
  private final Map<PropertyName, Property> properties = new HashMap<>(0);

  /**
   * Sets adjective satelite.
   *
   * @param adjectiveSatelite the adjective satelite
   */
  public void setAdjectiveSatelite(boolean adjectiveSatelite) {
    isAdjectiveSatelite = adjectiveSatelite;
  }

  /**
   * Sets part of speech.
   *
   * @param partOfSpeech the part of speech
   */
  public void setPartOfSpeech(WordNetPOS partOfSpeech) {
    this.partOfSpeech = partOfSpeech;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final SynsetImpl other = (SynsetImpl) obj;
    return Objects.equals(this.id, other.id);
  }

  @Override
  public String toString() {
    return id;
  }

  @Override
  public int compareTo(Synset o) {
    if (o == null) {
      return -1;
    }
    return id.compareTo(o.getId());
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public List<Sense> getSenses() {
    return Arrays.asList(senses);
  }

  @Override
  public LexicographerFile getLexicographerFile() {
    return lexicographerFile;
  }

  @Override
  public POS getPOS() {
    return partOfSpeech.toHermesPOS();
  }

  @Override
  public String getGloss() {
    return gloss;
  }

  @Override
  public Set<Synset> getRelatedSynsets(WordNetRelation relation) {
    return WordNet.getInstance().getRelatedSynsets(this, Preconditions.checkNotNull(relation));
  }

  @Override
  public HashMultimap<WordNetRelation, Synset> getRelatedSynsets() {
    return WordNet.getInstance().getRelatedSynsets(this);
  }

  @Override
  public int depth() {
    if (depth < 0) {
      synchronized (this) {
        if (depth < 0) {
          depth = (int) WordNet.getInstance().getRoots().stream()
            .filter(s -> s.getPOS() == getPOS())
            .mapToDouble(root -> WordNet.getInstance().distance(this, root))
            .min().orElse(-1);
        }
      }
    }
    return depth;
  }

  @Override
  public boolean isAdjectiveSatelitie() {
    return isAdjectiveSatelite;
  }

  /**
   * Sets gloss.
   *
   * @param gloss the gloss
   */
  public void setGloss(String gloss) {
    this.gloss = gloss;
  }

  /**
   * Sets lexicographer file.
   *
   * @param lexicographerFile the lexicographer file
   */
  public void setLexicographerFile(LexicographerFile lexicographerFile) {
    this.lexicographerFile = lexicographerFile;
  }

  /**
   * Sets senses.
   *
   * @param senses the senses
   */
  public void setSenses(Sense[] senses) {
    this.senses = senses;
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public <T extends Property> T getProperty(PropertyName name) {
    return Cast.as(properties.get(name));
  }

  void setProperty(PropertyName name, Property property) {
    properties.put(name, property);
  }


}//END OF Synset
