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

import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.apollo.ml.embedding.SparkWord2Vec;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.io.resource.Resource;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author David B. Bracewell
 */
public class WordEmbedding implements CorpusProcessor, Serializable {
   private static final long serialVersionUID = 1L;
   public final static String PROPERTY_PREFIX = "TokenEmbedding";
   @Getter
   @Setter
   private int dimension = 300;
   @Getter
   @Setter
   private int minCount = 5;
   @Getter
   @Setter
   private Resource output = null;
   @Getter
   @Setter
   private AnnotationType[] annotations = {Types.TOKEN};


   @Override
   public Corpus process(Corpus corpus, ProcessorContext context) throws Exception {
      SparkWord2Vec word2Vec = new SparkWord2Vec();
      word2Vec.setDimension(dimension);
      word2Vec.setMinCount(minCount);

      logInfo("Word2Vec dimension={0}, minCount={1}, annotations={2}",
              word2Vec.getDimension(),
              word2Vec.getMinCount(),
              Arrays.toString(annotations));

      Embedding embedding;
      if (annotations.length > 1) {
         embedding = word2Vec.train(
            corpus.asEmbeddingDataset(annotations[0], Arrays.copyOfRange(annotations, 1, annotations.length)));
      } else {
         embedding = word2Vec.train(corpus.asEmbeddingDataset(annotations[0]));
      }
      context.property(PROPERTY_PREFIX, embedding);

      if (output != null) {
         output.getParent().mkdirs();
         embedding.write(output);
      }

      return corpus;
   }

}//END OF WordEmbedding
