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
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.attribute.POS;
import com.davidbracewell.hermes.corpus.spi.CoNLLColumnProcessor;
import com.davidbracewell.hermes.corpus.spi.CoNLLRow;
import com.davidbracewell.hermes.corpus.spi.POSCorrection;
import com.davidbracewell.tuple.Tuple2;
import org.kohsuke.MetaInfServices;

import java.util.List;
import java.util.Map;

import static com.davidbracewell.hermes.corpus.spi.CoNLLFormat.EMPTY_FIELD;

/**
 * @author David B. Bracewell
 */
@MetaInfServices
public final class POSFieldProcessor implements CoNLLColumnProcessor {

  @Override
  public void processInput(Document document, List<CoNLLRow> documentRows, Map<Tuple2<Integer, Integer>, Long> sentenceIndexToAnnotationId) {
    documentRows.forEach(row -> {
      String posStr = row.getPos();
      if (posStr.contains("|")) {
        posStr = posStr.substring(0, posStr.indexOf('|'));
      }
      document.getAnnotation(row.getAnnotationID()).get().put(Types.PART_OF_SPEECH, POS.fromString(POSCorrection.pos(row.getWord(), posStr)));

    });
    document.getAnnotationSet().setIsCompleted(Types.PART_OF_SPEECH, true, "PROVIDED");
  }

  @Override
  public String processOutput(Annotation sentence, Annotation token, int index) {
    POS pos = token.getPOS();
    return pos == null ? EMPTY_FIELD : pos.asString();
  }

  @Override
  public String getFieldName() {
    return "POS";
  }

}//END OF POSFieldProcessor
