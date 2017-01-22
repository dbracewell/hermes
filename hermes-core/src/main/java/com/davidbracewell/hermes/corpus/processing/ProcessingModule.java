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

package com.davidbracewell.hermes.corpus.processing;

import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.logging.Loggable;

import java.io.Serializable;

/**
 * The interface Corpus processor.
 *
 * @author David B. Bracewell
 */
public interface ProcessingModule extends Serializable, Loggable {

   /**
    * Process corpus.
    *
    * @param corpus  the corpus
    * @param context the context
    * @return the corpus
    * @throws Exception the exception
    */
   Corpus process(Corpus corpus, ProcessorContext context) throws Exception;

}//END OF CorpusProcessor
