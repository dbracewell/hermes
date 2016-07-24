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

import com.davidbracewell.hermes.AttributeType;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.io.structured.StructuredWriter;

import java.io.IOException;

/**
 * <p>
 * Provides functionality for custom encoding and decoding of attribute values to/from structured formats.
 * </p>
 *
 * @author David B. Bracewell
 */
public interface AttributeValueCodec {

  /**
   * Encodes the given value.
   *
   * @param writer    the writer to write the encoding to
   * @param attributeType the attribute whose value is being encoded
   * @param value     the value to encode
   * @throws IOException something went wrong writing
   */
  void encode(StructuredWriter writer, AttributeType attributeType, Object value) throws IOException;

  /**
   * Decodes an attribute value from the given reader.
   *
   * @param reader    the reader to read from
   * @param attributeType the attribute whose value needs decoding
   * @param value     the value
   * @return the attribute value
   * @throws IOException something went wrong reading
   */
  Object decode(StructuredReader reader, AttributeType attributeType, Object value) throws IOException;

  boolean isObject();

  boolean isArray();

}//END OF AttributeValueCodec