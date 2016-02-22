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
import com.davidbracewell.hermes.corpus.DocumentFormat;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.StructuredFormat;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.Collections;

/**
 * The type XML format.
 *
 * @author David B. Bracewell
 */
@MetaInfServices(DocumentFormat.class)
public class XMLFormat extends FileBasedFormat {
  private static final long serialVersionUID = 1L;

  @Override
  public String name() {
    return "XML";
  }

  @Override
  public Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
    return Collections.singleton(Document.read(StructuredFormat.XML, resource));
  }

  @Override
  public void write(Resource resource, Document document) throws IOException {
    document.write(StructuredFormat.XML, resource);
  }

}//END OF XMLFormat