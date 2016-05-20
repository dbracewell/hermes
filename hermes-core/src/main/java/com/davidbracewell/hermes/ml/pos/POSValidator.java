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

package com.davidbracewell.hermes.ml.pos;

import com.davidbracewell.apollo.ml.Instance;
import com.davidbracewell.apollo.ml.sequence.SequenceValidator;
import com.davidbracewell.hermes.attribute.POS;
import com.davidbracewell.string.StringPredicates;
import com.davidbracewell.string.StringUtils;

/**
 * @author David B. Bracewell
 */
public class POSValidator implements SequenceValidator {
  private static final long serialVersionUID = 1L;

  @Override
  public boolean isValid(String label, String previousLabel, Instance instance) {
    String word = instance.getFeatures().stream().filter(f -> f.getName().startsWith("w[0]=")).map(f -> f.getName().substring(5)).findFirst().orElse(StringUtils.EMPTY);
    if (StringUtils.isNullOrBlank(word)) {
      return true;
    }
    POS pos = POS.fromString(label);
    if (pos == null) {
      return true;
    }

    switch (word) {
      case "\"":
      case "``":
      case "''":
      case "\"\"":
      case "`":
        return pos.isTag(POS.QUOTE);
      case "'":
        return pos.isTag(POS.QUOTE, POS.POS, POS.COLON);
      case "#":
        return pos.isTag(POS.HASH);
      case ",":
        return pos.isTag(POS.COMMA);
      case ":":
      case ";":
      case "...":
      case "--":
      case "::":
      case "-":
        return pos.isTag(POS.COLON);
      case "$":
        return pos.isTag(POS.DOLLAR);
      case ".":
      case "!":
      case "?":
        return pos.isTag(POS.PERIOD, POS.COLON);
      case "{":
        return pos.isTag(POS.LCB);
      case "}":
        return pos.isTag(POS.RCB);
      case "[":
        return pos.isTag(POS.LSB);
      case "]":
        return pos.isTag(POS.RSB);
      case "(":
        return pos.isTag(POS.LRB);
      case ")":
        return pos.isTag(POS.RRB);
      case "&":
        return pos.isTag(POS.CC, POS.SYM);
    }

    boolean hasLetterOrDigit = StringPredicates.HAS_LETTER_OR_DIGIT.test(word);
    if (!hasLetterOrDigit && word.endsWith("-")) {
      return pos.isTag(POS.COLON);
    }

    if (word.contains("$")) {
      return pos.isTag(POS.SYM, POS.CD, POS.DOLLAR);
    }

    if (word.equals("%")) {
      return pos.isTag(POS.SYM);
    }

    if (!hasLetterOrDigit) {
      return pos.isTag(POS.SYM, POS.CD);
    }

    return !pos.isTag(
      POS.QUOTE, POS.HASH, POS.COMMA, POS.COLON, POS.DOLLAR, POS.PERIOD,
      POS.LCB, POS.RCB, POS.LSB, POS.RSB, POS.LRB, POS.RRB
    );
  }
}//END OF POSValidator
