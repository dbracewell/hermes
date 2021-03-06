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

package com.davidbracewell.hermes.extraction.caduceus;

import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.AttributeType;
import com.davidbracewell.hermes.Types;
import lombok.Builder;
import lombok.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author David B. Bracewell
 */
@Value
@Builder
class CaduceusAnnotationProvider implements Serializable {
   private static final long serialVersionUID = 1L;
   private final String group;
   private final AnnotationType annotationType;
   private final Map<AttributeType, Val> attributes;
   private final Set<String> requires;

   static CaduceusAnnotationProvider fromMap(Map<String, Object> groupMap, String programName, String ruleName) throws IOException {
      if (!groupMap.containsKey("type")) {
         throw new IOException("An annotation must provide a type.");
      }

      Map<AttributeType, Val> attributeValMap = new HashMap<>();
      if (groupMap.containsKey("attributes")) {
         attributeValMap = readAttributes(
            CaduceusProgram.ensureList(groupMap.get("attributes"), "Attributes should be specified as a list."));
      }
      attributeValMap.put(Types.CADUCEUS_RULE, Val.of(programName + "::" + ruleName));

      Set<String> requires = new HashSet<>();
      if (groupMap.containsKey("requires")) {
         requires.addAll(Val.of(groupMap.get("requires")).asList(String.class));
      }

      return builder()
                .annotationType(Types.annotation(groupMap.get("type").toString()))
                .group(groupMap.getOrDefault("capture", "*").toString())
                .attributes(attributeValMap)
                .requires(requires)
                .build();
   }

   private static Map<AttributeType, Val> readAttributes(List<Object> list) throws IOException {
      Map<AttributeType, Val> result = new HashMap<>();
      for (Object o : list) {
         CaduceusProgram.ensureMap(o, "Attribute values should be key-value pairs").entrySet()
                        .forEach(entry -> {
                           AttributeType attributeType = AttributeType.create(entry.getKey());
                           result.put(attributeType, Val.of(attributeType.getValueType().decode(entry.getValue())));
                        });
      }
      return result;
   }

}//END OF Caduceus
