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

import com.davidbracewell.Tag;
import com.davidbracewell.conversion.Val;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * <p>
 * An <code>AttributedObject</code> is an object that has zero or more attributes associated with it. An attribute
 * may represent a learned annotation (e.g. part of speech or word sense), metadata (e.g. author or source url), or a
 * feature (e.g. suffix or phonetic encoding).
 * </p>
 *
 * @author David B. Bracewell
 */
public interface AttributedObject {

  /**
   * Gets the set of attributes and values associated with the object.
   *
   * @return the attributes and their values
   */
  Set<Map.Entry<Attribute, Val>> attributeValues();

  /**
   * Gets the attributes associated with the object
   *
   * @return the attributes associated with the object.
   */
  Set<Attribute> attributes();

  /**
   * Determines if an attribute of a given name is associated with the object
   *
   * @param attribute The attribute name
   * @return True if the attribute is associated with the object, False otherwise
   */
  boolean contains(Attribute attribute);

  /**
   * Gets the value for a given attribute name
   *
   * @param attribute the attribute name
   * @return the value associated with the attribute or null
   */
  Val get(Attribute attribute);

  default <T> T get(@NonNull Attribute attribute, @NonNull Class<T> clazz) {
    return get(attribute).as(clazz);
  }

  default int getAttributeAsInt(@NonNull Attribute attribute) {
    return get(attribute).asIntegerValue();
  }

  default double getAttributeAsDouble(@NonNull Attribute attribute) {
    return get(attribute).asDouble();
  }

  default String getAttributeAsString(@NonNull Attribute attribute) {
    return get(attribute).asString();
  }

  default <T> List<T> getAttributeAsList(@NonNull Attribute attribute) {
    return get(attribute).cast();
  }

  default <T extends Tag> T getAttributeAsTag(@NonNull Attribute attribute) {
    return get(attribute).cast();
  }


  default <T> Set<T> getAttributeAsSet(@NonNull Attribute attribute) {
    return get(attribute).cast();
  }

  default <K, V> Map<K, V> getAttributeAsMap(@NonNull Attribute attribute) {
    return get(attribute).cast();
  }

  /**
   * Sets the value of an attribute. Removes the attribute if the value is null and ignores setting a value if the
   * attribute is null.
   *
   * @param attribute the attribute name
   * @param value     the value
   * @return The old value of the attribute or null
   */
  Val put(Attribute attribute, Object value);

  /**
   * Sets the value of an attribute if a value is not already set. Removes the attribute if the value is null and
   * ignores setting a value if the attribute is null.
   *
   * @param attribute the attribute name
   * @param value     the value
   * @return The old value of the attribute or null
   */
  default Val putIfAbsent(Attribute attribute, Object value) {
    if (!contains(attribute)) {
      synchronized (this) {
        if (!contains(attribute)) {
          return put(attribute, value);
        }
      }
    }
    return null;
  }

  /**
   * Sets the value of an attribute if a value is not already set. Removes the attribute if the value is null and
   * ignores setting a value if the attribute is null.
   *
   * @param attribute the attribute name
   * @param supplier  the supplier to generate the new value
   * @return The old value of the attribute or null
   */
  default Val putIfAbsent(Attribute attribute, @NonNull Supplier<?> supplier) {
    if (!contains(attribute)) {
      synchronized (this) {
        if (!contains(attribute)) {
          return put(attribute, supplier.get());
        }
      }
    }
    return null;
  }

  /**
   * Sets all attributes in a given map.
   *
   * @param map the attribute-value map
   */
  default void putAll(Map<Attribute, ?> map) {
    if (map != null) {
      map.entrySet().stream().forEach(e -> this.put(e.getKey(), e.getValue()));
    }
  }

  /**
   * Removes an attribute from the object.
   *
   * @param attribute the attribute name
   * @return the value that was associated with the attribute
   */
  Val remove(Attribute attribute);


}//END OF AttributedObject
