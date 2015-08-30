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
public final class Types {

  /**
   * Entity annotation type
   */
  public static final AnnotationType ENTITY = AnnotationType.create("ENTITY");
  /**
   * phrase chunk annotation type
   */
  public static final AnnotationType PHRASE_CHUNK = AnnotationType.create("PHRASE_CHUNK");
  /**
   * sentence annotation type
   */
  public static final AnnotationType SENTENCE = AnnotationType.create("SENTENCE");
  /**
   * token annotation type
   */
  public static final AnnotationType TOKEN = AnnotationType.create("TOKEN");
  /**
   * dependency node annotation type
   */
  public static final AnnotationType DEPENDENCY_NODE = AnnotationType.create("DEPENDENCY_NODE");
  /**
   * lemma annotation type
   */
  public static final AnnotationType LEMMA = AnnotationType.create("LEMMA");
  /**
   * lexicon match annotation type
   */
  public static final AnnotationType LEXICON_MATCH = AnnotationType.create("LEXICON_MATCH");
  /**
   * part of speech annotation type
   */
  public static final AnnotationType PART_OF_SPEECH = AnnotationType.create("PART_OF_SPEECH");



  public static final AnnotationType TOKEN_TYPE_ENTITY = AnnotationType.create("TOKEN_TYPE_ENTITY");


}//END OF AnnotationTypes
