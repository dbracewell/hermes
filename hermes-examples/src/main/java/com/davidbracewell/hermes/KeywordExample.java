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

import com.davidbracewell.hermes.extraction.TermExtractor;
import com.davidbracewell.hermes.extraction.keyword.NPClusteringKeywordExtractor;
import com.davidbracewell.hermes.extraction.keyword.TermKeywordExtractor;
import com.davidbracewell.hermes.filter.StopWords;

/**
 * @author David B. Bracewell
 */
public class KeywordExample {


   public static void main(String[] args) throws Exception {
      Hermes.initializeApplication("", args);
      Document document = Document.create(
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
      document.annotate(Types.TOKEN, Types.SENTENCE, Types.PHRASE_CHUNK);
      NPClusteringKeywordExtractor ke = new NPClusteringKeywordExtractor();
      System.out.println("   NPClusteringKeywordExtractor");
      ke.extract(document).topN(10).forEach((kw, score) -> System.out.println(kw + "\t" + score));
      System.out.println();


      System.out.println("   TFKeywordExtractor");
      TermKeywordExtractor tfke = new TermKeywordExtractor(TermExtractor.create()
                                                                        .annotationType(Types.PHRASE_CHUNK)
                                                                        .toStringFunction(
                                                                           pc -> pc.trim(StopWords.isStopWord())
                                                                                   .getLemma())
                                                                        .filter(pc -> pc.getPOS().isInstance(POS.NOUN))
      );
      tfke.extract(document).topN(10).forEach((kw, score) -> System.out.println(kw + "\t" + score));


   }


}//END OF KeywordExample
