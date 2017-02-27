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

package com.davidbracewell.hermes.extraction;

import com.davidbracewell.Copyable;
import lombok.NonNull;

/**
 * The type Term spec.
 *
 * @author David B. Bracewell
 */
public class TermExtractor extends AbstractTermExtractor<TermExtractor> implements Copyable<TermExtractor> {
   private static final long serialVersionUID = 1L;

   /**
    * Instantiates a new Term spec.
    */
   public TermExtractor() {

   }

   /**
    * Instantiates a new Term spec.
    *
    * @param copy the copy
    */
   public TermExtractor(@NonNull TermExtractor copy) {
      super(copy);
   }

   /**
    * Create term spec.
    *
    * @return the term spec
    */
   public static TermExtractor create() {
      return new TermExtractor();
   }


   @Override
   public TermExtractor copy() {
      return new TermExtractor(this);
   }
}//END OF TermSpec
