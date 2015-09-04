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

import java.util.Set;

/**
 * <p>Annotators produce annotations for a given document. A single annotator may produce one ore more types of
 * annotations. Additionally, multiple annotators may provide the same type of annotations.
 * <p/>
 *
 * @author David B. Bracewell
 */
public interface Annotator {


  /**
   * Annotates a document with one or more annotations of the type defined in <code>provided()</code>.
   *
   * @param document The document to annotate
   */
  void annotate(Document document);


  /**
   * The set of annotation types that are provided by this annotator
   *
   * @return the set of provided annotation types
   */
  Set<AnnotationType> provides();

  /**
   * The annotation types required to be on a document before this annotator can annotate
   *
   * @return the set of required annotation types
   */
  Set<AnnotationType> requires();


}//END OF Annotator
