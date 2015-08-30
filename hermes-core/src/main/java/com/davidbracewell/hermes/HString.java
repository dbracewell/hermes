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

import com.davidbracewell.Language;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * The interface H string.
 *
 * @author David B. Bracewell
 */
public interface HString extends AttributedObject, Annotated {

  /**
   * Content equal.
   *
   * @param content the content
   * @return the boolean
   */
  default boolean contentEqual(CharSequence content) {
    return toString().contentEquals(content);
  }

  /**
   * Content equal ignore case.
   *
   * @param content the content
   * @return the boolean
   */
  default boolean contentEqualIgnoreCase(String content) {
    return toString().equalsIgnoreCase(content);
  }

  /**
   * Ends with.
   *
   * @param suffix the suffix
   * @return the boolean
   */
  default boolean endsWith(String suffix) {
    return toString().endsWith(suffix);
  }

  /**
   * Find h string.
   *
   * @param text the text
   * @return the h string
   */
  default HString find(String text) {
    return find(text, 0);
  }

  /**
   * Find h string.
   *
   * @param text  the text
   * @param start the start
   * @return the h string
   */
  default HString find(@Nonnull String text, int start) {
    Preconditions.checkPositionIndex(start, length());
    int pos = indexOf(text, start);
    if (pos == -1) {
      return Fragments.empty(this);
    }
    return new Fragment(document(), start() + pos, start() + text.length());
  }

  /**
   * Index of.
   *
   * @param text the text
   * @return the int
   */
  default int indexOf(String text) {
    return indexOf(text, 0);
  }

  /**
   * Index of.
   *
   * @param text  the text
   * @param start the start
   * @return the int
   */
  default int indexOf(String text, int start) {
    Preconditions.checkPositionIndex(start, length());
    return text == null ? -1 : toString().indexOf(text, start);
  }

  /**
   * Is this fragment an annotation?
   *
   * @return True if this fragment represents an annotation
   */
  default boolean isAnnotation() {
    return false;
  }

  /**
   * Is this fragment a document?
   *
   * @return True if this fragment represents a document
   */
  default boolean isDocument() {
    return false;
  }

  /**
   * Returns true this fragment is an instance of the given annotation type
   *
   * @param type the annotation type
   * @return True if this fragment is an annotation of the given type
   */
  default boolean isInstance(AnnotationType type) {
    return false;
  }

  /**
   * Matches boolean.
   *
   * @param regex the regex
   * @return the boolean
   */
  default boolean matches(String regex) {
    if (regex != null) {
      return toString().matches(regex);
    }
    return false;
  }

  /**
   * Starts with.
   *
   * @param prefix the prefix
   * @return the boolean
   */
  default boolean startsWith(String prefix) {
    return toString().startsWith(prefix);
  }

  @Override
  default CharSequence subSequence(int start, int end) {
    return toString().subSequence(start, end);
  }

  /**
   * Sub string.
   *
   * @param relativeStart the start
   * @param relativeEnd   the end
   * @return the h string
   */
  default HString subString(int relativeStart, int relativeEnd) {
    Preconditions.checkPositionIndexes(relativeStart, relativeEnd, length());
    return new Fragment(document(), start() + relativeStart, start() + relativeEnd);
  }

  /**
   * Gets the language of the fragment.
   *
   * @return The language of the fragment
   */
  default Language getLanguage() {
    if (containsAttribute(Attrs.LANGUAGE)) {
      return getAttribute(Attrs.LANGUAGE);
    }
    if (document() == null) {
      return Language.UNKNOWN;
    }
    return document().getLanguage();
  }

  /**
   * Sets the language of the fragment
   *
   * @param language The language of the fragment.
   */
  default void setLanguage(Language language) {
    putAttribute(Attrs.LANGUAGE, language);
  }

  default List<Annotation> tokens() {
    return getOverlapping(Types.TOKEN);
  }

  default int tokenLength() {
    return tokens().size();
  }

  default Annotation tokenAt(int tokenIndex) {
    return tokens().get(tokenIndex);
  }


  default List<Annotation> sentences() {
    return getOverlapping(Types.SENTENCE);
  }


}//END OF HString
