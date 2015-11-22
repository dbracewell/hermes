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

import com.davidbracewell.annotation.DynamicEnumeration;

import static com.davidbracewell.hermes.tag.EntityType.ROOT;
/**
 * The interface Entities.
 *
 * @author David B. Bracewell
 */
@DynamicEnumeration(
  className = "EntityType",
  hierarchical = true,
  configPrefix = "Entity",
  rootName = "ENTITY"
)
public interface Entities {

  /**
   * The constant PERSON.
   */
  com.davidbracewell.hermes.tag.EntityType PERSON = com.davidbracewell.hermes.tag.EntityType.create("PERSON", ROOT);
  /**
   * The constant ORGANIZATION.
   */
  com.davidbracewell.hermes.tag.EntityType ORGANIZATION = com.davidbracewell.hermes.tag.EntityType.create("ORGANIZATION", ROOT);
  /**
   * The constant LOCATION.
   */
  com.davidbracewell.hermes.tag.EntityType LOCATION = com.davidbracewell.hermes.tag.EntityType.create("LOCATION", ROOT);
  /**
   * The constant NUMBER.
   */
  com.davidbracewell.hermes.tag.EntityType NUMBER = com.davidbracewell.hermes.tag.EntityType.create("NUMBER", ROOT);
  /**
   * The constant MONEY.
   */
  com.davidbracewell.hermes.tag.EntityType MONEY = com.davidbracewell.hermes.tag.EntityType.create("MONEY", NUMBER);
  /**
   * The constant PERCENTAGE.
   */
  com.davidbracewell.hermes.tag.EntityType PERCENTAGE = com.davidbracewell.hermes.tag.EntityType.create("PERCENTAGE", NUMBER);
  /**
   * The constant DATE_TIME.
   */
  com.davidbracewell.hermes.tag.EntityType DATE_TIME = com.davidbracewell.hermes.tag.EntityType.create("DATE_TIME", ROOT);
  /**
   * The constant DATE.
   */
  com.davidbracewell.hermes.tag.EntityType DATE = com.davidbracewell.hermes.tag.EntityType.create("DATE", DATE_TIME);
  /**
   * The constant TIME.
   */
  com.davidbracewell.hermes.tag.EntityType TIME = com.davidbracewell.hermes.tag.EntityType.create("TIME", DATE_TIME);
  /**
   * The constant INTERNET.
   */
  com.davidbracewell.hermes.tag.EntityType INTERNET = com.davidbracewell.hermes.tag.EntityType.create("INTERNET", ROOT);
  /**
   * The constant EMAIL.
   */
  com.davidbracewell.hermes.tag.EntityType EMAIL = com.davidbracewell.hermes.tag.EntityType.create("EMAIL", INTERNET);
  /**
   * The constant URL.
   */
  com.davidbracewell.hermes.tag.EntityType URL = com.davidbracewell.hermes.tag.EntityType.create("URL", INTERNET);
  /**
   * The constant EMOTICON.
   */
  com.davidbracewell.hermes.tag.EntityType EMOTICON = com.davidbracewell.hermes.tag.EntityType.create("EMOTICON", INTERNET);
  /**
   * The constant HASHTAG.
   */
  com.davidbracewell.hermes.tag.EntityType HASH_TAG = com.davidbracewell.hermes.tag.EntityType.create("HASH_TAG", INTERNET);
  /**
   * The constant HASHTAG.
   */
  com.davidbracewell.hermes.tag.EntityType REPLY = com.davidbracewell.hermes.tag.EntityType.create("REPLY", INTERNET);
}//END OF Entities
