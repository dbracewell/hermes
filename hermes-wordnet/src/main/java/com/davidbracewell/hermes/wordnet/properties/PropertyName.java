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

package com.davidbracewell.hermes.wordnet.properties;

import com.davidbracewell.DynamicEnum;
import com.davidbracewell.EnumValue;

import java.io.ObjectStreamException;
import java.util.Collection;

/**
 * <p>Dynamic enum for property names.</p>
 *
 * @author David B. Bracewell
 */
public class PropertyName extends EnumValue {
  private final static DynamicEnum<PropertyName> ENUM = new DynamicEnum<>();
  private static final long serialVersionUID = -7353904628779809419L;

  PropertyName(String name) {
    super(name);
  }

  /**
   * Value of.
   *
   * @param name the name
   * @return the structured format
   */
  public static PropertyName valueOf(String name) {
    return ENUM.valueOf(name);
  }


  /**
   * Values collection.
   *
   * @return the collection
   */
  public static Collection<PropertyName> values() {
    return ENUM.values();
  }

  /**
   * Create property name.
   *
   * @param name the name
   * @return the property name
   */
  public static PropertyName create(String name) {
    return ENUM.register(new PropertyName(name));
  }

  Object readResolve() throws ObjectStreamException {
    return ENUM.register(this);
  }


  /**
   * The constant INFO_CONTENT.
   */
  public static final PropertyName INFO_CONTENT = create("INFORMATION_CONTENT");
  /**
   * The constant INFO_CONTENT_RESNIK.
   */
  public static final PropertyName INFO_CONTENT_RESNIK = create("INFORMATION_CONTENT_RESNIK");
  /**
   * The constant SUMO_CONCEPT.
   */
  public static final PropertyName SUMO_CONCEPT = create("SUMO_CONCEPT");
  /**
   * The constant SENTIMENT.
   */
  public static final PropertyName SENTIMENT = create("SENTIMENT");
  /**
   * The constant DOMAIN.
   */
  public static final PropertyName DOMAIN = create("DOMAIN");


}//END OF PropertyName
