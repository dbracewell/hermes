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
 * @author David B. Bracewell
 */
public final class Attrs {

  /**
   * Confidence value associated with an annotation
   */
  public static final Attribute CONFIDENCE = Attribute.create("CONFIDENCE");
  /**
   * The lemma version of a span
   */
  public static final Attribute LEMMA = Attribute.create("LEMMA");
  /**
   * The tag associated with a span
   */
  public static final Attribute TAG = Attribute.create("TAG");
  /**
   * The type of token
   */
  public static final Attribute TOKEN_TYPE = Attribute.create("TOKEN_TYPE");
  /**
   * The index of a span with regards to a document
   */
  public static final Attribute INDEX = Attribute.create("INDEX");
  /**
   * The part-of-speech assocaited with a span
   */
  public static final Attribute PART_OF_SPEECH = Attribute.create("PART_OF_SPEECH");
  /**
   * The Language assocaited with a span
   */
  public static final Attribute LANGUAGE = Attribute.create("LANGUAGE");
  /**
   * Document author
   */
  public static final Attribute COLLECTION = Attribute.create("COLLECTION");
  /**
   * Document author
   */
  public static final Attribute AUTHOR = Attribute.create("AUTHOR");
  /**
   * Document CATEGORY
   */
  public static final Attribute CATEGORY = Attribute.create("CATEGORY");
  /**
   * Document keywords
   */
  public static final Attribute KEYPHRASES = Attribute.create("KEYPHRASES");
  /**
   * Publication date of document
   */
  public static final Attribute PUBLICATION_DATE = Attribute.create("PUBLICATION_DATE");
  /**
   * Document source
   */
  public static final Attribute SOURCE = Attribute.create("SOURCE");
  /**
   * Document title
   */
  public static final Attribute TITLE = Attribute.create("TITLE");
  /**
   * File used to create the document
   */
  public static final Attribute FILE = Attribute.create("FILE");

  public static final Attribute PARENT = Attribute.create("PARENT");

  public static final Attribute DEPENDENCY_RELATION = Attribute.create("DEPENDENCY_RELATION");

  public static final Attribute ENTITY_TYPE = Attribute.create("ENTITY_TYPE");

}//END OF Attrs
