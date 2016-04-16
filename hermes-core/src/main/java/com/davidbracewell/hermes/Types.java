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
 * <p>Common Annotation Types</p>
 *
 * @author David B. Bracewell
 */
public interface Types {

  /**
   * Document author
   */
  AttributeType AUTHOR = AttributeType.create("AUTHOR");
  /**
   * Document CATEGORY
   */
  AttributeType CATEGORY = AttributeType.create("CATEGORY");
  /**
   * Document author
   */
  AttributeType COLLECTION = AttributeType.create("COLLECTION");
  /**
   * Confidence value associated with an annotation
   */
  AttributeType CONFIDENCE = AttributeType.create("CONFIDENCE");
  /**
   * The constant DEPENDENCY.
   */
  RelationType DEPENDENCY = RelationType.create("DEPENDENCY");
  /**
   * The constant DEPENDENCY_RELATION.
   */
  AttributeType DEPENDENCY_RELATION = AttributeType.create("DEPENDENCY_RELATION");
  /**
   * Entity annotation type
   */
  AnnotationType ENTITY = AnnotationType.create("ENTITY");
  /**
   * The constant ENTITY_TYPE.
   */
  AttributeType ENTITY_TYPE = AttributeType.create("ENTITY_TYPE");
  /**
   * File used to create the document
   */
  AttributeType FILE = AttributeType.create("FILE");
  /**
   * The index of a span with regards to a document
   */
  AttributeType INDEX = AttributeType.create("INDEX");
  /**
   * Document keywords
   */
  AttributeType KEYPHRASES = AttributeType.create("KEYPHRASES");
  /**
   * The Language assocaited with a span
   */
  AttributeType LANGUAGE = AttributeType.create("LANGUAGE");
  /**
   * The lemma version of a span
   */
  AttributeType LEMMA = AttributeType.create("LEMMA");
  /**
   * lexicon match annotation type
   */
  AnnotationType LEXICON_MATCH = AnnotationType.create("LEXICON_MATCH");
  /**
   * The constant LYRE_RULE.
   */
  AttributeType LYRE_RULE = AttributeType.create("LYRE_RULE");
  /**
   * The constant MATCHED_STRING.
   */
  AttributeType MATCHED_STRING = AttributeType.create("MATCHED_STRING");
  /**
   * The constant PARENT.
   */
  AttributeType PARENT = AttributeType.create("PARENT");
  /**
   * The part-of-speech assocaited with a span
   */
  AttributeType PART_OF_SPEECH = AttributeType.create("PART_OF_SPEECH");
  /**
   * phrase chunk annotation type
   */
  AnnotationType PHRASE_CHUNK = AnnotationType.create("PHRASE_CHUNK");
  /**
   * Publication date of document
   */
  AttributeType PUBLICATION_DATE = AttributeType.create("PUBLICATION_DATE");
  /**
   * The constant SENSE.
   */
  AttributeType SENSE = AttributeType.create("SENSE");
  /**
   * sentence annotation type
   */
  AnnotationType SENTENCE = AnnotationType.create("SENTENCE");
  /**
   * Document source
   */
  AttributeType SOURCE = AttributeType.create("SOURCE");
  /**
   * The STEM.
   */
  AttributeType STEM = AttributeType.create("STEM");
  /**
   * The tag associated with a span
   */
  AttributeType TAG = AttributeType.create("TAG");
  /**
   * Document title
   */
  AttributeType TITLE = AttributeType.create("TITLE");
  /**
   * token annotation type
   */
  AnnotationType TOKEN = AnnotationType.create("TOKEN");
  /**
   * The type of token
   */
  AttributeType TOKEN_TYPE = AttributeType.create("TOKEN_TYPE");
  /**
   * The constant TOKEN_TYPE_ENTITY.
   */
  AnnotationType TOKEN_TYPE_ENTITY = AnnotationType.create("TOKEN_TYPE_ENTITY");
  /**
   * The TRANSLITERATION.
   */
  AttributeType TRANSLITERATION = AttributeType.create("TRANSLITERATION");
  /**
   * The constant WORD_SENSE.
   */
  AnnotationType WORD_SENSE = AnnotationType.create("WORD_SENSE");

  /**
   * Annotation annotation type.
   *
   * @param name the name
   * @return the annotation type
   */
  static AnnotationType annotation(String name) {
    return AnnotationType.create(name);
  }

  /**
   * Attribute attribute type.
   *
   * @param name the name
   * @return the attribute type
   */
  static AttributeType attribute(String name) {
    return AttributeType.create(name);
  }

  /**
   * Relation relation type.
   *
   * @param name the name
   * @return the relation type
   */
  static RelationType relation(String name) {
    return RelationType.create(name);
  }


}//END OF AnnotationTypes
