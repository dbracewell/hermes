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

package com.davidbracewell.hermes.annotator;

import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Annotator;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.string.StringUtils;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author David B. Bracewell
 */
public class RegexAnnotator implements Annotator, Serializable {
  private static final long serialVersionUID = 1L;
  private final Pattern regex;
  private final AnnotationType providedType;

  public RegexAnnotator(@Nonnull String regex, AnnotationType providedType) {
    regex = StringUtils.trim(regex);
    if (!regex.startsWith("\\b")) {
      regex = "\\b" + regex;
    }
    if (!regex.endsWith("\\b")) {
      regex += "\\b";
    }
    this.regex = Pattern.compile(regex);
    this.providedType = providedType;
  }

  @Override
  public void annotate(Document document) {
    Matcher matcher = document.matcher(regex);
    AnnotationType type = providedType != null ? providedType : AnnotationType.create("UNKNOWN_REGEX");
    while (matcher.find()) {
      document.createAnnotation(type, matcher.start(), matcher.end());
    }
  }

  @Override
  public Set<AnnotationType> provides() {
    return providedType == null ? Collections.emptySet() : Collections.singleton(providedType);
  }

  @Override
  public Set<AnnotationType> requires() {
    return Collections.emptySet();
  }

}//END OF RegexAnnotator
