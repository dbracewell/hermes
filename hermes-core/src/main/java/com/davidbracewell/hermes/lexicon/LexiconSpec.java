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

import com.davidbracewell.Tag;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.AttributeType;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.regex.QueryToPredicate;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.csv.CSVReader;
import com.davidbracewell.logging.Logger;
import com.davidbracewell.parsing.ParseException;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * The type Lexicon spec.
 *
 * @author David B. Bracewell
 */
@Data
public class LexiconSpec implements Serializable {
  private static final Logger log = Logger.getLogger(LexiconSpec.class);

  private static final long serialVersionUID = 1L;
  private boolean probabilistic;
  private boolean hasConstraints;
  private boolean caseSensitive;
  private AttributeType tagAttributeType;
  private boolean useResourceNameAsTag;
  private Resource resource;

  /**
   * Instantiates a new Lexicon spec.
   */
  public LexiconSpec() {
    this(false, false, false, null, null, false);
  }

  /**
   * Instantiates a new Lexicon spec.
   *
   * @param caseSensitive        the case sensitive
   * @param hasConstraints       the has constraints
   * @param probabilistic        the probabilistic
   * @param resource             the resource
   * @param tagAttributeType         the tag attribute
   * @param useResourceNameAsTag the use resource name as tag
   */
  @Builder
  public LexiconSpec(boolean caseSensitive, boolean hasConstraints, boolean probabilistic, Resource resource, AttributeType tagAttributeType, boolean useResourceNameAsTag) {
    this.caseSensitive = caseSensitive;
    this.hasConstraints = hasConstraints;
    this.probabilistic = probabilistic;
    this.resource = resource;
    this.tagAttributeType = tagAttributeType;
    this.useResourceNameAsTag = useResourceNameAsTag;
  }

  /**
   * Create lexicon.
   *
   * @return the lexicon
   * @throws Exception the exception
   */
  public Lexicon create() throws Exception {
    Lexicon lexicon = new TrieLexicon(caseSensitive, probabilistic, tagAttributeType);
    if (resource != null) {
      String base = resource.baseName().replaceFirst("\\.[^\\.]*$", "");
      try (CSVReader reader = CSV.builder().reader(resource)) {
        reader.forEach(row -> {
          String lemma = row.get(0);
          double probability = 1d;
          Tag tag = null;
          SerializablePredicate<HString> constraint = null;

          int nc = 1;
          if (tagAttributeType != null && !useResourceNameAsTag) {
            tag = tagAttributeType.getValueType().convert(row.get(nc));
            if (tag == null) {
              log.warn("{0} is an invalid {1}, skipping entry {2}.", row.get(nc), tagAttributeType.name(), row);
              return;
            }
            nc++;
          } else if (tagAttributeType != null) {
            tag = tagAttributeType.getValueType().convert(base);
            Preconditions.checkNotNull(tag, row.get(nc) + " is an invalid tag.");
          }

          if (probabilistic && row.size() > nc && Doubles.tryParse(row.get(nc)) != null) {
            probability = Double.parseDouble(row.get(nc));
            nc++;
          }


          if (hasConstraints && row.size() > nc) {
            try {
              constraint = QueryToPredicate.parse(row.get(nc));
            } catch (ParseException e) {
              if (tag == null) {
                log.warn("Error parsing constraint {0}, skipping entry {1}.", row.get(nc), row);
                return;
              }
            }
          }

          lexicon.add(new LexiconEntry(lemma, probability, constraint, tag));
        });
      }
    }
    return lexicon;
  }

}//END OF LexiconSpec
