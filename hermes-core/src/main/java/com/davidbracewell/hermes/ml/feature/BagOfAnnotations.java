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
import com.davidbracewell.collection.counter.Counters;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.stream.StreamingContext;

import java.util.Set;

/**
 * The type Bag of annotations.
 *
 * @author David B. Bracewell
 */
public class BagOfAnnotations implements Featurizer<HString> {
  private static final long serialVersionUID = 1L;
  /**
   * The Feature spec.
   */
  final AbstractFeatureSpec<?> featureSpec;

  /**
   * Instantiates a new Bag of annotations.
   *
   * @param featureSpec the feature spec
   */
  public BagOfAnnotations(AbstractFeatureSpec featureSpec) {
    this.featureSpec = featureSpec;
  }


  @Override
  @Cached(keyMaker = HStringKeyMaker.class)
  public Set<Feature> apply(HString hString) {
    MStream<String> stream = StreamingContext.local().stream(
      hString.stream(featureSpec.getAnnotationType())
        .filter(featureSpec.getFilter())
        .map(featureSpec.getToStringFunction())
    );
    return featureSpec.getValueCalculator().apply(Counters.newCounter(stream.countByValue()));
  }


  /**
   * Builder builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The type Builder.
   */
  public static class Builder extends AbstractFeatureSpec<Builder> {
    private static final long serialVersionUID = 1L;

    /**
     * Build bag of annotations.
     *
     * @return the bag of annotations
     */
    public BagOfAnnotations build() {
      return new BagOfAnnotations(this);
    }
  }

}//END OF BagOfAnnotations
