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

import com.davidbracewell.collection.Counter;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.filter.StopWords;

import java.util.List;

/**
 * @author David B. Bracewell
 */
public class NGramExample {

  public static void main(String[] args) throws Exception {
    Config.initialize("NGramExample");
    Document document = DocumentProvider.getAnnotatedDocument();

    //Get all token level unigrams
    List<HString> unigrams = document.tokenNGrams(1);
    unigrams.forEach(System.out::println);
    System.out.println("============================");

    //Get all token level unigrams removing stopwords
    unigrams = document.tokenNGrams(1, true);
    unigrams.forEach(System.out::println);
    System.out.println("============================");

    //Gets sentence level unigrams
    List<HString> sentenceUnigrams = document.ngrams(1, Types.SENTENCE);
    sentenceUnigrams.forEach(System.out::println);
    System.out.println("============================");

    //Get character level trigrams
    List<HString> trigrams = document.charNGrams(3);
    trigrams.forEach(System.out::println);
    System.out.println("============================");


    //Generate token counts ignoring stopwords and folding tokens into lower case
    Counter<String> counter = document.count(
      Types.TOKEN,
      StopWords.getInstance(document.getLanguage()),
      HString::toLowerCase
    );
    counter.entries().forEach(e -> System.out.println(e.getKey() + " => " + e.getValue()));
    System.out.println("============================");

  }

}//END OF NGramExample
