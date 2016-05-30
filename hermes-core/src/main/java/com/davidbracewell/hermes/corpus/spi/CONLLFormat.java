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
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;
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
@MetaInfServices(CorpusFormat.class)
public class CONLLFormat extends FileBasedFormat {
  private static final long serialVersionUID = 1L;


  public enum FieldType {
    INDEX {
      @Override
      public FieldProcessor getProcessor(int index) {
        return IndexProcessor.INSTANCE;
      }
    },
    WORD {
      @Override
      public FieldProcessor getProcessor(int index) {
        return WordProcessor.INSTANCE;
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
        String word = wordInfo.get(wordIndex).replaceAll(StringUtils.MULTIPLE_WHITESPACE, "");
        switch (word) {
          case "\"\"":
          case "``":
          case "''":
            word = "\"";
            break;
        }
        tokens.add(word);
      } else {
        System.err.println("BAD: " + wordInfo);
      }
    }
    Document document = documentFactory.fromTokens(tokens);
    for (FieldProcessor processor : getProcessors()) {
      processor.process(document, rows);
    }
    return document;
  }


  @Override
  public Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
    List<List<String>> rows = new ArrayList<>();
    List<Tuple2<Integer, Integer>> sentenceBoundaries = new ArrayList<>();
    List<Document> documents = new LinkedList<>();

    int start = 0;
    int end = 0;
    byte state = 0;

    boolean docPerSent = Config.get("CONLL.docPerSent").asBooleanValue(false);

    for (String line : resource.readLines()) {
      if (StringUtils.isNullOrBlank(line)) {
        if (state == 1) { //END OF SENTENCE
          if (docPerSent) {
            Document doc = createDocument(rows, documentFactory);
            doc.createAnnotation(Types.SENTENCE, 0, doc.length());
            doc.getAnnotationSet().setIsCompleted(Types.SENTENCE, true, "PROVIDED");
            doc.put(Types.FILE, resource.descriptor());
            documents.add(doc);
            rows.clear();
          } else {
            sentenceBoundaries.add(Tuple2.of(start, end));
          }
          start = -1;
        }
        state = 0;
      } else {
        if (state == 0) {
          start = end;
          state = 1;
        }
        rows.add(Arrays.asList(line.split(Config.get("CONLL.fs").asString("\\s+"))));
        end++;
      }

    }

    if (state == 1) {
      if (docPerSent) {
        Document doc = createDocument(rows, documentFactory);
        doc.createAnnotation(Types.SENTENCE, 0, doc.length());
        doc.getAnnotationSet().setIsCompleted(Types.SENTENCE, true, "PROVIDED");
        doc.put(Types.FILE, resource.descriptor());
        documents.add(doc);
        rows.clear();
      } else {
        sentenceBoundaries.add(Tuple2.of(start, end));
      }
    }
    if (!docPerSent) {
      Document doc = createDocument(rows, documentFactory);
      sentenceBoundaries.forEach(t -> doc.createAnnotation(Types.SENTENCE, doc.tokenAt(t.v1).start(), doc.tokenAt(t.v2 - 1).end()));
      doc.getAnnotationSet().setIsCompleted(Types.SENTENCE, true, "PROVIDED");
      doc.put(Types.FILE, resource.descriptor());
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
    String fieldSep = Config.get("CONLL.fs").asString("\\s+").replaceFirst("[\\*\\+]$", "");
    if (fieldSep.equals("\\s")) {
      fieldSep = " ";
    }
    for (Annotation sentence : document.sentences()) {
      for (int i = 0; i < sentence.tokenLength(); i++) {
        for (int p = 0; p < processors.size(); p++) {
          if (p > 0) {
            builder.append(fieldSep);
          }
          builder.append(processors.get(p).processOutput(sentence, sentence.tokenAt(i), i));
        }
        builder.append(SystemInfo.LINE_SEPARATOR);
      }
      builder.append(SystemInfo.LINE_SEPARATOR);
      builder.append(SystemInfo.LINE_SEPARATOR);
    }
    resource.write(builder.toString());
  }

}//END OF CONLLFormat
