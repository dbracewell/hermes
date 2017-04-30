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
import com.davidbracewell.io.resource.StringResource;
import com.davidbracewell.json.JsonWriter;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

import java.io.IOException;
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
    * Exports the currently loaded Hermes type system in JSON.
    *
    * @return Json representing the type system.
    */
   public static String exportTypeSystem() {
      Resource out = new StringResource();
      try {
         exportTypeSystem(out);
         return out.readToString().trim();
      } catch (IOException e) {
         throw Throwables.propagate(e);
      }
   }

   /**
    * Exports the currently loaded Hermes type system in JSON.
    *
    * @param output The resource to output the json representation of the type system.
    */
   public static void exportTypeSystem(@NonNull Resource output) throws IOException {
      try (JsonWriter writer = new JsonWriter(output)) {
         writer.beginDocument(true);
         for (AnnotationType atv : AnnotationType.values()) {
            writer.beginObject();
            writer.property("name", atv.name());
            writer.property("type", atv.type());
            writer.property("parent", atv.getParent().name());
            writer.property("tagType", atv.getTagAttribute().name());
            writer.endObject();
         }
         for (AttributeType atv : AttributeType.values()) {
            writer.beginObject();
            writer.property("name", atv.name());
            writer.property("type", atv.type());
            writer.property("valueType", atv.getValueType());
            writer.endObject();
         }
         for (RelationType rtv : RelationType.values()) {
            writer.beginObject();
            writer.property("name", rtv.name());
            writer.property("type", rtv.type());
            writer.endObject();
         }
         writer.endDocument();
      }

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
    * Initializes an application using Hermes with a given program name, command line arguments, and config packages to
    * load. Note: that the hermes package is always loaded.
    *
    * @param programName the program name
    * @param args        the command line arguments
    * @param packages    extra config packages to load
    * @return the non-named command line arguments
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
    * Initializes an application using Hermes with the given command line arguments. Determines the program name from
    * the calling class and also any config package associated with the calling class. Note: that the hermes package is
    * always loaded.
    *
    * @param args the command line arguments
    * @return the non-named command line arguments
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
    * <p>Common method for loading a model using java deserialization. The method uses the double-checked locking where
    * it synchronizes on the given <code>lock</code> object. The <code>modelGetter</code> is used to check if the model
    * is already loaded and the <code>modelSetter</code> is used to set the model. </p>
    *
    * <p>The method will look in the following locations in order for the mode:
    * <ol>
    * <li>configProperty.language.model</li>
    * <li>classpath:hermes/language/model/modelName</li>
    * <li>modelDir/language/model/modelName</li>
    * <li>configProperty.model</li>
    * <li>classpath:hermes/model/modelName</li>
    * <li>modelDir/model/modelName</li>
    * </ol>
    * where <code>language</code> is the two-letter (lowercased) language code.
    * </p>
    *
    * @param <T>            the type of model being loaded
    * @param lock           the lock to synchronize on
    * @param language       the language of the model
    * @param configProperty the config property to use for locating the model location
    * @param modelName      the name of the model to load
    * @param modelGetter    supplier to use to check if the model is already loaded
    * @param modelSetter    consumer to use to set the model object
    * @return the loaded model
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
