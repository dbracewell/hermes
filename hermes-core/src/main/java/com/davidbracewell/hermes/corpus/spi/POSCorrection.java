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

package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.string.StringPredicates;

/**
 * @author David B. Bracewell
 */
public interface POSCorrection {

  static String word(String word, String pos) {
    switch (pos) {
      case "``":
      case "''":
        return "\"";
      case "-LRB-":
        return "(";
      case "-LSB-":
        return "[";
      case "-LCB-":
        return "{";
      case "-RRB-":
        return ")";
      case "-RCB-":
        return "}";
      case "-RSB-":
        return "]";
      case "\\/":
        return "/";
    }
    switch (word) {
      case "\"\"":
      case "``":
      case "''":
      case "”":
      case "“":
        return "\"";
      case "-LRB-":
        return "(";
      case "-RRB-":
        return ")";
      case "-LSB-":
        return "[";
      case "-RSB-":
        return "]";
      case "-LCB-":
        return "{";
      case "-RCB-":
        return "}";
      case "’s":
        return "'s";
      case "’":
        return "'";
    }
    return word;
  }

  static String pos(String word, String pos) {
    switch (word) {
      case "-":
        return ":";
      case "%":
        return "SYM";
      case "[":
        return "-LSB-";
      case "]":
        return "-RSB-";
      case "(":
        return "-LRB-";
      case ")":
        return "-RRB-";
      case "{":
        return "-LCB-";
      case "}":
        return "-RCB-";
    }

    if (pos.equals("HYPH")) {
      return ":";
    }

    if (StringPredicates.HAS_LETTER_OR_DIGIT.negate().test(word) && pos.startsWith("NN")) {
      return "SYM";
    }

    return pos;
  }
}//END OF POSCorrection
