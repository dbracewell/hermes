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

package com.davidbracewell.hermes.wordnet.properties;

/**
 * <p>Sumo Concept: see http://www.adampease.org/OP/</p>
 *
 * @author dbracewell
 */
public enum SumoRelation {
  /**
   * Equal sumo relation.
   */
  EQUAL,
  /**
   * Instance of sumo relation.
   */
  INSTANCE_OF,
  /**
   * Subsumed sumo relation.
   */
  SUBSUMED,
  /**
   * Not equal sumo relation.
   */
  NOT_EQUAL,
  /**
   * Not instance of sumo relation.
   */
  NOT_INSTANCE_OF,
  /**
   * Not subsumed sumo relation.
   */
  NOT_SUBSUMED;


  /**
   * From string sumo relation.
   *
   * @param string the string
   * @return the sumo relation
   */
  public static SumoRelation fromString(String string) {
    try {
      return SumoRelation.valueOf(string);
    } catch (Exception e) {
      //ignore
    }
    switch (string) {
      case "=":
        return EQUAL;
      case "+":
        return SUBSUMED;
      case "@":
        return INSTANCE_OF;
      case ":":
        return NOT_EQUAL;
      case "[":
        return NOT_SUBSUMED;
      case "]":
        return NOT_INSTANCE_OF;
    }
    throw new IllegalArgumentException("Unknown relation " + string);
  }

}//END OF SumoRelation