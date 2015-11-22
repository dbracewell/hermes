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
import com.davidbracewell.hermes.*;

import java.util.List;

/**
 * @author David B. Bracewell
 */
public class IOBFieldProcessor implements FieldProcessor {

  private final int index;
  private final AnnotationType annotationType;
  private final Attribute attribute;

  public IOBFieldProcessor(int index, AnnotationType annotationType, Attribute attribute) {
    this.index = index;
    this.annotationType = annotationType;
    this.attribute = attribute;
  }


  @Override
  public void process(Document document, List<List<String>> rows) {
    int start = -1;
    String tag = null;

    for (int i = 0; i < rows.size(); i++) {
      String upper = rows.get(i).get(index).toUpperCase();
      if (start > -1 && (upper.startsWith("O") || upper.startsWith("B"))) {
        addAnnotation(document, start, i, tag);
        start = -1;
        tag = null;
      }

      if (upper.startsWith("B")) {
        start = i;
        tag = rows.get(i).get(index).substring(2);
      }
    }

    if (start != -1) {
      addAnnotation(document, start, document.tokenLength(), tag);
    }

  }

  private void addAnnotation(Document document, int start, int end, String tag) {
    document.createAnnotation(
      annotationType,
      document.tokenAt(start).start(),
      document.tokenAt(end - 1).end(),
      Collect.map(attribute, attribute.getValueType().convert(tag))
    );
  }

  public static IOBFieldProcessor chunkProcessor(int index) {
    return new IOBFieldProcessor(index, Types.PHRASE_CHUNK, Attrs.PART_OF_SPEECH);
  }

  public static IOBFieldProcessor nameEntityProcessor(int index) {
    return new IOBFieldProcessor(index, Types.ENTITY, Attrs.ENTITY_TYPE);
  }

}//END OF IOBFieldProcessor
