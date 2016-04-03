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
import com.davidbracewell.string.StringUtils;

import java.io.ObjectStreamException;
import java.util.Collection;

/**
 * @author David B. Bracewell
 */
public final class RelationType extends EnumValue implements Annotatable {

  private static final DynamicEnum<RelationType> index = new DynamicEnum<>();
  private static final long serialVersionUID = 1L;

  private RelationType(String name) {
    super(name);
  }

  /**
   * Creates a new RelationType Type or retrieves an already existing one for a given name
   *
   * @param name the name
   * @return the RelationType type
   */
  public static RelationType create(String name) {
    if (StringUtils.isNullOrBlank(name)) {
      throw new IllegalArgumentException(name + " is invalid");
    }
    return index.register(new RelationType(name));
  }

  /**
   * Determine if an RelationType exists for the given name
   *
   * @param name the name
   * @return True if it exists, otherwise False
   */
  public static boolean isDefined(String name) {
    return index.isDefined(name);
  }

  /**
   * Gets the RelationType from its name. Throws an <code>IllegalArgumentException</code> if the name is not valid.
   *
   * @param name the name as a string
   * @return the RelationType for the string
   */
  public static RelationType valueOf(String name) {
    return index.valueOf(name);
  }

  /**
   * Returns the values for this dynamic enum
   *
   * @return All known RelationType
   */
  public static Collection<RelationType> values() {
    return index.values();
  }

  @Override
  public String getTypeName() {
    return "Relation";
  }

  private Object readResolve() throws ObjectStreamException {
    if (isDefined(name())) {
      return index.valueOf(name());
    }
    Object o = index.register(this);
    return o;
  }


}//END OF RelationType

