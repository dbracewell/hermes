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

package com.davidbracewell.hermes.caduceus;

import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.Attrs;
import lombok.Builder;
import lombok.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
@Value
@Builder
class CaduceusAnnotationProvider implements Serializable {
  private static final long serialVersionUID = 1L;
  private final String group;
  private final AnnotationType annotationType;
  private final Map<Attribute, Val> attributes;

  protected static CaduceusAnnotationProvider fromMap(Map<String, Object> groupMap, String programName, String ruleName) throws IOException {
    CaduceusAnnotationProviderBuilder builder = builder();

    if (!groupMap.containsKey("type")) {
      throw new IOException("An annotation must provide a type.");
    }

    builder.annotationType(AnnotationType.create(groupMap.get("type").toString()));
    builder.group(
      groupMap.containsKey("capture") ? groupMap.get("capture").toString() : "*"
    );
    Map<Attribute, Val> attributeValMap = new HashMap<>();
    if (groupMap.containsKey("attributes")) {
      attributeValMap = readAttributes(CaduceusProgram.ensureList(groupMap.get("attributes"), "Attributes should be specified as a list."));
    }
    attributeValMap.put(Attrs.LYRE_RULE, Val.of(programName + "::" + ruleName));
    builder.attributes(attributeValMap);
    return builder.build();
  }

  private static Map<Attribute, Val> readAttributes(List<Object> list) throws IOException {
    Map<Attribute, Val> result = new HashMap<>();
    for (Object o : list) {
      Map<String, Object> m = CaduceusProgram.ensureMap(o, "Attribute values should be key-value pairs");
      m.entrySet().forEach(entry -> {
        Attribute attribute = Attribute.create(entry.getKey());
        result.put(attribute, Val.of(attribute.getValueType().convert(entry.getValue())));
      });
    }
    return result;
  }

}//END OF Caduceus
