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


import java.io.Serializable;
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
class Fragment implements HString, Serializable {
  private static final long serialVersionUID = 1L;
  private final Map<Attribute, Object> attributes = new HashMap<>(5);
  private final Document owner;
  private final int start;
  private final int end;

  Fragment(Document owner, int start, int end) {
    this.owner = owner;
    this.start = start;
    this.end = end;
  }

  Fragment(HString string) {
    this.owner = string.document();
    this.start = string.start();
    this.end = string.end();
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
  public Map<Attribute, Object> getAttributes() {
    return attributes;
  }

  @Override
  public int start() {
    return start;
  }

  @Override
  public int end() {
    return end;
  }

  @Override
  public String toString() {
    return document().toString().substring(start, end);
  }


}//END OF Fragment
