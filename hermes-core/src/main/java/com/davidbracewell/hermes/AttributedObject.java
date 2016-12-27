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

import java.util.HashMap;
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
   Set<Map.Entry<AttributeType, Val>> attributeEntrySet();

   /**
    * Gets the attribute types associated with the object
    *
    * @return the attribute types associated with the object.
    */
   Set<AttributeType> attributeTypeSet();

   /**
    * Determines if an attribute of a given type is associated with the object
    *
    * @param attributeType The attribute type
    * @return True if the attribute is associated with the object, False otherwise
    */
   boolean contains(AttributeType attributeType);

   /**
    * Gets the value for a given attribute type
    *
    * @param attributeType the attribute type
    * @return the value associated with the attribute or null
    */
   Val get(AttributeType attributeType);

   /**
    * Gets the value for a given attribute type converting it to the given class.
    *
    * @param <T>           the value type of the attribute
    * @param attributeType the attribute type
    * @param clazz         Class information for the value
    * @return the the value of the given attribute type or null
    */
   default <T> T get(@NonNull AttributeType attributeType, @NonNull Class<T> clazz) {
      return get(attributeType).as(clazz);
   }

   /**
    * Gets the value of the given attribute type as an int value
    *
    * @param attributeType the attribute type
    * @return the attribute value as an int
    */
   default int getAsInt(@NonNull AttributeType attributeType) {
      return get(attributeType).asIntegerValue();
   }

   /**
    * Gets the value of the given attribute type as a long value
    *
    * @param attributeType the attribute type
    * @return the attribute value as a long
    */
   default long getAsLong(@NonNull AttributeType attributeType) {
      return get(attributeType).asLongValue();
   }


   /**
    * Gets the value of the given attribute type as a boolean value
    *
    * @param attributeType the attribute type
    * @return the attribute value as a boolean
    */
   default boolean getAsBoolean(@NonNull AttributeType attributeType) {
      return get(attributeType).asBooleanValue();
   }

   /**
    * Gets the value of the given attribute type as a double value
    *
    * @param attributeType the attribute type
    * @return the attribute value as a double
    */
   default double getAsDouble(@NonNull AttributeType attributeType) {
      return get(attributeType).asDouble();
   }

   /**
    * Gets the value of the given attribute type as a string value
    *
    * @param attributeType the attribute type
    * @return the attribute value as a string
    */
   default String getAsString(@NonNull AttributeType attributeType) {
      return get(attributeType).asString();
   }

   /**
    * Gets the value of the given attribute type as a list
    *
    * @param <T>           the list element type
    * @param attributeType the attribute type
    * @param elementType   class information for the element type
    * @return the attribute value as a list
    */
   default <T> List<T> getAsList(@NonNull AttributeType attributeType, @NonNull Class<T> elementType) {
      return get(attributeType).asList(elementType);
   }

   /**
    * Gets the value of the given attribute type as a tag
    *
    * @param <T>           the tag type
    * @param attributeType the attribute type
    * @return the attribute value as a tag
    */
   default <T extends Tag> T getAttributeAsTag(@NonNull AttributeType attributeType) {
      return get(attributeType).cast();
   }

   /**
    * Gets the value of the given attribute type as a set
    *
    * @param <T>           the list element type
    * @param attributeType the attribute type
    * @param elementType   class information for the element set
    * @return the attribute value as a list
    */
   default <T> Set<T> getAsSet(@NonNull AttributeType attributeType, @NonNull Class<T> elementType) {
      return get(attributeType).asSet(elementType);
   }

   /**
    * Gets the value of the given attribute type as a map
    *
    * @param <V>           the value element type
    * @param attributeType the attribute type
    * @param valueClass    class information for the value
    * @return the attribute value as a map
    */
   default <V> Map<String, V> getAsMap(@NonNull AttributeType attributeType, @NonNull Class<V> valueClass) {
      return get(attributeType).asMap(HashMap.class, String.class, valueClass);
   }

   /**
    * Sets the value of an attribute. Removes the attribute if the value is null and ignores setting a value if the
    * attribute is null.
    *
    * @param attributeType the attribute type
    * @param value         the value
    * @return The old value of the attribute or null
    */
   Val put(AttributeType attributeType, Object value);

   /**
    * Sets the value of an attribute if a value is not already set. Removes the attribute if the value is null and
    * ignores setting a value if the attribute is null.
    *
    * @param attributeType the attribute type
    * @param value         the value
    * @return The old value of the attribute or null
    */
   default Val putIfAbsent(AttributeType attributeType, Object value) {
      return putIfAbsent(attributeType, () -> value);
   }

   /**
    * Sets the value of an attribute if a value is not already set. Removes the attribute if the value is null and
    * ignores setting a value if the attribute is null.
    *
    * @param attributeType the attribute type
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
    * @param attributeType the attribute type
    * @return the value that was associated with the attribute
    */
   Val remove(AttributeType attributeType);


}//END OF AttributedObject
