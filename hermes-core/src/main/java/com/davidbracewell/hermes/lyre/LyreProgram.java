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

package com.davidbracewell.hermes.lyre;

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.regex.TokenMatcher;
import com.davidbracewell.hermes.regex.TokenRegex;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.*;

/**
 * @author David B. Bracewell
 */
public final class LyreProgram implements Serializable {
  private static final long serialVersionUID = 1L;
  private final List<LyreRule> rules = new LinkedList<>();

  public LyreProgram() {

  }

  public LyreProgram(Collection<LyreRule> rules) {
    this.rules.addAll(rules);
  }

  public void execute(@NonNull HString input) {
    final Document document = input.document();
    rules.forEach(rule -> {
      TokenMatcher matcher = rule.getRegex().matcher(input);
      while (matcher.find()) {
        Annotation annotation = document.createAnnotation(rule.getAnnotationType(), matcher.start(), matcher.end());
        if (Double.isFinite(rule.getConfidence()) && rule.getConfidence() > 0) {
          annotation.put(Attrs.CONFIDENCE, rule.getConfidence());
        }
        annotation.putAll(rule.getAttributes());
        for (Annotation other : annotation.get(rule.getAnnotationType())) {
          if (!other.equals(annotation)) {
            Val rName = other.get(Attrs.LYRE_RULE);
            if (!rName.isNull() && rName.equals(rule.getAttributes().get(Attrs.LYRE_RULE))) {
              document.getAnnotationSet().remove(annotation);
              break;
            }
          }
        }
      }
    });
  }

  public static LyreProgram read(@NonNull Resource resource) throws IOException {
    LyreProgram program = new LyreProgram();
    try (Reader reader = resource.reader()) {

      Map<String, Object> map = ensureMap(new Yaml().load(reader));
      Map<String, Object> defaults = null;
      if (map.containsKey("defaults")) {
        defaults = ensureMap(map.get("defaults"));
      }
      Map<String, Object> rules = ensureMap(map.get("rules"));

      AnnotationType defaultAnnotationType = null;
      double defaultConfidence = -1;
      Map<Attribute, Val> defaultAttributes = new HashMap<>();

      if (defaults != null) {
        if (defaults.containsKey("confidence")) {
          defaultConfidence = Double.valueOf(defaults.get("confidence").toString());
        }
        if (defaults.containsKey("annotationType")) {
          defaultAnnotationType = AnnotationType.create(defaults.get("annotationType").toString());
        }
        if (defaults.containsKey("attributes")) {
          defaultAttributes.putAll(readAttributes(ensureMap(defaults.get("attributes"))));
        }
      }

      for (Map.Entry<String, Object> entry : rules.entrySet()) {
        Map<String, Object> ruleMap = ensureMap(entry.getValue());
        String pattern = Val.of(ruleMap.get("pattern")).asString();
        AnnotationType type = Val.of(ruleMap.get("annotationType")).as(AnnotationType.class, defaultAnnotationType);
        double confidence = Val.of(ruleMap.get("confidence")).asDoubleValue(defaultConfidence);
        Map<Attribute, Val> attributes = ruleMap.containsKey("attributes") ? readAttributes(ensureMap(ruleMap.get("attributes"))) : defaultAttributes;

        if (StringUtils.isNullOrBlank(pattern) || type == null) {
          throw new IOException("Invalid format at: " + entry.getKey() + " : " + entry.getValue());
        }

        try {
          Map<Attribute, Val> effective = attributes == null ? new HashMap<>(defaultAttributes) : attributes;
          effective.put(Attrs.LYRE_RULE, Val.of(resource.descriptor() + "::" + entry.getKey()));
          program.rules.add(new LyreRule(
            resource.descriptor(),
            entry.getKey(),
            TokenRegex.compile(pattern),
            confidence,
            type,
            attributes
          ));
        } catch (ParseException e) {
          throw new IOException("Invalid pattern for rule " + entry.getKey());
        }

      }

    }
    return program;
  }


  private static Map<String, Object> ensureMap(Object o) throws IOException {
    Map<String, Object> map = Cast.as(o);
    if (map == null) {
      throw new IOException("Invalid Lyre Format");
    }
    return map;
  }

  private static Map<Attribute, Val> readAttributes(Map<String, Object> map) {
    Map<Attribute, Val> result = new HashMap<>();
    map.entrySet().forEach(entry -> {
      Attribute attribute = Attribute.create(entry.getKey());
      result.put(attribute, Val.of(attribute.getValueType().convert(entry.getValue())));
    });
    return result;
  }

}//END OF LyreProgram
