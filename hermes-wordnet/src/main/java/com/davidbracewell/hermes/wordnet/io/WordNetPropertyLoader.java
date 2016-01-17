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

package com.davidbracewell.hermes.wordnet.io;

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.wordnet.Synset;
import com.davidbracewell.hermes.wordnet.properties.Property;
import com.davidbracewell.hermes.wordnet.properties.PropertyName;

/**
 * <p>Loads properties associated with Synsets for a given instance of a WordNet database.</p>
 *
 * @author David B. Bracewell
 */
public abstract class WordNetPropertyLoader {


  /**
   * Loads the properties into the given database
   *
   * @param db the database to load the properties for
   */
  public abstract void load(WordNetDB db);


  /**
   * Set property.
   *
   * @param synset   the synset
   * @param name     the name
   * @param property the property
   */
  protected final void setProperty(Synset synset, PropertyName name, Property property) {
    Cast.as(synset, SynsetImpl.class).setProperty(name, property);
  }


}//END OF WordNetPropertyLoader
