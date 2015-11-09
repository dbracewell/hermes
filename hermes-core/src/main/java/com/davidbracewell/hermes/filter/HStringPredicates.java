package com.davidbracewell.hermes.filter;

import com.davidbracewell.Tag;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.function.SerializableBiPredicate;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.tag.StringTag;
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
        return Cast.<Annotation>as(hString).getTag().filter(tag -> tag.isInstance(target)).isPresent();
      }
      return hString.get(Attrs.TAG).equals(target);
    };
  }

  static SerializablePredicate<HString> hasTagInstance(@NonNull final String target) {
    return hString -> {
      if (hString.isAnnotation()) {
        Annotation annotation = Cast.as(hString);
        Tag tag = (annotation.getType().getTagAttribute() == null) ? new StringTag(target) : annotation.getType().getTagAttribute().getValueType().convert(target);
        return annotation.getTag().filter(t -> tag.isInstance(tag)).isPresent();
      }
      return hString.get(Attrs.TAG).equals(target);
    };
  }

  /**
   * Has attribute serializable predicate.
   *
   * @param attribute the attribute
   * @return the serializable predicate
   */
  static SerializablePredicate<HString> hasAttribute(@NonNull final Attribute attribute) {
    return hString -> hString.contains(attribute);
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
    return hString -> hString.isEmpty();
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
   * @param attribute the attribute
   * @param value     the value
   * @return the serializable predicate
   */
  static SerializablePredicate<HString> attributeMatch(@NonNull final Attribute attribute, final Object value) {
    final Object convertedValue = attribute.getValueType().convert(value);
    final boolean isTag = Tag.class.isAssignableFrom(attribute.getValueType().getType());
    return annotation -> {
      if (isTag) {
        return Cast.<Tag>as(annotation.get(attribute)).isInstance(Cast.<Tag>as(convertedValue));
      }
      return annotation.get(attribute).equals(convertedValue);
    };
  }

  /**
   * Is overlapping serializable bi predicate.
   *
   * @return the serializable bi predicate
   */
  static SerializableBiPredicate<HString, HString> isOverlapping() {
    return (hString, hString2) -> hString.overlaps(hString2);
  }

  /**
   * Is non overlapping serializable bi predicate.
   *
   * @return the serializable bi predicate
   */
  static SerializableBiPredicate<HString, HString> isNonOverlapping() {
    return isOverlapping().negate();
  }


}//END OF HStringPredicates
