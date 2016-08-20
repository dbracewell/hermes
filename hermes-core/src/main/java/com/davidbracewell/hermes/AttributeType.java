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
import com.davidbracewell.Tag;
import com.davidbracewell.collection.map.Maps;
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.attribute.AttributeValueCodec;
import com.davidbracewell.hermes.attribute.CommonCodecs;
import com.davidbracewell.hermes.attribute.EntityType;
import com.davidbracewell.hermes.attribute.POS;
import com.davidbracewell.io.structured.ElementType;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.io.structured.StructuredSerializable;
import com.davidbracewell.io.structured.StructuredWriter;
import com.davidbracewell.reflection.Reflect;
import com.davidbracewell.reflection.ReflectionException;
import com.davidbracewell.reflection.ValueType;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.*;

/**
 * <p> An <code>Attribute</code> represents a name and value type. Attributes are crated via the {@link
 * #create(String)}
 * or the {@link #create(String, Class)} static methods. The value type of an attribute is either defined via the
 * create
 * method or via a config parameter using a value type (see {@link ValueType} for information of defining the type).
 * Attributes that do not have a defined type default to being Strings. An attribute can define a custom codec ({@link
 * AttributeValueCodec}*) for encoding and decoding its value using  the <code>codec</code> property, e.g.
 * <code>Attribute.NAME.codec=fully.qualified.codec.name</code>.  Note that the <code>Attribute</code> class only
 * represents the name and type of an attribute. </p> <p> Attribute names are normalized so that an Attribute created
 * with the name <code>partofspeech</code> and one created with the name <code>PartOfSpeech</code> are equal (see
 * {@link
 * DynamicEnum} for normalization information). </p> <p> When attributes are written to a structured format their type
 * is checked against what is defined. Differences in type will by default cause ignore the attribute and not write it
 * to file. You can set <code>Attribute.ignoreTypeChecks</code> to <code>false</code> to ensure the type and throw an
 * <code>IllegalArgumentException</code> when there is a mismatch. </p>
 *
 * @author David B. Bracewell
 */
public final class AttributeType extends EnumValue implements AnnotatableType {

  private static final DynamicEnum<AttributeType> index = new DynamicEnum<>();
  private static final long serialVersionUID = 1L;
  private static final ImmutableMap<Class<?>, AttributeValueCodec> defaultCodecs = ImmutableMap.
    <Class<?>, AttributeValueCodec>builder()
    .put(Double.class, CommonCodecs.DOUBLE)
    .put(Integer.class, CommonCodecs.INTEGER)
    .put(String.class, CommonCodecs.STRING)
    .put(Long.class, CommonCodecs.LONG)
    .put(Boolean.class, CommonCodecs.BOOLEAN)
    .put(POS.class, CommonCodecs.PART_OF_SPEECH)
    .put(EntityType.class, CommonCodecs.ENTITY_TYPE)
    .put(Tag.class, CommonCodecs.TAG)
    .put(Date.class, CommonCodecs.DATE)
    .put(Language.class, CommonCodecs.LANGUAGE)
    .build();
  private static final String typeName = "Attribute";
  private volatile ValueType valueType;
  private volatile transient AttributeValueCodec codec;

