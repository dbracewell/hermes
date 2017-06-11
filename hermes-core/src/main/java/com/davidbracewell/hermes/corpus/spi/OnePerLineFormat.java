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

import com.davidbracewell.collection.Collect;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Logger;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * The type One per line format.
 *
 * @author David B. Bracewell
 */
public class OnePerLineFormat extends FileBasedFormat {
   private static final long serialVersionUID = 1L;

   private final CorpusFormat subFormat;

   /**
    * Instantiates a new One per line format.
    *
    * @param subFormat the sub format
    */
   public OnePerLineFormat(@NonNull CorpusFormat subFormat) {
      this.subFormat = subFormat;
   }

   @Override
   public String extension() {
      return subFormat.extension() + "_opl";
   }

   @Override
   public boolean isOnePerLine() {
      return true;
   }

   @Override
   public String name() {
      return "OPL";
   }

   @Override
   public Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
      return Collect.asIterable(new LineIterator(resource, subFormat, documentFactory));
   }

   @Override
   public String toString(Document document) {
      String output = subFormat.toString(document);
      if (subFormat.isOnePerLine()) {
         return output.trim() + "\n";
      }
      return output.replaceAll("\n", "\\n") + "\n";
   }

   private static class LineIterator implements Iterator<Document> {

      final BufferedReader reader;
      final CorpusFormat format;
      final DocumentFactory documentFactory;
      final Logger log = Logger.getLogger(LineIterator.class);
      final Queue<Document> documentQueue = new LinkedList<>();
      boolean isClosed = false;
      String line = null;

      private LineIterator(Resource input, CorpusFormat format, DocumentFactory documentFactory) {
         this.documentFactory = documentFactory;
         try {
            this.reader = new BufferedReader(input.reader());
            this.format = format;
         } catch (IOException e) {
            throw Throwables.propagate(e);
         }
      }

      private boolean advance() {
         if (isClosed) {
            return false;
         }
         if (!documentQueue.isEmpty()) {
            return true;
         }
         try {
            line = reader.readLine();
            while (line != null && StringUtils.isNullOrBlank(line)) {
               line = reader.readLine();
            }

            if (line == null) {
               reader.close();
               isClosed = true;
               return false;
            } else {
               for (Document d : format.read(Resources.fromString(line.replaceAll("\\r?\\n", "\n")), documentFactory)) {
                  documentQueue.add(d);
               }
            }

         } catch (IOException e) {
            log.warn(e);
            line = null;
         }

         return documentQueue.size() > 0;
      }

      @Override
      public boolean hasNext() {
         return advance();
      }

      @Override
      public Document next() {
         if (!advance()) {
            throw new NoSuchElementException();
         }
         Document toReturn = documentQueue.remove();
         advance();
         return toReturn;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

}//END OF OnePerLineFormat
