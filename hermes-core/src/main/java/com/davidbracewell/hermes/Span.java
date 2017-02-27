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

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.guava.common.base.Preconditions;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p> Represents a starting (inclusive) and ending (exclusive) portion of a <code>CharSequence</code>. </p>
 *
 * @author David B. Bracewell
 */
public class Span implements Serializable, Comparable<Span> {
   private static final long serialVersionUID = 1L;
   private final int start;
   private final int end;


   /**
    * Instantiates a new Span.
    *
    * @param start the start
    * @param end   the end
    */
   public Span(int start, int end) {
      Preconditions.checkArgument(end >= start, "Ending offset must be >= Starting offset");
      this.end = end;
      this.start = start;
   }

   /**
    * The starting offset
    *
    * @return The start offset (inclusive).
    */
   public int start() {
      return start;
   }

   /**
    * The ending offset
    *
    * @return The ending offset (exclusive).
    */
   public int end() {
      return end;
   }

   /**
    * The length of the span
    *
    * @return The length of the span
    */
   public int length() {
      return end() - start();
   }

   /**
    * Checks if the span is empty (<code>start == end</code>)
    *
    * @return True if the span is empty, False if not
    */
   public boolean isEmpty() {
      return length() == 0 || start() < 0 || end() < 0;
   }

   /**
    * Returns true if the bounds of other text are connected with the bounds of this text.
    *
    * @param other The other text to check if this one overlaps
    * @return True if the two texts are in the same document and overlap, False otherwise
    */
   public boolean overlaps(Span other) {
      return other != null && this.start() < other.end() && this.end() > other.start();
   }

   /**
    * Returns true if the bounds of the other text do not extend outside the bounds of this text.
    *
    * @param other The other text to check if this one encloses
    * @return True if the two texts are in the same document and this text encloses the other, False otherwise
    */
   public boolean encloses(Span other) {
      return other != null && other.start() >= this.start() && other.end() < this.end();
   }

   @Override
   public String toString() {
      return "(" + start + ", " + end + ")";
   }

   @Override
   public int hashCode() {
      return Objects.hash(start, end);
   }

   @Override
   public boolean equals(Object other) {
      return other != null &&
                other.getClass().equals(Span.class) &&
                Cast.<Span>as(other).start == this.start &&
                Cast.<Span>as(other).end == this.end;
   }

   @Override
   public int compareTo(Span o) {
      if (o == null) {
         return -1;
      }
      if (start < o.start) {
         return -1;
      }
      if (start > o.start) {
         return 1;
      }
      return Integer.compare(end, o.end);
   }

}//END OF Span
