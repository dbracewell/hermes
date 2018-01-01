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

import com.davidbracewell.DynamicEnum;
import com.davidbracewell.EnumValue;
import com.davidbracewell.Language;
import com.davidbracewell.annotation.Preload;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.conversion.NewObjectConverter;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.tokenization.TokenType;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

import java.text.DateFormat;
import java.util.*;

/**
 * Auto generated using enumGen.py
 * The type AttributeValueType2.
 */
@Preload
public abstract class AttributeValueType extends EnumValue implements Comparable<AttributeValueType> {
   /**
    * The constant CANONICAL_NAME.
    */
   public static final String CANONICAL_NAME = AttributeValueType.class.getCanonicalName();
   private static final long serialVersionUID = 1L;
   private static final Set<AttributeValueType> values = com.davidbracewell.guava.common.collect.Sets.newConcurrentHashSet();
   private static final NewObjectConverter<Enum> converter = new NewObjectConverter<>(Enum.class);

   /**
    * The type Dynamic enum type.
    */
   public abstract static class DYNAMIC_ENUM_TYPE extends AttributeValueType {
      private final NewObjectConverter<EnumValue> converter = new NewObjectConverter<>(EnumValue.class);
      private static final long serialVersionUID = 1L;

      /**
       * Instantiates a new Dynamic enum type.
       *
       * @param name the name
       */
      protected DYNAMIC_ENUM_TYPE(String name) {
         super(name);
      }

      @Override
      public Class<?> getType() {
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
   }

   /**
    * The type Enum type.
    */
   public abstract static class ENUM_TYPE extends AttributeValueType {
      private static final long serialVersionUID = 1L;

      /**
       * Instantiates a new Enum type.
       *
       * @param name the name
       */
      protected ENUM_TYPE(String name) {
         super(name);
      }

      @Override
      public Class<?> getType() {
         return null;
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

   }

   /**
    * The constant GENERIC_ENUM.
    */
   public static AttributeValueType GENERIC_ENUM = new ENUM_TYPE("ENUM") {
      private static final long serialVersionUID = 1L;
   };

   /**
    * The constant GENERIC_DYNAMIC_ENUM.
    */
   public static AttributeValueType GENERIC_DYNAMIC_ENUM = new DYNAMIC_ENUM_TYPE("DYNAMIC_ENUM") {
      private static final long serialVersionUID = 1L;
   };

   /**
    * String value
    */
   public final static AttributeValueType STRING = new AttributeValueType("STRING") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
         return String.class;
      }

      @Override
      protected Object encodeImpl(Object value) {
         return Convert.convert(value, String.class);
      }

      @Override
      @SuppressWarnings("unchecked")
      protected String decodeImpl(Object value) {
         return value.toString();
      }
   };

