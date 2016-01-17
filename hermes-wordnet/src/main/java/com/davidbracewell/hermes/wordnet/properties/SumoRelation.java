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
 * @author dbracewell
 */
public enum SumoRelation {
  EQUAL,
  INSTANCE_OF,
  SUBSUMED,
  NOT_EQUAL,
  NOT_INSTANCE_OF,
  NOT_SUBSUMED;


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