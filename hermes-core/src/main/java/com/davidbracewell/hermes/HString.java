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
import com.davidbracewell.collection.Counter;
import com.davidbracewell.collection.Counters;
import com.davidbracewell.conversion.Val;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The interface H string.
 *
 * @author David B. Bracewell
 */
public abstract class HString extends Span implements CharSequence, AttributedObject, Annotated {
  private static final long serialVersionUID = 1L;

  public HString(int start, int end) {
    super(start, end);
  }


  /**
   * Content equal.
   *
   * @param content the content
   * @return the boolean
   */
  public boolean contentEqual(CharSequence content) {
    return toString().contentEquals(content);
  }

  /**
   * Content equal ignore case.
   *
   * @param content the content
   * @return the boolean
   */
  public boolean contentEqualIgnoreCase(String content) {
    return toString().equalsIgnoreCase(content);
  }

  /**
   * Ends with.
   *
   * @param suffix the suffix
   * @return the boolean
   */
  public boolean endsWith(String suffix) {
    return toString().endsWith(suffix);
  }

  /**
   * Find h string.
   *
   * @param text the text
   * @return the h string
   */
  public HString find(String text) {
    return find(text, 0);
  }

  /**
   * Find h string.
   *
   * @param text  the text
   * @param start the start
   * @return the h string
   */
  public HString find(@Nonnull String text, int start) {
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
  public int indexOf(String text) {
    return indexOf(text, 0);
  }

  /**
   * Index of.
   *
   * @param text  the text
   * @param start the start
   * @return the int
   */
  public int indexOf(String text, int start) {
    Preconditions.checkPositionIndex(start, length());
    return text == null ? -1 : toString().indexOf(text, start);
  }

  /**
   * Is this fragment an annotation?
   *
   * @return True if this fragment represents an annotation
   */
  public boolean isAnnotation() {
    return false;
  }

  /**
   * Is this fragment a document?
   *
   * @return True if this fragment represents a document
   */
  public boolean isDocument() {
    return false;
  }

  /**
   * Returns true this fragment is an instance of the given annotation type
   *
   * @param type the annotation type
   * @return True if this fragment is an annotation of the given type
   */
  public boolean isInstance(AnnotationType type) {
    return false;
  }

  /**
   * Matches boolean.
   *
   * @param regex the regex
   * @return the boolean
   */
  public boolean matches(String regex) {
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
  public boolean startsWith(String prefix) {
    return toString().startsWith(prefix);
  }

  public Matcher matcher(String pattern) {
    return Pattern.compile(pattern).matcher(this);
  }

  public Matcher matcher(@Nonnull Pattern pattern) {
    return pattern.matcher(this);
  }


  @Override
  public CharSequence subSequence(int start, int end) {
    return toString().subSequence(start, end);
  }

  /**
   * Sub string.
   *
   * @param relativeStart the start
   * @param relativeEnd   the end
   * @return the h string
   */
  public HString substring(int relativeStart, int relativeEnd) {
    Preconditions.checkPositionIndexes(relativeStart, relativeEnd, length());
    return new Fragment(document(), start() + relativeStart, start() + relativeEnd);
  }

  /**
   * Gets the language of the fragment.
   *
   * @return The language of the fragment
   */
  public Language getLanguage() {
    if (hasAttribute(Attrs.LANGUAGE)) {
      return getAttribute(Attrs.LANGUAGE).as(Language.class);
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
  public void setLanguage(Language language) {
    putAttribute(Attrs.LANGUAGE, language);
  }


  @Override
  public List<Annotation> getStartingHere(AnnotationType type) {
    if (document() != null && type != null) {
      return document().getStartingAt(type, start());
    }
    return Collections.emptyList();
  }

  @Override
  public List<Annotation> getOverlapping(AnnotationType type) {
    if (document() != null && type != null) {
      return document().getOverlapping(type, this);
    }
    return Collections.emptyList();
  }

  @Override
  public List<Annotation> getDuring(AnnotationType type) {
    if (document() != null && type != null) {
      return document().getDuring(type, this);
    }
    return Collections.emptyList();
  }

  @Override
  public List<Annotation> getContaining(AnnotationType type) {
    if (document() != null && type != null) {
      return document().getContaining(type, this);
    }
    return Collections.emptyList();
  }


  /**
   * Exposes the underlying attributes as a Map
   *
   * @return The attribute names and values as a map
   */
  protected abstract Map<Attribute, Val> getAttributeMap();

  @Override
  public Set<Map.Entry<Attribute, Val>> getAttributes() {
    return getAttributeMap().entrySet();
  }

  @Override
  public boolean hasAttribute(Attribute attribute) {
    return getAttributeMap().containsKey(attribute);
  }

  @Override
  public Val getAttribute(Attribute attribute) {
    if (attribute == null) {
      return Val.NULL;
    }
    if (getAttributeMap().containsKey(attribute)) {
      return getAttributeMap().get(attribute);
    }
    return getAttributeMap().get(attribute.goldStandardVersion());
  }

  @Override
  public Val putAttribute(Attribute attribute, Object value) {
    if (attribute != null) {
      Val val = Val.of(value);
      if (val.isNull()) {
        return removeAttribute(attribute);
      }
      return getAttributeMap().put(attribute, val);
    }
    return Val.NULL;
  }

  @Override
  public Val removeAttribute(Attribute attribute) {
    return getAttributeMap().remove(attribute);
  }


  public String getLemma() {
    if (hasAttribute(Attrs.LEMMA)) {
      return getAttribute(Attrs.LEMMA).asString();
    }
    return "";
  }

  public Counter<String> countLemmas(AnnotationType type) {
    return count(type, HString::getLemma);
  }

  public Counter<String> count(AnnotationType type) {
    return count(type, Object::toString);
  }

  public Counter<String> count(AnnotationType type, @Nonnull Function<HString, String> transform) {
    return count(type, a -> true, transform);
  }

  public Counter<String> count(AnnotationType type, @Nonnull Predicate<? super Annotation> predicate, @Nonnull Function<HString, String> transform) {
    return Counters.newHashMapCounter(getOverlapping(type).stream()
            .filter(predicate)
            .map(transform)
            .collect(Collectors.toList())
    );
  }


  public String toLowerCase() {
    return toString().toLowerCase();
  }

  public String toUpperCase() {
    return toString().toUpperCase();
  }


  public static HString union(@Nonnull HString first, @Nonnull HString second, HString... others) {
    Preconditions.checkArgument(first.document() == second.document(), "Cannot union strings from different documents");
    Document owner = first.document();
    int start = Math.min(first.start(), second.start());
    int end = Math.max(first.end(), second.end());
    if (others != null) {
      for (HString hString : others) {
        Preconditions.checkArgument(owner == hString.document(), "Cannot union strings from different documents");
        start = Math.min(start, hString.start());
        end = Math.max(end, hString.end());
      }
    }
    return new Fragment(owner, start, end);
  }

  public HString union(@Nonnull HString other, HString... evenMore) {
    return HString.union(this, other, evenMore);
  }

}//END OF HString
