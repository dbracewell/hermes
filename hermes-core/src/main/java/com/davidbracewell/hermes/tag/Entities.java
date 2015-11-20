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
  EntityType PERSON = EntityType.create("PERSON", ROOT);
  /**
   * The constant ORGANIZATION.
   */
  EntityType ORGANIZATION = EntityType.create("ORGANIZATION", ROOT);
  /**
   * The constant LOCATION.
   */
  EntityType LOCATION = EntityType.create("LOCATION", ROOT);
  /**
   * The constant NUMBER.
   */
  EntityType NUMBER = EntityType.create("NUMBER", ROOT);
  /**
   * The constant MONEY.
   */
  EntityType MONEY = EntityType.create("MONEY", NUMBER);
  /**
   * The constant PERCENTAGE.
   */
  EntityType PERCENTAGE = EntityType.create("PERCENTAGE", NUMBER);
  /**
   * The constant DATE_TIME.
   */
  EntityType DATE_TIME = EntityType.create("DATE_TIME", ROOT);
  /**
   * The constant DATE.
   */
  EntityType DATE = EntityType.create("DATE", DATE_TIME);
  /**
   * The constant TIME.
   */
  EntityType TIME = EntityType.create("TIME", DATE_TIME);
  /**
   * The constant INTERNET.
   */
  EntityType INTERNET = EntityType.create("INTERNET", ROOT);
  /**
   * The constant EMAIL.
   */
  EntityType EMAIL = EntityType.create("EMAIL", INTERNET);
  /**
   * The constant URL.
   */
  EntityType URL = EntityType.create("URL", INTERNET);
  /**
   * The constant EMOTICON.
   */
  EntityType EMOTICON = EntityType.create("EMOTICON", INTERNET);

}//END OF Entities
