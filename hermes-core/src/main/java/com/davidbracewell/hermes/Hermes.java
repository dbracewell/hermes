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

import com.davidbracewell.Language;
import com.davidbracewell.SystemInfo;
import com.davidbracewell.config.Config;
import com.davidbracewell.config.Configurator;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>Convenience methods for getting common configuration options. </p>
 *
 * @author David B. Bracewell
 */
public final class Hermes {

   /**
    * The Hermes package
    */
   public static final String HERMES_PACKAGE = "com.davidbracewell.hermes";

   private Hermes() {
      throw new IllegalAccessError();
   }


   /**
    * Get the default language. The default language is specified using <code>hermes.DefaultLanguage</code>. If the
    * configuration option is not set, it will default to the language matching the system locale.
    *
    * @return the default language
    */
   public static Language defaultLanguage() {
      return Config.get("hermes.DefaultLanguage").as(Language.class, Language.fromLocale(Locale.getDefault()));
   }

   /**
    * Initialize application string [ ].
    *
    * @param programName the program name
    * @param args        the args
    * @param packages    the packages
    * @return the string [ ]
    */
   public static String[] initializeApplication(String programName, String[] args, String... packages) {
      String[] leftOver = Config.initialize(programName, args);
      //Ensure that the core hermes config is loaded
      Config.loadPackageConfig(HERMES_PACKAGE);
      if (packages != null) {
         for (String aPackage : packages) {
            if (!HERMES_PACKAGE.equals(aPackage)) {
               Config.loadPackageConfig(aPackage);
            }
         }
      }
      return leftOver;
   }

   /**
    * Initialize application string [ ].
    *
    * @param args the args
    * @return the string [ ]
    */
   public static String[] initializeApplication(String[] args) {
      StackTraceElement[] elements = Thread.currentThread().getStackTrace();
      String programName = "";
      String packageName = null;
      for (int i = 0; i < elements.length && StringUtils.isNullOrBlank(programName); i++) {
         String className = elements[i].getClassName();
         if (!className.equals(Hermes.class.getName()) && !className.startsWith("java")) {
            int idx = className.lastIndexOf('.');
            programName = className.substring(idx + 1);
            packageName = className.substring(0, idx);
         }
      }
      return initializeApplication(programName, args, packageName);
   }

   /**
    * Initialize worker.
    *
    * @param config the config
    */
   public static void initializeWorker(Config config) {
      Configurator.INSTANCE.configure(config);
   }


   /**
    * Load model t.
    *
    * @param <T>            the type parameter
    * @param lock           the lock
    * @param language       the language
    * @param configProperty the config property
    * @param modelName      the model name
    * @param modelGetter    the model getter
    * @param modelSetter    the model setter
    * @return the t
    */
   public static <T> T loadModel(@NonNull Object lock, @NonNull Language language, @NonNull String configProperty, @NonNull String modelName, @NonNull Supplier<T> modelGetter, @NonNull Consumer<T> modelSetter) {
      if (modelGetter.get() == null) {
         synchronized (lock) {
            String langCode = language.getCode().toLowerCase();
            Resource modelDir = Config.get("models.dir").asResource(Resources.from(SystemInfo.USER_HOME));
            Resource classpathDir = Resources.fromClasspath("hermes/");
            if (modelGetter.get() == null) {
               Exception thrownException = null;

               for (Resource r : new Resource[]{
                  Config.get(configProperty, language, "model").asResource(),
                  classpathDir.getChild(langCode).getChild("model").getChild(modelName),
                  modelDir.getChild(langCode).getChild(modelName),
                  Config.get(configProperty, "model").asResource(),
                  classpathDir.getChild("model").getChild(modelName),
                  modelDir.getChild(modelName)
               }) {
                  if (r != null && r.exists()) {
                     try {
                        T model = r.readObject();
                        modelSetter.accept(model);
                        return model;
                     } catch (Exception e) {
                        thrownException = e;
                     }
                  }
               }

               if (thrownException == null) {
                  throw new RuntimeException(modelName + " does not exist");
               } else {
                  throw Throwables.propagate(thrownException);
               }

            }
         }
      }

      return modelGetter.get();
   }


}//END OF Hermes
