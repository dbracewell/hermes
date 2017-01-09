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

package com.davidbracewell.hermes.lexicon;

import com.davidbracewell.Lazy;
import com.davidbracewell.function.Unchecked;
import com.davidbracewell.io.Resources;

import java.io.Serializable;

/**
 * <p>Universal lexicons that can be used across languages.</p>
 *
 * @author David B. Bracewell
 */
public final class GlobalLexica implements Serializable {
   private static final long serialVersionUID = 1L;

   private static volatile Lazy<WordList> tlds = new Lazy<>(Unchecked.supplier(() -> SimpleWordList.read(
      Resources.fromClasspath("com/davidbracewell/hermes/lexicon/tlds.txt"), true)));
   private static volatile Lazy<TrieWordList> abbreviations = new Lazy<>(Unchecked.supplier(() -> TrieWordList.read(
      Resources.fromClasspath("com/davidbracewell/hermes/lexicon/abbreviations.txt"), false)));
   private static volatile Lazy<TrieWordList> emoticons = new Lazy<>(Unchecked.supplier(() -> TrieWordList.read(
      Resources.fromClasspath("com/davidbracewell/hermes/lexicon/emoticons.txt"), false)));

   private GlobalLexica() {
      throw new IllegalAccessError();
   }

   /**
    * Gets a lexicon (as a WordList) of the top level internet domain names.
    *
    * @return the top level domains
    */
   public static WordList getTopLevelDomains() {
      return tlds.get();
   }

   /**
    * Gets a lexicon (as a TrieWordList) of common abbreviations.
    *
    * @return the abbreviations
    */
   public static TrieWordList getAbbreviations() {
      return abbreviations.get();
   }

   /**
    * Gets a lexicon (as a TrieWordList) of emoticons.
    *
    * @return the emoticons
    */
   public static TrieWordList getEmoticons() {
      return emoticons.get();
   }


}//END OF GlobalLexica
