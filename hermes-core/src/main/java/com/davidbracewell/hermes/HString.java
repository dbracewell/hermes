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
import com.davidbracewell.apollo.linalg.Vector;
import com.davidbracewell.apollo.linalg.VectorComposition;
import com.davidbracewell.apollo.linalg.VectorCompositions;
import com.davidbracewell.apollo.ml.LabeledDatum;
import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.apollo.ml.sequence.SequenceInput;
import com.davidbracewell.collection.Span;
import com.davidbracewell.collection.Streams;
import com.davidbracewell.collection.list.Lists;
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
 * <p> Represents a java string on steroids. HStrings act as a <code>CharSequence</code> and have methods similar to
 * those on <code>String</code>. Methods that do not modify the underlying string representation, e.g. substring, find,
 * will return another HString whereas methods that modify the string, e.g. lower casing, return a String object.
 * Additionally, HStrings have a span (i.e. start and end positions), attributes, and annotations. HStrings allow for
 * methods to process documents, fragments, and annotations in a uniform fashion. Methods on the HString facilitate
 * determining if the object is an annotation ({@link #isAnnotation()} or is a document ({@link #isDocument()}). </p>
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
      return union(Lists.union(Arrays.asList(first, second), Arrays.asList(others)));
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
         if (!hString.isEmpty()) {
            if (owner == null && hString.document() != null) {
               owner = hString.document();
            } else if (hString.document() == null || owner != hString.document()) {
               throw new IllegalArgumentException("Cannot union strings from different documents");
            }
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
    * <p>Constructs a relation graph with the given relation types as the edges and the given annotation types as the
    * vertices (the {@link #interleaved(AnnotationType...)} method is used to get the annotations). Relations will be
    * determine for annotations by including the relations of their sub-annotations (i.e. sub-spans). This allows, for
    * example, a dependency graph to be built over other annotation types, e.g. phrase chunks.</p>
    *
    * @param relationTypes   the relation types making up the edges
    * @param annotationTypes annotation types making up the vertices
    * @return the relation graph
    */
   public RelationGraph annotationGraph(@NonNull Tuple relationTypes, @NonNull AnnotationType... annotationTypes) {
      RelationGraph g = new RelationGraph();
      List<Annotation> vertices = interleaved(annotationTypes);
      Set<RelationType> relationTypeList = Streams
                                              .asStream(relationTypes.iterator())
                                              .filter(r -> r instanceof RelationType)
                                              .map(Cast::<RelationType>as)
                                              .collect(Collectors.toSet());
      g.addVertices(vertices);
      for (Annotation source : vertices) {
         Collection<Relation> relations = source.relations(true);
         for (Relation relation : relations) {
            if (relationTypeList.contains(relation.getType())) {
               relation
                  .getTarget(document())
                  .ifPresent(target -> {
                     target = g.containsVertex(target)
                              ? target
                              : target
                                   .stream(AnnotationType.ROOT)
                                   .filter(g::containsVertex)
                                   .findFirst()
                                   .orElse(null);
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

   @Override
   public List<Annotation> annotations() {
      if (document() == null) {
         return Collections.emptyList();
      }
      return document().get(AnnotationType.ROOT, this);
   }

   /**
    * Gets this HString as an annotation. If the HString is already an annotation it is simply cast. Otherwise a
    * detached annotation of type <code>AnnotationType.ROOT</code> is created.
    *
    * @return An annotation.
    */
   public Annotation asAnnotation() {
      if (this instanceof Annotation) {
         return Cast.as(this);
      } else if (document() != null) {
         return document()
                   .annotationBuilder()
                   .type(AnnotationType.ROOT)
                   .bounds(this)
                   .attributes(this)
                   .createDetached();
      }
      return Fragments.detachedAnnotation(AnnotationType.ROOT, start(), end());
   }

   /**
    * Creates a labeled data point from this HString applying the given label function to determine the label, i.e.
    * class.
    *
    * @param labelFunction the label function to determine the label (or class) of the data point (e.g. part-of-speech,
    *                      sentiment, etc.)
    * @return the labeled datum
    */
   public LabeledDatum<HString> asLabeledData(@NonNull Function<HString, ?> labelFunction) {
      return LabeledDatum.of(labelFunction.apply(this), this);
   }

   /**
    * Creates a labeled data point from this HString using the value of the given attribute type as the label, i.e.
    * class.
    *
    * @param attributeTypeLabel the attribute type whose value will become the label of the data point
    * @return the labeled datum
    */
   public LabeledDatum<HString> asLabeledData(@NonNull AttributeType attributeTypeLabel) {
      return LabeledDatum.of(get(attributeTypeLabel), this);
   }

   /**
    * Creates an unlabeled {@link SequenceInput} of tokens from this HString.
    *
    * @return the sequence input
    */
   public SequenceInput<Annotation> asSequence() {
      return new SequenceInput<>(Cast.cast(tokens()));
   }

   /**
    * Creates a labeled {@link SequenceInput} of tokens from this HString applying the given label function to each
    * token to determine its label, i.e. class..
    *
    * @param labelFunction the label function to determine the label (or class) of the data point (e.g. part-of-speech,
    *                      sentiment, etc.)
    * @return the sequence input
    */
   public SequenceInput<Annotation> asSequence(@NonNull Function<? super Annotation, String> labelFunction) {
      SequenceInput<Annotation> si = new SequenceInput<>();
      for (Annotation token : tokens()) {
         si.add(token, labelFunction.apply(token));
      }
      return si;
   }

   public Vector asVector(@NonNull VectorComposition composition) {
      return asVector(composition, Types.TOKEN);
   }

   public Vector asVector() {
      return asVector(VectorCompositions.SVD, Types.TOKEN);
   }

   public Vector asVector(@NonNull VectorComposition composition, @NonNull AnnotationType... types) {
      Embedding embedding = LanguageData.getDefaultEmbedding(getLanguage());
      Vector v = Vector.dZeros(embedding.dimension());
      interleaved(types).forEach(a -> {
         if (embedding.containsKey(a.toString())) {
            v.addSelf(embedding.get(a.toString()));
         } else if (embedding.containsKey(a.getLemma())) {
            v.addSelf(embedding.get(a.getLemma()));
         } else if (embedding.containsKey(a.toLowerCase())) {
            v.addSelf(embedding.get(a.toLowerCase()));
         } else if (embedding.containsKey(a.getPOS().name())) {
            v.addSelf(embedding.get(a.getPOS().name()));
         }
      });
      return v;
   }

   @Override
   public Set<Map.Entry<AttributeType, Val>> attributeEntrySet() {
      return getAttributeMap().entrySet();
   }

   /**
    * Extracts character n-grams of the given order (e.g. 1=unigram, 2=bigram, etc.)
    *
    * @param order the order of the n-gram to extract
    * @return the list of character n-grams of given order making up this HString
    */
   public List<HString> charNGrams(int order) {
      return charNGrams(order, order);
   }

   /**
    * Extracts all character n-grams from the given minimum to given maximum order (e.g. 1=unigram, 2=bigram, etc.)
    *
    * @param minOrder the minimum order
    * @param maxOrder the maximum order
    * @return the list of character n-grams of order <code>minOrder</code> to <code>maxOrder</code> making up this
    * HString
    * @throws IllegalArgumentException If minOrder > maxOrder or minOrder <= 0
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
    * Context h string.
    *
    * @param windowSize the window size
    * @return the h string
    */
   public HString context(int windowSize) {
      return context(Types.TOKEN, windowSize);
   }

   /**
    * Context h string.
    *
    * @param type       the type
    * @param windowSize the window size
    * @return the h string
    */
   public HString context(@NonNull AnnotationType type, int windowSize) {
      return leftContext(type, windowSize).union(rightContext(type, windowSize));
   }

   /**
    * Creates a {@link RelationGraph} with dependency edges and token vertices.
    *
    * @return the dependency relation graph
    */
   public RelationGraph dependencyGraph() {
      return annotationGraph($(Types.DEPENDENCY), Types.TOKEN);
   }

   /**
    * Creates a {@link RelationGraph} with dependency edges and vertices made up of the given types.
    *
    * @param types The annotation types making up the vertices of the dependency relation graph.
    * @return the dependency relation graph
    */
   public RelationGraph dependencyGraph(@NonNull AnnotationType... types) {
      return annotationGraph($(Types.DEPENDENCY), types);
   }

   @Override
   public Tuple2<String, Annotation> dependencyRelation() {
      if (head().isAnnotation()) {
         return head()
                   .asAnnotation()
                   .dependencyRelation();
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
    * Finds the given text in this HString starting from the beginning of this HString. If the document is annotated
    * with tokens, the match will extend to the token(s) covering the match.
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
    * Finds all occurrences of the  given regular expression in this HString starting from the beginning of this
    * HString
    *
    * @param regex the regular expression to search for
    * @return A list of HString that are matches to the given regular expression
    */
   public Stream<HString> findAllPatterns(@NonNull String regex) {
      return findAllPatterns(Pattern.compile(regex));
   }

   /**
    * Finds all matches to the given token regular expression in this HString.
    *
    * @param regex the token regex to match
    * @return Stream of matches
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
    * Finds all occurrences of the  given regular expression in this HString starting from the beginning of this
    * HString
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
    * Gets the lemmatized version of the HString. Lemmas of longer phrases are constructed from token lemmas.
    *
    * @return The lemmatized version of the HString.
    */
   public String getLemma() {
      if (isInstance(Types.TOKEN)) {
         if (contains(Types.SPELLING_CORRECTION)) {
            return get(Types.SPELLING_CORRECTION).asString();
         }
         if (contains(Types.LEMMA)) {
            return get(Types.LEMMA).asString();
         }
         return toLowerCase();
      }
      return tokens()
                .stream()
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

   /**
    * Gets the stemmed version of the HString. Stems of token are determined using the <code>Stemmer</code> associated
    * with the language that the token is in. Tokens store their stem using the <code>STEM</code> attribute, so that the
    * stem only needs to be calculated once.Stems of longer phrases are constructed from token stems.
    *
    * @return The stemmed version of the HString.
    */
   public String getStem() {
      if (isInstance(Types.TOKEN)) {
         putIfAbsent(Types.STEM, Stemmers
                                    .getStemmer(getLanguage())
                                    .stem(this));
         return get(Types.STEM).asString();
      }
      return tokens()
                .stream()
                .map(HString::getStem)
                .collect(Collectors.joining(getLanguage().usesWhitespace() ? " " : ""));
   }

   @Override
   public final int hashCode() {
      return super.hashCode();
   }

   /**
    * Gets head.
    *
    * @return the head
    */
   public HString head() {
      return tokens()
                .stream()
                .filter(t -> t
                                .parent()
                                .isEmpty())
                .map(Cast::<HString>as)
                .findFirst()
                .orElseGet(() -> tokens()
                                    .stream()
                                    .filter(t -> !this.overlaps(t.parent()))
                                    .map(Cast::<HString>as)
                                    .findFirst()
                                    .orElse(this));
   }

   public String inSentence() {
      if (this.isInstance(Types.SENTENCE)) {
         return toString();
      }
      Annotation sentence = first(Types.SENTENCE);
      if (sentence.isEmpty()) {
         return StringUtils.EMPTY;
      }

      String tag = isAnnotation() ? Cast.<Annotation>as(this)
                                       .getTag()
                                       .map(Tag::name)
                                       .orElse(Cast.<Annotation>as(this).getType().name())
                                  : "b";

      StringBuilder builder = new StringBuilder();
      int modStart = start() - sentence.start();
      int modEnd = end() - sentence.start();
      if (start() == sentence.start()) {
         builder.append("<").append(tag).append(">")
                .append(toString())
                .append("</").append(tag).append("> ")
                .append(sentence.substring(modEnd, sentence.end() - sentence.start()));
      } else if (end() == sentence.end()) {
         builder.append(sentence.substring(0, modStart))
                .append("<").append(tag).append(">")
                .append(toString())
                .append("</").append(tag).append(">");
      } else {
         builder.append(sentence.substring(0, modStart))
                .append("<").append(tag).append(">")
                .append(toString())
                .append("</").append(tag).append(">")
                .append(sentence.substring(modEnd, sentence.end() - sentence.start()));
      }

      return builder.toString();
   }

   /**
    * <p> Returns the annotations of the given types that overlap this string in a maximum match fashion. Each token in
    * the string is examined and the annotation type with the longest span on that token is chosen. If more than one
    * type has the span length, the first one found will be chose, i.e. the order in which the types are passed in to
    * the method can effect the outcome. </p> <p> Examples where this is useful is when dealing with multi-word
    * expressions. Using the interleaved method you can retrieve all tokens and multi-word expressions to fully match
    * the span of the string. </p>
    *
    * @param types The other types to examine
    * @return The list of interleaved annotations
    */
   public List<Annotation> interleaved(@NonNull AnnotationType... types) {
      if (types == null || types.length == 0) {
         return Collections.emptyList();
      }


      List<Annotation> annotations = new ArrayList<>();
      for (int i = 0; i < tokenLength(); ) {
         Annotation annotation = Fragments.detachedEmptyAnnotation();
         for (AnnotationType other : types) {
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
    * Left context h string.
    *
    * @param windowSize the window size
    * @return the h string
    */
   public HString leftContext(int windowSize) {
      return leftContext(Types.TOKEN, windowSize);
   }

   /**
    * Left context h string.
    *
    * @param type       the type
    * @param windowSize the window size
    * @return the h string
    */
   public HString leftContext(@NonNull AnnotationType type, int windowSize) {
      windowSize = Math.abs(windowSize);
      Preconditions.checkArgument(windowSize >= 0);
      int sentenceStart = sentence().start();
      if (windowSize == 0 || start() <= sentenceStart) {
         return Fragments.detachedEmptyHString();
      }
      HString context = firstToken().previous(type);
      for (int i = 1; i < windowSize; i++) {
         HString next = context
                           .firstToken()
                           .previous(type);
         if (next.end() <= sentenceStart) {
            break;
         }
         context = context.union(next);
      }
      return context;
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
    * Right context h string.
    *
    * @param windowSize the window size
    * @return the h string
    */
   public HString rightContext(int windowSize) {
      return rightContext(Types.TOKEN, windowSize);
   }

   /**
    * Right context h string.
    *
    * @param type       the type
    * @param windowSize the window size
    * @return the h string
    */
   public HString rightContext(@NonNull AnnotationType type, int windowSize) {
      windowSize = Math.abs(windowSize);
      Preconditions.checkArgument(windowSize >= 0);
      int sentenceEnd = sentence().end();
      if (windowSize == 0 || end() >= sentenceEnd) {
         return Fragments.detachedEmptyHString();
      }
      HString context = lastToken().next(type);
      for (int i = 1; i < windowSize; i++) {
         HString next = context
                           .lastToken()
                           .next(type);
         if (next.start() >= sentenceEnd) {
            break;
         }
         context = context.union(next);
      }
      return context;
   }

   /**
    * Splits this HString using the given predicate to apply against tokens. An example of where this might be useful is
    * when we want to split long phrases on different punctuation, e.g. commas or semicolons.
    *
    * @param delimiterPredicate the predicate to use to determine if a token is a delimiter or not
    * @return the list of split HString
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

   @Override
   public final List<Annotation> startingHere(AnnotationType type) {
      if (type == null) {
         return Collections.emptyList();
      }
      return get(type, annotation -> annotation.start() == start() && annotation.isInstance(type));
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

   public String tag(@NonNull AnnotationType type, String defaultTag) {
      Preconditions.checkArgument(StringUtils.isNotNullOrBlank(defaultTag), "Default tag must not be null or blank.");
      List<Annotation> annotations = get(type);
      if (annotations.size() == 0) {
         return toString();
      }
      int modStart = start();
      StringBuilder builder = new StringBuilder();
      int lastEnd = 0;
      for (Annotation a : annotations) {
         String tag = a.getTag().map(Tag::name).orElse(defaultTag);
         String taggedAnnotation = "<" + tag + ">" + a + "</" + tag + ">";
         int start = a.start() - modStart;
         int end = a.end() - modStart;
         if (start > lastEnd) {
            builder.append(substring(lastEnd, start));
         }
         builder.append(taggedAnnotation);
         lastEnd = end;
      }
      if (lastEnd < length()) {
         builder.append(substring(lastEnd, length()));
      }
      return builder.toString();
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
      return tokens()
                .stream()
                .map(t -> t.toString() + delimiter + t
                                                        .get(Types.PART_OF_SPEECH)
                                                        .as(POS.class, POS.ANY)
                                                        .asString())
                .collect(Collectors.joining(" "));
   }

   @Override
   public String toString() {
      if (document() == null || isEmpty()) {
         return StringUtils.EMPTY;
      }
      return document()
                .toString()
                .substring(start(), end());
   }

   /**
    * Trims tokens off the left and right of this HString that match the given predicate.
    *
    * @param toTrimPredicate the predicate to use to determine if a token should be removed (evaulate to TRUE) or kept
    *                        (evaluate to FALSE).
    * @return the trimmed HString
    */
   public HString trim(@NonNull Predicate<? super Annotation> toTrimPredicate) {
      return trimRight(toTrimPredicate).trimLeft(toTrimPredicate);
   }

   /**
    * Trims tokens off the left of this HString that match the given predicate.
    *
    * @param toTrimPredicate the predicate to use to determine if a token should be removed (evaulate to TRUE) or kept
    *                        (evaluate to FALSE).
    * @return the trimmed HString
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
    * Trims tokens off the right of this HString that match the given predicate.
    *
    * @param toTrimPredicate the predicate to use to determine if a token should be removed (evaulate to TRUE) or kept
    *                        (evaluate to FALSE).
    * @return the trimmed HString
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
    * @param other the HString to union with
    * @return A new HString representing the union over the spans of the given HStrings.
    */
   public HString union(@NonNull HString other) {
      return HString.union(this, other);
   }

}//END OF HString
