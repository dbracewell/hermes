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


import com.davidbracewell.conversion.Val;
import com.davidbracewell.string.StringUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * A fragment represents an arbitrary span of text, which includes an entire document and annotations on the document.
 * Fragments are <code>AttributedObjects</code> meaning zero or more attributes can be assigned to the fragment.
 * Fragments have access to the document they are from as well as methods for retrieving annotations that overlap with
 * the fragment.
 * </p>
 *
 * @author David B. Bracewell
 */
class Fragment extends HString {
  private static final long serialVersionUID = 1L;
  private final Map<Attribute, Val> attributes = new HashMap<>(5);
  private final Document owner;

  Fragment(Document owner, int start, int end) {
    super(start, end);
    this.owner = owner;
  }

  Fragment(@Nonnull HString string) {
    super(string.start(), string.end());
    this.owner = string.document();
  }

  Fragment() {
    super(0, 0);
    this.owner = null;
  }

  @Override
  public char charAt(int index) {
    return owner.charAt(index);
  }

  @Override
  public Document document() {
    return owner;
  }

  @Override
  protected Map<Attribute, Val> getAttributeMap() {
    return attributes;
  }


  @Override
  public String toString() {
    if (document() == null) {
      return StringUtils.EMPTY;
    }
    return document().toString().substring(start(), end());
  }


}//END OF Fragment
