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

import com.davidbracewell.hermes.HString;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.csv.CSVReader;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Probabilistic lexicon.
 *
 * @author David B. Bracewell
 */
public abstract class ProbabilisticLexicon extends Lexicon {

  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Tag lexicon.
   *
   * @param caseSensitive the case sensitive
   */
  protected ProbabilisticLexicon(boolean caseSensitive) {
    super(caseSensitive);
  }

  /**
   * Load probabilistic lexicon.
   *
   * @param lexicons the lexicons
   * @return the map
   */
  protected Map<String, Double> loadProbabilisticLexicon(Collection<Resource> lexicons) {
    Map<String, Double> dict = new HashMap<>();
    try {
      for (Resource lexicon : lexicons) {
        try (CSVReader csvReader = CSV.builder().reader(lexicon)) {
          csvReader.forEach(row -> {
            if (row.size() >= 2) {
              if (isCaseSensitive()) {
                dict.put(row.get(0), Double.parseDouble(row.get(1)));
              } else {
                dict.put(row.get(0).toLowerCase(), Double.parseDouble(row.get(1)));
              }
            }
          });
        }
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    return dict;
  }

  /**
   * Probability double.
   *
   * @param lexicalItem the lexical item
   * @return the double
   */
  public final double probability(String lexicalItem) {
    if (lexicalItem == null) {
      return 0d;
    }
    if (isCaseSensitive()) {
      return probabilityImpl(lexicalItem);
    }
    return probabilityImpl(lexicalItem.toLowerCase());
  }

  public final double probability(HString fragment) {
    if (fragment == null) {
      return 0d;
    }
    return Math.max(probability(fragment.toString()), probability(fragment.getLemma()));
  }


  protected abstract double probabilityImpl(String lexicalItem);

}//END OF ProbabilisticLexicon
