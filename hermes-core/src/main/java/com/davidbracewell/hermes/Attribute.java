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
import com.davidbracewell.collection.Collect;
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.io.structured.ElementType;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.io.structured.StructuredWriter;
import com.davidbracewell.reflection.ValueType;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.*;

/**
 * <p>
 * An <code>Attribute</code> represents a name and value type. Attributes are crated via the {@link #create(String)} or
 * the {@link #create(String, Class)} static methods. The value type of an attribute is either defined via the create
 * method or via a config parameter using a value type (see {@link ValueType} for information of defining the type).
 * Attributes that do not have a defined type default to being Strings. An attribute can define a custom codec ({@link
 * AttributeValueCodec}*) for encoding and decoding its value using  the <code>codec</code> property, e.g.
 * <code>Attribute.NAME.codec=fully.qualified.codec.name</code>.  Note that the <code>Attribute</code> class only
 * represents the name and type of an attribute.
 * </p>
 * <p>
 * Attribute names are normalized so that an Attribute created with the name <code>partofspeech</code> and one created
 * with the name <code>PartOfSpeech</code> are equal (see {@link DynamicEnum} for normalization information).
 * </p>
 * <p>
 * Gold standard attributes can be constructed using the {@link #goldStandardVersion()} method of an attribute.
 * The name of a gold standard attribute has <code>@</code> prepended to the name.
 * Type information for gold standard attributes is taken from the non-gold standard definintion.
 * This means that the gold standard attribute for part of speech (<code>@PART_OF_SPEECH</code> would get its type
 * information from its non-gold standard form <code>PART_OF_SPEECH</code>).
 * </p>
 * <p>
 * When attributes are written to a structured format their type is checked against what is defined. Differences in
 * type will by default cause ignore the attribute and not write it to file. You can set
 * <code>Attribute.ignoreTypeChecks</code> to <code>false</code> to ensure the type and throw an
 * <code>IllegalArgumentException</code> when there is a mismatch.
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
   * Creates a new  attribute with the given name and value type
   *
   * @param name      the name of the attribute
   * @param valueType the type of attribute's value
   * @return the attribute
   * @throws IllegalArgumentException If the name is invalid or an attribute exists with this name, but a differenty
   *                                  value type.
   */
  public static Attribute create(String name, @Nonnull Class<?> valueType) {
    if (StringUtils.isNullOrBlank(name)) {
      throw new IllegalArgumentException(name + " is invalid");
    }
    Preconditions.checkArgument(name.charAt(0) != '@', "Cannot create a Gold Standard attribute with a given value type");
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
   * Creates an attribute with the given name.
   *
   * @param name the name of the attribute
   * @return the attribute
   * @throws IllegalArgumentException If the name is invalid
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
  public static boolean isDefined(String name) {
    return index.isDefined(name);
  }

  static Tuple2<Attribute, Val> read(StructuredReader reader) throws IOException {

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
      Optional<AttributeValueCodec> decoder = attribute.getCodec();

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

    return Tuple2.of(attribute, Val.of(value));
  }

  static Map<Attribute, Val> readAttributeList(StructuredReader reader) throws IOException {
    Map<Attribute, Val> attributeValMap = new HashMap<>();
    while (reader.peek() != ElementType.END_OBJECT) {
      Collect.put(attributeValMap, read(reader));
    }
    return attributeValMap;
  }

  /**
   * Gets the attribute associated with a string.
   *
   * @param name the name as a string
   * @return the attribute for the string
   * @throws IllegalArgumentException if the name is not a valid attribute
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

  boolean checkType(Val value) {
    if (value != null && !Config.get("Attribute", "ignoreTypeChecks").asBoolean(false)) {
      ValueType valueType = getValueType();
      if (!valueType.getType().isAssignableFrom(value.getWrappedClass())) {
        if (Config.get("Attribute.ignoreTypeErrors").asBooleanValue(false)) {
          return false;
        }
        throw new IllegalArgumentException(
          value + " [" + value.getClass().getName() + "] is of wrong type. " +
            name() + "'s defined type is " + valueType.getType().getName());
      }
    }
    return true;
  }

  private Optional<AttributeValueCodec> getCodec() {
    return Optional.ofNullable(Config.get("Attribute", nonGoldStandardVersion().name(), "codec").as(AttributeValueCodec.class));
  }

  /**
   * Gets class information for the type of values this attribute is expected to have. Types are defined via
   * configuration as follows: <code>Attribute.NAME.type = class</code>. If not defined String.class will be returned.
   *
   * @return The class associated with this attributes values
   */
  public ValueType getValueType() {
    return ValueType.fromConfig("Attribute" + "." + nonGoldStandardVersion().name());
  }

  /**
   * Get the gold standard version of this attribute.
   *
   * @return the gold standard version of this attribute
   */
  public Attribute goldStandardVersion() {
    if (isGoldStandard()) {
      return this;
    }
    return Attribute.create("@" + name());
  }

  /**
   * Determines if this attribute is a gold standard.
   *
   * @return True if this is a gold standard attribute, False otherwise
   */
  public boolean isGoldStandard() {
    return name().startsWith("@");
  }

  /**
   * Gets the non-gold standard version.
   *
   * @return the non-gold standard version of this attribute
   */
  public Attribute nonGoldStandardVersion() {
    if (isGoldStandard()) {
      return Attribute.create(name().substring(1));
    }
    return this;
  }

  private Object readResolve() throws ObjectStreamException {
    if (isDefined(name())) {
      return index.valueOf(name());
    }
    return this;
  }

  void write(StructuredWriter writer, Object val) throws IOException {
    ValueType valueType = getValueType();
    Optional<AttributeValueCodec> encoder = getCodec();
    Val wrapped = Val.of(val);

    if (checkType(Val.of(val))) {
      if (valueType.isCollection()) {
        writer.beginArray(name());
        Collection<?> collection = wrapped.asCollection(valueType.getType(), valueType.getParameterTypes()[0]);
        for (Object o : collection) {
          writer.writeValue(o);
        }
        writer.endArray();
      } else if (encoder.isPresent()) {
        writer.beginObject(name());
        encoder.get().encode(writer, this, wrapped.asObject(Object.class));
        writer.endObject();
      } else if (valueType.isMap()) {
        writer.beginObject(name());
        Map<?, ?> map = wrapped.asMap(valueType.getParameterTypes()[0], valueType.getParameterTypes()[1]);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          writer.writeKeyValue(Convert.convert(entry.getKey(), String.class), entry.getValue());
        }
        writer.endObject();
      } else {
        writer.writeKeyValue(name(), wrapped.get());
      }
    }

  }


}//END OF Attribute
