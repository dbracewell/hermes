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

package com.davidbracewell.hermes.attribute;

import com.davidbracewell.annotation.DynamicEnumeration;

import static com.davidbracewell.hermes.attribute.EntityType.ROOT;

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
   com.davidbracewell.hermes.attribute.EntityType PERSON = com.davidbracewell.hermes.attribute.EntityType.create(
         "PERSON",
         ROOT);
   /**
    * The constant ORGANIZATION.
    */
   com.davidbracewell.hermes.attribute.EntityType ORGANIZATION = com.davidbracewell.hermes.attribute.EntityType.create(
         "ORGANIZATION",
         ROOT);
   /**
    * The constant LOCATION.
    */
   com.davidbracewell.hermes.attribute.EntityType LOCATION = com.davidbracewell.hermes.attribute.EntityType.create(
         "LOCATION",
         ROOT);
   /**
    * The constant NUMBER.
    */
   com.davidbracewell.hermes.attribute.EntityType NUMBER = com.davidbracewell.hermes.attribute.EntityType.create(
         "NUMBER",
         ROOT);

   com.davidbracewell.hermes.attribute.EntityType CARDINAL = com.davidbracewell.hermes.attribute.EntityType.create(
         "CARDINAL",
         NUMBER);

   com.davidbracewell.hermes.attribute.EntityType ORDIANL = com.davidbracewell.hermes.attribute.EntityType.create(
         "ORDINAL",
         NUMBER);


   /**
    * The constant MONEY.
    */
   com.davidbracewell.hermes.attribute.EntityType MONEY = com.davidbracewell.hermes.attribute.EntityType.create("MONEY",
                                                                                                                NUMBER);
   /**
    * The constant PERCENTAGE.
    */
   com.davidbracewell.hermes.attribute.EntityType PERCENTAGE = com.davidbracewell.hermes.attribute.EntityType.create(
         "PERCENTAGE",
         NUMBER);
   /**
    * The constant DATE_TIME.
    */
   com.davidbracewell.hermes.attribute.EntityType DATE_TIME = com.davidbracewell.hermes.attribute.EntityType.create(
         "DATE_TIME",
         ROOT);
   /**
    * The constant DATE.
    */
   com.davidbracewell.hermes.attribute.EntityType DATE = com.davidbracewell.hermes.attribute.EntityType.create("DATE",
                                                                                                               DATE_TIME);
   /**
    * The constant TIME.
    */
   com.davidbracewell.hermes.attribute.EntityType TIME = com.davidbracewell.hermes.attribute.EntityType.create("TIME",
                                                                                                               DATE_TIME);
   /**
    * The constant INTERNET.
    */
   com.davidbracewell.hermes.attribute.EntityType INTERNET = com.davidbracewell.hermes.attribute.EntityType.create(
         "INTERNET",
         ROOT);
   /**
    * The constant EMAIL.
    */
   com.davidbracewell.hermes.attribute.EntityType EMAIL = com.davidbracewell.hermes.attribute.EntityType.create("EMAIL",
                                                                                                                INTERNET);
   /**
    * The constant URL.
    */
   com.davidbracewell.hermes.attribute.EntityType URL = com.davidbracewell.hermes.attribute.EntityType.create("URL",
                                                                                                              INTERNET);
   /**
    * The constant EMOTICON.
    */
   com.davidbracewell.hermes.attribute.EntityType EMOTICON = com.davidbracewell.hermes.attribute.EntityType.create(
         "EMOTICON",
         INTERNET);
   /**
    * The constant HASHTAG.
    */
   com.davidbracewell.hermes.attribute.EntityType HASH_TAG = com.davidbracewell.hermes.attribute.EntityType.create(
         "HASH_TAG",
         INTERNET);
   /**
    * The constant HASHTAG.
    */
   com.davidbracewell.hermes.attribute.EntityType REPLY = com.davidbracewell.hermes.attribute.EntityType.create("REPLY",
                                                                                                                INTERNET);
}//END OF Entities
