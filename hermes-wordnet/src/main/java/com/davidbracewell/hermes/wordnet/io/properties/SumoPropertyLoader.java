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

package com.davidbracewell.hermes.wordnet.io.properties;

import com.davidbracewell.hermes.wordnet.Synset;
import com.davidbracewell.hermes.wordnet.io.WordNetDB;
import com.davidbracewell.hermes.wordnet.properties.PropertyName;
import com.davidbracewell.hermes.wordnet.properties.SumoConcept;
import com.davidbracewell.hermes.wordnet.properties.SumoRelation;
import com.davidbracewell.io.resource.Resource;
import com.google.common.base.Strings;

import java.util.List;

/**
 * @author dbracewell
 */
public class SumoPropertyLoader extends TSVPropertyLoader {

  public SumoPropertyLoader(Resource resource) {
    super(resource, "SUMO");
  }

  @Override
  protected void processRow(List<String> row, WordNetDB db, PropertyName name) {
    if( row.size() >= 4 ){
      final String key = row.get(0) + row.get(1);
      Synset synset = db.getSynsetFromId(
        Strings.padStart(key, 9, '0').toLowerCase()
      );

      for (int i = 2; i < row.size(); i += 2) {
        if (Strings.isNullOrEmpty(row.get(i)) || Strings.isNullOrEmpty(row.get(i + 1))) {
          break;
        }
        String concept = row.get(i);
        SumoRelation relation = SumoRelation.fromString(row.get(i + 1));

        setProperty(
          synset,
          name,
          new SumoConcept(concept,relation)
        );
      }
    }
  }

}//END OF SumoPropertyLoader
