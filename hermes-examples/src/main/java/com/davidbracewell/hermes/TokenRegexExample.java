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

import com.davidbracewell.hermes.lexicon.Lexicon;
import com.davidbracewell.hermes.lexicon.LexiconManager;
import com.davidbracewell.hermes.lexicon.TrieLexicon;
import com.davidbracewell.hermes.regex.TokenMatcher;
import com.davidbracewell.hermes.regex.TokenRegex;
import com.davidbracewell.parsing.ParseException;

import java.util.Set;

/**
 * @author David B. Bracewell
 */
public class TokenRegexExample {

  public static void main(String[] args) throws Exception {
    //Initializes configuration settings
    Hermes.initializeApplication(args);

    Document document = DocumentFactory.getInstance().create(
      " Alice was beginning to get very tired of sitting by her sister on the bank, and of having nothing to do: once or " +
        "twice she had peeped into the book her sister was reading, but it had no pictures or conversations in it, " +
        "'and what is the use of a book,' thought Alice 'without pictures or conversations?' " +
        "So she was considering in her own mind (as well as she could, for the hot day made her feel very sleepy " +
        "and stupid), whether the pleasure of making a daisy-chain would be worth the trouble of getting up and " +
        "picking the daisies, when suddenly a White Rabbit with pink eyes ran close by her. " +
        "There was nothing so very remarkable in that; nor did Alice think it so very much out of the way to hear " +
        "the Rabbit say to itself, 'Oh dear! Oh dear! I shall be late!' (when she thought it over afterwards, it " +
        "occurred to her that she ought to have wondered at this, but at the time it all seemed quite natural); but " +
        "when the Rabbit actually took a watch out of its waistcoat-pocket, and looked at it, and then hurried on, " +
        "Alice started to her feet, for it flashed across her mind that she had never before seen a rabbit with either " +
        "a waistcoat-pocket, or a watch to take out of it, and burning with curiosity, she ran across the field after " +
        "it, and fortunately was just in time to see it pop down a large rabbit-hole under the hedge. Excerpt taken from: https://www.gutenberg.org/files/11/11-h/11-h.htm."
    );
    Pipeline.process(document, Types.TOKEN, Types.SENTENCE, Types.PART_OF_SPEECH, Types.PHRASE_CHUNK, Types.DEPENDENCY, Types.LEMMA, Types.ENTITY);



    //Find all instances of Alice ignoring case
    doRegex("(?i)alice", document);

    //Narrow down the alices from above to only those followed by any form of the word "be"
    doRegex("(?i)alice (?> (?l)be)", document);

    //Find all instances of words matching the regular expression rab.* ignoring case
    doRegex("/rab.*/i", document);

    //Find a simple noun phrase made up of one or more nouns or pronouns followed by a
    // simple verb phrase made up as one or more verbs. Label the noun phrase as the subject
    // and the verb phrase as the predicate.
    doRegex("(?<SUBJECT> ($NOUN | $PRONOUN)+ ) (?<PREDICATE> $VERB+)", document);

    //Lets take what we did above, but use Phrase Chunks
    doRegex("(?<SUBJECT> {PHRASE_CHUNK $NOUN}) (?<PREDICATE> {PHRASE_CHUNK $VERB})", document);

    //Find all the nsubj in the document
    doRegex("@DEPENDENCY:nsubj", document);

    //Extract all contiguous non-stopwords
    doRegex("^${STOPWORD}+", document);

    //Lets build a dummy lexicon
    Lexicon lexicon = new TrieLexicon(false,false,Types.TAG);
    lexicon.add("once");
    lexicon.add("twice");
    lexicon.add("was beginning to get"); //This will only match at the phrase chunk level
    LexiconManager.register("wonderland", lexicon);

    //Now we can use the lexicon as a match criteria in the regex
    //Note that if the lexicon contains multiword expressions, you will
    //need to make sure you are matching against an multiword annotation
    doRegex("%wonderland | {PHRASE_CHUNK %wonderland}", document);

    //We can match on arbitrary attributes, here we find all URL tokens
    doRegex("$TOKEN_TYPE:URL", document);

    //Do the same as above, but use the parent of the URL Entity type, INTERNET
    doRegex("{ENTITY $INTERNET}", document);

  }

  private static void doRegex(String pattern, Document document) throws ParseException {
    TokenRegex regex = TokenRegex.compile(pattern);
    TokenMatcher matcher = regex.matcher(document);
    System.out.println("=====================================");
    System.out.println(regex.pattern());
    System.out.println("=====================================");
    while (matcher.find()) {
      Set<String> groupNames = matcher.groupNames();
      if (groupNames.isEmpty()) {
        System.out.println(matcher.group());
      } else {
        for (String group : groupNames) {
          System.out.print(group + "=" + matcher.group(group) + ", ");
        }
        System.out.println(matcher.group());
      }
    }
  }


}//END OF TokenRegexExample
