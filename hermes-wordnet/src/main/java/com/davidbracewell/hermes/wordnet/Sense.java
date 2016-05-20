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
import com.davidbracewell.hermes.attribute.POS;
import com.google.common.collect.HashMultimap;

import java.util.List;
import java.util.Set;

/**
 * The interface Sense.
 *
 * @author David B. Bracewell
 */
public interface Sense extends Comparable<Sense> {

  /**
   * ASCII form of a word as entered in the synset by the lexicographer, with spaces replaced by underscore characters
   * (_). The text of the word is case sensitive, in contrast to its form in the corresponding index. pos file, that
   * contains only lower-case forms. In data.adj , a word is followed by a syntactic marker if one was specified in the
   * lexicographer file. A syntactic marker is appended, in parentheses, onto word without any intervening spaces. See
   * wninput(5WN) for a list of the syntactic markers for adjectives.
   *
   * @return the lemma
   */
  String getLemma();

  /**
   * Gets pOS.
   *
   * @return the pOS
   */
  POS getPOS();

  /**
   * Senses in WordNet are generally ordered from most to least frequently used, with the most common sense numbered 1.
   * Frequency of use is determined by the number of times a sense is tagged in the various semantic concordance texts.
   * Senses that are not semantically tagged follow the ordered senses. The tagsense_cnt field for each entry in the
   * index.pos files indicates how many of the senses in the list have been tagged.
   *
   * @return the sense number
   */
  int getSenseNumber();

  /**
   * One digit hexadecimal integer that, when appended onto lemma , uniquely identifies a sense within a lexicographer
   * file. lex_id numbers usually start with 0 , and are incremented as additional senses of the word are added to the
   * same file, although there is no requirement that the numbers be consecutive or begin with 0 . Note that a value of
   * 0 is the default, and therefore is not present in lexicographer files.
   *
   * @return the lexical id
   */
  int getLexicalId();

  /**
   * Gets synset position.
   *
   * @return the synset position
   */
  int getSynsetPosition();

  /**
   * Gets related senses.
   *
   * @param relation the relation
   * @return the related senses
   */
  Set<Sense> getRelatedSenses(WordNetRelation relation);

  /**
   * Gets related senses.
   *
   * @return the related senses
   */
  HashMultimap<WordNetRelation, Sense> getRelatedSenses();

  /**
   * Gets synset.
   *
   * @return the synset
   */
  Synset getSynset();

  /**
   * Gets adjective marker.
   *
   * @return the adjective marker
   */
  AdjectiveMarker getAdjectiveMarker();

  /**
   * Gets id.
   *
   * @return the id
   */
  String getId();

  /**
   * Depth int.
   *
   * @return the int
   */
  int depth();

  /**
   * Gets language.
   *
   * @return the language
   */
  Language getLanguage();

  /**
   * Gets verb frames.
   *
   * @return the verb frames
   */
  List<VerbFrame> getVerbFrames();

}//END OF Sense
