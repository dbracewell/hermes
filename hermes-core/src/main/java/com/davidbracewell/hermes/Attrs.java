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

package com.davidbracewell.hermes;

/**
 * Commonly used Attribute types
 *
 * @author David B. Bracewell
 */
public interface Attrs {

  static Attribute attribute(String name) {
    return Attribute.create(name);
  }

  Attribute SENSE = Attribute.create("SENSE");

  /**
   * Document author
   */
  Attribute AUTHOR = Attribute.create("AUTHOR");
  /**
   * Document CATEGORY
   */
  Attribute CATEGORY = Attribute.create("CATEGORY");
  /**
   * Document author
   */
  Attribute COLLECTION = Attribute.create("COLLECTION");
  /**
   * Confidence value associated with an annotation
   */
  Attribute CONFIDENCE = Attribute.create("CONFIDENCE");
  /**
   * The constant DEPENDENCY_RELATION.
   */
  Attribute DEPENDENCY_RELATION = Attribute.create("DEPENDENCY_RELATION");
  /**
   * The constant ENTITY_TYPE.
   */
  Attribute ENTITY_TYPE = Attribute.create("ENTITY_TYPE");
  /**
   * File used to create the document
   */
  Attribute FILE = Attribute.create("FILE");
  /**
   * The index of a span with regards to a document
   */
  Attribute INDEX = Attribute.create("INDEX");
  /**
   * Document keywords
   */
  Attribute KEYPHRASES = Attribute.create("KEYPHRASES");
  /**
   * The Language assocaited with a span
   */
  Attribute LANGUAGE = Attribute.create("LANGUAGE");
  /**
   * The lemma version of a span
   */
  Attribute LEMMA = Attribute.create("LEMMA");
  /**
   * The constant PARENT.
   */
  Attribute PARENT = Attribute.create("PARENT");
  /**
   * The part-of-speech assocaited with a span
   */
  Attribute PART_OF_SPEECH = Attribute.create("PART_OF_SPEECH");
  /**
   * Publication date of document
   */
  Attribute PUBLICATION_DATE = Attribute.create("PUBLICATION_DATE");
  /**
   * Document source
   */
  Attribute SOURCE = Attribute.create("SOURCE");
  /**
   * The STEM.
   */
  Attribute STEM = Attribute.create("STEM");
  /**
   * The tag associated with a span
   */
  Attribute TAG = Attribute.create("TAG");
  /**
   * Document title
   */
  Attribute TITLE = Attribute.create("TITLE");
  /**
   * The type of token
   */
  Attribute TOKEN_TYPE = Attribute.create("TOKEN_TYPE");
  /**
   * The TRANSLITERATION.
   */
  Attribute TRANSLITERATION = Attribute.create("TRANSLITERATION");


  Attribute LEXICON_MATCH = Attribute.create("LEXICON_MATCH");

  Attribute LYRE_RULE = Attribute.create("LYRE_RULE");


}//END OF Attrs
