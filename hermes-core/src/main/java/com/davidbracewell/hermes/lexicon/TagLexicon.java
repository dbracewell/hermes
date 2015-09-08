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

import com.davidbracewell.conversion.Convert;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.tag.Tag;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.csv.CSVReader;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.util.*;

/**
 * The type Tag lexicon.
 *
 * @author David B. Bracewell
 */
public abstract class TagLexicon extends ProbabilisticLexicon {

  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Tag lexicon.
   *
   * @param caseSensitive the case sensitive
   */
  protected TagLexicon(boolean caseSensitive) {
    super(caseSensitive);
  }


  /**
   * Load lexicon.
   *
   * @param lexicons the lexicons
   * @param tagType  the tag type
   * @return the map
   */
  protected Map<String, Tuple2<Tag, Double>> loadLexicon(Collection<Resource> lexicons, Class<? extends Tag> tagType) {
    Map<String, Tuple2<Tag, Double>> map = new HashMap<>();
    for (Resource lexicon : lexicons) {
      try (CSVReader reader = CSV.builder().reader(lexicon)) {
        List<String> row;
        while ((row = reader.nextRow()) != null) {
          String lexicalItem = null;
          String tagValue = null;
          String probability = "1";
          switch (row.size()) {
            case 0:
              continue;
            case 1:
              lexicalItem = row.get(0);
              break;
            case 2:
              lexicalItem = row.get(0);
              tagValue = row.get(1);
              break;
            default:
              lexicalItem = row.get(0);
              tagValue = row.get(1);
              probability = row.get(2);
          }

          if (lexicalItem != null) {
            if (!isCaseSensitive()) {
              lexicalItem = lexicalItem.toLowerCase();
            }
            map.put(lexicalItem, Tuple2.<Tag, Double>of(Convert.convert(tagValue, tagType), Convert.convert(probability, Double.class)));
          }

        }
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }

    return map;
  }


  /**
   * Looks up the tag for a given fragment
   *
   * @param fragment The fragment to look up
   * @return The tag for the fragment
   */
  public final Optional<Tag> lookup(HString fragment) {
    if (fragment == null) {
      return Optional.empty();
    }
    Optional<Tag> tag = lookup(fragment.toString());
    if (!tag.isPresent()) {
      tag = lookup(fragment.getLemma());
    }
    return tag;
  }

  /**
   * Looks up the tag for a given lexical item
   *
   * @param lexicalItem The lexical item to look up
   * @return The tag for the lexical item
   */
  public final Optional<Tag> lookup(String lexicalItem) {
    if (lexicalItem == null) {
      return Optional.empty();
    }
    Optional<Tag> tag = lookupImpl(lexicalItem);
    if (!tag.isPresent() && !isCaseSensitive()) {
      tag = lookupImpl(lexicalItem.toLowerCase());
    }

    return tag;
  }

  /**
   * Lookup implementation.
   *
   * @param nonNullLexicalItem the non null lexical ttem
   * @return the optional tag of the lexical item
   */
  protected abstract Optional<Tag> lookupImpl(String nonNullLexicalItem);


}//END OF TagLexicon
