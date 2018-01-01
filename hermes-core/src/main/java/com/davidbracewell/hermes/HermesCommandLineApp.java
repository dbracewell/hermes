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

package com.davidbracewell.hermes;

import com.davidbracewell.application.CommandLineApplication;
import com.davidbracewell.cli.Option;
import com.davidbracewell.collection.Sets;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * <p>Base class to create command line applications utilizing the Hermes framework. Has preset command line parameters
 * for:
 * <pre>
 *    --distributed (-d) : Determines if distributed (Apache Spark) mode is used. Defaults to false
 *    --input (--i) : The input location of the corpus to process (Required)
 *    --inputFormat (--if) : The format of the input corpus. Defaults to one json per line.
 *    --output (--o) : The output location to write the corpus to.
 *    --outputFormat (--if) : The format of the output corpus. Defaults to one json per line.
 * </pre>
 *
 * </p>
 *
 * @author David B. Bracewell
 */
public abstract class HermesCommandLineApp extends CommandLineApplication {
   private static final long serialVersionUID = 1L;

   /**
    * Determines if distributed (Apache Spark) mode is used. Defaults to false
    */
   @Option(description = "True if the corpus will work in distributed mode.", defaultValue = "false", aliases = {"d"})
   boolean distributed;

   /**
    * The input location of the corpus to process
    */
   @Option(description = "The location of the corpus to process.", required = true, aliases = {"i", "corpus"})
   Resource input;

   /**
    * The format of the input corpus. Defaults to one json per line.
    */
   @Option(description = "The format of the input corpus.", defaultValue = "JSON_OPL", aliases = {"if"})
   String inputFormat;

   /**
    * The output location to write the corpus to.
    */
   @Option(description = "The location to save the output of the processing.", aliases = {"o"})
   Resource output;

   /**
    * The format of the output corpus. Defaults to one json per line.
    */
   @Option(description = "The format of the output corpus.", defaultValue = "JSON_OPL", aliases = {"of"})
   String outputFormat;


   private final Set<String> requiredPackages = Sets.set(Hermes.HERMES_PACKAGE);


   /**
    * Creates a corpus based on the command line parameters.
    *
    * @return the corpus
    */
   public Corpus getCorpus() {
      return Corpus.builder()
                   .distributed(distributed)
                   .source(inputFormat, input)
                   .build();
   }


   /**
    * Writes the given corpus based on the output location and format.
    *
    * @param corpus the corpus to write
    * @throws IOException Something went wrong writing the corpus
    */
   public void writeCorpus(@NonNull Corpus corpus) throws IOException {
      corpus.write(outputFormat, output);
   }

   /**
    * Gets the location of the input corpus.
    *
    * @return the input location of the corpus
    */
   public Resource getInputLocation() {
      return input;
   }

   /**
    * Gets the input corpus format.
    *
    * @return the input corpus format
    */
   public String getInputFormat() {
      return inputFormat;
   }

   /**
    * Is distributed boolean.
    *
    * @return the boolean
    */
   public boolean isDistributed() {
      return distributed;
   }


   /**
    * Instantiates a new Hermes command line app.
    */
   protected HermesCommandLineApp() {
      super();
   }

   /**
    * Instantiates a new Hermes command line app.
    *
    * @param applicationName the application name
    */
   protected HermesCommandLineApp(String applicationName) {
      super(applicationName);
   }

   /**
    * Instantiates a new Hermes command line app.
    *
    * @param applicationName  the application name
    * @param requiredPackages the extra packages
    */
   protected HermesCommandLineApp(String applicationName, String... requiredPackages) {
      super(applicationName);
      if (requiredPackages != null) {
         this.requiredPackages.addAll(Arrays.asList(requiredPackages));
      }
   }

   /**
    * Gets output location.
    *
    * @return the output location
    */
   public Resource getOutputLocation() {
      return output;
   }

   @Override
   public final void setup() throws Exception {
      Hermes.initializeApplication(getName(), getAllArguments());
      requiredPackages.forEach(Config::loadPackageConfig);
      Config.setAllCommandLine(getAllArguments());
   }

}//END OF HermesCommandLineApp
