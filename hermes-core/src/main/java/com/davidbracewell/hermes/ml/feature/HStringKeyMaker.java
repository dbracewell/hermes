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

package com.davidbracewell.hermes.ml.feature;

import com.davidbracewell.cache.KeyMaker;
import com.davidbracewell.hermes.HString;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author David B. Bracewell
 */
public class HStringKeyMaker implements KeyMaker, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public Object make(Class<?> clazz, Method method, Object[] args) {
    Object[] array = new Object[args.length + 2];
    array[0] = clazz;
    array[1] = method;
    for (int i = 0, j = 2; i < args.length; i++, j++) {
      array[j] = HString.class.isInstance(args[i]) ? args[i].toString() : args[i];
    }
    return Arrays.hashCode(array);
  }

}//END OF HStringKeyMaker