   /**
    * Integer value
    */
   public final static AttributeValueType INTEGER = new AttributeValueType("INTEGER") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
         return Integer.class;
      }

      @Override
      protected Object encodeImpl(Object value) {
         return value;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected Integer decodeImpl(Object value) {
         return value instanceof Number ? Cast.<Number>as(value).intValue() : Convert.convert(value, Integer.class);
      }
   };


   /**
    * Long value
    */
   public final static AttributeValueType LONG = new AttributeValueType("LONG") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
         return Integer.class;
      }

      @Override
      protected Object encodeImpl(Object value) {
         return value;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected Long decodeImpl(Object value) {
         return value instanceof Number ? Cast.<Number>as(value).longValue() : Convert.convert(value, Long.class);
      }
   };


   /**
    * Double value
    */
   public final static AttributeValueType DOUBLE = new AttributeValueType("DOUBLE") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
         return Integer.class;
      }

      @Override
      protected Object encodeImpl(Object value) {
         return value;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected Double decodeImpl(Object value) {
         return value instanceof Number ? Cast.<Number>as(value).doubleValue() : Convert.convert(value, Double.class);
      }
   };

   /**
    * Boolean value
    */
   public final static AttributeValueType BOOLEAN = new AttributeValueType("BOOLEAN") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
         return String.class;
      }

      @Override
      protected Object encodeImpl(Object value) {
         return value.toString();
      }

      @Override
      @SuppressWarnings("unchecked")
      protected Boolean decodeImpl(Object value) {
         return value instanceof Boolean ? Cast.<Boolean>as(value) : Convert.convert(value, Boolean.class);
      }
   };


   /**
    * Language value
    */
   public final static AttributeValueType LANGUAGE = new AttributeValueType("LANGUAGE") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
         return String.class;
      }

      @Override
      protected Object encodeImpl(Object value) {
         return value.toString();
      }

      @Override
      @SuppressWarnings("unchecked")
      protected Language decodeImpl(Object value) {
         return value instanceof Language ? Cast.as(value) : Language.fromString(value.toString());
      }
   };

   /**
    * Value type for {@link POS}
    */
   public final static AttributeValueType PART_OF_SPEECH = new AttributeValueType("PART_OF_SPEECH") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
         return String.class;
      }

      @Override
      protected Object encodeImpl(Object value) {
         return value.toString();
      }

      @Override
      @SuppressWarnings("unchecked")
      protected POS decodeImpl(Object value) {
         return value instanceof POS ? Cast.as(value) : POS.fromString(value.toString());
      }
   };

   /**
    * Value type for {@link EntityType}
    */
   public final static AttributeValueType ENTITY_TYPE = new AttributeValueType("ENTITY_TYPE") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
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
   };

   /**
    * Value type for Dates
    */
   public final static AttributeValueType DATE = new AttributeValueType("DATE") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
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
   };


   /**
    * Value type for {@link TokenType}
    */
   public final static AttributeValueType TOKEN_TYPE = new AttributeValueType("TOKEN_TYPE") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
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
   };


   /**
    * Value type for URLs
    */
   public final static AttributeValueType URL = new AttributeValueType("URL") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
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
   };


   /**
    * Value type for {@link StringTag}s
    */
   public final static AttributeValueType STRING_TAG = new AttributeValueType("STRING_TAG") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
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
   };


   /**
    * Default Value Type
    */
   public final static AttributeValueType DEFAULT = new AttributeValueType("DEFAULT") {
      private static final long serialVersionUID = 1L;

      @Override
      public Class<?> getType() {
         throw new UnsupportedOperationException();
      }

      @Override
      protected <T> T decodeImpl(Object value) {
         throw new UnsupportedOperationException();
      }

      @Override
      protected Object encodeImpl(Object value) {
         throw new UnsupportedOperationException();
      }
   };


   /**
    * Instantiates a new Attribute value type.
    *
    * @param name the name
    */
   protected AttributeValueType(String name) {
      super(CANONICAL_NAME, name);
   }


   /**
    * Gets type.
    *
    * @return the type
    */
   public abstract Class<?> getType();

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


   /**
    * <p>Retrieves all currently known values of AttributeValueType2.</p>
    *
    * @return An unmodifiable collection of currently known values for AttributeValueType2.
    */
   public static Collection<AttributeValueType> values() {
      return Collections.unmodifiableSet(values);
   }

   /**
    * <p>Returns the constant of AttributeValueType2 with the specified name.The normalized version of the specified
    * name will be matched allowing for case and space variations.</p>
    *
    * @param name the name
    * @return The constant of AttributeValueType2 with the specified name
    * @throws IllegalArgumentException if the specified name is not a member of AttributeValueType2.
    */
   public static AttributeValueType valueOf(@NonNull String name) {
      return DynamicEnum.valueOf(AttributeValueType.class, name);
   }

   @Override
   public int compareTo(@NonNull AttributeValueType o) {
      return this.canonicalName().compareTo(o.canonicalName());
   }

}//END OF AttributeValueType2
