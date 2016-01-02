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

import com.davidbracewell.SystemInfo;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.DocumentFormat;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.CSVFormatter;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Joiner;
import lombok.NonNull;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(DocumentFormat.class)
public class CONLLFormat extends FileBasedFormat {
  private static final long serialVersionUID = 1L;


  public enum FieldType {
    INDEX {
      @Override
      public FieldProcessor getProcessor(int index) {
        return NoOptProcessor.INSTANCE;
      }
    },
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
    ENTITY {
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

  private volatile List<FieldProcessor> processors;
  private int wordIndex;

  private List<FieldProcessor> getProcessors() {
    if (processors == null) {
      synchronized (this) {
        if (processors == null) {
          processors = new ArrayList<>();
          int i = 0;
          for (FieldType fieldType : Config.get("CONLL.fields").as(FieldType[].class, new FieldType[]{FieldType.WORD, FieldType.POS, FieldType.CHUNK})) {
            processors.add(fieldType.getProcessor(i));
            if (fieldType == FieldType.WORD) {
              wordIndex = i;
            }
            i++;
          }
        }
      }
    }
    return processors;
  }

  private Document createDocument(List<List<String>> rows, DocumentFactory documentFactory) {
    getProcessors();
    List<String> tokens = new ArrayList<>();
    for (List<String> wordInfo : rows) {
      if (wordInfo.size() > wordIndex) {
        tokens.add(wordInfo.get(wordIndex));
      } else {
        System.err.println("BAD: " + wordInfo);
      }
    }
    Document document = documentFactory.fromTokens(tokens);
    document.createAnnotation(Types.SENTENCE, 0, document.length());
    document.getAnnotationSet().setIsCompleted(Types.SENTENCE, true, "PROVIDED");
    for (FieldProcessor processor : getProcessors()) {
      processor.process(document, rows);
    }
    return document;
  }

  @Override
  public Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
    List<Document> documents = new ArrayList<>();

    List<List<String>> rows = new ArrayList<>();

    for (String line : resource.readLines()) {
      if (StringUtils.isNullOrBlank(line)) {
        if (!rows.isEmpty()) {
          Document doc = createDocument(rows, documentFactory);
          doc.put(Attrs.FILE, resource.descriptor());
          documents.add(doc);
        }
        rows = new ArrayList<>();
      } else {
        rows.add(Arrays.asList(line.split(Config.get("CONLL.fs").asString("\\s+"))));
      }
    }

    if (!rows.isEmpty()) {
      Document doc = createDocument(rows, documentFactory);
      doc.put(Attrs.FILE, resource.descriptor());
      documents.add(doc);
    }


    return documents;
  }

  @Override
  public String name() {
    return "CONLL";
  }

  @Override
  public void write(@NonNull Resource resource, @NonNull Document document) throws IOException {
    StringBuilder builder = new StringBuilder();
    List<FieldProcessor> processors = getProcessors();
    for(Annotation sentence : document.sentences()){
      for( int i = 0; i < sentence.tokenLength(); i++){
        for(int p = 0; p < processors.size(); p++){
          if( p > 0 ){
            builder.append(" ");
          }
          builder.append(processors.get(p).processOutput(sentence,sentence.tokenAt(i)));
        }
      }
      builder.append(SystemInfo.LINE_SEPARATOR);
    }
    resource.write(builder.toString());
  }

}//END OF CONLLFormat
