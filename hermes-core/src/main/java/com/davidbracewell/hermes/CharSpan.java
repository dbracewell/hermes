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

/**
 * @author David B. Bracewell
 */
public interface CharSpan extends CharSequence {

  /**
   * @return The start offset of the <code>CharSpan</code> (inclusive).
   */
  int start();

  /**
   * @return The end offset of the <code>CharSpan</code> (non-inclusive).
   */
  int end();

  @Override
  default int length() {
    return end() - start();
  }

  /**
   * Checks if the span is empty (<code>start == end</code>)
   *
   * @return True if the span is empty, False if not
   */
  default boolean isEmpty() {
    return length() == 0;
  }

  /**
   * Returns true if the bounds of other text are connected with the bounds of this text.
   *
   * @param other The other text to check if this one overlaps
   * @return True if the two texts are in the same document and overlap, False otherwise
   */
  default boolean overlaps(CharSpan other) {
    return other != null && this.start() < other.end() && this.end() > other.start();
  }

  /**
   * Returns true if the bounds of the other text do not extend outside the bounds of this text.
   *
   * @param other The other text to check if this one encloses
   * @return True if the two texts are in the same document and this text encloses the other, False otherwise
   */
  default boolean encloses(CharSpan other) {
    return other != null && other.start() >= this.start() && other.end() < this.end();
  }

}//END OF CharSpan
