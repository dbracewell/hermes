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

package com.davidbracewell.hermes.corpus;

import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.io.resource.Resource;

import java.io.IOException;

/**
 * @author David B. Bracewell
 */
public interface CorpusFormat {

  String JSON = "JSON";
  String CONLL = "CONLL";
  String PLAIN_TEXT = "TEXT";

  String JSON_OPL = "JSON_OPL";
  String CONLL_OPL = "CONLL_OPL";
  String PLAIN_TEXT_OPL = "TEXT_OPL";


  Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException;

  String name();

}//END OF CorpusFormat
