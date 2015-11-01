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
import com.davidbracewell.config.Config;
import com.davidbracewell.string.StringUtils;

import java.util.Locale;

/**
 * <p>Convenience methods for getting common configuration options. </p>
 *
 * @author David B. Bracewell
 */
public interface Hermes {

  /**
   * Get the default language. The default language is specified using <code>hermes.DefaultLanguage</code>. If the
   * configuration option is not set, it will default to the language matching the system locale.
   *
   * @return the default language
   */
  static Language defaultLanguage() {
    return Config.get("hermes.DefaultLanguage").as(Language.class, Language.fromLocale(Locale.getDefault()));
  }

  /**
   * Initialize application string [ ].
   *
   * @param programName the program name
   * @param args        the args
   * @return the string [ ]
   */
  static String[] initializeApplication(String programName, String[] args) {
    String[] leftOver = Config.initialize(programName, args);
    //Ensure that the core hermes config is loaded
    Config.loadPackageConfig("com.davidbracewell.hermes");
    return leftOver;
  }

  /**
   * Initialize application string [ ].
   *
   * @param args the args
   * @return the string [ ]
   */
  static String[] initializeApplication(String[] args) {
    StackTraceElement[] elements = Thread.currentThread().getStackTrace();
    String programName = "";
    for (int i = 0; i < elements.length && StringUtils.isNullOrBlank(programName); i++) {
      String className = elements[i].getClassName();
      if (!className.equals(Hermes.class.getName()) && !className.contains("java")) {
        int idx = className.lastIndexOf('.');
        programName = className.substring(idx + 1);
      }
    }
    return initializeApplication(programName, args);
  }

  static void initializeWorker(Config config) {
    Configurator.INSTANCE.configure(config);
  }


}//END OF Hermes
