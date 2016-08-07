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

package com.davidbracewell.hermes.corpus.spi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author David B. Bracewell
 */
public final class CoNLLProcessors {
  private CoNLLProcessors() {
    throw new IllegalAccessError();
  }

  private static final Map<String, CoNLLColumnProcessor> processorMap;

  static {
    ImmutableMap.Builder<String, CoNLLColumnProcessor> builder = ImmutableMap.builder();
    for (CoNLLColumnProcessor processor : ServiceLoader.load(CoNLLColumnProcessor.class)) {
      builder.put(processor.getFieldName().toUpperCase(), processor);
    }
    processorMap = builder.build();
  }


  public static CoNLLColumnProcessor get(@NonNull String fieldName) {
    Preconditions.checkArgument(processorMap.containsKey(fieldName), fieldName + " is an unknown processing type.");
    return processorMap.get(fieldName.toUpperCase());
  }

  public static List<CoNLLColumnProcessor> get(@NonNull Collection<String> fieldNames) {
    return fieldNames.stream().map(CoNLLProcessors::get).collect(Collectors.toList());
  }

  public static List<CoNLLColumnProcessor> get(@NonNull String... fieldNames) {
    return Stream.of(fieldNames).map(CoNLLProcessors::get).collect(Collectors.toList());
  }


}//END OF CoNLLProcessors
