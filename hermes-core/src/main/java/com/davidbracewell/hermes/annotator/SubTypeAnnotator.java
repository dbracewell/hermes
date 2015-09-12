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

package com.davidbracewell.hermes.annotator;

import com.davidbracewell.hermes.*;

import java.util.*;

/**
 * The type Sub type annotator.
 *
 * @author David B. Bracewell
 */
public class SubTypeAnnotator implements Annotator {

  private final AnnotationType annotationType;
  private final Set<AnnotationType> subTypes;
  private final boolean nonOverlapping;

  /**
   * Instantiates a new Sub type annotator.
   *
   * @param annotationType the annotation type
   * @param nonOverlapping the non overlapping
   * @param subTypes       the sub types
   */
  public SubTypeAnnotator(AnnotationType annotationType, boolean nonOverlapping, AnnotationType... subTypes) {
    this(annotationType, nonOverlapping, Arrays.asList(subTypes));
  }

  /**
   * Instantiates a new Sub type annotator.
   *
   * @param annotationType the annotation type
   * @param subTypes       the sub types
   */
  public SubTypeAnnotator(AnnotationType annotationType, AnnotationType... subTypes) {
    this(annotationType, true, Arrays.asList(subTypes));
  }

  /**
   * Instantiates a new Sub type annotator.
   *
   * @param annotationType the annotation type
   * @param nonOverlapping the non overlapping
   * @param subTypes       the sub types
   */
  public SubTypeAnnotator(AnnotationType annotationType, boolean nonOverlapping, Collection<AnnotationType> subTypes) {
    this.annotationType = annotationType;
    this.subTypes = new HashSet<>(subTypes);
    this.nonOverlapping = nonOverlapping;
  }

  /**
   * Instantiates a new Sub type annotator.
   *
   * @param annotationType the annotation type
   * @param subTypes       the sub types
   */
  public SubTypeAnnotator(AnnotationType annotationType, Collection<AnnotationType> subTypes) {
    this(annotationType, true, subTypes);
  }

  private List<Annotation> getAnnotations(HString fragment) {
    List<Annotation> annotations = new ArrayList<>();
    for (AnnotationType subType : subTypes) {
      annotations.addAll(fragment.getOverlapping(subType));
    }
    return annotations;
  }

  private Annotation compare(Annotation a1, Annotation a2) {
    if (a1 == null) {
      return a2;
    }
    if (a2 == null) {
      return a1;
    }

    double a1S = a1.tokenLength() * a1.get(Attrs.CONFIDENCE).asDoubleValue(1.0);
    double a2S = a2.tokenLength() * a2.get(Attrs.CONFIDENCE).asDoubleValue(1.0);
    if (a1S > a2S) {
      return a1;
    } else if (a2S > a1S) {
      return a2;
    } else if (a1.tokenLength() > a2.tokenLength()) {
      return a1;
    } else if (a2.tokenLength() > a1.tokenLength()) {
      return a2;
    }

    return a1;
  }

  @Override
  public void annotate(Document document) {
    subTypes.forEach(subType -> Pipeline.process(document, subType));
    if (nonOverlapping) {
      List<Annotation> annotations = getAnnotations(document);

      for (Annotation a : annotations) {
        if (document.getAnnotationSet().contains(a)) {
          for (Annotation a2 : getAnnotations(a)) {
            if (a.equals(compare(a, a2))) {
              document.getAnnotationSet().remove(a2);
            } else {
              document.getAnnotationSet().remove(a);
              break;
            }
          }
        }
      }

    }
  }

  @Override
  public Set<AnnotationType> provides() {
    return Collections.singleton(annotationType);
  }

  @Override
  public Set<AnnotationType> requires() {
    return Collections.singleton(Types.TOKEN);
  }

}//END OF SubTypeAnnotator
