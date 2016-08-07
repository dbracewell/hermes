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

package com.davidbracewell.hermes.corpus.spi;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
@Data
public final class CoNLLRow implements Serializable {
  private static final long serialVersionUID = 1L;
  private int start = -1;
  private int end = -1;
  private int index = -1;
  private int sentence = -1;
  private String word;
  private int parent = -1;
  private String depRelation = null;
  private String pos = null;
  private long annotationID = -1L;
  private Map<String, String> otherProperties = new HashMap<>(3);


  public void addOther(@NonNull String name, @NonNull String value) {
    otherProperties.put(name.toUpperCase(), value);
  }

  public boolean hasOther(@NonNull String name) {
    return otherProperties.containsKey(name.toUpperCase());
  }

  public String getOther(@NonNull String name) {
    return otherProperties.get(name.toUpperCase());
  }

}//END OF CoNLLRow
