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

package com.davidbracewell.hermes.corpus.spi.conll;

import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.AttributeType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.corpus.spi.CoNLLColumnProcessor;
import com.davidbracewell.hermes.corpus.spi.CoNLLRow;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;

import java.util.List;
import java.util.Map;

import static com.davidbracewell.collection.map.Maps.map;

/**
 * @author David B. Bracewell
 */
public abstract class IOBFieldProcessor implements CoNLLColumnProcessor {

   private final AnnotationType annotationType;
   private final AttributeType attributeType;

   public IOBFieldProcessor(AnnotationType annotationType, AttributeType attributeType) {
      this.annotationType = annotationType;
      this.attributeType = attributeType;
   }

   private boolean isI(String value, String target) {
      if (value == null || value.startsWith("O") || value.startsWith("B-")) {
         return false;
      }
      return value.startsWith("I-") && value.substring(2).toUpperCase().equals(target);
   }

   protected String normalizeTag(String tag) {
      return tag;
   }

   @Override
   public void processInput(Document document, List<CoNLLRow> rows, Map<Tuple2<Integer, Integer>, Long> sentenceIndexToAnnotationId) {
      final String TYPE = getFieldName();
      for (int i = 0; i < rows.size(); ) {
         if (rows.get(i).hasOther(TYPE)) {
            String value = rows.get(i).getOther(TYPE).toUpperCase();
            if (StringUtils.isNotNullOrBlank(value) && (value.startsWith("B-") || value.startsWith("I-"))) {
               int start = rows.get(i).getStart();
               String tag = value.substring(2);
               i++;
               while (i < rows.size() && isI(rows.get(i).getOther(TYPE), tag)) {
                  i++;
               }
               i--;
               int end = rows.get(i).getEnd();
               String normalizedTag = normalizeTag(tag);
               if (StringUtils.isNotNullOrBlank(normalizedTag)) {
                  document.createAnnotation(annotationType,
                                            start,
                                            end,
                                            map(attributeType,
                                                attributeType.getValueType().convert(normalizeTag(tag))));
               }
            }
         }
         i++;
      }
      document.getAnnotationSet().setIsCompleted(annotationType, true, "PROVIDED");
   }

   @Override
   public String processOutput(Annotation document, Annotation token, int index) {
      Annotation a = token.first(annotationType);
      if (a.isDetached()) {
         return "O";
      }
      return a.getTag().map(tag -> (a.firstToken() == token ? "B-" : "I-") + tag.name()).orElse("O");
   }


}//END OF IOBFieldProcessor
