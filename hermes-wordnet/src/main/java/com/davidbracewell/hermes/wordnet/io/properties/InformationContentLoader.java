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

import com.davidbracewell.collection.Counter;
import com.davidbracewell.collection.Counters;
import com.davidbracewell.hermes.attribute.POS;
import com.davidbracewell.hermes.wordnet.Synset;
import com.davidbracewell.hermes.wordnet.WordNetPOS;
import com.davidbracewell.hermes.wordnet.io.WordNetDB;
import com.davidbracewell.hermes.wordnet.io.WordNetPropertyLoader;
import com.davidbracewell.hermes.wordnet.properties.InformationContent;
import com.davidbracewell.hermes.wordnet.properties.PropertyName;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.stream.MStream;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import java.io.IOException;

/**
 * @author David B. Bracewell
 */
public class InformationContentLoader extends WordNetPropertyLoader {

  private final Resource resource;
  private final PropertyName propertyName;

  public InformationContentLoader(Resource resource, String resourceName) {
    Preconditions.checkArgument(!StringUtils.isNullOrBlank(resourceName));
    this.resource = Preconditions.checkNotNull(resource);
    this.propertyName = PropertyName.create(resourceName);
  }

  @Override
  public void load(WordNetDB db) {
    Counter<String> ic = Counters.newHashMapCounter();
    Counter<POS> roots = Counters.newHashMapCounter();
    try (MStream<String> stream = resource.lines()) {
      stream.forEach(line -> {
        line = line.trim();
        if (!StringUtils.isNullOrBlank(line) && !line.startsWith("wnver")) {
          String[] parts = line.split("\\s+");
          String key = parts[0];
          double cnt = Double.parseDouble(parts[1]);
          ic.set(key, cnt);
          if (parts.length == 3 && parts[2].equalsIgnoreCase("ROOT")) {
            roots.increment(WordNetPOS.fromString(key.substring(key.length() - 1)).toHermesPOS(), cnt);
          }
        }
      });
    } catch (IOException ioe) {
      throw Throwables.propagate(ioe);
    }
    for (String key : ic.items()) {
      Synset synset = db.getSynsetFromId(
        Strings.padStart(key, 9, '0').toLowerCase()
      );
      setProperty(
        synset,
        propertyName,
        new InformationContent(ic.get(key) / roots.get(synset.getPOS()))
      );
    }
  }

}//END OF InformationContentLoader
