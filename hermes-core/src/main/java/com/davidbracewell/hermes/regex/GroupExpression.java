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

import com.davidbracewell.parsing.ParserToken;
import com.davidbracewell.parsing.expressions.Expression;
import com.davidbracewell.parsing.expressions.MultivalueExpression;
import lombok.NonNull;

import java.util.Collection;

/**
 * The type Group expression.
 *
 * @author David B. Bracewell
 */
public class GroupExpression extends MultivalueExpression {
  /**
   * The Group name.
   */
  public final String groupName;


  /**
   * Default Constructor
   *
   * @param value     The value
   * @param type      The type of the value
   * @param groupName the group name
   */
  public GroupExpression(Collection<Expression> value, @NonNull ParserToken token) {
    super(value, token.type);
    this.groupName = token.getText().substring(2);
  }

}//END OF GroupExpression
