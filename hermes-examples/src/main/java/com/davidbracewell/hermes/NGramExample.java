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

import com.davidbracewell.collection.Counters;
import com.davidbracewell.config.Config;

/**
 * @author David B. Bracewell
 */
public class NGramExample {

  public static void main(String[] args) throws Exception {
    Config.initialize("NGramExample");
    Document document = DocumentProvider.getDocument();
    Pipeline.process(document, Types.SENTENCE, Types.TOKEN);

    System.out.println(
      document.sentences()
        .stream()
        .flatMap(sentence -> sentence.charNGrams(1).stream())
        .map(HString::toLowerCase)
        .collect(Counters.collector())
        .merge(
          document.sentences()
            .stream()
            .flatMap(sentence -> sentence.charNGrams(2).stream())
            .map(HString::toLowerCase)
            .collect(Counters.collector())
        )
        .merge(
          document.sentences()
            .stream()
            .flatMap(sentence -> sentence.charNGrams(3).stream())
            .map(HString::toLowerCase)
            .collect(Counters.collector())
        )
    );

    System.out.println(
      document.sentences()
        .stream()
        .flatMap(sentence -> sentence.tokenNGrams(2, a -> !a.toLowerCase().startsWith("a")).stream())
        .map(HString::toLowerCase)
        .collect(Counters.collector())
    );


    System.out.println(
      Fragments
        .string("Blue moon is rising over the red sea.")
        .charNGrams(2)
        .stream()
        .map(HString::toUpperCase)
        .collect(Counters.collector())
    );

  }

}//END OF NGramExample
