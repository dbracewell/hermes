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

import com.davidbracewell.Tag;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Convert;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.tag.EntityType;
import com.davidbracewell.hermes.tag.POS;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.io.structured.StructuredWriter;

import java.io.IOException;
import java.util.Date;

/**
 * @author David B. Bracewell
 */
public enum CommonCodecs implements AttributeValueCodec {
  INTEGER {
    @Override
    public void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
      writer.writeKeyValue(attribute.name(), (Integer) value);
    }

    @Override
    public Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
      return Convert.convert(value, Integer.class);
    }
  },
  DOUBLE {
    @Override
    public void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
      writer.writeKeyValue(attribute.name(), value);
    }

    @Override
    public Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
      return Convert.convert(value, Double.class);
    }
  },
  FLOAT {
    @Override
    public void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
      writer.writeKeyValue(attribute.name(), value);
    }

    @Override
    public Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
      return Convert.convert(value, Float.class);
    }
  },
  LONG {
    @Override
    public void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
      writer.writeKeyValue(attribute.name(), value);
    }

    @Override
    public Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
      return Convert.convert(value, Long.class);
    }
  },
  STRING {
    @Override
    public void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
      writer.writeKeyValue(attribute.name(), value);
    }

    @Override
    public Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
      return value.toString();
    }
  },
  BOOLEAN {
    @Override
    public void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
      writer.writeKeyValue(attribute.name(), value);
    }

    @Override
    public Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
      return Convert.convert(value, Boolean.class);
    }
  },
  TAG {
    @Override
    public void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
      writer.writeKeyValue("tagClass", value.getClass().getName());
      writer.writeKeyValue("tagValue", value.toString());
    }

    @Override
    public Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
      Class<? extends Tag> tagClass = Cast.as(reader.nextKeyValue("tagClass").asClass());
      return reader.nextKeyValue("tagValue").as(tagClass);
    }

    @Override
    public boolean isObject() {
      return true;
    }
  },
  PART_OF_SPEECH {
    @Override
    public void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
      writer.writeKeyValue(attribute.name(), value);
    }

    @Override
    public Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
      return Convert.convert(value, POS.class);
    }
  },
  ENTITY_TYPE {
    @Override
    public void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
      writer.writeKeyValue(attribute.name(), value);
    }

    @Override
    public Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
      return Convert.convert(value, EntityType.class);
    }
  },
  DATE {
    @Override
    public void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
      writer.writeKeyValue(attribute.name(), Cast.<Date>as(value).getTime());
    }

    @Override
    public Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
      return new Date(Convert.convert(value, Long.class));
    }
  };

  @Override
  public boolean isObject() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }

}//END OF CommonCodecs
