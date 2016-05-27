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

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.regex.TokenMatcher;
import com.davidbracewell.hermes.regex.TokenRegex;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import lombok.NonNull;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The type Caduceus program.
 *
 * @author David B. Bracewell
 */
public final class CaduceusProgram implements Serializable {
  private static final long serialVersionUID = 1L;
  private final List<CaduceusRule> rules = new LinkedList<>();

  /**
   * Instantiates a new Caduceus program.
   */
  public CaduceusProgram() {

  }

  /**
   * Read Caduceus program.
   *
   * @param resource the resource
   * @return the lyre program
   * @throws IOException the io exception
   */
  public static CaduceusProgram read(@NonNull Resource resource) throws IOException {
    CaduceusProgram program = new CaduceusProgram();

    try (Reader reader = resource.reader()) {
      List<Object> rules = ensureList(new Yaml().load(reader), "Caduceus rules should be specified in a list");

      for (Object entry : rules) {
        Map<String, Object> ruleMap = ensureMap(entry, "Rule entry should be a map");
        String ruleName = Val.of(ruleMap.get("name")).asString();
        String pattern = Val.of(ruleMap.get("pattern")).asString();
        if (StringUtils.isNullOrBlank(pattern)) {
          throw new IOException("No pattern specified: " + entry);
        }
        if (StringUtils.isNullOrBlank(ruleName)) {
          throw new IOException("No rule name specified: " + entry);
        }

        List<CaduceusAnnotationProvider> annotationProviders = new LinkedList<>();
        if (ruleMap.containsKey("annotations")) {
          List<Object> annotationList = ensureList(ruleMap.get("annotations"), "Annotations should be specified as a list.");
          for (Object o : annotationList) {
            annotationProviders.add(CaduceusAnnotationProvider.fromMap(ensureMap(o, "Annotation entries should be specified as a map."), resource.descriptor(), ruleName));
          }
        }

        List<CaduceusRelationProvider> relationProviders = new LinkedList<>();
        if (ruleMap.containsKey("relations")) {
          List<Object> relations = ensureList(ruleMap.get("relations"), "Relations should be specified as a list.");
          for (Object o : relations) {
            relationProviders.add(CaduceusRelationProvider.fromMap(ensureMap(o, "Relation entries should be specified as a map.")));
          }
        }

        try {
          program.rules.add(new CaduceusRule(
            resource.descriptor(),
            ruleName,
            TokenRegex.compile(pattern),
            annotationProviders,
            relationProviders
          ));
        } catch (ParseException e) {
          throw new IOException("Invalid pattern for rule " + ruleName);
        }

      }

    }
    return program;
  }

  static List<Object> ensureList(Object o, String error) throws IOException {
    if (!(o instanceof List)) {
      throw new IOException("Invalid Caduceus Format: " + error);
    }
    return Cast.as(o);
  }

  static Map<String, Object> ensureMap(Object o, String error) throws IOException {
    if (!(o instanceof Map)) {
      throw new IOException("Invalid Caduceus Format: " + error);
    }
    return Cast.as(o);
  }

  private Annotation createOrGet(Document document, AnnotationType type, HString span, Map<AttributeType, Val> attributeValMap) {
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
    for (CaduceusRule rule : rules) {
      TokenMatcher matcher = rule.getRegex().matcher(document);
      while (matcher.find()) {
        ArrayListMultimap<String, Annotation> groups = ArrayListMultimap.create();

        //Process all the annotation providers
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

        if (!groups.containsKey("*")) {
          groups.putAll("*", matcher.group().tokens());
        }


        HashMultimap<String, Tuple2<Annotation, Relation>> relations = HashMultimap.create();
        for (CaduceusRelationProvider rp : rule.getRelationProviders()) {
          List<Annotation> sourceAnnotations = rp.getSource().getAnnotations(groups, matcher);
          List<Annotation> targetAnnotations = rp.getTarget().getAnnotations(groups, matcher);
          for (Annotation source : sourceAnnotations) {
            for (Annotation target : targetAnnotations) {
              relations.put(rp.getName(), Tuple2.of(source, new Relation(rp.getRelationType(), rp.getRelationValue(), target.getId())));
              if (rp.isReciprocal()) {
                relations.put(rp.getName(), Tuple2.of(target, new Relation(rp.getRelationType(), rp.getRelationValue(), source.getId())));
              }
            }
          }
        }


        rule.getRelationProviders().stream()
          .filter(rp -> StringUtils.isNullOrBlank(rp.getRequires()) || relations.containsKey(rp.getRequires()))
          .forEach(rp -> relations.get(rp.getName()).forEach(t -> t.getV1().add(t.getV2())));

      }
    }


  }

}//END OF CaduceusProgram
