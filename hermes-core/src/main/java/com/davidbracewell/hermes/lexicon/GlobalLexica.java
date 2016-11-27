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

import com.davidbracewell.collection.Trie;
import com.davidbracewell.io.Resources;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public final class GlobalLexica implements Serializable {
   private static final long serialVersionUID = 1L;
   private static volatile Set<String> tlds = null;
   private static volatile Set<String> abbreviations = null;
   private static volatile Trie<String> emoticons = null;

   private GlobalLexica() {
      throw new IllegalAccessError();
   }


   /**
    * Gets a lexicon (as a set) of the top level internet domain names.
    */
   public static Set<String> getTopLevelDomains() {
      if (tlds == null) {
         synchronized (GlobalLexica.class) {
            if (tlds == null) {
               try {
                  tlds = ImmutableSet.copyOf(Resources.fromClasspath("com/davidbracewell/hermes/lexicon/tlds.txt")
                                                      .readLines().stream()
                                                      .map(line -> line.trim().toLowerCase())
                                                      .collect(Collectors.toSet()));
               } catch (IOException e) {
                  throw Throwables.propagate(e);
               }
            }
         }
      }
      return tlds;
   }

   /**
    * Gets a lexicon (as a set) of the top level internet domain names.
    */
   public static Set<String> getAbbreviations() {
      if (abbreviations == null) {
         synchronized (GlobalLexica.class) {
            if (abbreviations == null) {
               try {
                  abbreviations = ImmutableSet.copyOf(Resources.fromClasspath(
                     "com/davidbracewell/hermes/lexicon/abbreviations.txt")
                                                               .readLines().stream()
                                                               .map(line -> line.trim().toLowerCase())
                                                               .collect(Collectors.toSet()));
               } catch (IOException e) {
                  throw Throwables.propagate(e);
               }
            }
         }
      }
      return abbreviations;
   }

   /**
    * Gets a lexicon (as a Trie) of emoticons.
    */
   public static Trie<String> getEmoticons() {
      if (emoticons == null) {
         synchronized (GlobalLexica.class) {
            if (emoticons == null) {
               try {
                  emoticons = new Trie<>();
                  Resources.fromClasspath(
                     "com/davidbracewell/hermes/lexicon/emoticons.txt")
                           .readLines().stream()
                           .map(line -> line.trim().toLowerCase())
                           .forEach(emo -> emoticons.put(emo.toLowerCase(), emo));
               } catch (IOException e) {
                  throw Throwables.propagate(e);
               }
            }
         }
      }
      return emoticons;
   }


}//END OF GlobalLexica
