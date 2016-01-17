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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
public class WordNetRelation implements Serializable {
  private static final Map<String, WordNetRelation> index = Maps.newConcurrentMap();
  private static final long serialVersionUID = 6838741399586844677L;
  private final String code;
  private final String name;
  private final String reciprocal;
  private final double weight;


  /**
   * The constant ALSO_SEE.
   */
  public static final WordNetRelation ALSO_SEE = create("ALSO_SEE", "^", null, Double.POSITIVE_INFINITY);
  /**
   * The constant ANTONYM.
   */
  public static final WordNetRelation ANTONYM = create("ANTONYM", "!", "ANTONYM", Double.POSITIVE_INFINITY);
  /**
   * The constant ATTRIBUTE.
   */
  public static final WordNetRelation ATTRIBUTE = create("ATTRIBUTE", "=", null, Double.POSITIVE_INFINITY);
  /**
   * The constant CAUSE.
   */
  public static final WordNetRelation CAUSE = create("CAUSE", ">", null, Double.POSITIVE_INFINITY);
  /**
   * The constant DERIVATIONALLY_RELATED.
   */
  public static final WordNetRelation DERIVATIONALLY_RELATED = create("DERIVATIONALLY_RELATED", "+", "DERIVATIONALLY_RELATED", Double.POSITIVE_INFINITY);
  /**
   * The constant DERIVED_FROM_ADJ.
   */
  public static final WordNetRelation DERIVED_FROM_ADJ = create("DERIVED_FROM_ADJ", "\\", null, Double.POSITIVE_INFINITY);
  /**
   * The constant ENTAILMENT.
   */
  public static final WordNetRelation ENTAILMENT = create("ENTAILMENT", "*", null, Double.POSITIVE_INFINITY);
  /**
   * The constant HOLONYM_MEMBER.
   */
  public static final WordNetRelation HOLONYM_MEMBER = create("HOLONYM_MEMBER", "#m", "MERONYM_MEMBER", Double.POSITIVE_INFINITY);
  /**
   * The constant HOLONYM_PART.
   */
  public static final WordNetRelation HOLONYM_PART = create("HOLONYM_PART", "#p", "MERONYM_PART", Double.POSITIVE_INFINITY);
  /**
   * The constant HOLONYM_SUBSTANCE.
   */
  public static final WordNetRelation HOLONYM_SUBSTANCE = create("HOLONYM_SUBSTANCE", "#s", "MERONYM_SUBSTANCE", Double.POSITIVE_INFINITY);
  /**
   * The constant HYPERNYM.
   */
  public static final WordNetRelation HYPERNYM = create("HYPERNYM", "@", "HYPONYM", 1d);
  /**
   * The constant HYPERNYM_INSTANCE.
   */
  public static final WordNetRelation HYPERNYM_INSTANCE = create("HYPERNYM_INSTANCE", "@i", "HYPONYM_INSTANCE", Double.POSITIVE_INFINITY);
  /**
   * The constant HYPONYM.
   */
  public static final WordNetRelation HYPONYM = create("HYPONYM", "~", "HYPERNYM", 1d);
  /**
   * The constant HYPONYM_INSTANCE.
   */
  public static final WordNetRelation HYPONYM_INSTANCE = create("HYPONYM_INSTANCE", "~i", "HYPERNYM_INSTANCE", Double.POSITIVE_INFINITY);
  /**
   * The constant MERONYM_MEMBER.
   */
  public static final WordNetRelation MERONYM_MEMBER = create("MERONYM_MEMBER", "%m", "HOLONYM_MEMBER", Double.POSITIVE_INFINITY);
  /**
   * The constant MERONYM_PART.
   */
  public static final WordNetRelation MERONYM_PART = create("MERONYM_PART", "%p", "HOLONYM_PART", Double.POSITIVE_INFINITY);
  /**
   * The constant MERONYM_SUBSTANCE.
   */
  public static final WordNetRelation MERONYM_SUBSTANCE = create("MERONYM_SUBSTANCE", "%s", "HOLONYM_SUBSTANCE", Double.POSITIVE_INFINITY);
  /**
   * The constant PARTICIPLE.
   */
  public static final WordNetRelation PARTICIPLE = create("PARTICIPLE", "<", null, Double.POSITIVE_INFINITY);
  /**
   * The constant PERTAINYM.
   */
  public static final WordNetRelation PERTAINYM = create("PERTAINYM", "\\", null, Double.POSITIVE_INFINITY);
  /**
   * The constant REGION.
   */
  public static final WordNetRelation REGION = create("REGION", ";r", "REGION_MEMBER", Double.POSITIVE_INFINITY);
  /**
   * The constant REGION_MEMBER.
   */
  public static final WordNetRelation REGION_MEMBER = create("REGION_MEMBER", "-r", "REGION", Double.POSITIVE_INFINITY);
  /**
   * The constant SIMILAR_TO.
   */
  public static final WordNetRelation SIMILAR_TO = create("SIMILAR_TO", "&", "SIMILAR_TO", Double.POSITIVE_INFINITY);
  /**
   * The constant SYNSET.
   */
  public static final WordNetRelation SYNSET = create("SYNSET", "$$$", "SYNSET_ELEMENT", Double.POSITIVE_INFINITY);
  /**
   * The constant SYNSET_ELEMENT.
   */
  public static final WordNetRelation SYNSET_ELEMENT = create("SYNSET_ELEMENT", "$$$e", "SYNSET", Double.POSITIVE_INFINITY);
  /**
   * The constant TOPIC.
   */
  public static final WordNetRelation TOPIC = create("TOPIC", ";c", "TOPIC_MEMBER", Double.POSITIVE_INFINITY);
  /**
   * The constant TOPIC_MEMBER.
   */
  public static final WordNetRelation TOPIC_MEMBER = create("TOPIC_MEMBER", "-c", "TOPIC", Double.POSITIVE_INFINITY);
  /**
   * The constant UNKNOWN.
   */
  public static final WordNetRelation UNKNOWN = create("UNKNOWN", "????", null, Double.POSITIVE_INFINITY);
  /**
   * The constant USAGE.
   */
  public static final WordNetRelation USAGE = create("USAGE", ";u", "USAGE_MEMBER", Double.POSITIVE_INFINITY);
  /**
   * The constant USAGE_MEMBER.
   */
  public static final WordNetRelation USAGE_MEMBER = create("USAGE_MEMBER", "-u", "USAGE", Double.POSITIVE_INFINITY);
  /**
   * The constant VERB_GROUP.
   */
  public static final WordNetRelation VERB_GROUP = create("VERB_GROUP", "$", "VERB_GROUP", Double.POSITIVE_INFINITY);


