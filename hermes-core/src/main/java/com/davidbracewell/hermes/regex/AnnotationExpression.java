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

package com.davidbracewell.hermes.regex;

import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.parsing.ParserTokenType;
import com.davidbracewell.parsing.expressions.Expression;
import com.davidbracewell.parsing.expressions.MultivalueExpression;

import java.util.Collection;

/**
 * @author David B. Bracewell
 */
public class AnnotationExpression extends MultivalueExpression {

  public final AnnotationType annotationType;

  /**
   * Default Constructor
   *
   * @param value The value
   * @param type  The type of the value
   */
  public AnnotationExpression(Collection<Expression> value, ParserTokenType type, AnnotationType annotationType) {
    super(value, type);
    this.annotationType = annotationType;
  }

  @Override
  public String toString() {
    return "{" + annotationType.name() + " " + super.toString() + "}";
  }
}//END OF AnnotationExpression
