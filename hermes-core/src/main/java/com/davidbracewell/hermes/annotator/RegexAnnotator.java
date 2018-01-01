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

import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Creates annotations of a given type based on a given regular expression.
 * </p>
 *
 * @author David B. Bracewell
 */
public class RegexAnnotator implements Annotator, Serializable {
   private static final long serialVersionUID = 1L;
   private final Pattern regex;
   private final AnnotationType providedType;

   /**
    * Instantiates a new Regex annotator.
    *
    * @param regex        the regex
    * @param providedType the provided type
    */
   public RegexAnnotator(@NonNull String regex, @NonNull AnnotationType providedType) {
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

   /**
    * Instantiates a new Regex annotator.
    *
    * @param regex        the regex
    * @param providedType the provided type
    */
   public RegexAnnotator(@NonNull String regex, @NonNull String providedType) {
      this(regex, AnnotationType.create(providedType));
   }

   @Override
   public void annotate(Document document) {
      Matcher matcher = document.matcher(regex);
      while (matcher.find()) {
         document.createAnnotation(providedType, matcher.start(), matcher.end(), Collections.emptyMap());
      }
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(providedType);
   }

   @Override
   public String getVersion() {
      return "(" + regex.pattern() + ")";
   }

}//END OF RegexAnnotator
