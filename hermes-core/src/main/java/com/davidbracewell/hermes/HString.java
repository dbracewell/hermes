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
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.guava.common.base.Preconditions;
import com.davidbracewell.hermes.extraction.regex.TokenMatcher;
import com.davidbracewell.hermes.extraction.regex.TokenRegex;
import com.davidbracewell.hermes.morphology.Stemmers;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple;
import com.davidbracewell.tuple.Tuple2;
import lombok.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public abstract class HString extends Span implements StringLike, AttributedObject, AnnotatedObject, RelationalObject {
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
    * Creates a new string by performing a union over the spans of two or more HStrings. The new HString will have a
    * span that starts at the minimum starting position of the given strings and end at the maximum ending position of
    * the given strings.
    *
    * @param strings the HStrings to union
    * @return A new HString representing the union over the spans of the given HStrings.
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

   /**
    * Tagged text string.
    *
    * @param type the type
    * @return the string
    */
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
    * Creates a string representation where this HString is tagged inside its sentence. For non-annotations the
    * representation is as follows: <code>The [quick] brown fox...</code>. For annotations with a valid <code>Tag</code>
    * the representation is as follows: <code>The &lt;SPEED&gt;quick&lt;/SPEED&gt; brown fox...</code>
    *
    * @return A string representing this HString tagged inside its sentence, Empty string if there is no sentence.
    */
   public String inSentence() {
      StringBuilder builder = new StringBuilder();
      Annotation sentence = first(Types.SENTENCE);
      if (!sentence.isEmpty()) {
         int ss = sentence.start();
         int se = sentence.end();
         int as = Math.max(0, start() - ss);
         int ae = Math.min(se, end() - ss);
         if (as > ss) {
            builder.append(sentence.subSequence(0, as));
         }

         String openTag = "[";
         String closeTag = "]";

         if (isAnnotation() && asAnnotation().filter(a -> a.getTag().isPresent()).isPresent()) {
            String tag = asAnnotation().get().getTag().get().name();
            openTag = "<" + tag + ">";
            closeTag = "</" + tag + ">";
         }


         builder.append(openTag).append(toString()).append(closeTag);

         if (ae < se) {
            builder.append(sentence.subSequence(ae, se - ss));
         }

      }
      return builder.toString();
   }

   /**
    * <p>Constructs a relation graph with the given relation types as the edges and the given annotation types as the
    * vertices (the {@link #interleaved(AnnotationType, AnnotationType...)} method is used to get the annotations).
    * Relations will be determine for annotations by including the relations of their sub-annotations (i.e. sub-spans).
    * This allows, for example, a dependency graph to be built over other annotation types, e.g. phrase chunks.</p>
    *
    * @param relationTypes   the relation types making up the edges
    * @param type            the primary annotation type making up the vertices
    * @param annotationTypes secondary annotation types making up the vertices
    * @return the relation graph
    */
   public RelationGraph annotationGraph(@NonNull Tuple relationTypes, @NonNull AnnotationType type, AnnotationType... annotationTypes) {
      RelationGraph g = new RelationGraph();
      List<Annotation> vertices = interleaved(type, annotationTypes);
      Set<RelationType> relationTypeList = Streams.asStream(relationTypes.iterator())
                                                  .filter(r -> r instanceof RelationType)
                                                  .map(Cast::<RelationType>as).collect(Collectors.toSet());
      g.addVertices(vertices);
      for (Annotation source : vertices) {
         Collection<Relation> relations = source.relations(true);
         for (Relation relation : relations) {
            if (relationTypeList.contains(relation.getType())) {
               relation.getTarget(document()).ifPresent(target -> {
                  target = g.containsVertex(target) ? target : target.stream(AnnotationType.ROOT).filter(
                     g::containsVertex).findFirst().orElse(null);
                  if (target != null) {
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

   /**
    * Gets this HString as an annotation
    *
    * @return An optional that is empty if the HString is not an annotation.
    */
   public Optional<Annotation> asAnnotation() {
      if (this instanceof Annotation) {
         return Optional.of(Cast.as(this));
      }
      return Optional.empty();
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
    * Dependency graph relation graph.
    *
    * @return the relation graph
    */
   public RelationGraph dependencyGraph() {
      return annotationGraph($(Types.DEPENDENCY), Types.TOKEN);
   }

   /**
    * Dependency graph relation graph.
    *
    * @param type1 the type 1
    * @param other the other
    * @return the relation graph
    */
   public RelationGraph dependencyGraph(AnnotationType type1, AnnotationType... other) {
      return annotationGraph($(Types.DEPENDENCY), type1, other);
   }

   @Override
   public Tuple2<String, Annotation> dependencyRelation() {
      if (head().isAnnotation()) {
         return head().asAnnotation().get().dependencyRelation();
      }
      return $(StringUtils.EMPTY, Fragments.emptyAnnotation(document()));
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


   @Override
   public final boolean equals(Object other) {
      return this == other;
   }

   /**
    * Finds the given text in this HString starting from the beginning of this HString. If the document is
    * annotated with tokens, the match will extend to the token(s) covering the match.
    *
    * @param text the text to search for
    * @return the HString for the match or empty if no match is found.
    */
   public HString find(String text) {
      return find(text, 0);
   }

   /**
    * Finds the given text in this HString starting from the given start index of this HString. If the document is
    * annotated with tokens, the match will extend to the token(s) covering the match.
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

      //If we have tokens expand the match to the overlaping tokens.
      if (document() != null && document().isCompleted(Types.TOKEN)) {
         return union(substring(pos, pos + text.length()).tokens());
      }

      return substring(pos, pos + text.length());
   }

   /**
    * Finds all occurrences of the given text in this HString starting
    *
    * @param text the text to search for
    * @return A list of HString that are matches to the given string
    */
   public Stream<HString> findAll(@NonNull String text) {
      return Streams.asStream(new Iterator<HString>() {
         Integer pos = null;
         int start = 0;

         private boolean advance() {
            if (pos == null) {
               pos = indexOf(text, start);
            }
            return pos != -1;
         }

         @Override
         public boolean hasNext() {
            return advance();
         }

         @Override
         public HString next() {
            if (!advance()) {
               throw new NoSuchElementException();
            }
            int n = pos;
            pos = null;
            start = n + 1;
            //If we have tokens expand the match to the overlaping tokens.
            if (document() != null && document().isCompleted(Types.TOKEN)) {
               return union(substring(n, n + text.length()).tokens());
            }
            return substring(n, n + text.length());
         }
      });
   }

   /**
    * Finds all occurrences of the  given regular expression in this HString starting from the beginning of this HString
    *
    * @param regex the regular expression to search for
    * @return A list of HString that are matches to the given regular expression
    */
   public Stream<HString> findAllPatterns(@NonNull String regex) {
      return findAllPatterns(Pattern.compile(regex));
   }

   /**
    * Find all patterns list.
    *
    * @param regex the regex
    * @return the list
    */
   public Stream<HString> findAllPatterns(@NonNull TokenRegex regex) {
      return Streams.asStream(new Iterator<HString>() {
         TokenMatcher m = regex.matcher(HString.this);
         HString nextMatch = null;

         private boolean advance() {
            if (nextMatch == null && m.find()) {
               nextMatch = m.group();
            }
            return nextMatch != null;
         }

         @Override
         public boolean hasNext() {
            return advance();
         }

         @Override
         public HString next() {
            if (!advance()) {
               throw new NoSuchElementException();
            }
            HString toReturn = nextMatch;
            nextMatch = null;
            return toReturn;
         }
      });
   }

   /**
    * Finds all occurrences of the  given regular expression in this HString starting from the beginning of this HString
    *
    * @param regex the regular expression to search for
    * @return A list of HString that are matches to the given regular expression
    */
   public Stream<HString> findAllPatterns(@NonNull Pattern regex) {
      return Streams.asStream(new Iterator<HString>() {
         Matcher m = regex.matcher(HString.this);
         int start = -1;
         int end = -1;

         private boolean advance() {
            if (start == -1) {
               if (m.find()) {
                  start = m.start();
                  end = m.end();
               }
            }
            return start != -1;
         }

         @Override
         public boolean hasNext() {
            return advance();
         }

         @Override
         public HString next() {
            if (!advance()) {
               throw new NoSuchElementException();
            }
            HString sub = substring(start, end);
            start = -1;
            end = -1;
            //If we have tokens expand the match to the overlaping tokens.
            if (document() != null && document().isCompleted(Types.TOKEN)) {
               return union(sub.tokens());
            }
            return sub;
         }
      });
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
   public List<Annotation> annotations() {
      if (document() == null) {
         return Collections.emptyList();
      }
      return document().get(AnnotationType.ROOT, this);
   }

   /**
    * Exposes the underlying attributes as a Map
    *
    * @return The attribute names and values as a map
    */
   protected abstract Map<AttributeType, Val> getAttributeMap();

   @Override
   public Language getLanguage() {
      if (contains(Types.LANGUAGE)) {
         return get(Types.LANGUAGE).as(Language.class);
      }
      if (document() == null) {
         return Hermes.defaultLanguage();
      }
      return document().getLanguage();
   }


   @Override
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
   public final List<Annotation> startingHere(AnnotationType type) {
      if (type == null) {
         return Collections.emptyList();
      }
      return get(type, annotation -> annotation.start() == start() && annotation.isInstance(type));
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

   @Override
   public final int hashCode() {
      return super.hashCode();
   }

   /**
    * <p>
    * Returns the annotations of the given types that overlap this string in a maximum match fashion. Each token in the
    * string is examined and the annotation type with the longest span on that token is chosen. If more than one type
    * has the span length, the first one found will be chose, i.e. the order in which the types are passed in to the
    * method can effect the outcome.
    * </p>
    * <p>
    * Examples where this is useful is when dealing with multi-word expressions. Using the interleaved method you can
    * retrieve all tokens and multi-word expressions to fully match the span of the string.
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

   /**
    * Gets head.
    *
    * @return the head
    */
   public HString head() {
      return tokens().stream()
                     .filter(t -> t.parent().isEmpty())
                     .map(Cast::<HString>as)
                     .findFirst()
                     .orElseGet(() -> tokens().stream()
                                              .filter(t -> !this.overlaps(t.parent()))
                                              .map(Cast::<HString>as)
                                              .findFirst()
                                              .orElse(this));
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
    * Trim h string.
    *
    * @param toTrimPredicate the to trim predicate
    * @return the h string
    */
   public HString trim(@NonNull Predicate<? super Annotation> toTrimPredicate) {
      return trimRight(toTrimPredicate).trimLeft(toTrimPredicate);
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


}//END OF HString
