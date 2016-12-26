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
   Set<Map.Entry<AttributeType, Val>> attributeValues();

   /**
    * Gets the attributes associated with the object
    *
    * @return the attributes associated with the object.
    */
   Set<AttributeType> attributes();

   /**
    * Determines if an attribute of a given name is associated with the object
    *
    * @param attributeType The attribute name
    * @return True if the attribute is associated with the object, False otherwise
    */
   boolean contains(AttributeType attributeType);

   /**
    * Gets the value for a given attribute name
    *
    * @param attributeType the attribute name
    * @return the value associated with the attribute or null
    */
   Val get(AttributeType attributeType);

   /**
    * Get t.
    *
    * @param <T>           the type parameter
    * @param attributeType the attribute type
    * @param clazz         the clazz
    * @return the t
    */
   default <T> T get(@NonNull AttributeType attributeType, @NonNull Class<T> clazz) {
      return get(attributeType).as(clazz);
   }

   /**
    * Gets attribute as int.
    *
    * @param attributeType the attribute type
    * @return the attribute as int
    */
   default int getAttributeAsInt(@NonNull AttributeType attributeType) {
      return get(attributeType).asIntegerValue();
   }

   /**
    * Gets attribute as double.
    *
    * @param attributeType the attribute type
    * @return the attribute as double
    */
   default double getAttributeAsDouble(@NonNull AttributeType attributeType) {
      return get(attributeType).asDouble();
   }

   /**
    * Gets attribute as string.
    *
    * @param attributeType the attribute type
    * @return the attribute as string
    */
   default String getAttributeAsString(@NonNull AttributeType attributeType) {
      return get(attributeType).asString();
   }

   /**
    * Gets attribute as list.
    *
    * @param <T>           the type parameter
    * @param attributeType the attribute type
    * @return the attribute as list
    */
   default <T> List<T> getAttributeAsList(@NonNull AttributeType attributeType) {
      return get(attributeType).cast();
   }

   /**
    * Gets attribute as tag.
    *
    * @param <T>           the type parameter
    * @param attributeType the attribute type
    * @return the attribute as tag
    */
   default <T extends Tag> T getAttributeAsTag(@NonNull AttributeType attributeType) {
      return get(attributeType).cast();
   }


   /**
    * Gets attribute as set.
    *
    * @param <T>           the type parameter
    * @param attributeType the attribute type
    * @return the attribute as set
    */
   default <T> Set<T> getAttributeAsSet(@NonNull AttributeType attributeType) {
      return get(attributeType).cast();
   }

   /**
    * Gets attribute as map.
    *
    * @param <K>           the type parameter
    * @param <V>           the type parameter
    * @param attributeType the attribute type
    * @return the attribute as map
    */
   default <K, V> Map<K, V> getAttributeAsMap(@NonNull AttributeType attributeType) {
      return get(attributeType).cast();
   }

   /**
    * Sets the value of an attribute. Removes the attribute if the value is null and ignores setting a value if the
    * attribute is null.
    *
    * @param attributeType the attribute name
    * @param value         the value
    * @return The old value of the attribute or null
    */
   Val put(AttributeType attributeType, Object value);

   /**
    * Sets the value of an attribute if a value is not already set. Removes the attribute if the value is null and
    * ignores setting a value if the attribute is null.
    *
    * @param attributeType the attribute name
    * @param value         the value
    * @return The old value of the attribute or null
    */
   default Val putIfAbsent(AttributeType attributeType, Object value) {
      if (!contains(attributeType)) {
         synchronized (this) {
            if (!contains(attributeType)) {
               return put(attributeType, value);
            }
         }
      }
      return null;
   }

   /**
    * Sets the value of an attribute if a value is not already set. Removes the attribute if the value is null and
    * ignores setting a value if the attribute is null.
    *
    * @param attributeType the attribute name
    * @param supplier      the supplier to generate the new value
    * @return The old value of the attribute or null
    */
   default Val putIfAbsent(AttributeType attributeType, @NonNull Supplier<?> supplier) {
      if (!contains(attributeType)) {
         synchronized (this) {
            if (!contains(attributeType)) {
               return put(attributeType, supplier.get());
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
   default void putAll(@NonNull Map<AttributeType, ?> map) {
      map.entrySet().forEach(e -> this.put(e.getKey(), e.getValue()));
   }

   /**
    * Removes an attribute from the object.
    *
    * @param attributeType the attribute name
    * @return the value that was associated with the attribute
    */
   Val remove(AttributeType attributeType);


}//END OF AttributedObject
