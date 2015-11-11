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

package com.davidbracewell.hermes.lexicon.spi;

import com.davidbracewell.Tag;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.lexicon.Lexicon;
import com.davidbracewell.hermes.lexicon.TrieTagLexicon;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.structured.csv.CSVReader;
import com.davidbracewell.logging.Logger;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(LexiconSupplier.class)
public class TrieTagLexiconSupplier implements LexiconSupplier {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(TrieTagLexiconSupplier.class);

  @Override
  public Lexicon get(String lexiconName) throws IOException {
    Attribute tagAttribute = Attribute.create(Config.get(lexiconName, "tag").asString());
    boolean isCaseSensitive = Config.get(lexiconName, "caseSensitive").asBooleanValue(true);
    Map<String, Tag> map = new HashMap<>();
    try (CSVReader reader = CSV.builder().reader(Config.get(lexiconName, "resource").asResource())) {
      reader.forEach(row -> {
          if (row.size() >= 2) {
            String lexicalItem = isCaseSensitive ? row.get(0).trim() : row.get(0).trim().toLowerCase();
            Tag tag = tagAttribute.getValueType().convert(row.get(1));
            if (tag != null) {
              map.put(lexicalItem, tag);
            } else {
              log.warn("Invalid tag {0}", row.get(0));
            }
          }
        }
      );
    }
    TrieTagLexicon lexicon = new TrieTagLexicon(tagAttribute, map, isCaseSensitive);
    lexicon.setFuzzyMatch(Config.get(lexiconName, "fuzzyMatch").asBooleanValue(false));
    return lexicon;
  }

  @Override
  public Class<?> getLexiconClass() {
    return TrieTagLexicon.class;
  }

}//END OF TrieTagLexiconSupplier
