package com.davidbracewell.hermes.filter;

import com.davidbracewell.Tag;
import com.davidbracewell.collection.Span;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.function.SerializableBiPredicate;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.extraction.regex.QueryToPredicate;
import com.davidbracewell.parsing.ParseException;
import com.davidbracewell.string.StringPredicates;
import lombok.NonNull;

import java.util.regex.Pattern;

/**
 * The interface H string predicates.
 *
 * @author David B. Bracewell
 */
public interface HStringPredicates {

   /**
    * Content serializable predicate.
    *
    * @param charPredicate the char predicate
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> contentMatch(@NonNull final SerializablePredicate<CharSequence> charPredicate) {
      return charPredicate::test;
   }


   /**
    * Content serializable predicate.
    *
    * @param charPredicate the char predicate
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> lemmaMatch(@NonNull final SerializablePredicate<CharSequence> charPredicate) {
      return hString -> charPredicate.test(hString.getLemma());
   }

   /**
    * Content match serializable predicate.
    *
    * @param target the target
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> contentMatch(@NonNull final String target) {
      return contentMatch(StringPredicates.MATCHES(target));
   }

   /**
    * Content match serializable predicate.
    *
    * @param target        the target
    * @param caseSensitive the case sensitive
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> contentMatch(@NonNull final String target, final boolean caseSensitive) {
      return contentMatch(StringPredicates.MATCHES(target, caseSensitive));
   }

   /**
    * Content match serializable predicate.
    *
    * @param target        the target
    * @param caseSensitive the case sensitive
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> lemmaMatch(@NonNull final String target, final boolean caseSensitive) {
      return lemmaMatch(StringPredicates.MATCHES(target, caseSensitive));
   }


   /**
    * Content regex match serializable predicate.
    *
    * @param pattern the pattern
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> contentRegexMatch(@NonNull final Pattern pattern) {
      return contentMatch(cs -> pattern.matcher(cs).find());
   }

   /**
    * Content regex match serializable predicate.
    *
    * @param pattern the pattern
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> contentRegexMatch(@NonNull final String pattern) {
      final Pattern regex = Pattern.compile(pattern);
      return contentMatch(cs -> regex.matcher(cs).find());
   }

   /**
    * Content regex match serializable predicate.
    *
    * @param pattern       the pattern
    * @param caseSensitive the case sensitive
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> contentRegexMatch(@NonNull final String pattern, boolean caseSensitive) {
      if (caseSensitive) {
         return contentRegexMatch(pattern);
      }
      return contentRegexMatch(pattern, Pattern.CASE_INSENSITIVE);
   }

   /**
    * Content regex match serializable predicate.
    *
    * @param pattern the pattern
    * @param flags   the flags
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> contentRegexMatch(@NonNull final String pattern, final int flags) {
      final Pattern regex = Pattern.compile(pattern, flags);
      return contentMatch(cs -> regex.matcher(cs).find());
   }


   /**
    * Tag serializable predicate.
    *
    * @param target the target
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> hasTagInstance(@NonNull final Tag target) {
      return hString -> {
         if (hString.isAnnotation()) {
            return hString.asAnnotation().isInstanceOfTag(target);
         }
         return hString.get(Types.TAG).equals(target);
      };
   }

   /**
    * Has tag instance serializable predicate.
    *
    * @param target the target
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> hasTagInstance(@NonNull final String target) {
      return hString -> {
         POS pos = null;
         try {
            pos = POS.valueOf(target);
         } catch (Exception e) {
            //ignore;
         }
         if (hString == null) {
            return false;
         }
         if (hString.isAnnotation()) {
            return hString.asAnnotation().isInstanceOfTag(target) || (pos != null && hString.getPOS().isInstance(pos));
         } else if (hString.tokenLength() == 1) {
            return hString.tokenStream()
                          .filter(a -> a.isInstanceOfTag(target))
                          .count() > 0 || (pos != null && hString.getPOS().isInstance(pos));
         }
         return hString.get(Types.TAG).equals(target) || (pos != null && hString.getPOS().isInstance(pos));
      };
   }

   /**
    * Has attribute serializable predicate.
    *
    * @param attributeType the attribute
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> hasAttribute(@NonNull final AttributeType attributeType) {
      return hString -> hString.contains(attributeType);
   }

   /**
    * Has annotation serializable predicate.
    *
    * @param annotationType the annotation type
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> hasAnnotation(@NonNull final AnnotationType annotationType) {
      return hString -> hString.get(annotationType).size() > 0;
   }

   /**
    * Instance of serializable predicate.
    *
    * @param annotationType the annotation type
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> instanceOf(@NonNull final AnnotationType annotationType) {
      return hString -> hString.isInstance(annotationType);
   }

   /**
    * Is empty serializable predicate.
    *
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> isEmpty() {
      return Span::isEmpty;
   }

   /**
    * Is not empty serializable predicate.
    *
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> isNotEmpty() {
      return isEmpty().negate();
   }


   /**
    * Attribute serializable predicate.
    *
    * @param attributeType the attribute
    * @param value         the value
    * @return the serializable predicate
    */
   static SerializablePredicate<HString> attributeMatch(@NonNull final AttributeType attributeType, final Object value) {
      final Object convertedValue = attributeType.getValueType().decode(value);
      final boolean isTag = Tag.class.isInstance(convertedValue);

      return annotation -> {
         if (isTag) {
            Tag tag = annotation.get(attributeType).as(Tag.class);
            if (tag == null) {
               return false;
            }
            return tag.isInstance(Cast.<Tag>as(convertedValue));
         }
         return annotation.get(attributeType).equals(convertedValue);
      };

   }

   /**
    * Is overlapping serializable bi predicate.
    *
    * @return the serializable bi predicate
    */
   static SerializableBiPredicate<HString, HString> isOverlapping() {
      return HString::overlaps;
   }

   /**
    * Is non overlapping serializable bi predicate.
    *
    * @return the serializable bi predicate
    */
   static SerializableBiPredicate<HString, HString> isNonOverlapping() {
      return isOverlapping().negate();
   }


   /**
    * Parse serializable predicate.
    *
    * @param query the query
    * @return the serializable predicate
    * @throws ParseException the parse exception
    */
   static SerializablePredicate<HString> parse(@NonNull String query) throws ParseException {
      return QueryToPredicate.parse(query);
   }


}//END OF HStringPredicates
