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

package com.davidbracewell.hermes.processor;

import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.Corpus;
import lombok.NonNull;

import java.util.Arrays;

/**
 * The type Annotate processor.
 *
 * @author David B. Bracewell
 */
public class AnnotateProcessor implements CorpusProcessor {
   private static final long serialVersionUID = 1L;
   private AnnotatableType[] types = {Types.SENTENCE, Types.LEMMA, Types.PHRASE_CHUNK, Types.DEPENDENCY, Types.ENTITY};

   @Override
   public Corpus process(Corpus corpus, ProcessorContext context) throws Exception {
      logInfo("Annotating corpus for {0}", Arrays.toString(types));
      return corpus.annotate(types);
   }


   /**
    * Get types string [ ].
    *
    * @return the string [ ]
    */
   public String[] getTypes() {
      return null;
   }

   /**
    * Sets types.
    *
    * @param types the types
    */
   public void setTypes(@NonNull String[] types) {
      this.types = Arrays.stream(types)
                         .map(Types::from)
                         .toArray(AnnotatableType[]::new);
   }

}//END OF AnnotateProcessor
