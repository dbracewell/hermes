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

import com.davidbracewell.collection.counter.Counter;

import java.util.regex.Matcher;

/**
 * @author David B. Bracewell
 */
public class GettingStarted {

  public static void main(String[] args) throws Exception {
    //Initializes configuration settings
    Hermes.initializeApplication(args);

    //Documents are created using the DocumentFactory class which takes care of preprocessing text (e.g
    //normalizing white space and unicode) and constructing a document.
    Document document = DocumentFactory.getInstance().create("The quick brown fox jumps over the lazy dog.");
    //Note: for convenience a document can also be created using static methods on the document class.
    //eg. Document document = Document.create("The quick brown fox jumps over the lazy dog.");

    //The pipeline defines the type of annotations/attributes that will be added to the document.
    Pipeline.process(document, Types.TOKEN, Types.SENTENCE);
    //Annotators that provide the given annotation types and their dependencies are defined via configuration files
    //common annotations are defined in com/davidbracewell/hermes/annotations.conf
    //Note: the annotators that provide a given type can be language dependent.

    //For each sentence (Types.SENTENCE) print to standard out
    //Sentences and tokens have convenience accessor methods (sentences() and tokens()), but can be retrieved in a
    //similar manner as other annotations using get(AnnotationType).
    document.sentences().forEach(System.out::println);

    //Counts the token lemmas in the document (also lower cases)
    //We have not provided lemma annotations to the document, so instead it will simply lowercase the tokens
    Counter<String> unigrams = document.countLemmas(Types.TOKEN);

    //Prints: Count(the) = 2
    System.out.println("Count(the) = " + unigrams.get("the"));

    //Add a custom annotation, by performing a regex for fox or dog
    //Since HStrings act like super charged strings, we can do simple annotations using the builtin Java regex engine.

    //First define the type ANIMAL_MENTION (Note that Types.type(String) is the same as AnnotationType.create(String))
    AnnotationType animalMention = Types.annotation("ANIMAL_MENTION");

    //Second create annotations based on a regular expression match
    //We will match fox or dog with word breaks on both sides
    Matcher matcher = document.matcher("\\b(fox|dog)\\b");
    while (matcher.find()) {
      //Creating the annotation is done using the document and only requires the type and the character offsets
      document.createAnnotation(animalMention, matcher.start(), matcher.end());
      //More complicated annotations would also provide attributes, for example Entity Type word Word Sense.
    }

    //Print out the animal mention annotations
    //We should print out fox[16, 19] and dog[40, 43]
    document.get(animalMention).forEach(a -> System.out.println(a + "[" + a.start() + ", " + a.end() + "]"));

  }

}//END OF GettingStarted
