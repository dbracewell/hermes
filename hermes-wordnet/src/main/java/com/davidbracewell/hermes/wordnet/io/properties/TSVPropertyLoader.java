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

import com.davidbracewell.hermes.wordnet.io.WordNetDB;
import com.davidbracewell.hermes.wordnet.io.WordNetPropertyLoader;
import com.davidbracewell.hermes.wordnet.properties.PropertyName;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.csv.CSVReader;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import java.util.List;

/**
 * @author dbracewell
 */
public abstract class TSVPropertyLoader extends WordNetPropertyLoader {
  private final Resource resource;
  private final PropertyName propertyName;

  public TSVPropertyLoader(Resource resource, String resourceName) {
    Preconditions.checkArgument(!StringUtils.isNullOrBlank(resourceName));
    this.resource = Preconditions.checkNotNull(resource);
    this.propertyName = PropertyName.create(resourceName);
  }

  @Override
  public void load(WordNetDB db) {
    try(CSVReader reader = CSV.builder().delimiter('\t').comment('#').reader(resource)){
      List<String> row;
      while( (row=reader.nextRow()) != null ){
        processRow(row,db,propertyName);
      }
    }catch (Exception e ){
      throw Throwables.propagate(e);
    }
  }

  protected abstract void processRow(List<String> row, WordNetDB db, PropertyName name);

}//END OF TSVPropertyLoader
