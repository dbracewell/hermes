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

import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.regex.QueryToPredicate;
import com.davidbracewell.hermes.regex.TokenMatcher;
import com.davidbracewell.hermes.tag.RelationType;
import com.davidbracewell.hermes.tag.Relations;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.string.StringUtils;
import com.google.common.collect.Multimap;
import lombok.Builder;
import lombok.Value;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Lyre relation point.
 *
 * @author David B. Bracewell
 */
@Value
public class CaduceusRelationPoint {
  private final AnnotationType annotationType;
  private final String captureGroup;
  private final RelationType relationType;
  private final String relationValue;
  private final SerializablePredicate<HString> constraint;

  /**
   * Instantiates a new Lyre relation point.
   *
   * @param annotationType the annotation type
   * @param captureGroup   the capture group
   * @param relationType   the relation type
   * @param relationValue  the relation value
   */
  @Builder
  public CaduceusRelationPoint(AnnotationType annotationType, String captureGroup, RelationType relationType, String relationValue, String constraint) throws IOException {
    this.annotationType = annotationType;
    this.captureGroup = StringUtils.isNullOrBlank(captureGroup) ? "*" : captureGroup;
    this.relationType = relationType;
    this.relationValue = relationValue;
    try {
      this.constraint = StringUtils.isNullOrBlank(constraint) ? h -> true : QueryToPredicate.parse(constraint);
    } catch (ParseException e) {
      throw new IOException(e);
    }
  }


  protected static CaduceusRelationPoint fromMap(Map<String, Object> map) throws IOException {
    CaduceusRelationPointBuilder builder = CaduceusRelationPoint.builder();

    if (!map.containsKey("capture") && !map.containsKey("relation")) {
      throw new IOException("A source or target must contain either a capture or a relation: " + map);
    }
    if (map.containsKey("annotation")) {
      builder.annotationType(AnnotationType.create(map.get("annotation").toString()));
    }
    if (map.containsKey("capture")) {
      builder.captureGroup(map.get("capture").toString());
    }

    if (map.containsKey("relation")) {
      List<String> parts = StringUtils.split(map.get("relation").toString(), ':');
      if (parts.size() != 2) {
        throw new IOException("A source or target relation does not include relation name & value: " + map.get("relation"));
      }
      builder.relationType(RelationType.create(parts.get(0)));
      builder.relationValue(parts.get(parts.size() - 1));
    }

    if (map.containsKey("constraint")) {
      builder.constraint(map.get("constraint").toString());
    }

    return builder.build();
  }

  private Stream<Annotation> getAnnotationStream(Multimap<String, Annotation> groups, TokenMatcher matcher) {
    if (groups.containsKey(captureGroup)) {
      return groups.get(captureGroup).stream()
        .flatMap(a -> {
          if (annotationType == null) {
            return Collections.singleton(a).stream();
          }
          return a.get(annotationType).stream();
        });
    }
    return matcher.group(captureGroup).stream()
      .flatMap(hString -> {
        if (annotationType == null) {
          return Stream.empty();
        }
        return hString.get(annotationType).stream();
      });
  }

  /**
   * Gets annotations.
   *
   * @param groups the groups
   * @return the annotations
   */
  public List<Annotation> getAnnotations(Multimap<String, Annotation> groups, TokenMatcher matcher) {
    if (relationType == null) {
      return getAnnotationStream(groups, matcher).filter(constraint).collect(Collectors.toList());
    }

    return getAnnotationStream(groups, matcher)
      .flatMap(a -> {
          if (relationType.equals(Relations.DEPENDENCY)) {
            return a.children().stream().filter(a2 -> a2.dependencyRelation().filter(r -> r.getKey().equals(relationValue)).isPresent());
          }
          return a.sources(relationType, relationValue).stream();
        }
      ).flatMap(a -> {
          if (annotationType == null) {
            return Collections.singleton(a).stream();
          }
          return a.get(annotationType).stream();
        }
      ).filter(constraint)
      .collect(Collectors.toList());
  }


}//END OF CaduceusRelationPoint
