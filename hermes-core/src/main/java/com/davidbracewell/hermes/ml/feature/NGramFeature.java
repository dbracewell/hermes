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

package com.davidbracewell.hermes.ml.feature;

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.Featurizer;
import com.davidbracewell.cache.Cached;
import com.davidbracewell.collection.HashMapCounter;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.stream.StreamingContext;

import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class NGramFeature implements Featurizer<HString> {
  private static final long serialVersionUID = 1L;
  final Builder spec;

  protected NGramFeature(Builder spec) {
    this.spec = spec;
  }

  @Override
  @Cached(keyMaker = HStringKeyMaker.class)
  public Set<Feature> apply(HString hString) {
    return spec.getValueCalculator().apply(
      new HashMapCounter<>(
        StreamingContext.local().stream(hString.ngrams(spec.getAnnotationType(), spec.getMin(), spec.getMax()))
          .filter(spec.getFilter())
          .map(spec.getToStringFunction())
          .countByValue()
      )
    );
  }


  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends AbstractNGramFeatureSpec<Builder> {
    private static final long serialVersionUID = 1L;

    public NGramFeature build() {
      return new NGramFeature(this);
    }

  }

}//END OF NGramFeature