  /**
   * Instantiates a new Relation.
   *
   * @param name       the name
   * @param reciprocal the reciprocal
   * @param weight     the weight
   * @param code
   */
  protected WordNetRelation(String name, String reciprocal, double weight, String code) {
    this.name = name;
    this.code = code;
    this.reciprocal = Strings.isNullOrEmpty(reciprocal) ? null : reciprocal;
    this.weight = weight;
  }

  /**
   * Creates or gets a Metadata with the given name
   *
   * @param name       The name associated with the metadata
   * @param reciprocal the inverse relation
   * @param weight     the weight of the corresponding edge
   * @return The Metadata associated with the name or a new Metadata
   */
  public static WordNetRelation create(String name, String code, String reciprocal, double weight) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
    Preconditions.checkArgument(weight >= 0, "Weight must be >= 0");
    name = name.toUpperCase().trim().replaceAll("\\p{Z}+", "_");
    if (!index.containsKey(name)) {
      index.put(name, new WordNetRelation(name, reciprocal, weight, code));
    }
    return index.get(name);
  }

  /**
   * Get relation type.
   *
   * @param name the name
   * @return the relation type
   */
  public static WordNetRelation get(String name) {
    return index.get(name);
  }

  /**
   * For name.
   *
   * @param name the name
   * @return the relation
   */
  public static WordNetRelation forName(String name) {
    return index.get(name);
  }

  public static WordNetRelation forCode(WordNetPOS pos, String code) {
    if (pos == WordNetPOS.ADJECTIVE && PERTAINYM.code.equals(code)) {
      return PERTAINYM;
    } else if (DERIVED_FROM_ADJ.code.equals(code)) {
      return DERIVED_FROM_ADJ;
    }
    for (WordNetRelation type : index.values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException(code + " is unknown.");
  }

  /**
   * Gets reciprocal.
   *
   * @return the reciprocal
   */
  public WordNetRelation getReciprocal() {
    if (reciprocal == null) {
      return null;
    }
    return index.get(reciprocal);
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets weight.
   *
   * @return the weight
   */
  public double getWeight() {
    return weight;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final WordNetRelation other = (WordNetRelation) obj;
    return this.name.equals(other.name);
  }

  @Override
  public String toString() {
    return name;
  }

}//END OF RelationType
