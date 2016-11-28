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

package com.davidbracewell.hermes.annotator;

import com.davidbracewell.collection.map.Maps;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.tokenization.TokenType;
import com.davidbracewell.string.StringUtils;
import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p> A <code>BreakIterator</code> backed sentence annotator that has limited knowledge of abbreviations. </p>
 *
 * @author David B. Bracewell
 */
public class DefaultSentenceAnnotator implements Annotator, Serializable {
   private static final long serialVersionUID = 1L;

   private final char[] endOfSentence = new char[]{
      '\u0021',
      '\u002E',
      '\u002E',
      '\u003F',
      '\u0589',
      '\u061F',
      '\u06D4',
      '\u0700',
      '\u0701',
      '\u0702',
      '\u0964',
      '\u104A',
      '\u104B',
      '\u1362',
      '\u1367',
      '\u1368',
      '\u166E',
      '\u1803',
      '\u1809',
      '\u2024',
      '\u203C',
      '\u203D',
      '\u2047',
      '\u2048',
      '\u2049',
      '\u3002',
      '\uFE52',
      '\uFE52',
      '\uFE57',
      '\uFF01',
      '\uFF0E',
      '\uFF0E',
      '\uFF1F',
      '\uFF61',

   };

   private final char[] sContinue = new char[]{
      '\u002C',
      '\u002D',
      '\u003A',
      '\u055D',
      '\u060C',
      '\u060D',
      '\u07F8',
      '\u1802',
      '\u1808',
      '\u2013',
      '\u2014',
      '\u3001',
      '\uFE10',
      '\uFE11',
      '\uFE13',
      '\uFE31',
      '\uFE32',
      '\uFE50',
      '\uFE51',
      '\uFE55',
      '\uFE58',
      '\uFE63',
      '\uFF0C',
      '\uFF0D',
      '\uFF1A',
      '\uFF64'
   };

   private final Set<String> noSentenceBreak = ImmutableSet.<String>builder()
                                                  .add("admr.")
                                                  .add("al.")
                                                  .add("ala.")
                                                  .add("jan")
                                                  .add("feb")
                                                  .add("mar")
                                                  .add("apr")
                                                  .add("jun")
                                                  .add("jul")
                                                  .add("aug")
                                                  .add("sep")
                                                  .add("sept")
                                                  .add("oct")
                                                  .add("nov")
                                                  .add("dec")
                                                  .add("alta.")
                                                  .add("arc.")
                                                  .add("ariz.")
                                                  .add("ark.")
                                                  .add("atty.")
                                                  .add("attys.")
                                                  .add("ave.")
                                                  .add("bld.")
                                                  .add("blvd.")
                                                  .add("cal.")
                                                  .add("calif.")
                                                  .add("cl.")
                                                  .add("cmdr.")
                                                  .add("co.")
                                                  .add("col.")
                                                  .add("col.")
                                                  .add("colo.")
                                                  .add("conn.")
                                                  .add("corp.")
                                                  .add("cpl.")
                                                  .add("cres.")
                                                  .add("ct.")
                                                  .add("dak.")
                                                  .add("del.")
                                                  .add("det.")
                                                  .add("dist.")
                                                  .add("dr.")
                                                  .add("dr.")
                                                  .add("dr.")
                                                  .add("esp.")
                                                  .add("etc.")
                                                  .add("exp.")
                                                  .add("expy.")
                                                  .add("fed.")
                                                  .add("fla.")
                                                  .add("ft.")
                                                  .add("fw?y.")
                                                  .add("fy.")
                                                  .add("ga.")
                                                  .add("gen.")
                                                  .add("gen.")
                                                  .add("gov.")
                                                  .add("hway.")
                                                  .add("hwy.")
                                                  .add("ia.")
                                                  .add("id.")
                                                  .add("ida.")
                                                  .add("ill.")
                                                  .add("inc.")
                                                  .add("ind.")
                                                  .add("is.")
                                                  .add("jr.")
                                                  .add("kan.")
                                                  .add("kans.")
                                                  .add("ken.")
                                                  .add("ky.")
                                                  .add("la.")
                                                  .add("la.")
                                                  .add("lt.")
                                                  .add("ltd.")
                                                  .add("maj.")
                                                  .add("man.")
                                                  .add("mass.")
                                                  .add("md.")
                                                  .add("me.")
                                                  .add("mex.")
                                                  .add("mich.")
                                                  .add("minn.")
                                                  .add("miss.")
                                                  .add("mo.")
                                                  .add("mont.")
                                                  .add("mr.")
                                                  .add("mr.")
                                                  .add("mrs.")
                                                  .add("mrs.")
                                                  .add("ms.")
                                                  .add("mt.")
                                                  .add("neb.")
                                                  .add("nebr.")
                                                  .add("nev.")
                                                  .add("ok.")
                                                  .add("okla.")
                                                  .add("ont.")
                                                  .add("ore.")
                                                  .add("p.m.")
                                                  .add("pa.")
                                                  .add("pd.")
                                                  .add("pde?.")
                                                  .add("penn.")
                                                  .add("penna.")
                                                  .add("pl.")
                                                  .add("plz.")
                                                  .add("prof.")
                                                  .add("pvt.")
                                                  .add("qué.")
                                                  .add("rd.")
                                                  .add("rep.")
                                                  .add("reps.")
                                                  .add("rev.")
                                                  .add("sask.")
                                                  .add("sen.")
                                                  .add("sens.")
                                                  .add("sgt.")
                                                  .add("sr.")
                                                  .add("st.")
                                                  .add("supt.")
                                                  .add("tce.")
                                                  .add("tenn.")
                                                  .add("tex.")
                                                  .add("tx.")
                                                  .add("u.s.")
                                                  .add("u.s.a.")
                                                  .add("us.")
                                                  .add("usa.")
                                                  .add("usafa.")
                                                  .add("ut.")
                                                  .add("va.")
                                                  .add("vs.")
                                                  .add("vt.")
                                                  .add("wash.")
                                                  .add("wis.")
                                                  .add("wisc.")
                                                  .add("wy.")
                                                  .add("wyo.")
                                                  .add("yuk.")

