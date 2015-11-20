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
import com.davidbracewell.hermes.tag.RelationType;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import lombok.NonNull;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Lyre program.
 *
 * @author David B. Bracewell
 */
public final class LyreProgram implements Serializable {
  private static final long serialVersionUID = 1L;
  private final List<LyreRule> rules = new LinkedList<>();

  /**
   * Instantiates a new Lyre program.
   */
  public LyreProgram() {

  }

  /**
   * Instantiates a new Lyre program.
   *
   * @param rules the rules
   */
  public LyreProgram(Collection<LyreRule> rules) {
    this.rules.addAll(rules);
  }

  /**
   * Read lyre program.
   *
   * @param resource the resource
   * @return the lyre program
   * @throws IOException the io exception
   */
  public static LyreProgram read(@NonNull Resource resource) throws IOException {
    LyreProgram program = new LyreProgram();
    try (Reader reader = resource.reader()) {

      Map<String, Object> rules = ensureMap(new Yaml().load(reader));


      for (Map.Entry<String, Object> entry : rules.entrySet()) {
        Map<String, Object> ruleMap = ensureMap(entry.getValue());
        String pattern = Val.of(ruleMap.get("pattern")).asString();
        if (StringUtils.isNullOrBlank(pattern)) {
          throw new IOException("Invalid format at: " + entry.getKey() + " : " + entry.getValue());
        }


        List<LyreAnnotationProvider> annotationProviders = new LinkedList<>();
        if (ruleMap.containsKey("annotations")) {
          Map<String, Object> annotationMap = ensureMap(ruleMap.get("annotations"));
          for (String groupName : annotationMap.keySet()) {
            Map<String, Object> groupMap = ensureMap(annotationMap.get(groupName));
            Preconditions.checkNotNull(groupMap.get("type"), "Annotation must provide type");
            AnnotationType annotationType = AnnotationType.create(groupMap.get("type").toString());
            Map<Attribute, Val> attributeValMap = new HashMap<>();
            if (groupMap.containsKey("attributes")) {
              attributeValMap = readAttributes(ensureMap(groupMap.get("attributes")));
            }
            attributeValMap.put(Attrs.LYRE_RULE, Val.of(resource.descriptor() + "::" + entry.getKey()));
            annotationProviders.add(new LyreAnnotationProvider(groupName, annotationType, attributeValMap));
          }
        }

        List<LyreRelationProvider> relationProviders = new LinkedList<>();
        if (ruleMap.containsKey("relations")) {
          Map<String, Object> relationMap = ensureMap(ruleMap.get("relations"));
          for (String relationValue : relationMap.keySet()) {
            Map<String, Object> groupMap = ensureMap(relationMap.get(relationValue));
            Preconditions.checkNotNull(groupMap.get("type"), "Relation must provide type");
            Preconditions.checkNotNull(groupMap.get("source"), "Relation must provide source");
            Preconditions.checkNotNull(groupMap.get("target"), "Relation must provide target");
            RelationType type = RelationType.create(groupMap.get("type").toString());

            Map<String, Object> sourceMap = ensureMap(groupMap.get("source"));
            AnnotationType sourceType = sourceMap.containsKey("annotation") ? AnnotationType.create(sourceMap.get("annotation").toString()) : null;
            String source = sourceMap.get("capture").toString();

            Map<String, Object> targetMap = ensureMap(groupMap.get("target"));
            AnnotationType targetType = targetMap.containsKey("annotation") ? AnnotationType.create(targetMap.get("annotation").toString()) : null;
            String target = targetMap.get("capture").toString();

            relationValue = groupMap.containsKey("value") ? groupMap.get("value").toString() : relationValue;
            relationProviders.add(new LyreRelationProvider(type, relationValue, source, sourceType, target, targetType));
          }
        }

        try {
          program.rules.add(new LyreRule(
            resource.descriptor(),
            entry.getKey(),
            TokenRegex.compile(pattern),
            annotationProviders,
            relationProviders
          ));
        } catch (ParseException e) {
          throw new IOException("Invalid pattern for rule " + entry.getKey());
        }

      }

    }
    return program;
  }

  private static Map<String, Object> ensureMap(Object o) throws IOException {
    if (!(o instanceof Map)) {
      throw new IOException("Invalid Lyre Format");
    }
    return Cast.as(o);
  }

  private static Map<Attribute, Val> readAttributes(Map<String, Object> map) {
    Map<Attribute, Val> result = new HashMap<>();
    map.entrySet().forEach(entry -> {
      Attribute attribute = Attribute.create(entry.getKey());
      result.put(attribute, Val.of(attribute.getValueType().convert(entry.getValue())));
    });
    return result;
  }

  private Annotation createOrGet(Document document, AnnotationType type, HString span, Map<Attribute, Val> attributeValMap) {
    return document.substring(span.start(), span.end()).get(type).stream()
      .filter(a -> a.getType().equals(type) && a.attributeValues().equals(attributeValMap.entrySet()))
      .findFirst()
      .orElseGet(() -> document.createAnnotation(type, span, attributeValMap));
  }


  /**
   * Execute.
   *
   * @param document the document
   */
  public void execute(@NonNull Document document) {
    rules.forEach(rule -> {
      TokenMatcher matcher = rule.getRegex().matcher(document);
      while (matcher.find()) {

        ArrayListMultimap<String, Annotation> groups = ArrayListMultimap.create();
        rule.getAnnotationProviders().forEach(ap -> {

          if (ap.getGroup().equals("*")) {
            groups.put(
              ap.getGroup(),
              createOrGet(document, ap.getAnnotationType(), matcher.group(), ap.getAttributes())
            );

          } else {
            matcher.group(ap.getGroup()).forEach(g ->
              groups.put(
                ap.getGroup(),
                createOrGet(document, ap.getAnnotationType(), g, ap.getAttributes())
              )
            );
          }
        });


        rule.getRelationProviders().forEach(rp -> {
          List<Annotation> sourceAnnotations = rp.getSourceType() == null ?
            groups.get(rp.getSource()) :
            matcher.group(rp.getSource()).stream().flatMap(h -> h.get(rp.getSourceType()).stream()).collect(Collectors.toList());

          List<Annotation> targetAnnotations = rp.getTargetType() == null ?
            groups.get(rp.getTarget()) :
            matcher.group(rp.getTarget()).stream().flatMap(h -> h.get(rp.getTargetType()).stream()).collect(Collectors.toList());

          sourceAnnotations.forEach(source ->
            targetAnnotations.stream().filter(t -> t != source).forEach(target ->
              source.addRelation(new Relation(rp.getType(), rp.getValue(), target.getId()))
            )
          );
        });

      }
    });
  }

}//END OF LyreProgram
