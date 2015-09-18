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

/**
 * The type Text normalizer.
 * @author David B. Bracewell
 */
public abstract class TextNormalizer {


  /**
   * Performs a pre-processing operation on the input string in the given input language
   *
   * @param input The input text
   * @param inputLanguage The language of the input
   * @return The post-processed text
   */
  public final String apply(String input, Language inputLanguage) {
    if (input != null && shouldApply(inputLanguage)) {
      return performNormalization(input, inputLanguage);
    }
    return input;
  }

  /**
   * Performs a pre-processing operation on the input string in the given input language
   *
   * @param input The input text
   * @param language The language of the input
   * @return The post-processed text
   */
  protected abstract String performNormalization(String input, Language language);


  /**
   * Should apply.
   *
   * @param language the language
   * @return the boolean
   */
  protected final boolean shouldApply(Language language) {
    return Config.get(this.getClass(), language, "apply").asBoolean(true);
  }


}//END OF TextPreprocessor