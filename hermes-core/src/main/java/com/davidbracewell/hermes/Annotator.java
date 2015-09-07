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

import com.davidbracewell.config.Config;

import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * An annotator processes documents adding annotations of one or more types. An annotator may require specific types of
 * annotations to be present on the document before it can process the document (by default it requires none).
 * Annotators define a version ({@link #getVersion()}) that identifies the model, version, lexicon, etc. used by the
 * annotator to produce its annotations.
 * </p>
 * <p><b>Note</b>: Annotator implementations should be implemented in a thread safe manner.</p>
 *
 * @author David B. Bracewell
 */
public interface Annotator {


  /**
   * Annotates a document with one or more annotations of the types defined in <code>provided()</code>.
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
  default Set<AnnotationType> requires() {
    return Collections.emptySet();
  }


  /**
   * Gets the version of this annotator. The version may relate to a version number, model used, or something else to
   * identify the settings of the annotator.
   *
   * @return the version
   */
  default String getVersion() {
    return Config.get(this.getClass(), "version").asString("1.0");
  }


}//END OF Annotator
