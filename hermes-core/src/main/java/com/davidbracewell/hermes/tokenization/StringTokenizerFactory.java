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

package com.davidbracewell.hermes.tokenization;

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.logging.Logger;
import com.davidbracewell.reflection.Reflect;
import com.davidbracewell.reflection.ReflectionException;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import java.io.Reader;
import java.io.StringReader;

/**
 * The type String tokenizer factory.
 *
 * @author David B. Bracewell
 */
public class StringTokenizerFactory {

  private static volatile Logger log = Logger.getLogger(StringTokenizerFactory.class);

  /**
   * Create tokenizer.
   *
   * @param language the language
   * @param reader   the reader
   * @return the string tokenizer
   */
  public static StringTokenizer createTokenizer(Language language, Reader reader) {
    Preconditions.checkNotNull(language, "The Language cannot be null");

    Val val = Config.get(StringTokenizer.class.getName(), language);
    if (val.isNull() || val.asClass() == null) {
      log.warn("Could not find a definition for {0}.{1} returning StandardTokenizer", StringTokenizer.class.getName(), language);
      return new StandardTokenizer(reader);
    }

    reader = (reader == null ? new StringReader("") : reader);
    try {
      return Reflect.onClass(val.asClass()).create(reader).get();
    } catch (ReflectionException e) {
      log.severe("Error creating '{0}' : {1}", val.asString(), e);
      throw Throwables.propagate(e);
    }

  }

  /**
   * Create tokenizer.
   *
   * @param language the language
   * @param text     the text
   * @return the string tokenizer
   */
  public static StringTokenizer createTokenizer(Language language, HString text) {
    return createTokenizer(language, new StringReader(text.toString()));
  }


}//END OF StringTokenizerFactory
