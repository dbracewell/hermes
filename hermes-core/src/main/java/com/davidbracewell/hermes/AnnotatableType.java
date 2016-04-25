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
import com.davidbracewell.hermes.annotator.Annotator;
import com.davidbracewell.reflection.BeanUtils;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;
import lombok.NonNull;

/**
 * The interface Annotatable.
 *
 * @author David B. Bracewell
 */
public interface AnnotatableType {

  /**
   * Gets the annotator associated with this type for a given language.
   *
   * @param language the language for which the annotator is needed.
   * @return the annotator for this type and the given langauge
   * @throws IllegalStateException If this type is a gold standard annotation.
   */
  default Annotator getAnnotator(@NonNull Language language) {
    String key = Config.closestKey(type(), language, name(), "annotator");
    if (StringUtils.isNullOrBlank(key)) {
      throw new IllegalStateException("No annotator is defined for " + name() + " and " + language);
    }
    Annotator annotator = BeanUtils.parameterizeObject(Config.get(key).as(Annotator.class));
    Preconditions.checkNotNull(annotator, "Could not create the annotator [" + Config.get(key) + "] for " + name());
    Preconditions.checkArgument(annotator.satisfies().contains(this), "Attempting to register " + annotator.getClass().getName() + " for " + name() + " which it does not provide.");
    return annotator;
  }

  /**
   * Gets type name.
   *
   * @return the type name
   */
  String type();

  /**
   * Name string.
   *
   * @return the string
   */
  String name();

  /**
   * As type name string.
   *
   * @return the string
   */
  default String asTypeName() {
    return type() + "." + name();
  }


}//END OF Annotatable
