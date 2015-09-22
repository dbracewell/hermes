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

import com.davidbracewell.DynamicEnum;
import com.davidbracewell.EnumValue;
import com.davidbracewell.config.Config;

import java.io.ObjectStreamException;
import java.util.Collection;

/**
 * The type Entity type.
 *
 * @author David B. Bracewell
 */
public final class EntityType extends EnumValue implements Tag {
  private static final DynamicEnum<EntityType> index = new DynamicEnum<>();
  private static final long serialVersionUID = 1L;
  /**
   * The constant ENTITY.
   */
  public static EntityType ENTITY = new EntityType("ENTITY");

  private EntityType parent = null;

  private EntityType(String name) {
    super(name);
  }

  /**
   * Creates an entity type with the given name.
   *
   * @param name the name of the entity type
   * @return the entity type
   * @throws IllegalArgumentException If the name is invalid
   */
  public static EntityType create(String name) {
    if (index.isDefined(name)) {
      return index.valueOf(name);
    }
    return index.register(new EntityType(name));
  }

  /**
   * Creates an entity type with the given name.
   *
   * @param name   the name of the entity type
   * @param parent the parent entity of this entity type
   * @return the entity type
   * @throws IllegalArgumentException If the name is invalid
   */
  public static EntityType create(String name, EntityType parent) {
    if (index.isDefined(name)) {
      return index.valueOf(name);
    }
    EntityType entityType = index.register(new EntityType(name));
    if (entityType.parent != null && parent != null && entityType.parent != parent) {
      throw new IllegalArgumentException("Attempting to create an Entity named [" + name + "] with parent [" + parent + "], but an entity by that name already exists with the parent [" + entityType.parent + "]");
    }
    entityType.parent = parent;
    return entityType;
  }

  /**
   * Determine if a name is an existing entity type
   *
   * @param name the name
   * @return True if it exists, otherwise False
   */
  public static boolean isDefined(String name) {
    return index.isDefined(name);
  }

  /**
   * Gets the entity type associated with the name.
   *
   * @param name the name as a string
   * @return the entity type for the string
   * @throws IllegalArgumentException if the name is not a valid entity type
   */
  public static EntityType valueOf(String name) {
    return index.valueOf(name);
  }

  /**
   * The current collection of known entity types
   *
   * @return All known entity types
   */
  public static Collection<EntityType> values() {
    return index.values();
  }

  @Override
  public String asString() {
    return name();
  }

  /**
   * Gets this entity types' parent. The root <code>ENTITY</code> will return <code>null</code> for its parent.
   *
   * @return the parent of this entity
   */
  public EntityType getParent() {
    if (this == ENTITY) {
      return null;
    }
    if (parent == null) {
      synchronized (this) {
        if (parent == null) {
          parent = Config.get("EntityType", name(), "parent").as(EntityType.class, ENTITY);
        }
      }
    }
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

  private Object readResolve() throws ObjectStreamException {
    if (isDefined(name())) {
      return index.valueOf(name());
    }
    return this;
  }


}//END OF EntityType
