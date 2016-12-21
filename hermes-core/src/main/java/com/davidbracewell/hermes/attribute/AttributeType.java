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

package com.davidbracewell.hermes.attribute;

import com.davidbracewell.DynamicEnum;
import com.davidbracewell.EnumValue;
import com.davidbracewell.config.Config;
import com.davidbracewell.guava.common.collect.Sets;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.reflection.ValueType;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * An <code>Attribute</code> represents a name and value type. Attributes are crated via the {@link #create(String)}
 * or the {@link #create(String, AttributeValueType)} static methods. The value type of an attribute is either defined
 * via the create method or via a config parameter using a value type (see {@link ValueType} for information of defining
 * the type). Attributes that do not have a defined type default to being Strings.
 * </p>
 *
 * <p>
 * Attribute names are normalized so that an Attribute created with the name <code>partofspeech</code> and one
 * created with the name <code>PartOfSpeech</code> are equal (see {@link DynamicEnum} for normalization information).
 * </p>
 *
 * @author David B. Bracewell
 */
public final class AttributeType extends EnumValue implements AnnotatableType, Comparable<AttributeType> {
   public static final String CANONICAL_NAME = AttributeType.class.getCanonicalName();

   private static final long serialVersionUID = 1L;
   private static final Set<AttributeType> values = Sets.newConcurrentHashSet();
   private static final String typeName = "Attribute";


   private volatile AttributeValueType valueType = AttributeValueType.DEFAULT;

   private AttributeType(String name) {
      super(CANONICAL_NAME, name);
   }

   /**
    * <p>Creates a new or retrieves an existing instance of AttributeType with the given name.</p>
    *
    * @param name the specified name of the AttributeType
    * @return The instance of AttributeType corresponding th the give name.
    */
   public static AttributeType create(@NonNull String name) {
      return create(name, null);
   }

   /**
    * <p>Creates a new or retrieves an existing instance of AttributeType with the given name.</p>
    *
    * @param name      the specified name of the AttributeType
    * @param valueType the type of value the attribute is
    * @return The instance of AttributeType corresponding th the give name.
    */
   public static AttributeType create(String name, AttributeValueType valueType) {
      AttributeType toReturn = DynamicEnum.register(new AttributeType(name));
      if (valueType != null && toReturn.valueType == AttributeValueType.DEFAULT) {
         Config.setProperty(typeName + "." + toReturn.name() + ".type", valueType.toString());
         toReturn.valueType = valueType;
      } else if (valueType != null && toReturn.valueType != valueType) {
         throw new IllegalArgumentException("Attempting to change value type of " + name + " from " + toReturn.getValueType());
      }
      values.add(toReturn);
      return toReturn;
   }

   /**
    * <p>Retrieves all currently known values of AttributeType.</p>
    *
    * @return An unmodifiable collection of currently known values for AttributeType.
    */
   public static Collection<AttributeType> values() {
      return Collections.unmodifiableSet(values);
   }

   /**
    * <p>Returns the constant of AttributeType with the specified name.The normalized version of the specified name will
    * be matched allowing for case and space variations.</p>
    *
    * @return The constant of AttributeType with the specified name
    * @throws IllegalArgumentException if the specified name is not a member of AttributeType.
    */
   public static AttributeType valueOf(@NonNull String name) {
      return DynamicEnum.valueOf(AttributeType.class, name);
   }

   @Override
   public int compareTo(@NonNull AttributeType o) {
      return this.canonicalName().compareTo(o.canonicalName());
   }

//
//   boolean checkType(Val value) {
//      if (value == null || value.isNull() || Config.get("Attribute", "ignoreTypeChecks").asBoolean(false)) {
//         return false;
//      }
//      ValueType valueType = getValueType();
//      value = value.getWrappedClass().isInstance(Val.class) ? value.cast() : value;
//      if (!valueType.getType().isAssignableFrom(value.getWrappedClass())) {
//         if (Config.get("Attribute.ignoreTypeErrors").asBooleanValue(false)) {
//            return false;
//         }
//         throw new IllegalArgumentException(
//                                              value + " [" + value.getWrappedClass()
//                                                                  .getName() + "] is of wrong type. " +
//                                                 name() + "'s defined type is " + valueType.getType().getName());
//      }
//      return true;
//   }

   @Override
   public String type() {
      return typeName;
   }

   /**
    * Gets class information for the type of values this attribute is expected to have. Types are defined via
    * configuration as follows: <code>Attribute.NAME.type = class</code>. If not defined String.class will be returned.
    *
    * @return The class associated with this attributes values
    */
   public AttributeValueType getValueType() {
      if (valueType == AttributeValueType.DEFAULT) {
         synchronized (this) {
            if (valueType == AttributeValueType.DEFAULT) {
               valueType = AttributeValueType.valueOf(Config.get(typeName, name()).asString("STRING"));
            }
         }
      }
      return valueType;
   }

//   void write(StructuredWriter writer, Object val) throws IOException {
//      AttributeValueCodec encoder = getCodec();
//      Val wrapped = val instanceof Val ? Cast.as(val) : Val.of(val);
//      ValueType vType = getValueType();
//
//      //Ignore nulls
//      if (!wrapped.isNull()) {
//         //Check the type
//         if (checkType(wrapped)) {
//            //No encoder is specified
//            if (encoder == null) {
//               //The value type already knows how to write, because it's Writable
//               if (StructuredSerializable.class.isAssignableFrom(vType.getType())) {
//                  Cast.<StructuredSerializable>as(wrapped.get()).write(writer);
//               } else if (vType.isCollection()) {
//                  writer.beginArray(name());
//                  Collection<?> collection = wrapped.asCollection(valueType.getType(),
//                                                                  valueType.getParameterTypes()[0]);
//                  for (Object o : collection) {
//                     writer.writeValue(o);
//                  }
//                  writer.endArray();
//               } else if (vType.isMap()) {
//                  writer.beginObject(name());
//                  Map<?, ?> map = wrapped.asMap(valueType.getParameterTypes()[0], valueType.getParameterTypes()[1]);
//                  for (Map.Entry<?, ?> entry : map.entrySet()) {
//                     writer.writeKeyValue(Convert.convert(entry.getKey(), String.class), entry.getValue());
//                  }
//                  writer.endObject();
//               } else {
//                  writer.writeKeyValue(name(), wrapped.get());
//               }
//            } else if (encoder.isObject()) {
//               writer.beginObject(this.name());
//               encoder.encode(writer, this, wrapped.get());
//               writer.endObject();
//            } else if (encoder.isArray()) {
//               writer.beginArray(this.name());
//               encoder.encode(writer, this, wrapped.get());
//               writer.endArray();
//            } else {
//               encoder.encode(writer, this, wrapped.get());
//            }
//         }
//      }
//
//   }


}//END OF Attribute
