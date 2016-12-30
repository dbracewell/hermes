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
import com.davidbracewell.Tag;
import com.davidbracewell.apollo.ml.LabeledDatum;
import com.davidbracewell.apollo.ml.sequence.SequenceInput;
import com.davidbracewell.collection.Streams;
import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.collection.counter.Counters;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.guava.common.base.Preconditions;
import com.davidbracewell.hermes.attribute.POS;
import com.davidbracewell.hermes.corpus.TermSpec;
import com.davidbracewell.hermes.morphology.Stemmers;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple;
import lombok.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.davidbracewell.tuple.Tuples.$;

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
public abstract class HString extends Span implements CharSequence, AttributedObject, AnnotatedObject, RelationalObject {
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


   public String taggedText(@NonNull AnnotationType type) {
      StringBuilder builder = new StringBuilder();
      interleaved(type, Types.TOKEN).forEach(annotation -> {
         if (annotation.getType().equals(type)) {
            builder.append("<")
                   .append(annotation.getTag().map(Tag::name).orElse("?"))
                   .append(">")
                   .append(annotation)
                   .append("</")
                   .append(annotation.getTag().map(Tag::name).orElse("?"))
                   .append(">")
                   .append(" ");
         } else {
            builder.append(annotation).append(" ");
         }
      });
      return builder.toString();
   }


   /**
    * Trim left h string.
    *
    * @param toTrimPredicate the to trim predicate
    * @return the h string
    */
   public HString trimLeft(@NonNull Predicate<? super Annotation> toTrimPredicate) {
      int start = 0;
      while (start < tokenLength() && toTrimPredicate.test(tokenAt(start))) {
         start++;
      }
      if (start < tokenLength()) {
         return tokenAt(start).union(tokenAt(tokenLength() - 1));
      }
      return Fragments.empty(document());
   }

   /**
    * Trim right h string.
    *
    * @param toTrimPredicate the to trim predicate
    * @return the h string
    */
   public HString trimRight(@NonNull Predicate<? super Annotation> toTrimPredicate) {
      int end = tokenLength() - 1;
      while (end >= 0 && toTrimPredicate.test(tokenAt(end))) {
         end--;
      }
      if (end > 0) {
         return tokenAt(0).union(tokenAt(end));
      } else if (end == 0) {
         return tokenAt(0);
      }
      return Fragments.empty(document());
   }

   /**
    * Trim h string.
    *
    * @param toTrimPredicate the to trim predicate
    * @return the h string
    */
   public HString trim(@NonNull Predicate<? super Annotation> toTrimPredicate) {
      return trimRight(toTrimPredicate).trimLeft(toTrimPredicate);
   }


   public Counter<String> terms(@NonNull TermSpec termSpec) {
      return termSpec.getValueCalculator()
                     .adjust(Counters.newCounter(stream(termSpec.getAnnotationType())
                                                    .filter(termSpec.getFilter())
                                                    .map(termSpec.getToStringFunction())
                                                    .filter(StringUtils::isNotNullOrBlank)
                                                    .collect(Collectors.toList()))
                            );
   }

