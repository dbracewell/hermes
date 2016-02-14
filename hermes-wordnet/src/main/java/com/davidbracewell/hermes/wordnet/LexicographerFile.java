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


import com.davidbracewell.string.StringUtils;

/**
 * The enum Lexicographer file.
 *
 * @author David B. Bracewell
 */
public enum LexicographerFile {
  /**
   * all adjective clusters
   */
  ADJ_ALL("adj.all", 0, "all adjective clusters"),
  /**
   * relational adjectives (pertainyms)
   */
  ADJ_PERT("adj.pert", 1, "relational adjectives (pertainyms)"),
  /**
   * all adverbs
   */
  ADV_ALL("adv.all", 2, "all adverbs"),
  /**
   * unique beginner for nouns
   */
  NOUN_TOPS("noun.tops", 3, "unique beginner for nouns"),
  /**
   * nouns denoting acts or actions
   */
  NOUN_ACT("noun.act", 4, "nouns denoting acts or actions"),
  /**
   * nouns denoting animals
   */
  NOUN_ANIMAL("noun.animal", 5, "nouns denoting animals"),
  /**
   * nouns denoting man-made objects.
   */
  NOUN_ARTIFACT("noun.artifact", 6, "nouns denoting man-made objects"),
  /**
   * nouns denoting attributes of people and objects
   */
  NOUN_ATTRIBUTE("noun.attribute", 7, "nouns denoting attributes of people and objects"),
  /**
   * nouns denoting body parts
   */
  NOUN_BODY("noun.body", 8, "nouns denoting body parts"),
  /**
   * nouns denoting cognitive processes and contents
   */
  NOUN_COGNITION("noun.cognition", 9, "nouns denoting cognitive processes and contents"),
  /**
   * nouns denoting communicative processes and contents.
   */
  NOUN_COMMUNICATION("noun.communication", 10, "nouns denoting communicative processes and contents"),
  /**
   * nouns denoting natural events
   */
  NOUN_EVENT("noun.event", 11, "nouns denoting natural events"),
  /**
   * nouns denoting feelings and emotions
   */
  NOUN_FEELING("noun.feeling", 12, "nouns denoting feelings and emotions"),
  /**
   * nouns denoting foods and drinks
   */
  NOUN_FOOD("noun.food", 13, "nouns denoting foods and drinks"),
  /**
   * nouns denoting groupings of people or objects
   */
  NOUN_GROUP("noun.group", 14, "nouns denoting groupings of people or objects"),
  /**
   * nouns denoting spatial position
   */
  NOUN_LOCATION("noun.location", 15, "nouns denoting spatial position"),
  /**
   * nouns denoting goals
   */
  NOUN_MOTIVE("noun.motive", 16, "nouns denoting goals"),
  /**
   * nouns denoting natural objects (not man-made)
   */
  NOUN_OBJECT("noun.object", 17, "nouns denoting natural objects (not man-made)"),
  /**
   * nouns denoting people
   */
  NOUN_PERSON("noun.person", 18, "nouns denoting people"),
  /**
   * nouns denoting natural phenomena
   */
  NOUN_PHENOMENON("noun.phenomenon", 19, "nouns denoting natural phenomena"),
  /**
   * nouns denoting plants
   */
  NOUN_PLANT("noun.plant", 20, "nouns denoting plants"),
  /**
   * nouns denoting possession and transfer of possession
   */
  NOUN_POSSESSION("noun.possession", 21, "nouns denoting possession and transfer of possession"),
  /**
   * nouns denoting natural processes
   */
  NOUN_PROCESS("noun.process", 22, "nouns denoting natural processes"),
  /**
   * nouns denoting quantities and units of measure
   */
  NOUN_QUANTITY("noun.quantity", 23, "nouns denoting quantities and units of measure"),
  /**
   * nouns denoting relations between people or things or ideas.
   */
  NOUN_RELATION("noun.relation", 24, "nouns denoting relations between people or things or ideas"),
  /**
   * nouns denoting two and three dimensional shapes
   */
  NOUN_SHAPE("noun.shape", 25, "nouns denoting two and three dimensional shapes"),
  /**
   * nouns denoting stable states of affairs
   */
  NOUN_STATE("noun.state", 26, "nouns denoting stable states of affairs"),
  /**
   * nouns denoting substances
   */
  NOUN_SUBSTANCE("noun.substance", 27, "nouns denoting substances"),
  /**
   * nouns denoting time and temporal relations
   */
  NOUN_TIME("noun.time", 28, "nouns denoting time and temporal relations"),
  /**
   * verbs of grooming, dressing and bodily care
   */
  VERB_BODY("verb.body", 29, "verbs of grooming, dressing and bodily care"),
  /**
   * verbs of size, temperature change, intensifying, etc.
   */
  VERB_CHANGE("verb.change", 30, "verbs of size, temperature change, intensifying, etc."),
  /**
   * verbs of thinking, judging, analyzing, doubting
   */
  VERB_COGNITION("verb.cognition", 31, "verbs of thinking, judging, analyzing, doubting"),
  /**
   * The verbs of telling, asking, ordering, singing
   */
  VERB_COMMUNICATION("verb.communication", 32, "verbs of telling, asking, ordering, singing"),
  /**
   * The verbs of fighting, athletic activities
   */
  VERB_COMPETITION("verb.competition", 33, "verbs of fighting, athletic activities"),
  /**
   * verbs of eating and drinking
   */
  VERB_CONSUMPTION("verb.consumption", 34, "verbs of eating and drinking"),
  /**
   * verbs of touching, hitting, tying, digging
   */
  VERB_CONTACT("verb.contact", 35, "verbs of touching, hitting, tying, digging"),
  /**
   * verbs of sewing, baking, painting, performing
   */
  VERB_CREATION("verb.creation", 36, "verbs of sewing, baking, painting, performing"),
  /**
   * verbs of feeling
   */
  VERB_EMOTION("verb.emotion", 37, "verbs of feeling"),
  /**
   * verbs of walking, flying, swimming
   */
  VERB_MOTION("verb.motion", 38, "verbs of walking, flying, swimming"),
  /**
   * verbs of seeing, hearing, feeling
   */
  VERB_PERCEPTION("verb.perception", 39, "verbs of seeing, hearing, feeling"),
  /**
   * verbs of buying, selling, owning.
   */
  VERB_POSSESSION("verb.possession", 40, "verbs of buying, selling, owning"),
  /**
   * verbs of political and social activities and events
   */
  VERB_SOCIAL("verb.social", 41, "verbs of political and social activities and events"),
  /**
   * verbs of being, having, spatial relations
   */
  VERB_STATIVE("verb.stative", 42, "verbs of being, having, spatial relations"),
  /**
   * verbs of raining, snowing, thawing, thundering
   */
  VERB_WEATHER("verb.weather", 43, "verbs of raining, snowing, thawing, thundering"),
  /**
   * participial adjectives
   */
  ADJ_PPL("adj.ppl", 44, "participial adjectives");

  private final String description;
  private final String fileName;
  private final int id;

  private LexicographerFile(String fileName, int id, String description) {
    this.fileName = fileName;
    this.id = id;
    this.description = description;
  }

  /**
   * Parses a string to determine the correct enum value
   *
   * @param string The string to parse
   * @return The enum value
   */
  public static LexicographerFile fromString(String string) {
    if (!StringUtils.isNullOrBlank(string)) {
      try {
        return valueOf(string);
      } catch (Exception e) {
        for (LexicographerFile file : values()) {
          if (file.fileName.equals(string)) {
            return file;
          }
        }
      }
    }
    throw new IllegalArgumentException(string + " is not a valid LexicographerFile.");
  }

  /**
   * From id.
   *
   * @param id the id
   * @return the lexicographer file
   */
  public static LexicographerFile fromId(int id) {
    for (LexicographerFile lf : values()) {
      if (id == lf.id) {
        return lf;
      }
    }
    throw new IllegalArgumentException(id + " is invalid");
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  public int getId() {
    return id;
  }


  /**
   * Get description.
   *
   * @return the string
   */
  public String getDescription(){
    return description;
  }


}//END OF LexicographerFile
