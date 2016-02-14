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

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.io.structured.ElementType;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.io.structured.StructuredWriter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public abstract class CollectionCodec implements AttributeValueCodec, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public final void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
    for (Object o : Cast.<Collection<?>>as(value)) {
      encodeElement(writer, attribute, o);
    }
  }

  protected abstract void encodeElement(StructuredWriter writer, Attribute attribute, Object element) throws IOException;

  @Override
  public final Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
    List<Object> list = new LinkedList<>();
    while (reader.peek() != ElementType.END_ARRAY) {
      list.add(reader.nextValue());
    }
    return attribute.getValueType().convert(list);
  }


  @Override
  public final boolean isObject() {
    return false;
  }

  @Override
  public final boolean isArray() {
    return true;
  }
}//END OF CollectionCodec
