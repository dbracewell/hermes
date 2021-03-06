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

import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.collection.counter.Counters;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author David B. Bracewell
 */
public class AbstractTermExtractor<T extends AbstractExtractor> extends AbstractExtractor<T> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Term spec.
    */
   public AbstractTermExtractor() {

   }

   /**
    * Instantiates a new Term spec.
    *
    * @param copy the copy
    */
   @SuppressWarnings("unchecked")
   public AbstractTermExtractor(@NonNull AbstractTermExtractor copy) {
      super(copy);
   }


   /**
    * Executes the TermSpec returning a stream of stringified terms. This method does not use the TermSpec's {@link
    * com.davidbracewell.hermes.ml.feature.ValueCalculator}
    *
    * @param hString the HString to process
    * @return a stream of term counts
    */
   public Stream<String> stream(@NonNull HString hString) {
      return annotationStream(hString)
                .map(getTrimFunction())
                .filter(getFilter())
                .filter(Objects::nonNull)
                .filter(h -> !h.isEmpty())
                .map(getToStringFunction())
                .filter(StringUtils::isNotNullOrBlank);
   }


   /**
    * Executes the TermSpec returning a list of stringified terms. This method does not use the TermSpec's {@link
    * com.davidbracewell.hermes.ml.feature.ValueCalculator}
    *
    * @param hString the HString to process
    * @return a list of term counts
    */
   public List<String> collect(@NonNull HString hString) {
      return stream(hString).collect(Collectors.toList());
   }

   /**
    * Executes the TermSpec returning a set of stringified terms. This method does not use the TermSpec's {@link
    * com.davidbracewell.hermes.ml.feature.ValueCalculator}
    *
    * @param hString the HString to process
    * @return a set of term counts
    */
   public Set<String> unique(@NonNull HString hString) {
      return stream(hString).collect(Collectors.toSet());
   }

   /**
    * Fully executes the TermSpec returning a count of the stringified terms with their values adjusted according the
    * TermSpec's {@link com.davidbracewell.hermes.ml.feature.ValueCalculator}
    *
    * @param hString the HString to process
    * @return an adjusted counter of term counts
    */
   public Counter<String> count(@NonNull HString hString) {
      return getValueCalculator().adjust(Counters.newCounter(hString.stream(getAnnotationType())
                                                                    .map(getTrimFunction())
                                                                    .filter(getFilter())
                                                                    .map(getToStringFunction())
                                                                    .filter(StringUtils::isNotNullOrBlank)));
   }


}//END OF AbstractTermExtractor
