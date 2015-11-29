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

  static AnnotationType type(String name) {
    return AnnotationType.create(name);
  }

  /**
   * Entity annotation type
   */
  AnnotationType ENTITY = AnnotationType.create("ENTITY");
  /**
   * lemma annotation type
   */
  AnnotationType LEMMA = AnnotationType.create("LEMMA");
  /**
   * lexicon match annotation type
   */
  AnnotationType LEXICON_MATCH = AnnotationType.create("LEXICON_MATCH");
  /**
   * part of speech annotation type
   */
  AnnotationType PART_OF_SPEECH = AnnotationType.create("PART_OF_SPEECH");
  /**
   * phrase chunk annotation type
   */
  AnnotationType PHRASE_CHUNK = AnnotationType.create("PHRASE_CHUNK");
  /**
   * sentence annotation type
   */
  AnnotationType SENTENCE = AnnotationType.create("SENTENCE");

  /**
   * The STEM.
   */
  AnnotationType STEM = AnnotationType.create("STEM");

  /**
   * token annotation type
   */
  AnnotationType TOKEN = AnnotationType.create("TOKEN");
  /**
   * The constant TOKEN_TYPE_ENTITY.
   */
  AnnotationType TOKEN_TYPE_ENTITY = AnnotationType.create("TOKEN_TYPE_ENTITY");
  /**
   * The constant TRANSLITERATION.
   */
  AnnotationType TRANSLITERATION = AnnotationType.create("TRANSLITERATION");

  AnnotationType DEPENDENCY = AnnotationType.create("DEPENDENCY");

}//END OF AnnotationTypes