  private AttributeType(String name) {
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
  public static AttributeType create(String name, @NonNull Class<?> valueType) {
    if (StringUtils.isNullOrBlank(name)) {
      throw new IllegalArgumentException(name + " is invalid");
    }
    name = Types.toName(typeName, name);
    if (index.isDefined(name)) {
      AttributeType attributeType = index.valueOf(name);
      Preconditions.checkArgument(attributeType.getValueType().getType().equals(valueType),
                                  "Attempting to register an existing attribute with a new value type.");
      return attributeType;
    }
    AttributeType attributeType = index.register(new AttributeType(name));
    Config.setProperty("Attribute." + attributeType.name() + ".type", valueType.getName());
    return attributeType;
  }

  /**
   * Creates an attribute with the given name.
   *
   * @param name the name of the attribute
   * @return the attribute
   * @throws IllegalArgumentException If the name is invalid
   */
  public static AttributeType create(String name) {
    if (StringUtils.isNullOrBlank(name)) {
      throw new IllegalArgumentException(name + " is invalid");
    }
    return index.register(new AttributeType(Types.toName(typeName, name)));
  }

  /**
   * Determine if a name is an existing Attribute
   *
   * @param name the name
   * @return True if it exists, otherwise False
   */
  public static boolean isDefined(String name) {
    return index.isDefined(Types.toName(typeName, name));
  }

  static Object readObject(StructuredReader reader, AttributeType attributeType) throws IOException {
    ValueType valueType = attributeType.getValueType();
    if (valueType.isMap()) {
      return valueType.convert(reader.nextMap());
    } else {
      throw new RuntimeException(attributeType.name() + " is not defined as Map and does not have a declared decoder.");
    }
  }

  static Object readList(StructuredReader reader, AttributeType attributeType) throws IOException {
    ValueType valueType = attributeType.getValueType();
    List<Object> list = new ArrayList<>();
    while (reader.peek() != ElementType.END_ARRAY) {
      list.add(reader.nextValue().as(valueType.getParameterTypes()[0]));
    }
    return valueType.convert(list);
  }

  static Tuple2<AttributeType, Val> read(StructuredReader reader) throws IOException {

    AttributeType attributeType;
    Object value;

    switch (reader.peek()) {
      case BEGIN_OBJECT:
        attributeType = AttributeType.create(reader.beginObject());
        if (attributeType.getCodec() == null) {
          value = readObject(reader, attributeType);
        } else {
          value = attributeType.getCodec().decode(reader, attributeType, null);
        }
        reader.endObject();
        break;
      case BEGIN_ARRAY:
        attributeType = AttributeType.create(reader.beginArray());
        if (attributeType.getCodec() == null) {
          value = readList(reader, attributeType);
        } else {
          value = attributeType.getCodec().decode(reader, attributeType, null);
        }
        reader.endArray();
        break;
      default:
        Tuple2<String, Val> keyValue = reader.nextKeyValue();
        attributeType = AttributeType.create(keyValue.getKey());
        if (attributeType.getCodec() == null && StructuredSerializable.class.isAssignableFrom(attributeType
                                                                                  .getValueType()
                                                                                  .getType())) {
          try {
            value = Reflect.onClass(attributeType.getValueType().getType()).create();
            Cast.<StructuredSerializable>as(value).read(reader);
          } catch (ReflectionException e) {
            throw Throwables.propagate(e);
          }
        } else if (attributeType.getCodec() == null) {
          value = attributeType.getValueType().convert(keyValue.getValue());
        } else if (StructuredSerializable.class.isAssignableFrom(attributeType.getValueType().getType())) {
          try {
            value = Reflect.onClass(attributeType.getValueType().getType()).create();
            Cast.<StructuredSerializable>as(value).read(reader);
          } catch (ReflectionException e) {
            throw Throwables.propagate(e);
          }
        } else {
          value = attributeType.getCodec().decode(reader, attributeType, keyValue.getValue().get());
        }
    }


    return Tuple2.of(attributeType, Val.of(value));
  }

  static Map<AttributeType, Val> readAttributeList(StructuredReader reader) throws IOException {
    Map<AttributeType, Val> attributeValMap = new HashMap<>();
    while (reader.peek() != ElementType.END_OBJECT) {
      Maps.put(attributeValMap, read(reader));
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
  public static AttributeType valueOf(String name) {
    return index.valueOf(Types.toName(typeName, name));
  }

  /**
   * The current collection of known attributes
   *
   * @return All known attribute names
   */
  public static Collection<AttributeType> values() {
    return index.values();
  }

  private AttributeValueCodec getCodec() {
    if (codec == null) {
      synchronized (this) {
        if (codec == null) {
          codec = Config
            .get("Attribute", name(), "codec")
            .as(AttributeValueCodec.class, defaultCodecs.get(getValueType().getType()));
        }
      }
    }
    return codec;
  }

  boolean checkType(Val value) {
    if (value == null || value.isNull() || Config.get("Attribute", "ignoreTypeChecks").asBoolean(false)) {
      return false;
    }
    ValueType valueType = getValueType();
    value = value.getWrappedClass().isInstance(Val.class) ? value.cast() : value;
    if (!valueType.getType().isAssignableFrom(value.getWrappedClass())) {
      if (Config.get("Attribute.ignoreTypeErrors").asBooleanValue(false)) {
        return false;
      }
      throw new IllegalArgumentException(
        value + " [" + value.getWrappedClass().getName() + "] is of wrong type. " +
          name() + "'s defined type is " + valueType.getType().getName());
    }
    return true;
  }

  @Override
  public String type() {
    return "Attribute";
  }

  /**
   * Gets class information for the type of values this attribute is expected to have. Types are defined via
   * configuration as follows: <code>Attribute.NAME.type = class</code>. If not defined String.class will be returned.
   *
   * @return The class associated with this attributes values
   */
  public ValueType getValueType() {
    if (valueType == null) {
      synchronized (this) {
        if (valueType == null) {
          valueType = ValueType.fromConfig("Attribute" + "." + name());
        }
      }
    }
    return valueType;
  }


  private Object readResolve() throws ObjectStreamException {
    if (isDefined(name())) {
      return index.valueOf(name());
    }
    return index.register(this);
  }

  void write(StructuredWriter writer, Object val) throws IOException {
    AttributeValueCodec encoder = getCodec();
    Val wrapped = val instanceof Val ? Cast.as(val) : Val.of(val);
    ValueType vType = getValueType();


    //Ignore nulls
    if (!wrapped.isNull()) {
      //Check the type
      if (checkType(wrapped)) {
        //No encoder is specified
        if (encoder == null) {
          //The value type already knows how to write, because it's Writable
          if (StructuredSerializable.class.isAssignableFrom(vType.getType())) {
            Cast.<StructuredSerializable>as(wrapped.get()).write(writer);
          } else if (vType.isCollection()) {
            writer.beginArray(name());
            Collection<?> collection = wrapped.asCollection(valueType.getType(), valueType.getParameterTypes()[0]);
            for (Object o : collection) {
              writer.writeValue(o);
            }
            writer.endArray();
          } else if (vType.isMap()) {
            writer.beginObject(name());
            Map<?, ?> map = wrapped.asMap(valueType.getParameterTypes()[0], valueType.getParameterTypes()[1]);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
              writer.writeKeyValue(Convert.convert(entry.getKey(), String.class), entry.getValue());
            }
            writer.endObject();
          } else {
            writer.writeKeyValue(name(), wrapped.get());
          }
        } else if (encoder.isObject()) {
          writer.beginObject(this.name());
          encoder.encode(writer, this, wrapped.get());
          writer.endObject();
        } else if (encoder.isArray()) {
          writer.beginArray(this.name());
          encoder.encode(writer, this, wrapped.get());
          writer.endArray();
        } else {
          encoder.encode(writer, this, wrapped.get());
        }
      }
    }

  }


}//END OF Attribute
