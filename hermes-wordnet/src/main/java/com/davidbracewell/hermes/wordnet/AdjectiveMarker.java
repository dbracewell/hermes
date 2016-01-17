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

package com.davidbracewell.hermes.wordnet;

/**
 * The enum Adjective marker.
 *
 * @author David B. Bracewell
 */
public enum AdjectiveMarker {
  PREDICATE("(p)", "predicate position"),
  PRENOMINAL("(a)", "prenominal (attributive) position"),
  POSTNOMINAL("(ip)", "immediately postnominal position");


  private final String description;
  private final String tag;


  AdjectiveMarker(String tag, String description) {
    this.tag = tag;
    this.description = description;
  }

  /**
   * Gets tag.
   *
   * @return the tag
   */
  public String getTag() {
    return tag;
  }

  /**
   * Gets description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }


  /**
   * From tag.
   *
   * @param string the string
   * @return the adjective marker
   */
  public static AdjectiveMarker fromString(String string) {
    for (AdjectiveMarker marker : values()) {
      if (marker.tag.equals(string)) {
        return marker;
      }
    }
    return AdjectiveMarker.valueOf(string);
  }

}//END OF AdjectiveMarker
