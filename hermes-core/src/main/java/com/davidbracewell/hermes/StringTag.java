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

import com.davidbracewell.Tag;
import com.davidbracewell.guava.common.base.Preconditions;
import com.davidbracewell.string.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * A tag which is represented as a string. Care must be taken in that different string variations will represent
 * different tags.
 *
 * @author David B. Bracewell
 */
public class StringTag implements Tag, Serializable {
  private static final long serialVersionUID = 1L;
  private final String tag;

  /**
   * Default Constructor
   *
   * @param tag The tag
   */
  public StringTag(String tag) {
    Preconditions.checkArgument(!StringUtils.isNullOrBlank(tag), "Tag must not be null or blank.");
    this.tag = tag;
  }

  @Override
  public boolean isInstance(Tag tag) {
    return tag != null && equals(tag);
  }

  @Override
  public String name() {
    return toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(tag);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final StringTag other = (StringTag) obj;
    return Objects.equals(this.tag, other.tag);
  }

  @Override
  public String toString() {
    return tag;
  }

}//END OF StringTag
