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
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.io.structured.ElementType;
import com.davidbracewell.io.structured.StructuredIOException;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.io.structured.StructuredWriter;
import com.davidbracewell.reflection.ValueType;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.io.ObjectStreamException;
import java.util.*;

/**
 * <p>
 * An <code>Attribute</code> represents a name and value type. Attributes are crated via the {@link #create(String)} or
 * the {@link #create(String, Class)} static methods. The value type of an attribute is either defined via the create
 * method or via a config parameter, e.g. <code>Attribute.NAME.type=fully.qualified.className</code> (see {@link
 * com.davidbracewell.reflection.ReflectionUtils#getClassForName(String)}*** for a list of classes that can be
 * constructed
 * using a simple name). Attribute names are not case-sensitive meaning that <pre>partOfSpeech</pre> and
 * <pre>PartOfSpeech</pre> will equate to the same attribute.
 * </p>
 * <p>
 * </p>
 *
 * @author David B. Bracewell
 */
public class Attribute extends EnumValue {

  private static final DynamicEnum<Attribute> index = new DynamicEnum<>();
  private static final long serialVersionUID = 1L;

  private Attribute(String name) {
    super(name);
  }

  /**
   * Create attribute.
   *
   * @param name the name
   * @param valueType the value type
   * @return the attribute
   */
  public static Attribute create(String name, @Nonnull Class<?> valueType) {
    if (StringUtils.isNullOrBlank(name)) {
      throw new IllegalArgumentException(name + " is invalid");
    }
    if (index.isDefined(name)) {
      Attribute attribute = index.valueOf(name);
      Preconditions.checkArgument(attribute.getValueType().equals(valueType), "Attempting to register an existing attribute with a new value type.");
      return attribute;
    }
    Attribute attribute = index.register(new Attribute(name));
    Config.setProperty("Attribute." + attribute.name() + ".type", valueType.getName());
    return attribute;
  }

  /**
   * Creates a new feature name.
   *
   * @param name the name
   * @return the feature name
   */
  public static Attribute create(String name) {
    if (StringUtils.isNullOrBlank(name)) {
      throw new IllegalArgumentException(name + " is invalid");
    }
    return index.register(new Attribute(name));
  }

  /**
   * Determine if a name is an existing Attribute
   *
   * @param name the name
   * @return True if it exists, otherwise False
   */
  public static boolean exists(String name) {
    return index.isDefined(name);
  }

  static void read(StructuredReader reader, AttributedObject attributedObject) throws StructuredIOException {

    Attribute attribute;
    Object value;

    if (reader.peek() == ElementType.BEGIN_ARRAY) {

      attribute = Attribute.create(reader.beginArray());
      ValueType valueType = attribute.getValueType();

      Preconditions.checkArgument(valueType.isCollection(), attribute.name() + " is not defined as a collection.");

      List<Object> list = new ArrayList<>();
      while (reader.peek() != ElementType.END_ARRAY) {
        list.add(reader.nextValue().as(valueType.getParameterTypes()[0]));
      }
      value = valueType.convert(list);
      reader.endArray();

    } else if (reader.peek() == ElementType.BEGIN_OBJECT) {

      attribute = Attribute.create(reader.beginObject());
      ValueType valueType = attribute.getValueType();
      Optional<AttributeValueDecoder> decoder = attribute.getDecoder();

      if (decoder.isPresent()) {
        value = decoder.get().decode(reader, attribute);
      } else if (valueType.isMap()) {
        value = valueType.convert(reader.nextMap());
      } else {
        throw new RuntimeException(attribute.name() + " is not defined as Map and does not have a declared decoder.");
      }

      reader.endObject();

    } else {
      Tuple2<String, Val> keyValue = reader.nextKeyValue();
      attribute = Attribute.create(keyValue.getKey());
      value = attribute.getValueType().convert(keyValue.getValue());
    }

    attributedObject.putAttribute(attribute, value);
  }

  /**
   * Gets the attribute associated with a string.
   *
   * @param name the name as a string
   * @return the attribute for the string
   */
  public static Attribute valueOf(String name) {
    return index.valueOf(name);
  }

  /**
   * The current collection of known attributes
   *
   * @return All know attribute names
   */
  public static Collection<Attribute> values() {
    return index.values();
  }


  void write(StructuredWriter writer, Object val) throws StructuredIOException {
    ValueType valueType = getValueType();
    Optional<AttributeValueEncoder> encoder = getEncoder();

    if (valueType.isCollection()) {
      checkType(val);
      writer.beginArray(name());
      Collection<?> collection = Cast.as(val);
      for (Object o : collection) {
        writer.writeValue(o);
      }
      writer.endArray();
    } else if (encoder.isPresent()) {
      writer.beginObject(name());
      encoder.get().encode(writer, this, val);
      writer.endObject();
    } else if (valueType.isMap()) {
      checkType(val);
      writer.beginObject(name());
      Map<?, ?> map = Cast.as(val);
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        writer.writeKeyValue(Convert.convert(entry.getKey(), String.class), entry.getValue());
      }
      writer.endObject();
    } else {
      writer.writeKeyValue(name(), val);
    }
  }

  /**
   * Gets class information for the type of values this attribute is expected to have. Types are defined via
   * configuration as follows: <code>Attribute.NAME.type = class</code>. If not defined String.class will be returned.
   *
   * @return The class associated with this attributes values
   */
  public ValueType getValueType() {
    return ValueType.fromConfig("Attribute" + "." + name());
  }

  private Object readResolve() throws ObjectStreamException {
    if (exists(name())) {
      return index.valueOf(name());
    }
    return this;
  }

  private Optional<AttributeValueDecoder> getDecoder() {
    return Optional.ofNullable(Config.get("Attribute", name(), "decoder").as(AttributeValueDecoder.class));
  }


  private Optional<AttributeValueEncoder> getEncoder() {
    return Optional.ofNullable(Config.get("Attribute", name(), "encoder").as(AttributeValueEncoder.class));
  }

  void checkType(Object value) {
    if (value != null && !Config.get("Attribute", "ignoreTypeChecks").asBoolean(false)) {
      ValueType valueType = getValueType();
      if (!valueType.getType().isAssignableFrom(value.getClass())) {
        throw new IllegalArgumentException(
            value + " [" + value.getClass().getName() + "] is of wrong type. " +
                name() + "'s defined type is " + valueType.getType().getName());
      }
    }
  }

}//END OF AttributeName
