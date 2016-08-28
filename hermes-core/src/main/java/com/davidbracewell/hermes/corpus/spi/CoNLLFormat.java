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
import com.davidbracewell.collection.map.Maps;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.attribute.POS;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;
import lombok.NonNull;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.davidbracewell.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(CorpusFormat.class)
public class CoNLLFormat extends FileBasedFormat {
   private static final long serialVersionUID = 1L;
   public static final String FIELDS_PROPERTY = "CONLL.fields";
   public static final String FS_PROPERTY = "CONLL.fs";
   public static final String DOC_PER_SENT_PROPERTY = "CONLL.docPerSent";
   public static final String OVERRIDE_SENTENCES = "CONLL.overrideSentences";
   public static final String EMPTY_FIELD = "_";

   public static void setFields(@NonNull CoNLLColumnProcessor... types) {
      Config.setProperty(FIELDS_PROPERTY,
                         Stream.of(types).map(CoNLLColumnProcessor::getFieldName).collect(Collectors.joining(","))
                        );
   }

   public static void setFields(@NonNull String... types) {
      Config.setProperty(FIELDS_PROPERTY, Stream.of(types).map(String::toUpperCase).collect(Collectors.joining(",")));
   }

   public static void setOneDocumentPerSentence(boolean oneDocumentPerSentence) {
      Config.setProperty(DOC_PER_SENT_PROPERTY, Boolean.toString(oneDocumentPerSentence));
   }

   public static void setFieldSeparator(@NonNull String fs) {
      Config.setProperty(FS_PROPERTY, fs);
   }

   public static void setOverrideSentences(boolean override) {
      Config.setProperty(OVERRIDE_SENTENCES, Boolean.toString(override));
   }

   private List<CoNLLColumnProcessor> getProcessors() {
      List<String> fields = Config.get(FIELDS_PROPERTY).asList(String.class);
      if (fields == null || fields.isEmpty()) {
         fields = Arrays.asList("WORD", "POS", "CHUNK");
      }
      return CoNLLProcessors.get(fields);
   }

   private Document createDocument(String content, List<CoNLLRow> list, DocumentFactory documentFactory) {
      Document document = documentFactory.createRaw(content);
      int lastSentenceStart = -1;
      int sentenceIndex = 0;
      Map<Tuple2<Integer, Integer>, Long> sentenceIndexToIDMap = new HashMap<>();

      boolean keepSentences = !Config.get(OVERRIDE_SENTENCES).asBooleanValue(false);


      for (ListIterator<CoNLLRow> iterator = list.listIterator(); iterator.hasNext(); ) {
         CoNLLRow token = iterator.next();
         if (lastSentenceStart == -1) {
            lastSentenceStart = token.getStart();
         }
         token.setAnnotationID(document.createAnnotation(Types.TOKEN, token.getStart(), token.getEnd()).getId());
         sentenceIndexToIDMap.put($(token.getSentence(), token.getIndex()), token.getAnnotationID());
         if (!iterator.hasNext() || token.getSentence() != list.get(iterator.nextIndex()).getSentence()) {
            if (keepSentences) {
               document.createAnnotation(Types.SENTENCE,
                                         lastSentenceStart,
                                         token.getEnd(),
                                         Maps.map(Types.INDEX, sentenceIndex)
                                        );
            }
            sentenceIndex++;
            lastSentenceStart = -1;
         }
      }
      for (CoNLLColumnProcessor processor : getProcessors()) {
         processor.processInput(document, list, sentenceIndexToIDMap);
      }
      if (keepSentences) {
         document.getAnnotationSet().setIsCompleted(Types.SENTENCE, true, "PROVIDED");
      }
      document.getAnnotationSet().setIsCompleted(Types.TOKEN, true, "PROVIDED");
      return document;
   }


   @Override
   public Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
      List<Document> documents = new LinkedList<>();
      List<CoNLLRow> list = new ArrayList<>();
      int sentenceIndex = 0;
      StringBuilder content = new StringBuilder();
      int lastSize = 0;

      final List<CoNLLColumnProcessor> processors = getProcessors();
      final String FS = Config.get(FS_PROPERTY).asString("\\s+");
      final boolean oneDocumentPerSentence = Config.get(DOC_PER_SENT_PROPERTY).asBooleanValue(false);

      for (String line : resource.readLines()) {
         if (StringUtils.isNullOrBlank(line)) {
            if (list.size() > lastSize) {
               sentenceIndex++;
               if (oneDocumentPerSentence) {
                  documents.add(createDocument(content.toString(), list, documentFactory));
                  sentenceIndex = 0;
                  list.clear();
                  content.setLength(0);
                  lastSize = 0;
               }
            }
         } else {
            List<String> parts = Arrays.asList(line.split(FS));
            CoNLLRow row = new CoNLLRow();
            row.setSentence(sentenceIndex);
            for (int i = 0; i < processors.size(); i++) {
               if (StringUtils.isNullOrBlank(parts.get(i))) {
                  continue;
               }
               switch (processors.get(i).getFieldName()) {
                  case "INDEX":
                     row.setIndex(Integer.parseInt(parts.get(i)));
                     break;
                  case "WORD":
                     row.setWord(POSCorrection.word(parts.get(i).replaceAll(StringUtils.MULTIPLE_WHITESPACE, ""),
                                                    POS.ANY.asString()
                                                   )
                                );
                     break;
                  case "POS":
                     row.setPos(parts.get(i));
                     break;
                  case "HEAD":
                     row.setParent(Integer.parseInt(parts.get(i)));
                     break;
                  case "DEPENDENCY_RELATION":
                     row.setDepRelation(parts.get(i));
                     break;
                  case "IGNORE":
                     break;
                  default:
                     row.addOther(processors.get(i).getFieldName(), parts.get(i));
                     break;
               }
            }
            row.setStart(content.length());
            content.append(row.getWord()).append(" ");
            row.setEnd(content.length() - 1);
            list.add(row);
         }
      }

      if (list.size() > 0) {
         documents.add(createDocument(content.toString(), list, documentFactory));
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
      List<CoNLLColumnProcessor> processors = getProcessors();
      String fieldSep = Config.get(FS_PROPERTY).asString("\\s+").replaceFirst("[\\*\\+]$", "");
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
      }
      resource.write(builder.toString());
   }

}//END OF CoNLLFormat

