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

package com.davidbracewell.hermes.lyre;

import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.regex.TokenMatcher;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public final class LyreProgram implements Serializable {
  private static final long serialVersionUID = 1L;
  private final List<LyreRule> rules = new LinkedList<>();

  public LyreProgram() {

  }

  public LyreProgram(Collection<LyreRule> rules) {
    this.rules.addAll(rules);
  }

  public void execute(@NonNull HString input) {
    final Document document = input.document();
    rules.forEach(rule -> {
      TokenMatcher matcher = rule.getRegex().matcher(input);
      while (matcher.find()) {
        Annotation annotation = document.createAnnotation(rule.getAnnotationType(), matcher.start(), matcher.end());
        if (Double.isFinite(rule.getConfidence()) && rule.getConfidence() > 0) {
          annotation.put(Attrs.CONFIDENCE, rule.getConfidence());
        }
        annotation.putAll(rule.getAttributes());
      }
    });
  }


}//END OF LyreProgram
