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

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * A default implementation of an <code>AnnotationSet</code> that uses two sorted sets. One set is sorted by the
 * starting char index and the other by the ending char index. This allows a compromise between memory usage and time
 * to select annotations.
 * </p>
 *
 * @author David B. Bracewell
 */
public class DefaultAnnotationSet implements AnnotationSet, Serializable {
  private static final long serialVersionUID = 1L;

  private NavigableSet<Annotation> startSorted = new TreeSet<>(AnnotationOrdering.START_ORDERING);
  private NavigableSet<Annotation> endSorted = new TreeSet<>(AnnotationOrdering.END_ORDERING);
  private Map<AnnotationType, String> completed = new HashMap<>(4);
  private Map<Long, Annotation> idAnnotationMap = new HashMap<>(4);

  @Override
  public void add(Annotation annotation) {
    if (annotation != null && !annotation.isDetached()) {
      startSorted.add(annotation);
      endSorted.add(annotation);
      idAnnotationMap.put(annotation.getId(), annotation);
    }
  }

  @Override
  public boolean contains(Annotation annotation) {
    return startSorted.contains(annotation);
  }

  @Override
  public Annotation get(long id) {
    return idAnnotationMap.get(id);
  }

  @Override
  public String getAnnotationProvider(AnnotationType type) {
    return completed.get(type);
  }

  @Override
  public Set<AnnotationType> getCompleted() {
    return Collections.unmodifiableSet(completed.keySet());
  }

  @Override
  public boolean isCompleted(AnnotationType type) {
    return completed.containsKey(type);
  }

  @Override
  public Iterator<Annotation> iterator() {
    return Collections.unmodifiableSortedSet(startSorted).iterator();
  }

  @Override
  public Annotation next(Annotation annotation, AnnotationType type) {
    if (annotation == null || type == null) {
      return null;//Fragments.emptyOrphan();
    }
    return startSorted.tailSet(annotation, false).stream()
        .filter(a -> a.isInstance(type) && !a.isDetached())
        .findFirst().orElse(Fragments.detachedEmptyAnnotation());
  }

  @Override
  public Annotation previous(Annotation annotation, AnnotationType type) {
    if (annotation == null || type == null) {
      return null;//Fragments.emptyOrphan();
    }
    return startSorted.headSet(annotation, false).stream()
        .filter(a -> a.isInstance(type) && !a.isDetached())
        .findFirst().orElse(Fragments.detachedEmptyAnnotation());
  }

  @Override
  public void remove(Annotation annotation) {
    if (annotation != null) {
      startSorted.remove(annotation);
      endSorted.remove(annotation);
      idAnnotationMap.remove(annotation.getId());
    }
  }

  @Override
  public List<Annotation> select(@Nonnull Span range, @Nonnull Predicate<? super Annotation> criteria) {
    Annotation dummy = Fragments.detatchedAnnotation(null, range.end(), Integer.MAX_VALUE);
    Annotation dummy2 = Fragments.detatchedAnnotation(null, Integer.MIN_VALUE, range.start());

    return Sets.intersection(startSorted.headSet(dummy, true), endSorted.tailSet(dummy2, true))
        .stream()
        .filter(criteria)
        .collect(Collectors.toList());
  }

  @Override
  public List<Annotation> select(@Nonnull Predicate<? super Annotation> criteria) {
    return startSorted.stream().filter(criteria).collect(Collectors.toList());
  }

  @Override
  public void setIsCompleted(AnnotationType type, boolean isCompleted, String annotationProvider) {
    if (isCompleted) {
      completed.put(type, annotationProvider);
    } else {
      completed.remove(type);
    }
  }

  @Override
  public int size() {
    return startSorted.size();
  }


  private enum AnnotationOrdering implements Comparator<Annotation> {
    /**
     * The START_ORDERING.
     */
    START_ORDERING() {
      @Override
      public int compare(Annotation o1, Annotation o2) {
        int cmp = Integer.compare(o1.start(), o2.start());
        if (cmp == 0) {
          cmp = Ints.compare(o1.length(), o2.length());
        }
        if (cmp == 0) {
          cmp = Longs.compare(o1.getId(), o2.getId());
        }
        return cmp;
      }
    },
    /**
     * The END_ORDERING.
     */
    END_ORDERING() {
      @Override
      public int compare(Annotation o1, Annotation o2) {
        int cmp = Integer.compare(o1.end(), o2.end());
        if (cmp == 0) {
          cmp = Ints.compare(o1.length(), o2.length());
        }
        if (cmp == 0) {
          cmp = Longs.compare(o1.getId(), o2.getId());
        }
        return cmp;
      }
    }
  }

}//END OF DefaultAnnotationSet
