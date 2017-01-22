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

import com.davidbracewell.cli.Option;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.HermesCommandLineApp;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusFormats;
import com.davidbracewell.io.resource.Resource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Entry point to sequentially processing a corpus via one ore more {@link ProcessingModule}s. The list of processors
 * can be defined either via the <code>--processors</code> command line argument (which expects a comma separated list
 * of processor class names) or via the <code>--desc</code> argument, which specifies the processing description file to
 * load.</p>
 *
 * <p>Description files are in Mango's <code>Config</code> format. Individual {@link ProcessingModule}s are implemented
 * as beans, which can have their options set via configuration using Mango's capability to parameterize objects.</p>
 *
 * @author David B. Bracewell
 */
public final class CorpusProcessor extends HermesCommandLineApp implements Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * Name of the context parameter for the location of the input corpus
    */
   public static final String INPUT_LOCATION = "INPUT_LOCATION";
   /**
    * Name of the context parameter for the location to write the resulting corpus to.
    */
   public static final String OUTPUT_LOCATION = "OUTPUT_LOCATION";

   @Option(description = "List of corpus processors to run")
   private List<ProcessingModule> processors = new ArrayList<>();

   @Option(name = "desc", description = "Description file defining processors")
   private Resource processorDescription = null;


   /**
    * Instantiates a new Controller.
    */
   private CorpusProcessor() {
      super("CorpusProcessor", "com.davidbracewell.hermes");
   }


   /**
    * Instantiates a new Corpus processor.
    *
    * @param processors the processors
    */
   @Builder
   public CorpusProcessor(@Singular @NonNull List<ProcessingModule> processors) {
      super("CorpusProcessor", "com.davidbracewell.hermes");
      this.processors = processors;
   }

   /**
    * The entry point of application.
    *
    * @param args the input arguments
    * @throws Exception the exception
    */
   public static void main(String[] args) throws Exception {
      new CorpusProcessor().run(args);
   }


   /**
    * Process corpus.
    *
    * @param input   the input
    * @param context the context
    * @return the corpus
    * @throws Exception the exception
    */
   public Corpus process(@NonNull Corpus input, @NonNull ProcessorContext context) throws Exception {
      Corpus corpus = input;
      for (ProcessingModule processor : processors) {
         corpus = processor.process(corpus, context);
      }
      return corpus;
   }

   @Override
   protected void programLogic() throws Exception {
      ProcessorContext context = ProcessorContext.builder()
                                                 .property(INPUT_LOCATION, getInputLocation())
                                                 .property(OUTPUT_LOCATION, getOutputLocation())
                                                 .build();

      if (processors == null) {
         if (processorDescription == null) {
            throw new IllegalStateException("No processors were defined.");
         }
         Config.loadConfig(processorDescription);
         Config.setAllCommandLine(getAllArguments());
         processors = Config.get("processors").asList(ProcessingModule.class);
      }

      Corpus corpus = process(getCorpus(), context);
      if (getOutputLocation() != null) {
         corpus.write(CorpusFormats.JSON_OPL, getOutputLocation());
      }
      corpus.close();
      corpus.getStreamingContext().close();
   }


}//END OF Controller
