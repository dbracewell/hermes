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
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.io.structured.ElementType;
import com.davidbracewell.io.structured.StructuredIOException;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.io.structured.StructuredWriter;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.base.Preconditions;

import java.io.ObjectStreamException;
import java.util.*;

/**
 * <p>
 * An attribute in the TIPSTER architecture is feature-value pair where the feature names are arbitrary strings and
 * values are arbitrary types. We depart from TIPSTER and use <code>Attribute</code> to represent the feature
 * names.  A <code>Attribute</code> is a dynamic enum backed by a normalized version of the string and has associated
 * with it the type of value it expects.
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
   * Creates a new feature name.
   *
   * @param name the name
   * @return the feature name
   */
  public static Attribute create(String name) {
    if (StringUtils.isNullOrBlank(name)) {
      throw new IllegalArgumentException(name + " is invalid");
    }
    Preconditions.checkArgument(!name.contains("@"), "@ is invalid for attribute names");
    Preconditions.checkArgument(!name.contains("::"), ":: is invalid for attribute names");
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

  static void read(StructuredReader reader, Fragment fragment) throws StructuredIOException {


    if (reader.peek() == ElementType.BEGIN_ARRAY) {
      String[] split = reader.beginArray().split("::");
      Attribute attr = Attribute.create(split[0]);
      List<Object> values = new ArrayList<>();
      while (reader.peek() != ElementType.END_ARRAY) {
        values.add(reader.nextValue().as(attr.getValueType()));
      }
      reader.endArray();
      switch (split[1]) {
        case "List":
          fragment.putAttribute(attr, values);
          break;
        case "Set":
          fragment.putAttribute(attr, new LinkedHashSet<>(values));
          break;
        case "Array":
          fragment.putAttribute(attr, values.toArray());
          break;
      }
    } else {
      Tuple2<String, Val> keyValue = reader.nextKeyValue();
      Attribute attr = Attribute.create(keyValue.getKey());
      fragment.putAttribute(attr, keyValue.getValue().as(attr.getValueType()));
    }
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

  static void write(StructuredWriter writer, Attribute attribute, Object val) throws StructuredIOException {
    if (val instanceof Collection || val.getClass().isArray()) {
      String type;
      if (val.getClass().isArray()) {
        type = "Array";
      } else if (val instanceof Set) {
        type = "Set";
      } else {
        type = "List";
      }
      String key = attribute.name() + "::" + type;

      writer.beginArray(key);
      for (Object o : Convert.convert(val, Iterable.class)) {
        writer.writeValue((o instanceof Number) ? o : Convert.convert(o, String.class));
      }
      writer.endArray();

    } else {
      String key = attribute.name();
      writer.writeKeyValue(key, (val instanceof Number) ? val : Convert.convert(val, String.class));
    }
  }

  /**
   * Gets class information for the type of values this attribute is expected to have. Types are defined via
   * configuration as follows: <code>Attribute.NAME = class</code>. If not defined String.class will be returned.
   *
   * @return The class associated with this attributes values
   */
  public Class<?> getValueType() {
    return Config.get("Attribute", name()).asClass(String.class);
  }

  private Object readResolve() throws ObjectStreamException {
    if (exists(name())) {
      return index.valueOf(name());
    }
    return this;
  }


}//END OF AttributeName
