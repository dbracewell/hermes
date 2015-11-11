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

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.lexicon.Lexicon;
import com.davidbracewell.hermes.lexicon.SetLexicon;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.structured.csv.CSVReader;
import org.kohsuke.MetaInfServices;

import java.io.IOException;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(LexiconSupplier.class)
public class SetLexiconSupplier implements LexiconSupplier {
  private static final long serialVersionUID = 1L;

  @Override
  public Lexicon get(String lexiconName) throws IOException {
    SetLexicon lexicon = new SetLexicon(Config.get(lexiconName, "caseSensitive").asBooleanValue(true));
    try (CSVReader reader = CSV.builder().reader(Config.get(lexiconName, "resource").asResource())) {
      reader.forEach(row -> lexicon.add(row.get(0)));
    }
    return lexicon;
  }

  @Override
  public Class<?> getLexiconClass() {
    return SetLexicon.class;
  }
}//END OF SetLexiconSupplier
