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
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Relation;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.spi.CoNLLColumnProcessor;
import com.davidbracewell.hermes.corpus.spi.CoNLLRow;
import com.davidbracewell.tuple.Tuple2;
import org.kohsuke.MetaInfServices;

import java.util.List;
import java.util.Map;

import static com.davidbracewell.hermes.corpus.spi.CoNLLFormat.EMPTY_FIELD;
import static com.davidbracewell.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
@MetaInfServices
public class DependencyLinkProcessor implements CoNLLColumnProcessor {

  @Override
  public void processInput(Document document, List<CoNLLRow> documentRows, Map<Tuple2<Integer, Integer>, Long> sentenceIndexToAnnotationId) {
    documentRows.forEach(row -> {
      if (row.getParent() != 0) {
        long target = sentenceIndexToAnnotationId.get($(row.getSentence(), row.getParent()));
        document.getAnnotation(row.getAnnotationID()).get()
          .add(new Relation(Types.DEPENDENCY, row.getDepRelation(), target));
      }
    });
  }

  @Override
  public String processOutput(Annotation document, Annotation token, int index) {
    long targetID = token.dependencyRelation().map(Tuple2::getV2).map(Annotation::getId).orElse(-1L);
    if (targetID == -1) {
      return EMPTY_FIELD;
    }
    List<Annotation> sentence = document.tokens();
    for (int i = 0; i < sentence.size(); i++) {
      if (sentence.get(i).getId() == targetID) {
        return Integer.toString(i + 1);
      }
    }
    return EMPTY_FIELD;
  }

  @Override
  public String getFieldName() {
    return "DEPENDENCY_PARENT";
  }

}//END OF DependencyLinkProcessor
