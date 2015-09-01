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

import com.davidbracewell.io.structured.StructuredIOException;
import com.davidbracewell.io.structured.StructuredWriter;

/**
 * The interface Attribute value encoder.
 * @author David B. Bracewell
 */
public interface AttributeValueEncoder {

  /**
   * Encode void.
   *
   * @param writer the writer
   * @param attribute the attribute
   * @param value the value
   * @throws StructuredIOException the structured iO exception
   */
  void encode(StructuredWriter writer, Attribute attribute, Object value) throws StructuredIOException;

}//END OF AttributeValueEncoder
