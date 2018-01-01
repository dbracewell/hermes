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

import com.davidbracewell.Language;
import lombok.NonNull;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author David B. Bracewell
 */
public interface StringLike extends CharSequence {

   /**
    * Returns true if and only if this string contains the specified sequence of char values.
    *
    * @param string the sequence to search for
    * @return true if this string contains s, false otherwise
    */
   default boolean contains(@NonNull String string) {
      return toString().contains(string);
   }

   /**
    * Determines if the content of this HString equals the given char sequence.
    *
    * @param content the content to check for equality
    * @return True if equals, False otherwise
    */
   default boolean contentEquals(CharSequence content) {
      if( content == null){
         return false;
      }
      return toString().contentEquals(content);
   }

   /**
    * Determines if the content of this HString equals the given char sequence ignoring case.
    *
    * @param content the content to check for equality
    * @return True if equals, False otherwise
    */
   default boolean contentEqualsIgnoreCase(String content) {
      if (content == null) {
         return false;
      }
      return toString().equalsIgnoreCase(content);
   }

   /**
    * Determines if this HString ends with the given suffix.
    *
    * @param suffix the suffix to check
    * @return True ends with the given suffix, False otherwise
    */
   default boolean endsWith(String suffix) {
      return toString().endsWith(suffix);
   }

   /**
    * Gets the language of the HString. If no language is set for this HString, the language of document will be
    * returned. In the event that this HString is not associated with a document, the default language will be returned.
    *
    * @return The language of the HString
    */
   Language getLanguage();

   /**
    * Sets the language of the HString
    *
    * @param language The language of the HString.
    */
   void setLanguage(Language language);

   /**
    * Returns the index within this string of the first occurrence of the specified substring.
    *
    * @param text the substring to search for.
    * @return the index of the first occurrence of the specified substring, or -1 if there is no such occurrence.
    * @see String#indexOf(String) String#indexOf(String)String#indexOf(String)String#indexOf(String)String#indexOf(String)
    */
   default int indexOf(String text) {
      return indexOf(text, 0);
   }

   /**
    * Returns the index within this string of the first occurrence of the specified substring.
    *
    * @param text  the substring to search for.
    * @param start the index to to start searching from
    * @return the index of the first occurrence of the specified substring, or -1 if there is no such occurrence.
    * @see String#indexOf(String, int) String#indexOf(String, int)String#indexOf(String, int)String#indexOf(String,
    * int)String#indexOf(String, int)
    */
   default int indexOf(String text, int start) {
      return toString().indexOf(text, start);
   }

   /**
    * Returns a regular expression matcher for the given pattern over this HString
    *
    * @param pattern the pattern to search for
    * @return the matcher
    */
   default Matcher matcher(String pattern) {
      return Pattern.compile(pattern).matcher(this);
   }

   /**
    * Returns a regular expression matcher for the given pattern over this HString
    *
    * @param pattern the pattern to search for
    * @return the matcher
    */
   default Matcher matcher(@NonNull Pattern pattern) {
      return pattern.matcher(this);
   }

   /**
    * Tells whether or not this string matches the given regular expression.
    *
    * @param regex the regular expression
    * @return true if, and only if, this string matches the given regular expression
    * @see String#matches(String) String#matches(String)String#matches(String)String#matches(String)String#matches(String)
    */
   default boolean matches(String regex) {
      return toString().matches(regex);
   }

   /**
    * Replaces all substrings of this string that matches the given string with the given replacement.
    *
    * @param oldString the old string
    * @param newString the new string
    * @return the string
    * @see String#replace(CharSequence, CharSequence) String#replace(CharSequence, CharSequence)String#replace(CharSequence,
    * CharSequence)String#replace(CharSequence, CharSequence)String#replace(CharSequence, CharSequence)
    */
   default String replace(String oldString, String newString) {
      return toString().replace(oldString, newString);
   }

   /**
    * Replaces all substrings of this string that matches the given regular expression with the given replacement.
    *
    * @param regex       the regular expression
    * @param replacement the string to be substituted
    * @return the resulting string
    * @see String#replaceAll(String, String) String#replaceAll(String, String)String#replaceAll(String,
    * String)String#replaceAll(String, String)String#replaceAll(String, String)
    */
   default String replaceAll(String regex, String replacement) {
      return toString().replaceAll(regex, replacement);
   }

   /**
    * Replaces the first substring of this string that matches the given regular expression with the given replacement.
    *
    * @param regex       the regular expression
    * @param replacement the string to be substituted
    * @return the resulting string
    * @see String#replaceFirst(String, String) String#replaceFirst(String, String)String#replaceFirst(String,
    * String)String#replaceFirst(String, String)String#replaceFirst(String, String)
    */
   default String replaceFirst(String regex, String replacement) {
      return toString().replaceFirst(regex, replacement);
   }

   /**
    * Tests if this HString starts with the specified prefix.
    *
    * @param prefix the prefix
    * @return true if the HString starts with the specified prefix
    */
   default boolean startsWith(@NonNull String prefix) {
      return toString().startsWith(prefix);
   }

   @Override
   default CharSequence subSequence(int start, int end) {
      return toString().subSequence(start, end);
   }

   /**
    * Converts this string to a new character array.
    *
    * @return a newly allocated character array whose length is the length of this string and whose contents are
    * initialized to contain the character sequence represented by this string.
    */
   default char[] toCharArray() {
      return toString().toCharArray();
   }

   /**
    * To lower case.
    *
    * @return the string
    * @see String#toLowerCase(Locale) String#toLowerCase(Locale)String#toLowerCase(Locale)String#toLowerCase(Locale)String#toLowerCase(Locale)NOTE:
    * Uses locale associated with the HString's langauge
    */
   default String toLowerCase() {
      return toString().toLowerCase(getLanguage().asLocale());
   }

   /**
    * Converts the HString to upper case
    *
    * @return the upper case version of the HString
    * @see String#toUpperCase(Locale) String#toUpperCase(Locale)String#toUpperCase(Locale)String#toUpperCase(Locale)String#toUpperCase(Locale)NOTE:
    * Uses locale associated with the HString's langauge
    */
   default String toUpperCase() {
      return toString().toUpperCase(getLanguage().asLocale());
   }

   @Override
   default char charAt(int index) {
      return toString().charAt(index);
   }

}//END OF StringLike
