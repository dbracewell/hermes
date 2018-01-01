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

package com.davidbracewell.hermes.lexicon;

import com.davidbracewell.hermes.HString;
import lombok.NonNull;

/**
 * The interface L.
 *
 * @author David B. Bracewell
 */
public interface WordList extends Iterable<String> {

   /**
    * Contains boolean.
    *
    * @param string the string
    * @return the boolean
    */
   boolean contains(String string);

   /**
    * Contains boolean.
    *
    * @param string the string
    * @return the boolean
    */
   default boolean contains(@NonNull HString string) {
      return contains(string.toString());
   }

   /**
    * Size int.
    *
    * @return the int
    */
   int size();


   void merge(WordList other);

}//END OF WordList
