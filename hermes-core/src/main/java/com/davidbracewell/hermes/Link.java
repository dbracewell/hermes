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
import com.davidbracewell.io.structured.ElementType;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.tuple.Tuple2;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class Link implements Serializable, AttributedObject {
  private static final long serialVersionUID = 1L;
  private final long id;
  private final Map<Attribute, Val> attributeValMap = new HashMap<>();
  private volatile Document document;


  public Link(@NonNull Annotation link) {
    this.id = link.getId();
  }

  public Link(long id, @NonNull Map<Attribute, Val> attributeValMap) {
    this.id = id;
    this.attributeValMap.putAll(attributeValMap);
  }

  @Override
  public Set<Map.Entry<Attribute, Val>> attributeValues() {
    return attributeValMap.entrySet();
  }

  @Override
  public Set<Attribute> attributes() {
    return attributeValMap.keySet();
  }

  @Override
  public boolean contains(Attribute attribute) {
    return attributeValMap.containsKey(attribute);
  }

  @Override
  public Val get(Attribute attribute) {
    return attributeValMap.getOrDefault(attribute, Val.NULL);
  }

  @Override
  public Val put(Attribute attribute, Object value) {
    if (value == null) {
      return attributeValMap.remove(attribute);
    }
    return attributeValMap.put(attribute, Val.of(value));
  }

  @Override
  public Val remove(Attribute attribute) {
    return attributeValMap.remove(attribute);
  }

  public long getLink() {
    return id;
  }

  void setDocument(Document document) {
    if (this.document != null) {
      synchronized (this) {
        if (this.document != null) {
          this.document = document;
        }
      }
    }
  }


  static Tuple2<Long, Map<Attribute, Val>> read(StructuredReader reader) throws IOException {
    if (reader.peek() == ElementType.BEGIN_OBJECT) {
      reader.beginObject();
      Long id = null;
      Map<Attribute, Val> attrs = new HashMap<>(1);
      while (reader.peek() != ElementType.END_OBJECT) {
        if (reader.peek() == ElementType.NAME) {
          id = reader.nextKeyValue("linkId").getV2().asLong();
        } else if (reader.peek() == ElementType.BEGIN_OBJECT) {
          String name = reader.beginObject();
          if (name.equals("attributes")) {
            attrs = Attribute.readAttributeList(reader);
          } else {
            throw new IOException("Unexpected object named [" + name + "]");
          }
          reader.endObject();
        } else {
          throw new IOException("Unexpected " + reader.peek());
        }
      }
      reader.beginObject();
      return Tuple2.of(id, attrs);
    }
    throw new IOException("Unexpected " + reader.peek());
  }


}//END OF Link
