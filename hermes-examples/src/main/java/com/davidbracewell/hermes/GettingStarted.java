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

import java.util.regex.Matcher;

/**
 * @author David B. Bracewell
 */
public class GettingStarted {

  public static void main(String[] args) throws Exception {
    //Initializes configuration settings
    Config.initialize("GettingStarted");

    //Documents are created using the DocumentFactory class which takes care of preprocessing text (e.g
    //normalizing white space and unicode) and constructing a document.
    Document document = DocumentFactory.getInstance().create("The quick brown fox jumps over the lazy dog.");

    //The pipeline defines the type of annotations/attributes that will be added to the document.
    //Processing is done parallel when multiple documents are passed in.
    Pipeline.process(document, Types.TOKEN, Types.SENTENCE);

    //For each sentence (Types.SENTENCE) print in a part-of-speech representation
    document.sentences().forEach(System.out::println);

    //Counts the token lemmas in the document (also lower cases)
    Counter<String> unigrams = document.countLemmas(Types.TOKEN);
    //Prints: Count(the) = 2
    System.out.println("Count(the) = " + unigrams.get("the"));

    //Add a custom annotation, by performing a regex for fox or dog

    //First define the type
    AnnotationType animalMention = AnnotationType.create("ANIMAL_MENTION");

    //Second create annotations based on a regular expression match
    Matcher matcher = document.matcher("\\b(fox|dog)\\b");
    while (matcher.find()) {
      document.createAnnotation(animalMention, matcher.start(), matcher.end());
    }

    //Print out the animal mention annotations
    document.getOverlapping(animalMention).forEach(a -> System.out.println(a + "[" + a.start() + ", " + a.end() + "]"));
  }

}//END OF GettingStarted