   /**
    * Split list.
    *
    * @param delimiterPredicate the delimiter predicate
    * @return the list
    */
   public List<HString> split(@NonNull Predicate<? super Annotation> delimiterPredicate) {
      List<HString> result = new ArrayList<>();
      int start = -1;
      for (int i = 0; i < tokenLength(); i++) {
         if (delimiterPredicate.test(tokenAt(i))) {
            if (start != -1) {
               result.add(tokenAt(start).union(tokenAt(i - 1)));
            }
            start = -1;
         } else if (start == -1) {
            start = i;
         }
      }
      if (start != -1) {
         result.add(tokenAt(start).union(tokenAt(tokenLength() - 1)));
      }
      return result;
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
   public static HString union(@NonNull HString first, @NonNull HString second, HString... others) {
      Preconditions.checkArgument(first.document() == second.document(),
                                  "Cannot union strings from different documents");
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
   public static HString union(@NonNull Iterable<? extends HString> strings) {
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
   public Set<Map.Entry<AttributeType, Val>> attributeEntrySet() {
      return getAttributeMap().entrySet();
   }

   /**
    * Char n grams.
    *
    * @param order the order
    * @return the list
    */
   public List<HString> charNGrams(int order) {
      return charNGrams(order, order);
   }

   /**
    * Char n grams.
    *
    * @param minOrder the min order
    * @param maxOrder the max order
    * @return the list
    */
   public List<HString> charNGrams(int minOrder, int maxOrder) {
      Preconditions.checkArgument(minOrder <= maxOrder,
                                  "minimum ngram order must be less than or equal to the maximum ngram order");
      Preconditions.checkArgument(minOrder > 0, "minimum ngram order must be greater than 0.");
      List<HString> ngrams = new ArrayList<>();
      for (int i = 0; i < length(); i++) {
         for (int j = i + minOrder; j <= length() && j <= i + maxOrder; j++) {
            ngrams.add(substring(i, j));
         }
      }
      return ngrams;
   }

   @Override
   public boolean contains(AttributeType attributeType) {
      return attributeType != null && getAttributeMap().containsKey(attributeType);
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
   public Counter<String> count(AnnotationType type, @NonNull Function<? super Annotation, String> transform) {
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
   public Counter<String> count(AnnotationType type, @NonNull Predicate<? super Annotation> predicate, @NonNull Function<? super Annotation, String> transform) {
      return Counters.newCounter(get(type).stream()
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
      return this == other;
   }

   /**
    * Finds the given regular expression in this HString starting from the beginning of this HString
    *
    * @param regex the regular expression to search for
    * @return the HString for the match or empty if no match is found.
    */
   public HString findPattern(@NonNull String regex) {
      return findPattern(Pattern.compile(regex));
   }

   /**
    * Finds the given regular expression in this HString starting from the beginning of this HString
    *
    * @param regex the regular expression to search for
    * @return the HString for the match or empty if no match is found.
    */
   public HString findPattern(@NonNull Pattern regex) {
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
   public List<HString> findAllPatterns(@NonNull String regex) {
      return findAllPatterns(Pattern.compile(regex));
   }

   /**
    * Finds all occurrences of the  given regular expression in this HString starting from the beginning of this HString
    *
    * @param regex the regular expression to search for
    * @return A list of HString that are matches to the given regular expression
    */
   public List<HString> findAllPatterns(@NonNull Pattern regex) {
      Matcher m = regex.matcher(this);
      List<HString> matches = new ArrayList<>();
      while (m.find()) {
         if (document() != null && document().getAnnotationSet().isCompleted(Types.TOKEN)) {
            matches.add(union(substring(m.start(), m.end()).tokens()));
         } else {
            matches.add(substring(m.start(), m.end()));
         }
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
   public List<HString> findAll(@NonNull String text) {
      List<HString> matches = new ArrayList<>();
      for (int pos = indexOf(text, 0); pos < length() && pos != -1; pos = indexOf(text, pos + 1)) {
         if (document() != null && document().getAnnotationSet().isCompleted(Types.TOKEN)) {
            matches.add(union(substring(pos, pos + text.length()).tokens()));
         } else {
            matches.add(substring(pos, pos + text.length()));
         }
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
   public HString find(@NonNull String text, int start) {
      Preconditions.checkPositionIndex(start, length());
      int pos = indexOf(text, start);
      if (pos == -1) {
         return Fragments.empty(document());
      }
      if (document() != null && document().getAnnotationSet().isCompleted(Types.TOKEN)) {
         return union(substring(pos, pos + text.length()).tokens());
      }
      return substring(pos, pos + text.length());
   }

   @Override
   public Val get(AttributeType attributeType) {
      if (attributeType == null) {
         return Val.NULL;
      }
      return getAttributeMap().getOrDefault(attributeType, Val.NULL);
   }

   @Override
   public List<Annotation> get(AnnotationType type) {
      if (type == null) {
         return Collections.emptyList();
      }
      return get(type, annotation -> annotation.isInstance(type) && annotation.overlaps(this));
   }

   @Override
   public List<Annotation> getAllAnnotations() {
      return document().get(AnnotationType.ROOT, this);
   }

   /**
    * Exposes the underlying attributes as a Map
    *
    * @return The attribute names and values as a map
    */
   protected abstract Map<AttributeType, Val> getAttributeMap();

   /**
    * Gets the language of the HString. If no language is set for this HString, the language of document will be
    * returned. In the event that this HString is not associated with a document, the default language will be returned.
    *
    * @return The language of the HString
    */
   public Language getLanguage() {
      if (contains(Types.LANGUAGE)) {
         return get(Types.LANGUAGE).as(Language.class);
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
      put(Types.LANGUAGE, language);
   }

   /**
    * Gets the lemmatized version of the HString. Lemmas of longer phrases are constructed from token
    * lemmas.
    *
    * @return The lemmatized version of the HString.
    */
   public String getLemma() {
      if (isInstance(Types.TOKEN)) {
         if (contains(Types.LEMMA)) {
            return get(Types.LEMMA).asString();
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
    * @see String#indexOf(String) String#indexOf(String)String#indexOf(String)
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
    * @see String#indexOf(String, int) String#indexOf(String, int)String#indexOf(String, int)
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
    * As annotation optional.
    *
    * @return the optional
    */
   public Optional<Annotation> asAnnotation() {
      if (this instanceof Annotation) {
         return Optional.of(Cast.as(this));
      }
      return Optional.empty();
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
   public Matcher matcher(@NonNull Pattern pattern) {
      return pattern.matcher(this);
   }

   /**
    * Tells whether or not this string matches the given regular expression.
    *
    * @param regex the regular expression
    * @return true if, and only if, this string matches the given regular expression
    * @see String#matches(String) String#matches(String)String#matches(String)
    */
   public boolean matches(String regex) {
      return toString().matches(regex);
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
      return ngrams(Types.TOKEN, order);
   }

   /**
    * Ngrams list.
    *
    * @param minOrder the min order
    * @param maxOrder the max order
    * @return the list
    */
   public List<HString> tokenNGrams(int minOrder, int maxOrder) {
      return ngrams(Types.TOKEN, minOrder, maxOrder);
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
   public List<HString> ngrams(@NonNull AnnotationType annotationType, int order) {
      return ngrams(annotationType, order, order);
   }

   /**
    * Ngrams list.
    *
    * @param annotationType the annotation type
    * @param minOrder       the min order
    * @param maxOrder       the max order
    * @return the list
    */
   public List<HString> ngrams(@NonNull AnnotationType annotationType, int minOrder, int maxOrder) {
      Preconditions.checkArgument(minOrder <= maxOrder,
                                  "minimum ngram order must be less than or equal to the maximum ngram order");
      Preconditions.checkArgument(minOrder > 0, "minimum ngram order must be greater than 0.");
      List<HString> ngrams = new ArrayList<>();
      List<Annotation> annotations = get(annotationType);
      for (int i = 0; i < annotations.size(); i++) {
         for (int j = i + minOrder - 1; j < annotations.size() && j < i + maxOrder; j++) {
            ngrams.add(annotations.get(i).union(annotations.get(j)));
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
   public Val put(AttributeType attributeType, Object value) {
      if (attributeType != null) {
         Val val = Val.of(value);
         if (val.isNull()) {
            return remove(attributeType);
         }
         return getAttributeMap().put(attributeType, val);
      }
      return Val.NULL;
   }

   @Override
   public Val remove(AttributeType attributeType) {
      return getAttributeMap().remove(attributeType);
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
    * @see String#replace(CharSequence, CharSequence) String#replace(CharSequence, CharSequence)String#replace(CharSequence,
    * CharSequence)
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
    * @see String#replaceAll(String, String) String#replaceAll(String, String)String#replaceAll(String, String)
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
    * @see String#replaceFirst(String, String) String#replaceFirst(String, String)String#replaceFirst(String, String)
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
   public boolean contains(@NonNull String string) {
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
    * @see String#toLowerCase(Locale) String#toLowerCase(Locale)String#toLowerCase(Locale)NOTE: Uses locale associated
    * with the HString's langauge
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
                     .map(t -> t.toString() + delimiter + t.get(Types.PART_OF_SPEECH).as(POS.class, POS.ANY).asString())
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
    * @see String#toUpperCase(Locale) String#toUpperCase(Locale)String#toUpperCase(Locale)NOTE: Uses locale associated
    * with the HString's langauge
    */
   public String toUpperCase() {
      return toString().toUpperCase(getLanguage().asLocale());
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
   public HString union(@NonNull HString other, HString... evenMore) {
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
         putIfAbsent(Types.STEM, Stemmers.getStemmer(getLanguage()).stem(this));
         return get(Types.STEM).asString();
      }
      return tokens().stream()
                     .map(HString::getStem)
                     .collect(Collectors.joining(
                        getLanguage().usesWhitespace() ? " " : ""
                                                ));
   }


   /**
    * <p>
    * Returns the annotations of the given types that overlap this string in a maximum match fashion. Each token in the
    * string is examined and the annotation type with the longest span on that token is chosen. If more than one type
    * has the span length, the first one found will be chose, i.e. the order in which the types are passed in to the
    * method can effect the outcome.
    * </p>
    * <p>
    * Examples where this is useful is when dealing with multiword expressions. Using the interleaved method you can
    * retrieve all tokens and mutliword expressions to fully match the span of the string.
    * </p>
    *
    * @param type1  The first type (Must declare at leas one)
    * @param others The other types to examine
    * @return The list of interleaved annotations
    */
   public List<Annotation> interleaved(@NonNull AnnotationType type1, AnnotationType... others) {
      if (others == null || others.length == 0) {
         return get(type1);
      }

      List<Annotation> annotations = new ArrayList<>();
      for (int i = 0; i < tokenLength(); ) {
         Annotation annotation = Fragments.detachedEmptyAnnotation();
         for (Annotation temp : tokenAt(i).get(type1)) {
            if (temp.tokenLength() > annotation.tokenLength()) {
               annotation = temp;
            }
         }
         for (AnnotationType other : others) {
            for (Annotation temp : tokenAt(i).get(other)) {
               if (temp.tokenLength() > annotation.tokenLength()) {
                  annotation = temp;
               }
            }
         }

         if (annotation.isEmpty()) {
            i++;
         } else {
            i += annotation.tokenLength();
            annotations.add(annotation);
         }
      }
      return annotations;
   }

   /**
    * Gets head.
    *
    * @return the head
    */
   public HString getHead() {
      return tokens().stream()
                     .filter(t -> !t.parent().isPresent())
                     .map(Cast::<HString>as)
                     .findFirst()
                     .orElseGet(() -> tokens().stream()
                                              .filter(t -> !this.overlaps(t.parent().get()))
                                              .map(Cast::<HString>as)
                                              .findFirst()
                                              .orElse(this));
   }

   /**
    * As labeled data labeled datum.
    *
    * @param labelFunction the label function
    * @return the labeled datum
    */
   public LabeledDatum<HString> asLabeledData(@NonNull Function<HString, ? extends Object> labelFunction) {
      return LabeledDatum.of(labelFunction.apply(this), this);
   }

   /**
    * As labeled data labeled datum.
    *
    * @param attributeTypeLabel the attribute type label
    * @return the labeled datum
    */
   public LabeledDatum<HString> asLabeledData(@NonNull AttributeType attributeTypeLabel) {
      return LabeledDatum.of(get(attributeTypeLabel), this);
   }

   /**
    * As sequence sequence input.
    *
    * @return the sequence input
    */
   public SequenceInput<Annotation> asSequence() {
      return new SequenceInput<>(Cast.cast(tokens()));
   }

   /**
    * As sequence sequence input.
    *
    * @param labelFunction the label function
    * @return the sequence input
    */
   public SequenceInput<Annotation> asSequence(@NonNull Function<? super Annotation, String> labelFunction) {
      SequenceInput<Annotation> si = new SequenceInput<>();
      for (Annotation token : tokens()) {
         si.add(token, labelFunction.apply(token));
      }
      return si;
   }

   public RelationGraph dependencyGraph() {
      return annotationGraph($(Types.DEPENDENCY), Types.TOKEN);
   }

   public RelationGraph dependencyGraph(AnnotationType type1, AnnotationType... other) {
      return annotationGraph($(Types.DEPENDENCY), type1, other);
   }

   public RelationGraph annotationGraph(Tuple relationTypes, AnnotationType type, AnnotationType... annotationTypes) {
      RelationGraph g = new RelationGraph();
      List<Annotation> vertices = interleaved(type, annotationTypes);
      Set<RelationType> relationTypeList = Streams.asStream(relationTypes.iterator()).filter(
         r -> r instanceof RelationType)
                                                  .map(Cast::<RelationType>as).collect(Collectors.toSet());
      g.addVertices(vertices);

      for (Annotation source : vertices) {
         Collection<Relation> relations = source.allRelations(true);
         for (Relation relation : relations) {
            if (relationTypeList.contains(relation.getType())) {
               relation.getTarget(this).ifPresent(target -> {
                  if (g.containsVertex(target)) {
                     if (!g.containsEdge(source, target)) {
                        RelationEdge edge = g.addEdge(source, target);
                        edge.setRelation(relation.getValue());
                        edge.setRelationType(relation.getType());
                     }
                  }
               });
            }
         }
      }
      return g;
   }

}//END OF HString
