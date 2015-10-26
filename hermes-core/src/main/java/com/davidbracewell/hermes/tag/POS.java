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

package com.davidbracewell.hermes.tag;

import com.davidbracewell.Tag;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;

/**
 * Enumeration covering the parts-of-speech used in pos tagging, chunking, and parsing.
 *
 * @author David B. Bracewell
 */
public enum POS implements Tag {

  ANY(null, "UNKNOWN"),

  /**
   * Universal Tag Set
   */
  VERB(ANY, "VERB") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },
  NOUN(ANY, "NOUN") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },
  PRONOUN(ANY, "PRON") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },
  ADJECTIVE(ANY, "ADJ") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },
  ADVERB(ANY, "ADV") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },
  ADPOSITION(ANY, "ADP") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },
  CONJUNCTION(ANY, "CONJ") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },
  DETERMINER(ANY, "DET") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },
  NUMBER(ANY, "NUM") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },
  PARTICLE(ANY, "PRT") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },
  OTHER(ANY, "X") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },
  PUNCTUATION(ANY, ".") {
    @Override
    public boolean isUniversal() {
      return true;
    }
  },

  /**
   * Phrase Tags
   */
  VP(VERB, "VP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },
  NP(NOUN, "NP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },
  ADJP(ADJECTIVE, "ADJP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },
  PP(ADPOSITION, "PP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },
  ADVP(ADVERB, "ADVP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },
  SBAR(OTHER, "SBAR") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },
  CONJP(CONJUNCTION, "CONJP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },
  PRT(PARTICLE, "PRT") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },
  INTJ(OTHER, "INTJ") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },
  LST(OTHER, "LST") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },
  UCP(OTHER, "UCP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },

  /**
   * Penn Treebank Part of Speech Tags
   */
  CC(CONJUNCTION, "CC"), // Coordinating conjunction
  CD(NUMBER, "CD"), //Cardinal number
  DT(DETERMINER, "DT"), //Determiner
  EX(DETERMINER, "EX"), //Existential there
  FW(OTHER, "FW"), //Foreign word
  IN(ADPOSITION, "IN"), //Preposition or subordinating conjunction
  JJ(ADJECTIVE, "JJ"), // Adjective
  JJR(ADJECTIVE, "JJR"), //Adjective, comparative
  JJS(ADJECTIVE, "JJS"), //Adjective, superlative
  LS(OTHER, "LS"), //List item marker
  MD(VERB, "MD"), //Modal
  NN(NOUN, "NN"), //Noun, singular or mass
  NNS(NOUN, "NNS"), //Noun, plural
  NNP(NOUN, "NNP"), //Proper noun, singular
  NNPS(NOUN, "NNPS"), //Proper noun, plural
  PDT(DETERMINER, "PDT"), //Predeterminer
  POS(PARTICLE, "POS"), //Possessive ending
  PRP(PRONOUN, "PRP"), //Personal pronoun
  PRP$(PRONOUN, "PRP$"), //Possessive pronoun
  RB(ADVERB, "RB"), //Adverb
  RBR(ADVERB, "RBR"), //Adverb, comparative
  RBS(ADVERB, "RBS"), //Adverb, superlative
  RP(PARTICLE, "RP"), //Particle
  SYM(OTHER, "SYM"), //Symbol
  TO(PARTICLE, "TO"), //to
  UH(OTHER, "UH"), //Interjection
  VB(VERB, "VB"), //Verb, base form
  VBD(VERB, "VBD"), //Verb, past tense
  VBG(VERB, "VBG"),//Verb, gerund or present participle
  VBN(VERB, "VBN"),//Verb, past participle
  VBP(VERB, "VBP"),//Verb, non-3rd person singular present
  VBZ(VERB, "VBZ"),//Verb, 3rd person singular present
  WDT(DETERMINER, "WDT"),//Wh-determiner
  WP(PRONOUN, "WP"), //Wh-pronoun
  WP$(PRONOUN, "WP$"), //Possessive wh-pronoun
  WRB(ADVERB, "WRB"), //Wh-adverb
  PERIOD(PUNCTUATION, "."),
  HASH(PUNCTUATION, "#"),
  OPEN_QUOTE(PUNCTUATION, "``"),
  CLOSE_QUOTE(PUNCTUATION, "''"),
  QUOTE(PUNCTUATION, "\""),
  DOLLAR(PUNCTUATION, "$"),
  LRB(PUNCTUATION, "-LRB-"),
  RRB(PUNCTUATION, "-RRB-"),
  LCB(PUNCTUATION, "-LCB-"),
  RCB(PUNCTUATION, "-RCB-"),
  COMMA(PUNCTUATION, ","),
  SEMICOLON(PUNCTUATION, ":"),
  CLOSE_PARENS(PUNCTUATION, ")"),
  OPEN_PARENS(PUNCTUATION, "("),


  /**
   * Special Japanese Part of Speech Tags
   */
  ADN(ADJECTIVE, "ADN"),
  AUX(VERB, "AUX"),
  LOC(NOUN, "LOC"), // Noun Location
  ORG(NOUN, "ORG"), //Noun Organization
  PER(NOUN, "PER"), // Noun Person
  RPC(PARTICLE, "RPC"), //Case Particle


  /**
   * Special CHINESE Part of Speech Tags
   */
  AD(ADVERB, "AD"),
  AS(PARTICLE, "AS"),
  BA(OTHER, "BA"),
  CS(CONJUNCTION, "CS"),
  DEC(PARTICLE, "DEC"),
  DEG(PARTICLE, "DEG"),
  DER(PARTICLE, "DER"),
  DEV(PARTICLE, "DEV"),
  ETC(PARTICLE, "PRT"),
  IJ(OTHER, "IJ"),
  LB(OTHER, "LB"),
  LC(PARTICLE, "LC"),
  M(NUMBER, "M"),
  MSP(PARTICLE, "MSP"),
  NR(NOUN, "NR"),
  NT(NOUN, "NT"),
  OD(NUMBER, "OD"),
  ON(OTHER, "ON"),
  P(ADPOSITION, "P"),
  PN(PRONOUN, "PN"),
  PU(PUNCTUATION, "PU"),
  SB(OTHER, "SB"),
  SP(PARTICLE, "SP"),
  VA(VERB, "VA"),
  VC(VERB, "VC"),
  VE(VERB, "VE"),
  VV(VERB, "VV"),
  X(OTHER, "X"),

  CLP(OTHER, "CLP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },

  CP(OTHER, "CP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },

  DNP(OTHER, "DNP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },

  DP(DETERMINER, "DP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },

  DVP(OTHER, "DVP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },

  FRAG(OTHER, "FRAG") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },

  IP(OTHER, "IP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },

  LCP(OTHER, "LCP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },

  PRN(OTHER, "PRN") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  },

  QP(OTHER, "QP") {
    @Override
    public boolean isPhraseTag() {
      return true;
    }
  };

  private final String tag;
  private final com.davidbracewell.hermes.tag.POS parentType;

  private POS(com.davidbracewell.hermes.tag.POS parentType, String tag) {
    this.parentType = parentType;
    this.tag = tag;
  }

  /**
   * Parses a String converting the tag (its Enum-String or treebank string) into the appropriate enum value
   *
   * @param tag The tag to parts
   * @return The <code>PartOfSpeech</code>
   */
  public static com.davidbracewell.hermes.tag.POS fromString(String tag) {
    if (StringUtils.isNullOrBlank(tag)) {
      return null;
    }
    try {
      return com.davidbracewell.hermes.tag.POS.valueOf(tag);
    } catch (Exception e) {
      for (com.davidbracewell.hermes.tag.POS pos : com.davidbracewell.hermes.tag.POS.values()) {
        if (pos.tag.equals(tag) || pos.tag.equals(tag.toUpperCase())) {
          return pos;
        }
      }
    }
    throw new IllegalArgumentException(tag + " is not a know PartOfSpeech");
  }

  /**
   * Determines the best fundamental POS (NOUN, VERB, ADJECTIVE, or ADVERB) for a text.
   *
   * @param text The text
   * @return The part of speech
   */
  public static com.davidbracewell.hermes.tag.POS forText(HString text) {
    Preconditions.checkNotNull(text);

    if (text.contains(Attrs.PART_OF_SPEECH)) {
      return text.get(Attrs.PART_OF_SPEECH).cast();
    }

    com.davidbracewell.hermes.tag.POS tag = ANY;
    for (Annotation token : text.tokens()) {
      Tag temp = token.get(Attrs.PART_OF_SPEECH).cast();
      if (temp != null) {
        if (temp.isInstance(VERB)) {
          return VERB;
        } else if (temp.isInstance(NOUN)) {
          tag = NOUN;
        } else if (temp.isInstance(ADJECTIVE) && tag != NOUN) {
          tag = ADJECTIVE;
        } else if (temp.isInstance(ADVERB) && tag != NOUN) {
          tag = ADVERB;
        }
      }
    }

    return tag;
  }

  /**
   * @return True if the pos is a noun form
   */
  public boolean isNoun() {
    return getUniversalTag() == NOUN;
  }

  /**
   * @return True if the pos is a verb form
   */
  public boolean isVerb() {
    return getUniversalTag() == VERB;
  }

  /**
   * @return True if the pos is an adjective form
   */
  public boolean isAdjective() {
    return getUniversalTag() == ADJECTIVE;
  }

  /**
   * @return True if the pos is an adverb form
   */
  public boolean isAdverb() {
    return getUniversalTag() == ADVERB;
  }

  /**
   * @return True if the pos is a pronoun form
   */
  public boolean isPronoun() {
    return getUniversalTag() == PRONOUN;
  }

  /**
   * @return True if this is a number
   */
  public boolean isNumber() {
    return getUniversalTag() == NUMBER;
  }

  /**
   * @return True if this is an adposition
   */
  public boolean isAdposition() {
    return getUniversalTag() == ADPOSITION;
  }

  /**
   * @return True if this is a conjunction
   */
  public boolean isConjunction() {
    return getUniversalTag() == CONJUNCTION;
  }

  /**
   * @return True if this is a determiner
   */
  public boolean isDeterminer() {
    return getUniversalTag() == DETERMINER;
  }

  /**
   * @return True if this is a particle
   */
  public boolean isParticle() {
    return getUniversalTag() == PARTICLE;
  }

  /**
   * @return True if this is punctuation
   */
  public boolean isPunctuation() {
    return getUniversalTag() == PUNCTUATION;
  }

  /**
   * @return True if this is an other
   */
  public boolean isOther() {
    return getUniversalTag() == OTHER;
  }

  /**
   * @return True if the tag is at the phrase level
   */
  public boolean isPhraseTag() {
    return false;
  }

  /**
   * @return true if the tag is one of the universal tags
   */
  public boolean isUniversal() {
    return false;
  }

  /**
   * @return The treebank string representation
   */
  public String asString() {
    return tag;
  }

  /**
   * @return The parent part of speech or itself if it is a top level pos
   */
  public com.davidbracewell.hermes.tag.POS getParentType() {
    return parentType == null ? this : parentType;
  }

  /**
   * @return The universal tag
   */
  public com.davidbracewell.hermes.tag.POS getUniversalTag() {
    if (this == ANY) {
      return null;
    }
    com.davidbracewell.hermes.tag.POS tag = this;
    while (tag != null && tag.getParentType() != ANY && !tag.isUniversal()) {
      tag = tag.getParentType();
    }
    return tag;
  }

  @Override
  public boolean isInstance(Tag tag) {
    if (tag == null) {
      return false;
    }
    if (tag == com.davidbracewell.hermes.tag.POS.ANY) {
      return true;
    }
    if (tag instanceof com.davidbracewell.hermes.tag.POS) {
      com.davidbracewell.hermes.tag.POS other = (com.davidbracewell.hermes.tag.POS) tag;
      com.davidbracewell.hermes.tag.POS check = this;
      while (check != null) {
        if (check == other) {
          return true;
        }
        if (check == check.getParentType()) {
          return false;
        }
        check = check.getParentType();
      }
      return false;
    }
    return false;
  }

}// END OF PartOfSpeech
