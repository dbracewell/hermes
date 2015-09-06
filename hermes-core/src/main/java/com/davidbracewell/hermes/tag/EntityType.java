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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author David B. Bracewell
 */
public enum EntityType implements Tag {

  /**
   * ******************************
   * Special Root node
   * *******************************
   */
  ENTITY_ROOT(null),

  /**
   * ******************************
   * People
   * *******************************
   */
  PERSON(ENTITY_ROOT),

  /**
   * ******************************
   * Organizations
   * *******************************
   */
  ORGANIZATION(ENTITY_ROOT),

  /**
   * ******************************
   * Locations
   * *******************************
   */
  LOCATION(ENTITY_ROOT),

  /**
   * ******************************
   * Numbers
   * *******************************
   */
  NUMBER(ENTITY_ROOT),
  MONEY(NUMBER),
  PERCENTAGE(NUMBER),

  /**
   * ******************************
   * Date / Time
   * *******************************
   */
  DATE_TIME(NUMBER),
  DATE(DATE_TIME),
  TIME(DATE_TIME),

  /*********************************
   *  Web Related
   ********************************/
  WWW(ENTITY_ROOT),
  EMAIL(WWW),
  URL(WWW);


  private final EntityType parent;

  private EntityType(EntityType parent) {
    this.parent = parent;
  }

  /**
   * @return The path from this entity type to the dummy ENTITY_ROOT
   */
  public List<EntityType> getLineage() {
    List<EntityType> lineage = Lists.newArrayList();
    EntityType et = this;
    while (et != null) {
      lineage.add(et);
      et = et.parent;
    }
    return lineage;
  }

  /**
   * @return The parent type of this type
   */
  public EntityType getParent() {
    return parent;
  }

  @Override
  public boolean isInstance(Tag tag) {
    if (tag instanceof EntityType) {
      EntityType et = this;
      while (et != null) {
        if (et == tag) {
          return true;
        }
        et = et.parent;
      }
    }
    return false;
  }

  @Override
  public String asString() {
    return this.toString();
  }


}//END OF EntityType
