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

package com.davidbracewell.hermes.extraction;

import com.davidbracewell.collection.Streams;
import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.collection.counter.Counters;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.filter.StopWords;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple;
import lombok.NonNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.davidbracewell.tuple.Tuples.$;

/**
 * The type N gram feature spec.
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
public abstract class AbstractNGramExtractor<T extends AbstractNGramExtractor> extends AbstractExtractor<T> {
   private static final long serialVersionUID = 1L;
   private int min = 1;
   private int max = 1;

   /**
    * Instantiates a new Abstract n gram feature spec.
    */
   public AbstractNGramExtractor() {
   }

   /**
    * Instantiates a new Abstract n gram feature spec.
    *
    * @param copy the copy
    */
   public AbstractNGramExtractor(@NonNull AbstractNGramExtractor<T> copy) {
      super(copy);
      this.min = copy.min;
      this.max = copy.max;
   }

   /**
    * Order t.
    *
    * @param order the order
    * @return the t
    */
   public T order(int order) {
      this.min = order;
      this.max = order;
      return Cast.as(this);
   }

   @Override
   public T ignoreStopWords() {
      filter(StopWords.notHasStopWord());
      return Cast.as(this);
   }

   /**
    * Gets max.
    *
    * @return the max
    */
   public int getMax() {
      return max;
   }

   /**
    * Max t.
    *
    * @param max the max
    * @return the t
    */
   public T max(int max) {
      this.max = max;
      return Cast.as(this);
   }

   /**
    * Gets min.
    *
    * @return the min
    */
   public int getMin() {
      return min;
   }

   /**
    * Min t.
    *
    * @param min the min
    * @return the t
    */
   public T min(int min) {
      this.min = min;
      return Cast.as(this);
   }

   /**
    * Stream tuples stream.
    *
    * @param hString the h string
    * @return the stream
    */
   public Stream<Tuple> streamTuples(@NonNull HString hString) {
      return Streams.asStream(new NGramStringIterator(hString.get(getAnnotationType())));
   }

   /**
    * Collect tuples list.
    *
    * @param hString the h string
    * @return the list
    */
   public List<Tuple> collectTuples(@NonNull HString hString) {
      return streamTuples(hString).collect(Collectors.toList());
   }

   /**
    * Count tuples counter.
    *
    * @param hString the h string
    * @return the counter
    */
   public Counter<Tuple> countTuples(@NonNull HString hString) {
      return getValueCalculator().adjust(Counters.newCounter(streamTuples(hString)));
   }


   /**
    * Stream h string stream.
    *
    * @param hString the h string
    * @return the stream
    */
   public Stream<HString> streamHString(@NonNull HString hString) {
      return Streams.asStream(new NGramHStringIterator(hString.get(getAnnotationType())));
   }

   /**
    * Collect h string list.
    *
    * @param hString the h string
    * @return the list
    */
   public List<HString> collectHString(@NonNull HString hString) {
      return streamHString(hString).collect(Collectors.toList());
   }

   /**
    * Count h string counter.
    *
    * @param hString the h string
    * @return the counter
    */
   public Counter<HString> countHString(@NonNull HString hString) {
      return getValueCalculator().adjust(Counters.newCounter(streamHString(hString)));
   }

   private class NGramHStringIterator implements Iterator<HString> {
      private final List<Annotation> annotations;
      private final LinkedList<HString> buffer = new LinkedList<>();
      private int i = 0;

      private NGramHStringIterator(List<Annotation> annotations) {
         this.annotations = annotations;
         advance();
      }

      private boolean advance() {
         while (i < annotations.size() && buffer.isEmpty()) {
            for (int j = i + getMin() - 1; j < annotations.size() && j < i + getMax(); j++) {
               HString union = getTrimFunction().apply(annotations.get(i).union(annotations.get(j)));
               if (!union.isEmpty() && getFilter().test(union)) {
                  buffer.add(union);
               }
            }
            i++;
         }
         return !buffer.isEmpty();
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public HString next() {
         if (!advance()) {
            throw new NoSuchElementException();
         }
         return buffer.removeFirst();
      }
   }

   private class NGramStringIterator implements Iterator<Tuple> {
      private final List<Annotation> annotations;
      private final LinkedList<Tuple> buffer = new LinkedList<>();
      private int i = 0;

      private NGramStringIterator(List<Annotation> annotations) {
         this.annotations = annotations;
         advance();
      }

      private boolean advance() {
         while (i < annotations.size() && buffer.isEmpty()) {
            for (int j = i + getMin() - 1; j < annotations.size() && j < i + getMax(); j++) {
               HString union = getTrimFunction().apply(annotations.get(i).union(annotations.get(j)));
               if (!union.isEmpty() && getFilter().test(union)) {
                  Tuple tuple = $(union.stream(getAnnotationType())
                                       .map(getToStringFunction())
                                       .collect(Collectors.toList()));
                  if (tuple.stream().noneMatch(o -> StringUtils.isNullOrBlank(o.toString()))) {
                     buffer.add(tuple);
                  }
               }
            }
            i++;
         }
         return !buffer.isEmpty();
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public Tuple next() {
         if (!advance()) {
            throw new NoSuchElementException();
         }
         return buffer.removeFirst();
      }
   }


}//END OF AbstractNGramFeatureSpec
