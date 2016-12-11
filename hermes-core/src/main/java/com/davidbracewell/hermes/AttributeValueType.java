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
import com.davidbracewell.hermes.attribute.EntityType;
import com.davidbracewell.hermes.attribute.POS;
import com.davidbracewell.hermes.tokenization.TokenType;

import java.text.DateFormat;
import java.util.Date;

/**
 * The enum Attribute value type.
 *
 * @author David B. Bracewell
 */
public enum AttributeValueType {
   /**
    * Value type for Strings
    */
   STRING {
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
    * Value type for Integers
    */
   INTEGER {
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
    * Value type for Longs
    */
   LONG {
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
    * Value type for Doubles
    */
   DOUBLE {
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
    * Value type for Booleans
    */
   BOOLEAN {
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
      @SuppressWarnings("unchecked")
      protected EntityType decodeImpl(Object value) {
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
      if (value instanceof Val) {
         return decodeImpl(Cast.<Val>as(value).get());
      }
      return decodeImpl(value);
   }

   /**
    * Encode object.
    *
    * @param value the value
    * @return the object
    */
   public final Object encode(Val value) {
      if (value == null) {
         return null;
      }
      return encode(value.get());
   }

   /**
    * Encode object.
    *
    * @param value the value
    * @return the object
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
    * Encode object.
    *
    * @param value the value
    * @return the object
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


}//END OF AttributeValueType
