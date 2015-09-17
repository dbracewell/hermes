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
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.filters.StopWords;
import com.davidbracewell.hermes.morphology.Stemmers;
import com.davidbracewell.hermes.tag.POS;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * Represents a java string on steroids. HStrings act as a <code>CharSequence</code> and have methods similar to those
 * on <code>String</code>. Methods that do not modify the underlying string representation, e.g. substring, find, will
 * return another HString whereas methods that modify the string, e.g. lower casing, return a String object.
 * Additionally, HStrings have a span (i.e. start and end positions), attributes, and annotations. HStrings allow for
 * methods to process documents, fragments, and annotations in a uniform fashion. Methods on the HString facilitate
 * determining if the object is an annotation ({@link #isAnnotation()} or is a document ({@link #isDocument()}).
 * </p>
 *
 * @author David B. Bracewell
 */
public abstract class HString extends Span implements CharSequence, AttributedObject, AnnotatedObject {
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new HString.
   *
   * @param start the starting char offset of the string
   * @param end   the ending char offset of the string
   */
  HString(int start, int end) {
    super(start, end);
  }

  /**
   * Creates a new string by performing a union over the spans of two or more HStrings. The new HString will have a
   * span that starts at the minimum starting position of the given strings and end at the maximum ending position of
   * the given strings.
   *
   * @param first  the first HString
   * @param second the second HString
   * @param others the other HStrings to union
   * @return A new HString representing the union over the spans of the given HStrings.
   */
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
    if (start < 0 || start >= end) {
      return Fragments.empty(owner);
    }
    return new Fragment(owner, start, end);
  }

  /**
   * Union h string.
   *
   * @param strings the strings
   * @return the h string
   */
  public static HString union(@Nonnull Iterable<? extends HString> strings) {
    int start = Integer.MAX_VALUE;
    int end = Integer.MIN_VALUE;
    Document owner = null;
    for (HString hString : strings) {
      if (owner == null && hString.document() != null) {
        owner = hString.document();
      } else if (hString.document() == null || owner != hString.document()) {
        throw new IllegalArgumentException("Cannot union strings from different documents");
      }
      start = Math.min(start, hString.start());
      end = Math.max(end, hString.end());
    }
    if (start < 0 || start >= end) {
      return Fragments.empty(owner);
    }
    return new Fragment(owner, start, end);
  }

  @Override
  public Set<Map.Entry<Attribute, Val>> attributeValues() {
    return getAttributeMap().entrySet();
  }

  /**
   * Char n grams.
   *
   * @param order the order
   * @return the list
   */
  public List<HString> charNGrams(int order) {
    return charNGrams(order, c -> true);
  }

  /**
   * Char n grams.
   *
   * @param order  the order
   * @param filter the filter
   * @return the list
   */
  public List<HString> charNGrams(int order, @Nonnull Predicate<Character> filter) {
    List<HString> ngrams = new ArrayList<>();

    if (order <= 0) {
      return ngrams;
    }

    for (int i = 0; i <= length() - order; ) {
      if (filter.test(charAt(i))) {
        int badIndex = -1;

        for (int j = i + 1; j < i + order; j++) {
          if (!filter.test(charAt(j))) {
            badIndex = j;
            break;
          }
        }

        if (badIndex == -1) {
          ngrams.add(substring(i, i + order));
          i++;
        } else {
          i = badIndex + 1;
        }

      } else {
        i++;
      }

    }

    return ngrams;
  }

  @Override
  public boolean contains(Attribute attribute) {
    return getAttributeMap().containsKey(attribute) ||
      getAttributeMap().containsKey(attribute.goldStandardVersion());
  }

  /**
   * Determines if the content of this HString equals the given char sequence.
   *
   * @param content the content to check for equality
   * @return True if equals, False otherwise
   */
  public boolean contentEqual(CharSequence content) {
    return toString().contentEquals(content);
  }

  /**
   * Determines if the content of this HString equals the given char sequence ignoring case.
   *
   * @param content the content to check for equality
   * @return True if equals, False otherwise
   */
  public boolean contentEqualIgnoreCase(String content) {
    return toString().equalsIgnoreCase(content);
  }

  /**
   * Counts the string version of the given annotation types.
   *
   * @param type the annotation type to count
   * @return A counter whose key is the content of the annotations of the given type
   */
  public Counter<String> count(AnnotationType type) {
    return count(type, Object::toString);
  }

  /**
   * Counts the string version of the given annotation types. Strings are constructed using the provided function.
   *
   * @param type      the annotation type to count
   * @param transform the function to transform the annotation into a string
   * @return A counter whose key is the content of the annotations of the given type
   */
  public Counter<String> count(AnnotationType type, @Nonnull Function<? super Annotation, String> transform) {
    return count(type, a -> true, transform);
  }

  /**
   * Counts the string version of the given annotation types that satisfy the given predicate. Strings are constructed
   * using the provided function.
   *
   * @param type      the annotation type to count
   * @param predicate the predicate to test annotations against.
   * @param transform the function to transform the annotation into a string
   * @return A counter whose key is the content of the annotations of the given type
   */
  public Counter<String> count(AnnotationType type, @Nonnull Predicate<? super Annotation> predicate, @Nonnull Function<? super Annotation, String> transform) {
    return Counters.newHashMapCounter(get(type).stream()
        .filter(predicate)
        .map(transform)
        .collect(Collectors.toList())
    );
  }

  /**
   * Counts the lemmatized version of the given annotation types.
   *
   * @param type the annotation type to count
   * @return A counter whose key is the lemmatized content of the annotations of the given type
   */
  public Counter<String> countLemmas(AnnotationType type) {
    return count(type, HString::getLemma);
  }

  /**
   * Checks if this HString encloses the given other.
   *
   * @param other The other HString
   * @return True of this one encloses the given other.
   */
  public final boolean encloses(HString other) {
    if (other == null) {
      return false;
    }
    return (document() != null && other.document() != null) &&
      (document() == other.document())
      && super.encloses(other);
  }

  /**
   * Determines if this HString ends with the given suffix.
   *
   * @param suffix the suffix to check
   * @return True ends with the given suffix, False otherwise
   */
  public boolean endsWith(String suffix) {
    return toString().endsWith(suffix);
  }

  @Override
  public final boolean equals(Object other) {
    return super.equals(other);
  }

  /**
   * Finds the given regular expression in this HString starting from the beginning of this HString
   *
   * @param regex the regular expression to search for
   * @return the HString for the match or empty if no match is found.
   */
  public HString findPattern(@Nonnull String regex) {
    return findPattern(Pattern.compile(regex));
  }

  /**
   * Finds the given regular expression in this HString starting from the beginning of this HString
   *
   * @param regex the regular expression to search for
   * @return the HString for the match or empty if no match is found.
   */
  public HString findPattern(@Nonnull Pattern regex) {
    Matcher m = matcher(regex);
    if (m.find()) {
      return union(substring(m.start(), m.end()).tokens());
    }
    return Fragments.empty(document());
  }

  /**
   * Finds all occurrences of the  given regular expression in this HString starting from the beginning of this HString
   *
   * @param regex the regular expression to search for
   * @return A list of HString that are matches to the given regular expression
   */
  public List<HString> findAllPatterns(@Nonnull String regex) {
    return findAllPatterns(Pattern.compile(regex));
  }

  /**
   * Finds all occurrences of the  given regular expression in this HString starting from the beginning of this HString
   *
   * @param regex the regular expression to search for
   * @return A list of HString that are matches to the given regular expression
   */
  public List<HString> findAllPatterns(@Nonnull Pattern regex) {
    Matcher m = regex.matcher(this);
    List<HString> matches = new ArrayList<>();
    while (m.find()) {
      matches.add(union(substring(m.start(), m.end()).tokens()));
    }
    return matches;
  }

  /**
   * Finds the given text in this HString starting from the beginning of this HString
   *
   * @param text the text to search for
   * @return the HString for the match or empty if no match is found.
   */
  public HString find(String text) {
    return find(text, 0);
  }

  /**
   * Finds all occurrences of the given text in this HString starting
   *
   * @param text the text to search for
   * @return A list of HString that are matches to the given string
   */
  public List<HString> findAll(@Nonnull String text) {
    List<HString> matches = new ArrayList<>();
    for (int pos = indexOf(text, 0); pos < length() && pos != -1; pos = indexOf(text, pos)) {
      matches.add(union(substring(pos, pos + text.length()).tokens()));
    }
    return matches;
  }

  /**
   * Finds the given text in this HString starting from the given start index of this HString
   *
   * @param text  the text to search for
   * @param start the index to start the search from
   * @return the HString for the match or empty if no match is found.
   */
  public HString find(@Nonnull String text, int start) {
    Preconditions.checkPositionIndex(start, length());
    int pos = indexOf(text, start);
    if (pos == -1) {
      return Fragments.empty(document());
    }
    return union(substring(pos, pos + text.length()).tokens());
  }

  @Override
  public Val get(Attribute attribute) {
    if (attribute == null) {
      return Val.NULL;
    }
    if (getAttributeMap().containsKey(attribute)) {
      return getAttributeMap().get(attribute);
    }
    return Val.of(getAttributeMap().get(attribute.goldStandardVersion()));
  }

  @Override
  public List<Annotation> get(AnnotationType type) {
    if (type == null) {
      return Collections.emptyList();
    }
    return get(type, annotation -> annotation.isInstance(type) && annotation.overlaps(this));
  }

  /**
   * Exposes the underlying attributes as a Map
   *
   * @return The attribute names and values as a map
   */
  protected abstract Map<Attribute, Val> getAttributeMap();

  /**
   * Gets the language of the HString. If no language is set for this HString, the language of document will be
   * returned. In the event that this HString is not associated with a document, the default language will be returned.
   *
   * @return The language of the HString
   */
  public Language getLanguage() {
    if (contains(Attrs.LANGUAGE)) {
      return get(Attrs.LANGUAGE).as(Language.class);
    }
    if (document() == null) {
      return Hermes.defaultLanguage();
    }
    return document().getLanguage();
  }

  /**
   * Sets the language of the HString
   *
   * @param language The language of the HString.
   */
  public void setLanguage(Language language) {
    put(Attrs.LANGUAGE, language);
  }

  /**
   * Gets the lemmatized version of the HString. Lemmas of longer phrases are constructed from token
   * lemmas.
   *
   * @return The lemmatized version of the HString.
   */
  public String getLemma() {
    if (isInstance(Types.TOKEN)) {
      if (contains(Attrs.LEMMA)) {
        return get(Attrs.LEMMA).asString();
      }
      return toLowerCase();
    }
    return tokens().stream()
      .map(HString::getLemma)
      .collect(Collectors.joining(
        getLanguage().usesWhitespace() ? " " : ""
      ));
  }

  /**
   * Gets the part-of-speech of the HString
   *
   * @return The best part-of-speech for the HString
   */
  public POS getPOS() {
    return POS.forText(this);
  }

  @Override
  public final List<Annotation> getStartingHere(AnnotationType type) {
    if (type == null) {
      return Collections.emptyList();
    }
    return get(type, annotation -> annotation.start() == start() && annotation.isInstance(type));
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  /**
   * Returns the index within this string of the first occurrence of the specified substring.
   *
   * @param text the substring to search for.
   * @return the index of the first occurrence of the specified substring, or -1 if there is no such occurrence.
   * @see String#indexOf(String)
   */
  public int indexOf(String text) {
    return indexOf(text, 0);
  }

  /**
   * Returns the index within this string of the first occurrence of the specified substring.
   *
   * @param text  the substring to search for.
   * @param start the index to to start searching from
   * @return the index of the first occurrence of the specified substring, or -1 if there is no such occurrence.
   * @see String#indexOf(String, int)
   */
  public int indexOf(String text, int start) {
    return toString().indexOf(text, start);
  }

  /**
   * Is this HString an annotation?
   *
   * @return True if this HString represents an annotation
   */
  public boolean isAnnotation() {
    return false;
  }

  /**
   * Is this HString a document?
   *
   * @return True if this HString represents a document
   */
  public boolean isDocument() {
    return false;
  }

  /**
   * Returns true this HString is an instance of the given annotation type
   *
   * @param type the annotation type
   * @return True if this HString is an annotation of the given type
   */
  public boolean isInstance(AnnotationType type) {
    return false;
  }

  /**
   * Returns a regular expression matcher for the given pattern over this HString
   *
   * @param pattern the pattern to search for
   * @return the matcher
   */
  public Matcher matcher(String pattern) {
    return Pattern.compile(pattern).matcher(this);
  }

  /**
   * Returns a regular expression matcher for the given pattern over this HString
   *
   * @param pattern the pattern to search for
   * @return the matcher
   */
  public Matcher matcher(@Nonnull Pattern pattern) {
    return pattern.matcher(this);
  }

  /**
   * Tells whether or not this string matches the given regular expression.
   *
   * @param regex the regular expression
   * @return true if, and only if, this string matches the given regular expression
   * @see String#matches(String)
   */
  public boolean matches(String regex) {
    return toString().matches(regex);
  }

  /**
   * <p>
   * Extracts ngrams of the given annotation and order (e.g. unigram, bigram, trigram, etc.)
   * </p>
   *
   * @param order          the order, i.e. number of annotations in the ngarm
   * @param annotationType the type of annotation to extract
   * @return the ngrams
   */
  public List<HString> ngrams(int order, @Nonnull AnnotationType annotationType) {
    return ngrams(order, annotationType, true);
  }

  /**
   * <p>
   * Extracts ngrams of the given annotation and order (e.g. unigram, bigram, trigram, etc.)
   * </p>
   *
   * @param order           the order, i.e. number of annotations in the ngarm
   * @param annotationType  the type of annotation to extract
   * @param removeStopWords true ignore stop words using the StopWords class associated with the language of this
   *                        HString
   * @return the ngrams
   */
  public List<HString> ngrams(int order, @Nonnull AnnotationType annotationType, boolean removeStopWords) {
    if (removeStopWords) {
      return ngrams(
        order,
        annotationType,
        StopWords.getInstance(getLanguage())
      );
    }
    return ngrams(
      order,
      annotationType,
      t -> true
    );
  }

  /**
   * <p>
   * Extracts ngrams of the given annotation and order (e.g. unigram, bigram, trigram, etc.) that satisfy the given
   * filter.
   * </p>
   *
   * @param order          the order, i.e. number of annotations in the ngarm
   * @param annotationType the type of annotation to extract
   * @param filter         the filter to use to accept ngrams
   * @return the ngrams
   */
  public List<HString> ngrams(int order, @Nonnull AnnotationType annotationType, @Nonnull Predicate<? super HString> filter) {
    if (order <= 0) {
      return Collections.emptyList();
    } else if (order == 1) {
      return get(annotationType)
        .stream()
        .filter(filter)
        .collect(Collectors.toList());
    }


    List<HString> ngrams = new ArrayList<>();
    List<HString> annotations = Cast.cast(get(annotationType));
    for (int i = 0; i <= annotations.size() - order; ) {
      if (filter.test(annotations.get(i))) {
        int badIndex = -1;

        for (int j = i + 1; j < i + order; j++) {
          if (!filter.test(annotations.get(j))) {
            badIndex = j;
            break;
          }
        }

        if (badIndex == -1) {
          ngrams.add(HString.union(annotations.subList(i, i + order)));
          i++;
        } else {
          i = badIndex + 1;
        }

      } else {
        i++;
      }
    }

    return ngrams;
  }

  /**
   * Checks if this HString overlaps with the given other.
   *
   * @param other The other HString
   * @return True of this one overlaps with the given other.
   */
  public final boolean overlaps(HString other) {
    if (other == null) {
      return false;
    }
    return (document() != null && other.document() != null) &&
      (document() == other.document()) &&
      super.overlaps(other);
  }

  @Override
  public Val put(Attribute attribute, Object value) {
    if (attribute != null) {
      Val val = Val.of(value);
      if (val.isNull()) {
        return remove(attribute);
      }
      return getAttributeMap().put(attribute, val);
    }
    return Val.NULL;
  }

  @Override
  public Val remove(Attribute attribute) {
    return getAttributeMap().remove(attribute);
  }

  /**
   * Converts this string to a new character array.
   *
   * @return a newly allocated character array whose length is the length of this string and whose contents are
   * initialized to contain the character sequence represented by this string.
   */
  public char[] toCharArray() {
    return toString().toCharArray();
  }

  /**
   * Replaces all substrings of this string that matches the given string with the given replacement.
   *
   * @param oldString the old string
   * @param newString the new string
   * @return the string
   * @see String#replace(CharSequence, CharSequence)
   */
  public String replace(String oldString, String newString) {
    return toString().replace(oldString, newString);
  }

  /**
   * Replaces all substrings of this string that matches the given regular expression with the given replacement.
   *
   * @param regex       the regular expression
   * @param replacement the string to be substituted
   * @return the resulting string
   * @see String#replaceAll(String, String)
   */
  public String replaceAll(String regex, String replacement) {
    return toString().replaceAll(regex, replacement);
  }

  /**
   * Replaces the first substring of this string that matches the given regular expression with the given replacement.
   *
   * @param regex       the regular expression
   * @param replacement the string to be substituted
   * @return the resulting string
   * @see String#replaceFirst(String, String)
   */
  public String replaceFirst(String regex, String replacement) {
    return toString().replaceFirst(regex, replacement);
  }

  /**
   * Tests if this string starts with the specified prefix.
   *
   * @param prefix the prefix
   * @return true if the HString starts with the specified prefix
   */
  public boolean startsWith(String prefix) {
    return toString().startsWith(prefix);
  }

  /**
   * Returns true if and only if this string contains the specified sequence of char values.
   *
   * @param string the sequence to search for
   * @return true if this string contains s, false otherwise
   */
  public boolean contains(@Nonnull String string) {
    return toString().contains(string);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return toString().subSequence(start, end);
  }

  /**
   * Returns a new HString that is a substring of this one. The substring begins at the specified relativeStart and
   * extends to the character at index relativeEnd - 1. Thus the length of the substring is relativeEnd-relativeStart.
   *
   * @param relativeStart the relative start within in this HString
   * @param relativeEnd   the relative end within this HString
   * @return the specified substring.
   * @throws IndexOutOfBoundsException - if the relativeStart is negative, or relativeEnd is larger than the length of
   *                                   this HString object, or relativeStart is larger than relativeEnd.
   */
  public HString substring(int relativeStart, int relativeEnd) {
    Preconditions.checkPositionIndexes(relativeStart, relativeEnd, length());
    return new Fragment(document(), start() + relativeStart, start() + relativeEnd);
  }

  /**
   * To lower case.
   *
   * @return the string
   * @see String#toLowerCase(Locale) NOTE: Uses locale associated with the HString's langauge
   */
  public String toLowerCase() {
    return toString().toLowerCase(getLanguage().asLocale());
  }

  /**
   * Converts the HString to a string with part-of-speech information attached using <code>_</code> as the delimiter
   *
   * @return the HString with part-of-speech information attached to tokens
   */
  public String toPOSString() {
    return toPOSString('_');
  }

  /**
   * Converts the HString to a string with part-of-speech information attached using the given delimiter
   *
   * @param delimiter the delimiter to use to separate word and part-of-speech
   * @return the HString with part-of-speech information attached to tokens
   */
  public String toPOSString(char delimiter) {
    return tokens().stream()
      .map(t -> t.toString() + delimiter + t.get(Attrs.PART_OF_SPEECH).as(POS.class, POS.ANY).asString())
      .collect(Collectors.joining(" "));
  }

  @Override
  public String toString() {
    if (document() == null) {
      return StringUtils.EMPTY;
    }
    return document().toString().substring(start(), end());
  }

  /**
   * Converts the HString to upper case
   *
   * @return the upper case version of the HString
   * @see String#toUpperCase(Locale) NOTE: Uses locale associated with the HString's langauge
   */
  public String toUpperCase() {
    return toString().toUpperCase(getLanguage().asLocale());
  }

  /**
   * <p>
   * Extracts token level NGrams
   * </p>
   *
   * @param order the order of the ngram
   * @return the ngrams
   */
  public List<HString> tokenNGrams(int order) {
    return ngrams(order, Types.TOKEN, false);
  }

  /**
   * <p>
   * Extracts token level NGrams
   * </p>
   *
   * @param order           the order of the ngram
   * @param removeStopWords True remove stop words using the stop words associated with the language of the HString.
   * @return the ngrams
   */
  public List<HString> tokenNGrams(int order, boolean removeStopWords) {
    return ngrams(order, Types.TOKEN, removeStopWords);
  }

  /**
   * <p>
   * Extracts token level NGrams that satisfy the given filter
   * </p>
   *
   * @param order  the order of the ngram
   * @param filter the filter to use to accept tokens
   * @return the ngrams
   */
  public List<HString> tokenNGrams(int order, @Nonnull Predicate<? super HString> filter) {
    return ngrams(order, Types.TOKEN, filter);
  }

  /**
   * Creates a new string by performing a union over the spans of this HString and at least one more HString. The new
   * HString will have a span that starts at the minimum starting position of the given strings and end at the maximum
   * ending position of the given strings.
   *
   * @param other    the HString to union with
   * @param evenMore the other HStrings to union
   * @return A new HString representing the union over the spans of the given HStrings.
   */
  public HString union(@Nonnull HString other, HString... evenMore) {
    return HString.union(this, other, evenMore);
  }

  /**
   * Gets the stemmed version of the HString. Stems of token are determined using the <code>Stemmer</code> associated
   * with the language that the token is in. Tokens store their stem using the <code>STEM</code> attribute, so that the
   * stem only needs to be calculated once.Stems of longer phrases are constructed from token stems.
   *
   * @return The stemmed version of the HString.
   */
  public String getStem() {
    if (isInstance(Types.TOKEN)) {
      putIfAbsent(Attrs.STEM, Stemmers.getStemmer(getLanguage()).stem(this));
      return get(Attrs.STEM).asString();
    }
    return tokens().stream()
      .map(HString::getStem)
      .collect(Collectors.joining(
        getLanguage().usesWhitespace() ? " " : ""
      ));
  }


}//END OF HString
