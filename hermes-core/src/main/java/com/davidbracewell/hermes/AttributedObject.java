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

import com.davidbracewell.conversion.Val;

import java.util.Map;
import java.util.Set;

/**
 * <p>In the TIPSTER architecture an attribute represents features associated with different classes. Attributes in
 * TIPSTER are represented as feature-value pairs where features names are arbitrary strings and the values are
 * arbitrary types.In <code>com.davidbracewell.text</code> attributes are also represented as feature-value pairs, but
 * feature names are of the type {@link Attribute} which is a dynamic enum and values are of type
 * <code>Object</code> allowing any possible type. Values are cast to a specific type when calling the {@link
 * #getAttribute(Attribute)} method as a convenience.</p>
 *
 * @author David B. Bracewell
 */
public interface AttributedObject {

  /**
   * Gets the set of attribute names for the attributes associated with the object.
   *
   * @return the attribute names
   */
  Set<Map.Entry<Attribute, Val>> getAttributes();

  /**
   * Determines if an attribute of a given name is associated with the object
   *
   * @param attribute The attribute name
   * @return True if the attribute is associated with the object, False otherwise
   */
  boolean hasAttribute(Attribute attribute);

  /**
   * Gets the value for a given attribute name
   *
   * @param attribute the attribute name
   * @return the value associated with the attribute or null
   */
  Val getAttribute(Attribute attribute);

  /**
   * Sets the value of an attribute. Removes the attribute if the value is null and ignores setting a value if the
   * attribute is null.
   *
   * @param attribute the attribute name
   * @param value     the value
   * @return The old value of the attribute or null
   */
  Val putAttribute(Attribute attribute, Object value);

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
   * Removes an attribute from the object.
   *
   * @param attribute the attribute name
   * @return the value that was associated with the attribute
   */
  Val removeAttribute(Attribute attribute);


}//END OF AttributedObject
