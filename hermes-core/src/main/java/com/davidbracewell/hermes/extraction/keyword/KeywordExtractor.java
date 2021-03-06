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

package com.davidbracewell.hermes.extraction.keyword;

import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.hermes.HString;

import java.io.Serializable;

/**
 * <p>A keyword extractor determines the important words, phrases, or concepts in {@link HString} returning a counter of
 * keywords and their corresponding scores.</p>
 *
 * @author David B. Bracewell
 */
@FunctionalInterface
public interface KeywordExtractor extends Serializable {

   /**
    * Extracts keywords from the given HString
    *
    * @param hstring the text to extract keywords froms
    * @return A counter of keywords and their scores
    * @throws NullPointerException If the given HString is null
    */
   Counter<String> extract(HString hstring);

}//END OF KeywordExtractor
