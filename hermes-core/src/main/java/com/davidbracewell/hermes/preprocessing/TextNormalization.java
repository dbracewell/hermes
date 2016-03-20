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

package com.davidbracewell.hermes.preprocessing;

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.logging.Logger;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * <p>Class takes care of normalizing text using a number of {@link com.davidbracewell.hermes.preprocessing.TextNormalizer}s.
 * </p>
 *
 * @author David B. Bracewell
 */
public class TextNormalization implements Serializable {
  private static final long serialVersionUID = 1L;

  private static TextNormalization INSTANCE;

  private static final Logger log = Logger.getLogger(TextNormalization.class);
  private static final String LIST_CONFIG = "hermes.preprocessing.normalizers";
  private final List<TextNormalizer> preprocessors;

  /**
   * @return A TextNormalization class configured via config files.
   */
  public static TextNormalization configuredInstance() {
    if (INSTANCE == null) {
      synchronized (TextNormalization.class) {
        if (INSTANCE == null) {
          INSTANCE = new TextNormalization();
          INSTANCE.initConfig();
        }
      }
    }
    return INSTANCE;
  }

  /**
   * @return A TextNormalization that does no normalization
   */
  public static TextNormalization noOpInstance() {
    TextNormalization factory = new TextNormalization();
    factory.preprocessors.clear();
    return factory;
  }

  public static TextNormalization createInstance(Collection<? extends TextNormalizer> normalizers) {
    return new TextNormalization(Preconditions.checkNotNull(normalizers));
  }

  private TextNormalization() {
    preprocessors = Lists.newArrayList();
  }

  private TextNormalization(Collection<? extends TextNormalizer> normalizers) {
    preprocessors = Lists.newArrayList();
    preprocessors.addAll(normalizers);
  }


  private void initConfig() {
    if (Config.hasProperty(LIST_CONFIG)) {
      for (TextNormalizer normalizer : Config.get(LIST_CONFIG).asList(TextNormalizer.class)) {
        Preconditions.checkNotNull(normalizer, "Error in Config: " + Config.get(LIST_CONFIG).asString() + " : " + Config.get(LIST_CONFIG).asList(TextNormalizer.class));
        preprocessors.add(normalizer);
        if (log.isLoggable(Level.FINE)) {
          log.fine("Adding normalizer: {0}", normalizer.getClass());
        }
      }
    }
  }

  /**
   * Normalizes a string with a number of text normalizers.
   *
   * @param input    The input string
   * @param language The language of the input string
   * @return A normalized version of the string
   */
  public String normalize(String input, Language language) {
    if (input == null) {
      return null;
    }
    String finalString = input;
    for (TextNormalizer textNormalizer : preprocessors) {
      finalString = textNormalizer.apply(finalString, language);
    }
    return finalString;
  }


}//END OF TextNormalizer

