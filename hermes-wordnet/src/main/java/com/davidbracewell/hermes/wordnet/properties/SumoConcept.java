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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Sumo Concept: see http://www.adampease.org/OP/</p>
 *
 * @author dbracewell
 */
public class SumoConcept implements Property, Serializable {
  private static final long serialVersionUID = 1L;
  private final String conceptName;
  private final SumoRelation relation;

  /**
   * Instantiates a new Sumo concept.
   *
   * @param conceptName the concept name
   * @param relation    the relation
   */
  public SumoConcept(String conceptName, SumoRelation relation) {
    this.conceptName = conceptName;
    this.relation = relation;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return conceptName;
  }

  /**
   * Gets relation.
   *
   * @return the relation
   */
  public SumoRelation getRelation() {
    return relation;
  }

  @Override
  public String toString() {
    return "SumoConcept{" +
      "conceptName='" + conceptName + '\'' +
      ", relation=" + relation +
      '}';
  }

  @Override
  public Object get(String key) {
    if (key == null) {
      return null;
    }
    switch (key.toLowerCase()) {
      case "concept":
        return conceptName;
      case "relation":
        return relation;
    }
    return null;
  }

  @Override
  public Set<String> keySet() {
    return new HashSet<>(Arrays.asList("concept", "relation"));
  }

}//END OF SumoConcept
