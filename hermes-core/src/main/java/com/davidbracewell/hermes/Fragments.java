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

import com.davidbracewell.conversion.Val;
import com.davidbracewell.guava.common.base.Preconditions;
import lombok.NonNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * <p>
 * Convenience methods for constructing orphaned and empty fragments.
 * </p>
 *
 * @author David B. Bracewell
 */
public final class Fragments {

   private static final HString ORPHANED_EMPTY = new HString(0, 0) {
      private static final long serialVersionUID = 1L;

      @Override
      public Set<AttributeType> attributeTypeSet() {
         return Collections.emptySet();
      }

      @Override
      public char charAt(int index) {
         throw new IndexOutOfBoundsException();
      }

      @Override
      public Document document() {
         return null;
      }

      @Override
      public List<Annotation> get(AnnotationType type, Predicate<? super Annotation> filter) {
         return Collections.emptyList();
      }

      @Override
      protected Map<AttributeType, Val> getAttributeMap() {
         return Collections.emptyMap();
      }
   };


   private Fragments() {
      throw new IllegalAccessError();
   }

   /**
    * Creates a detached annotation, i.e. no document associated with it.
    *
    * @param type  the type of annotation
    * @param start the start of the span
    * @param end   the end of the span
    * @return the annotation
    */
   public static Annotation detachedAnnotation(@NonNull AnnotationType type, int start, int end) {
      return new Annotation(type, start, end);
   }

   /**
    * Creates a detached empty annotation, i.e. an empty span and no document associated with it.
    *
    * @return the annotation
    */
   public static Annotation detachedEmptyAnnotation() {
      return new Annotation();
   }

   /**
    * Creates a new HString that does not has no content or document associated with it.
    *
    * @return the new HString
    */
   public static HString detachedEmptyHString() {
      return ORPHANED_EMPTY;
   }

   /**
    * Creates an empty HString
    *
    * @param document the document
    * @return the new HString (associated with the given document if it is not null)
    */
   public static HString empty(Document document) {
      return new Fragment(document, 0, 0);
   }

   /**
    * Creates an empty annotation associated with the given document.
    *
    * @param document The document the annotation is associated with
    * @return The empty annotation
    */
   public static Annotation emptyAnnotation(@NonNull Document document) {
      return new Annotation(document, AnnotationType.ROOT, -1, -1);
   }

   /**
    * Creates a new HString that has content, but no document associated with it
    *
    * @param content the content of the string
    * @return the new HString
    */
   public static HString string(@NonNull String content) {
      return new HStringImpl(content);
   }

   private static class HStringImpl extends HString {
      private static final long serialVersionUID = 1L;

      private final String content;
      private final Map<AttributeType, Val> attributes = new HashMap<>(5);

      private HStringImpl(@NonNull String content) {
         super(0, content.length());
         this.content = content;
      }

      @Override
      public Set<AttributeType> attributeTypeSet() {
         return attributes.keySet();
      }

      @Override
      public char charAt(int index) {
         return content.charAt(index);
      }

      @Override
      public Document document() {
         return null;
      }

      @Override
      public int end() {
         return content.length();
      }

      @Override
      public HString find(@NonNull String text, int start) {
         Preconditions.checkPositionIndex(start, length());
         int pos = indexOf(text, start);
         if (pos == -1) {
            return Fragments.detachedEmptyHString();
         }
         return new HStringImpl(content.substring(pos, pos + text.length()));
      }

      @Override
      public List<Annotation> get(AnnotationType type, Predicate<? super Annotation> filter) {
         return Collections.emptyList();
      }

      @Override
      protected Map<AttributeType, Val> getAttributeMap() {
         return attributes;
      }

      @Override
      public int start() {
         return 0;
      }

      @Override
      public HString substring(int relativeStart, int relativeEnd) {
         return new HStringImpl(content.substring(relativeStart, relativeEnd));
      }

      @Override
      public String toString() {
         return this.content;
      }

   }


}//END OF Fragments
