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

import com.davidbracewell.EnumValue;
import com.davidbracewell.Language;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.conversion.NewObjectConverter;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.tokenization.TokenType;
import com.davidbracewell.string.StringUtils;

import java.text.DateFormat;
import java.util.*;

/**
 * <p> Acceptable types for attribute values. Other types may be assigned, but only these are supported. Each type can
 * be used inside a collection or as the value in a map whose keys are strings. </p>
 *
 * @author David B. Bracewell
 */
public enum AttributeValueType {
   /**
    * String value
    */
   STRING {
      @Override
      protected Class<?> getType() {
         return String.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected String decodeImpl(Object value) {
         return value.toString();
      }

      @Override
      protected Object encodeImpl(Object value) {
         return value;
      }
   },
   /**
    * Integer value
    */
   INTEGER {
      @Override
      protected Class<?> getType() {
         return Integer.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected Integer decodeImpl(Object value) {
         return value instanceof Number ? Cast.<Number>as(value).intValue() : Convert.convert(value, Integer.class);
      }

      @Override
      protected Object encodeImpl(Object value) {
         return value;
      }
   },
   /**
    * Long value
    */
   LONG {
      @Override
      protected Class<?> getType() {
         return Long.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected Long decodeImpl(Object value) {
         return value instanceof Number ? Cast.<Number>as(value).longValue() : Convert.convert(value, Long.class);
      }

      @Override
      protected Object encodeImpl(Object value) {
         return value;
      }
   },
   /**
    * Double value
    */
   DOUBLE {
      @Override
      protected Class<?> getType() {
         return Double.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected Double decodeImpl(Object value) {
         return value instanceof Number ? Cast.<Number>as(value).doubleValue() : Convert.convert(value, Double.class);
      }

      @Override
      protected Object encodeImpl(Object value) {
         return value;
      }

   },
   /**
    * Boolean value
    */
   BOOLEAN {
      @Override
      protected Class<?> getType() {
         return Boolean.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected Boolean decodeImpl(Object value) {
         return value instanceof Boolean ? Cast.<Boolean>as(value) : Convert.convert(value, Boolean.class);
      }

      @Override
      protected Object encodeImpl(Object value) {
         return Cast.<Boolean>as(value).toString();
      }
   },
   /**
    * Value type for {@link Language}
    */
   LANGUAGE {
      @Override
      protected Class<?> getType() {
         return Language.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected Language decodeImpl(Object value) {
         return value instanceof Language ? Cast.as(value) : Language.fromString(value.toString());
      }

      @Override
      protected Object encodeImpl(Object value) {
         return Cast.<Language>as(value).toString();
      }
   },
   /**
    * Value type for {@link POS}
    */
   PART_OF_SPEECH {
      @Override
      protected Class<?> getType() {
         return POS.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected POS decodeImpl(Object value) {
         return value instanceof POS ? Cast.as(value) : POS.fromString(value.toString());
      }

      @Override
      protected Object encodeImpl(Object value) {
         return Cast.<POS>as(value).toString();
      }
   },
   /**
    * Value type for {@link EntityType}
    */
   ENTITY_TYPE {
      @Override
      protected Class<?> getType() {
         return EntityType.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected EntityType decodeImpl(Object value) {
         if (StringUtils.isNullOrBlank(value.toString())) {
            return null;
         }
         return value instanceof EntityType ? Cast.as(value) : EntityType.create(value.toString());
      }

      @Override
      protected Object encodeImpl(Object value) {
         return Cast.<EntityType>as(value).name();
      }

   },
   /**
    * Value type for {@link TokenType}
    */
   TOKEN_TYPE {
      @Override
      protected Class<?> getType() {
         return TokenType.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected TokenType decodeImpl(Object value) {
         return value instanceof TokenType ? Cast.as(value) : TokenType.valueOf(value.toString());
      }

      @Override
      protected Object encodeImpl(Object value) {
         return Cast.<TokenType>as(value).name();
      }
   },
   /**
    * Value type for Dates
    */
   DATE {
      @Override
      protected Class<?> getType() {
         return Date.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected Date decodeImpl(Object value) {
         return value instanceof Date ? Cast.as(value) : Convert.convert(value, Date.class);
      }

      @Override
      protected Object encodeImpl(Object value) {
         return DateFormat.getDateInstance(DateFormat.FULL).format(Cast.as(value));
      }
   },
   /**
    * Value type for generic {@link EnumValue}s requiring the fully qualified name.
    */
   DYNAMIC_ENUM {
      private final NewObjectConverter<EnumValue> converter = new NewObjectConverter<>(EnumValue.class);

      @Override
      protected Class<?> getType() {
         return EnumValue.class;
      }

      @Override
      protected <T> T decodeImpl(Object value) {
         return Cast.as(converter.apply(value));
      }

      @Override
      protected Object encodeImpl(Object value) {
         return Cast.<EnumValue>as(value).canonicalName();
      }
   },
   /**
    * Value type for generic enums requiring the fully qualified name.
    */
   ENUM {
      private final NewObjectConverter<Enum> converter = new NewObjectConverter<>(Enum.class);

      @Override
      protected Class<?> getType() {
         return Enum.class;
      }

      @Override
      protected <T> T decodeImpl(Object value) {
         return Cast.as(converter.apply(value));
      }

      @Override
      protected Object encodeImpl(Object value) {
         Enum<?> e = Cast.as(value);
         return e.getDeclaringClass().getCanonicalName() + "." + e.toString();
      }
   },
   /**
    * Value type for URLs
    */
   URL {
      @Override
      protected Class<?> getType() {
         return java.net.URL.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected java.net.URL decodeImpl(Object value) {
         return value instanceof java.net.URL
                ? Cast.as(value) : Convert.convert(value, java.net.URL.class);
      }

      @Override
      protected Object encodeImpl(Object value) {
         return Cast.<java.net.URL>as(value).toString();
      }
   },
   /**
    * Value type for {@link StringTag}s
    */
   STRING_TAG {
      @Override
      protected Class<?> getType() {
         return StringTag.class;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected StringTag decodeImpl(Object value) {
         return value instanceof StringTag
                ? Cast.as(value) : new StringTag(value.toString());
      }

      @Override
      protected Object encodeImpl(Object value) {
         return Cast.<StringTag>as(value).name();
      }
   },
   DEFAULT {
      @Override
      @SuppressWarnings("unchecked")
      protected String decodeImpl(Object value) {
         return value.toString();
      }

      @Override
      protected Object encodeImpl(Object value) {
         return Convert.convert(value, String.class);
      }

      @Override
      protected Class<?> getType() {
         return String.class;
      }
   };

   /**
    * Converts a {@link Val} object to desired type
    *
    * @param <T>   the target type parameter
    * @param value the value to convert
    * @return the result of conversion
    */
   public final <T> T decode(Val value) {
      if (value == null) {
         return null;
      }
      return decode(value.get());
   }


   /**
    * Converts an object to desired type
    *
    * @param <T>   the target type parameter
    * @param value the value to convert
    * @return the result of conversion
    */
   public final <T> T decode(Object value) {
      if (value == null) {
         return null;
      }
      if (value instanceof Collection) {
         List<T> list = new ArrayList<>();
         Cast.<Collection<?>>as(value).forEach(o -> list.add(decode(o)));
         return Cast.as(list);
      }
      if (value instanceof Map) {
         Map<String, T> map = new HashMap<>();
         Cast.<Map<?, ?>>as(value).forEach((k, v) -> map.put(k.toString(), decode(v)));
         return Cast.as(map);
      }
      if (value instanceof Val) {
         return decodeImpl(Cast.<Val>as(value).get());
      }
      return decodeImpl(value);
   }

   /**
    * Encodes a value into a Json writeable type (String or Number).
    *
    * @param value the value
    * @return the encoded value
    */
   public final Object encode(Val value) {
      if (value == null) {
         return null;
      }
      return encode(value.get());
   }

   /**
    * Encodes a value into a Json writeable type (String or Number).
    *
    * @param value the value
    * @return the encoded value
    */
   public final Object encode(Object value) {
      if (value == null) {
         return null;
      }
      if (value instanceof Val) {
         return encodeImpl(Cast.<Val>as(value).get());
      }
      return encodeImpl(value);
   }


   /**
    * Implementation of a conversion from value to type writeable via JSON (Number or String). Value is guaranteed to
    * be non-null.
    *
    * @param value the value
    * @return the JSON writeable type
    */
   protected abstract Object encodeImpl(Object value);

   /**
    * Implementation of a conversion from object to desired type. Object is guaranteed to be non-null and not a Val
    * object.
    *
    * @param <T>   the target type parameter
    * @param value the value to convert
    * @return the result of conversion
    */
   protected abstract <T> T decodeImpl(Object value);


   protected abstract Class<?> getType();

}//END OF AttributeValueType
