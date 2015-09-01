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

import com.davidbracewell.text.util.TextPredicates;
import com.davidbracewell.text.util.TextUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import java.util.*;

/**
 * <p>
 * A default implementation of an <code>AnnotationSet</code> that uses two sorted sets. One set is sorted by the
 * starting char index and the other by the ending char index. This allows a compromise between memory usage and time
 * to select annotations.
 * </p>
 *
 * @author David B. Bracewell
 */
public class DefaultAnnotationSet extends AnnotationSet {
  private static final long serialVersionUID = 1L;

  private NavigableSet<Annotation> startSorted = new TreeSet<>(AnnotationOrdering.START_ORDERING);
  private NavigableSet<Annotation> endSorted = new TreeSet<>(AnnotationOrdering.END_ORDERING);
  private Map<AnnotationType, String> completed = new HashMap<>(4);
  private Map<Long, Annotation> idAnnotationMap = new HashMap<>(4);

  @Override
  public void add(Annotation annotation) {
    if (annotation != null && annotation != TextUtils.DETACHED_EMPTY_FRAGMENT) {
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
      return TextUtils.DETACHED_EMPTY_FRAGMENT;
    }
    return Iterables.find(startSorted.tailSet(annotation, false), TextPredicates.isOfType(type), TextUtils.DETACHED_EMPTY_FRAGMENT);
  }

  @Override
  public Annotation previous(Annotation annotation, AnnotationType type) {
    if (annotation == null || type == null) {
      return TextUtils.DETACHED_EMPTY_FRAGMENT;
    }
    return Iterables.find(startSorted.headSet(annotation, false).descendingSet(), TextPredicates.isOfType(type), TextUtils.DETACHED_EMPTY_FRAGMENT);
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
  public List<Annotation> select(Span range, Predicate<? super Annotation> criteria) {
    Preconditions.checkNotNull(range);
    Preconditions.checkNotNull(criteria);
    Annotation dummy = new Annotation(AnnotationType.ROOT, null, Long.MAX_VALUE, Span.of(range.end, Integer.MAX_VALUE));
    Annotation dummy2 = new Annotation(AnnotationType.ROOT, null, Long.MAX_VALUE, Span.of(Integer.MIN_VALUE, range.start));

    return new ArrayList<>(
        Collections2.filter(
            Sets.intersection(startSorted.headSet(dummy, true), endSorted.tailSet(dummy2, true)),
            criteria
        )
    );
  }

  @Override
  public List<Annotation> select(Predicate<? super Annotation> criteria) {
    Preconditions.checkNotNull(criteria);
    return new ArrayList<>(Collections2.filter(startSorted, criteria));
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
        int cmp = o1.span().compareTo(o2.span());
        if (cmp == 0) {
          cmp = Ints.compare(o1.span().length(), o2.span().length());
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
        int cmp = Ints.compare(o1.span().end, o2.span().end);
        if (cmp == 0) {
          cmp = Ints.compare(o1.span().length(), o2.span().length());
        }
        if (cmp == 0) {
          cmp = Longs.compare(o1.getId(), o2.getId());
        }
        return cmp;
      }
    }
  }

}//END OF DefaultAnnotationSet
