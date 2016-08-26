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

import com.davidbracewell.string.StringUtils;

import java.util.Collections;
import java.util.Set;

import static com.davidbracewell.collection.set.Sets.set;

/**
 * The interface String analyzer.
 * @author David B. Bracewell
 */
public interface StringAnalyzer {

   static Set<String> wordClasses(String string) {
      if (StringUtils.isNullOrBlank(string)) {
         return Collections.emptySet();
      }
      Set<String> classes = set(
            "AllCaps",
            "InitialCap",
            "AllLower"
                               );

      Character last = null;
      for (char c : string.toCharArray()) {

         if (Character.isLowerCase(c)) {

            classes.remove("AllCaps");
            if (last == null) {
               classes.remove("InitialCap");
            }


         } else if (Character.isUpperCase(c)) {
            classes.remove("AllLower");

         }


         last = c;
      }


      return classes;
   }


   /**
    * Shape string.
    *
    * @param string the string
    * @return the string
    */
   static String shape(String string) {
      if (StringUtils.isNullOrBlank(string)) {
         return StringUtils.EMPTY;
      }
      StringBuilder builder = new StringBuilder();
      String strForm = string.toString();
      for (int ci = 0; ci < strForm.length(); ci++) {
         char c = strForm.charAt(ci);
         if (Character.isUpperCase(c)) {
            builder.append("U");
         } else if (Character.isLowerCase(c)) {
            builder.append("L");
         } else if (Character.isDigit(c)) {
            builder.append("D");
         } else if (c == '.' || c == ',') {
            builder.append(".");
         } else if (c == ';' || c == ':' || c == '?' || c == '!') {
            builder.append(";");
         } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '=' || c == '|' || c == '_') {
            builder.append("-");
         } else if (c == '(' || c == '{' || c == '[' || c == '<') {
            builder.append("(");
         } else if (c == ')' || c == '}' || c == ']' || c == '>') {
            builder.append(")");
         } else {
            builder.append(c);
         }
      }
      return builder.toString();
   }

   /**
    * Shape string.
    *
    * @param string the string
    * @return the string
    */
   static String shape(HString string) {
      if (string == null || string.isEmpty()) {
         return StringUtils.EMPTY;
      }
      return shape(string.toString());
   }

}//END OF StringAnalyzer
