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

import com.davidbracewell.conversion.Cast;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public interface AttributedObject {

  /**
   * Gets the set of attribute names for the attributes associated with the object.
   *
   * @return the attribute names
   */
  default Set<Attribute> attributeNames() {
    return getAttributes().keySet();
  }

  /**
   * Determines if an attribute of a given name is associated with the object
   *
   * @param attribute The attribute name
   * @return True if the attribute is associated with the object, False otherwise
   */
  default boolean containsAttribute(Attribute attribute) {
    return getAttributes().containsKey(attribute);
  }

  /**
   * Gets the value for a given attribute name
   *
   * @param attribute the attribute name
   * @return the value associated with the attribute or null
   */
  default <T> T getAttribute(Attribute attribute) {
    return Cast.as(getAttributes().get(attribute));
  }

  /**
   * Gets the value for a given attribute name
   *
   * @param attribute the attribute name
   * @param clazz     The type of the attribute
   * @return the value associated with the attribute or null
   */
  default <T> T getAttribute(Attribute attribute, @Nonnull Class<T> clazz) {
    return Cast.as(getAttributes().get(attribute), clazz);
  }

  /**
   * Exposes the underlying attributes as a Map
   *
   * @return The attribute names and values as a map
   */
  Map<Attribute, Object> getAttributes();

  /**
   * Sets all attributes in a given map.
   *
   * @param map the attribute-value map
   */
  default void putAllAttributes(Map<Attribute, ?> map) {
    if (map != null) {
      map.entrySet().stream().forEach(e -> this.putAttribute(e.getKey(), e.getValue()));
    }
  }

  /**
   * Sets the value of an attribute. Removes the attribute if the value is null and ignores setting a value if the
   * attribute is null.
   *
   * @param <T>       the type of the value associated with the attribute
   * @param attribute the attribute name
   * @param value     the value
   * @return The old value of the attribute or null
   */
  default <T> T putAttribute(Attribute attribute, Object value) {
    if (attribute != null) {
      if (value == null) {
        return removeAttribute(attribute);
      }
      return Cast.as(getAttributes().put(attribute, value));
    }
    return null;
  }

  /**
   * Removes an attribute from the object.
   *
   * @param <T>       the type of the value associated with the attribute being removed
   * @param attribute the attribute name
   * @return the value that was associated with the attribute
   */
  default <T> T removeAttribute(Attribute attribute) {
    return Cast.as(getAttributes().remove(attribute));
  }


}//END OF AttributedObject
