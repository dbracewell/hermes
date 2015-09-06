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

package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.resource.Resource;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.Collections;

/**
 * <p>Processor for plain text documents.</p>
 *
 * @author David B. Bracewell
 */
@MetaInfServices(CorpusFormat.class)
public class PlainTextDocumentFormat extends FileBasedFormat {

  @Override
  public String name() {
    return "TEXT";
  }

  @Override
  protected Iterable<Document> readResource(Resource resource, DocumentFactory documentFactory) throws IOException {
    return Collections.singleton(documentFactory.create(resource.readToString().trim()));
  }
}//END OF PlainTextDocumentProcessor
