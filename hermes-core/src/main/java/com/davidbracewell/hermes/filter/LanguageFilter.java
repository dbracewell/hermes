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

package com.davidbracewell.hermes.filter;

import com.davidbracewell.Language;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.HString;
import lombok.NonNull;
import lombok.Value;

/**
 * <p>Filters HString based on their language. Useful in multi-language collections.</p>
 *
 * @author David B. Bracewell
 */
@Value
public class LanguageFilter implements SerializablePredicate<HString> {
  private static final long serialVersionUID = 1L;
  private final Language language;

  /**
   * Instantiates a new Language filter.
   *
   * @param language the language
   */
  public LanguageFilter(@NonNull Language language) {
    this.language = language;
  }

  @Override
  public boolean test(HString hString) {
    return hString != null && hString.getLanguage() == language;
  }

}//END OF LanguageFilter
