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

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Convenience methods for constructing orphaned and empty fragments.
 * </p>
 *
 * @author David B. Bracewell
 */
public final class Fragments {

  private Fragments() {
    throw new IllegalAccessError();
  }


  /**
   * Creates a new HString that does not has no content or document associated with it.
   *
   * @return the new HString
   */
  public static HString emptyOrphan() {
    return ORPHANED_EMPTY;
  }

  /**
   * Creates a new HString that has content, but no document associated with it
   *
   * @param content the content of the string
   * @return the new HString
   */
  public static HString orphan(@Nonnull String content) {
    return new HStringImpl(content);
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
   * Creates a detached empty annotation, i.e. an empty span and no document associated with it.
   *
   * @return the annotation
   */
  public static Annotation detachedEmptyAnnotation() {
    return new Annotation();
  }

  /**
   * Creates a detached annotation, i.e. no document associated with it.
   *
   * @param type  the type of annotation
   * @param start the start of the span
   * @param end   the end of the span
   * @return the annotation
   */
  public static Annotation detatchedAnnotation(AnnotationType type, int start, int end) {
    return new Annotation(type, start, end);
  }

  private static class HStringImpl extends HString {
    private static final long serialVersionUID = 1L;

    private final String content;
    private final Map<Attribute, Val> attributes = new HashMap<>(5);

    private HStringImpl(@Nonnull String content) {
      super(0, content.length());
      this.content = content;
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
    protected Map<Attribute, Val> getAttributeMap() {
      return attributes;
    }

    @Override
    public int start() {
      return 0;
    }

    @Override
    public int end() {
      return content.length();
    }
  }


  private static HString ORPHANED_EMPTY = new HString(0, 0) {
    private static final long serialVersionUID = 1L;

    @Override
    public char charAt(int index) {
      throw new IndexOutOfBoundsException();
    }

    @Override
    public Document document() {
      return null;
    }

    @Override
    protected Map<Attribute, Val> getAttributeMap() {
      return Collections.emptyMap();
    }
  };


}//END OF Fragments
