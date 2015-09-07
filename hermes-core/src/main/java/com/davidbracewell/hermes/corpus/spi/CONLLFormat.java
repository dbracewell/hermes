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

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.StringUtils;
import com.google.common.collect.FluentIterable;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(CorpusFormat.class)
public class CONLLFormat extends FileBasedFormat implements Serializable {
  private static final long serialVersionUID = 1L;


  public enum FieldType {
    WORD {
      @Override
      public FieldProcessor getProcessor(int index) {
        return NoOptProcessor.INSTANCE;
      }
    },
    POS {
      @Override
      public FieldProcessor getProcessor(int index) {
        return new POSFieldProcessor(index);
      }
    },
    CHUNK {
      @Override
      public FieldProcessor getProcessor(int index) {
        return IOBFieldProcessor.chunkProcessor(index);
      }
    },
    NE {
      @Override
      public FieldProcessor getProcessor(int index) {
        return IOBFieldProcessor.nameEntityProcessor(index);
      }
    },
    IGNORE {
      @Override
      public FieldProcessor getProcessor(int index) {
        return NoOptProcessor.INSTANCE;
      }
    };

    public abstract FieldProcessor getProcessor(int index);
  }

  private final List<FieldProcessor> processors;
  private int wordIndex;

  public CONLLFormat() {
    processors = new ArrayList<>();
    int i = 0;
    for (FieldType fieldType : Config.get(CONLLFormat.class, "fields").as(FieldType[].class, new FieldType[]{FieldType.WORD, FieldType.POS})) {
      processors.add(fieldType.getProcessor(i));
      if (fieldType == FieldType.WORD) {
        wordIndex = i;
      }
      i++;
    }
  }

  @Override
  protected Iterable<Document> readResource(Resource resource, DocumentFactory documentFactory) throws IOException {
    List<Document> documents = new ArrayList<>();

    List<List<String>> rows = new ArrayList<>();

    for (String line : resource) {
      if (StringUtils.isNullOrBlank(line)) {
        if (!rows.isEmpty()) {
          documents.add(createDocument(rows, documentFactory));
        }
        rows = new ArrayList<>();
      } else {
        rows.add(Arrays.asList(line.split("\\p{Z}")));
      }
    }

    if (!rows.isEmpty()) {
      documents.add(createDocument(rows, documentFactory));
    }


    return FluentIterable.from(documents);
  }

  private Document createDocument(List<List<String>> rows, DocumentFactory documentFactory) {
    List<String> tokens = new ArrayList<>();
    for (List<String> wordInfo : rows) {
      tokens.add(wordInfo.get(wordIndex));
    }
    Document document = documentFactory.fromTokens(tokens);
    document.createAnnotation(Types.SENTENCE, 0, document.length());
    document.getAnnotationSet().setIsCompleted(Types.SENTENCE, true, "Corpus");
    for (FieldProcessor processor : processors) {
      processor.process(document, rows);
    }
    return document;
  }

  @Override
  public String name() {
    return "CONLL";
  }

}//END OF CONLLFormat
