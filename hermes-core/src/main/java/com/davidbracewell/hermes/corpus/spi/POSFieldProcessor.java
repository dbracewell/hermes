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

import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.tag.POS;

import java.util.List;

/**
 * @author David B. Bracewell
 */
public class POSFieldProcessor implements FieldProcessor {

  private final int index;

  public POSFieldProcessor(int index) {
    this.index = index;
  }

  @Override
  public void process(Document document, List<List<String>> rows) {
    boolean completed = false;
    for (int i = 0; i < rows.size(); i++) {
      if (rows.get(i).size() > index && !rows.get(i).get(index).equals("_") && !rows.get(i).get(index).equals("-")) {
        String posStr = rows.get(i).get(index);
        if (posStr.contains("|")) {
          posStr = posStr.substring(0, posStr.indexOf('|'));
        }
        completed = true;
        document.tokenAt(i).put(Attrs.PART_OF_SPEECH, POS.fromString(posStr));
      }
    }
    if (completed) {
      document.getAnnotationSet().setIsCompleted(Types.PART_OF_SPEECH, true, "PROVIDED");
    }
  }

  @Override
  public String processOutput(Annotation sentence, Annotation token, int index) {
    POS pos = token.getPOS();
    return pos == null ? "-" : pos.asString();
  }

}//END OF POSFieldProcessor
