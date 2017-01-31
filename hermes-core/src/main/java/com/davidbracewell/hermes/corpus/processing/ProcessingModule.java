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

import com.davidbracewell.config.Config;
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


   /**
    * Gets the override status for this processing module, which can be defined using configuration in the form
    * <code>fully.qualified.class.name.override=true</code> or all processing can be reperformed using
    * <code>processing.override.all=true</code>. By default, the status is false, which means try to load the previous
    * state.
    *
    * @return True force reprocessing, False try to load the previous state.
    */
   default boolean getOverrideStatus() {
      return Config.get("processing.override.all")
                   .asBooleanValue(Config.get(this.getClass(), "override")
                                         .asBooleanValue(false));
   }

   /**
    * Loads from a previous processing state.
    *
    * @param corpus  the corpus being processed
    * @param context the context of the processor
    * @return the processing state (NOT_LOADED by default meaning there is no previous state).
    */
   default ProcessingState loadPreviousState(Corpus corpus, ProcessorContext context) {
      return ProcessingState.NOT_LOADED();
   }


}//END OF CorpusProcessor
