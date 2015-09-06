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

import com.davidbracewell.collection.NormalizedStringMap;
import com.davidbracewell.collection.Streams;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.corpus.spi.OnePerLineFormat;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Logger;
import com.davidbracewell.string.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * <p>Represents a corpus of documents in the form of an iterable. Provides methods for filtering using a predicate and
 * a convenience method for retrieving the first document.</p>
 *
 * @author David B. Bracewell
 */
public abstract class Corpus implements Iterable<Document> {
  private static final Logger log = Logger.getLogger(Corpus.class);
  private static final Map<String, CorpusFormat> corpusFormats = new NormalizedStringMap<>();

  static {
    for (CorpusFormat format : ServiceLoader.load(CorpusFormat.class)) {
      corpusFormats.put(format.name(), format);
    }
  }


  public static Corpus from(@Nonnull String format, @Nonnull final Resource resource) throws IOException {
    return from(format, resource, DocumentFactory.getInstance());
  }

  public static Corpus from(@Nonnull String format, @Nonnull final Resource resource, @Nonnull final DocumentFactory documentFactory) throws IOException {
    format = StringUtils.trim(format).toUpperCase();
    boolean isOPL = format.endsWith("_OPL");
    final String normFormat = format.replaceAll("_OPL$", "").trim();
    if (corpusFormats.containsKey(normFormat)) {
      return new Corpus() {
        final CorpusFormat cFormat = isOPL ? new OnePerLineFormat(corpusFormats.get(normFormat)) : corpusFormats.get(normFormat);

        @Override
        public Iterator<Document> iterator() {
          try {
            return cFormat.read(resource, documentFactory).iterator();
          } catch (IOException e) {
            log.log(Level.SEVERE, "", e);
            return Collections.emptyIterator();
          }
        }
      };
    }
    throw new IllegalArgumentException("No corpus format registered for " + format);
  }

  /**
   * Gets the first document
   *
   * @return The first document as an Optional
   */
  public Optional<Document> first() {
    return stream().findFirst();
  }

  /**
   * Stream stream.
   *
   * @return A stream over the documents in this document source
   */
  public Stream<Document> stream() {
    return Streams.from(this);
  }

}//END OF Corpus