                                                  .build();

   private boolean isEndOfSentenceMark(Annotation token) {
      char c = token.isEmpty() ? ' ' : token.charAt(token.length() - 1);
      return Arrays.binarySearch(endOfSentence, c) >= 0;
   }

   private boolean isContinue(Annotation token) {
      char c = token.isEmpty() ? ' ' : token.charAt(token.length() - 1);
      return Arrays.binarySearch(endOfSentence, c) >= 0;
   }

   private boolean isEndPunctuation(Annotation token) {
      if (token.length() != 1) {
         return false;
      }
      char n = token.charAt(0);
      int type = Character.getType(n);
      return n == '"' || type == Character.FINAL_QUOTE_PUNCTUATION || type == Character.END_PUNCTUATION;
   }

   private boolean addSentence(Document doc, int start, int end, int index) {
      while (start < doc.length() && Character.isWhitespace(doc.charAt(start))) {
         start++;
      }
      if (start <= end) {
         doc.createAnnotation(Types.SENTENCE,
                              start,
                              end,
                              Maps.map(Types.INDEX, index)
                             );
         return true;
      }
      return false;
   }

   private Annotation getToken(List<Annotation> tokens, int index) {
      if (index < 0 || index >= tokens.size()) {
         return Fragments.detachedEmptyAnnotation();
      }
      return tokens.get(index);
   }

   private boolean isAbbreviation(Annotation token) {
      return token.get(Types.TOKEN_TYPE).equals(TokenType.ACRONYM)
                || token.get(Types.TOKEN_TYPE).equals(TokenType.TIME);
   }

   private boolean expectsCapital(Annotation token) {
      return true;
   }

   private boolean isCapitalized(Annotation token) {
      return token.length() > 1 && (!StringUtils.hasLetter(token) || Character.isUpperCase(token.charAt(0)));
   }

   private int countNewLineBeforeNext(Document doc, Annotation cToken, Annotation nToken) {
      if (nToken.isEmpty()) {
         return 0;
      }
      int count = 0;
      for (int i = cToken.end(); i < nToken.start(); i++) {
         if (doc.charAt(i) == '\r' || doc.charAt(i) == '\n') {
            count++;
         }
      }
      return count;
   }

   private boolean isListMarker(Annotation token) {
      return token.contentEqual("*") || token.contentEqual("+") || token.contentEqual(">");
   }

   @Override
   public void annotate(Document doc) {
      List<Annotation> tokens = doc.tokens();
      int start = 0;
      int sentenceIndex = 0;
      int lastEnd = -1;

      for (int ti = 0; ti < tokens.size(); ti++) {
         Annotation cToken = tokens.get(ti);
         Annotation nToken = getToken(tokens, ti + 1);
         if (start == -1) {
            start = cToken.start();
         }

         boolean isAbbreviation = isAbbreviation(cToken);
         if ((isAbbreviation && expectsCapital(cToken) && !isCapitalized(nToken))
                || (!isAbbreviation && isEndOfSentenceMark(cToken))
            ) {

            while (isEndOfSentenceMark(nToken)) {
               ti++;
               cToken = nToken;
               nToken = getToken(tokens, ti + 1);
            }

            if (isEndPunctuation(nToken)) {
               ti++;
               cToken = nToken;
               nToken = getToken(tokens, ti + 1);
            }

            if (!isContinue(nToken) && addSentence(doc, start, cToken.end(), sentenceIndex)) {
               sentenceIndex++;
               lastEnd = cToken.end();
               start = -1;
            }

         } else {
            int newLines = countNewLineBeforeNext(doc, cToken, nToken);
            if (newLines > 1
                   || (newLines == 1 && isCapitalized(nToken))
                   || (newLines == 1 && isListMarker(nToken))) {
               //Two or more line ends typically signifies a section heading, so treat it as a sentence.
               if (addSentence(doc, start, cToken.end(), sentenceIndex)) {
                  sentenceIndex++;
                  lastEnd = cToken.end();
                  start = -1;
               }
            }
         }
      }

      if (lastEnd < tokens.get(tokens.size() - 1).end()) {
         addSentence(doc, start, tokens.get(tokens.size() - 1).end(), sentenceIndex);
      }

   }

   @Override
   public Set<AnnotatableType> requires() {
      return Collections.singleton(Types.TOKEN);
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.SENTENCE);
   }


}//END OF DefaultSentenceAnnotator
