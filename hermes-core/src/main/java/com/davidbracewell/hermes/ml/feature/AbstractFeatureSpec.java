package com.davidbracewell.hermes.ml.feature;

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.filter.StopWords;
import lombok.NonNull;
import lombok.ToString;

import java.io.Serializable;

/**
 * The type Feature spec.
 *
 * @param <T> the type parameter
 * @author David B. Bracewell
 */
@ToString
public abstract class AbstractFeatureSpec<T extends AbstractFeatureSpec> implements Serializable {
   private static final long serialVersionUID = 1L;

   private AnnotationType annotationType = Types.TOKEN;
   private SerializableFunction<HString, HString> trimFunction = h -> h;
   private SerializableFunction<HString, String> toStringFunction = HString::toString;
   private SerializablePredicate<? super HString> filter = hString -> true;
   private ValueCalculator valueCalculator = ValueCalculator.Frequency;

   /**
    * Instantiates a new Abstract feature spec.
    */
   public AbstractFeatureSpec() {

   }

   /**
    * Instantiates a new Abstract feature spec.
    *
    * @param copy the copy
    */
   public AbstractFeatureSpec(@NonNull AbstractFeatureSpec<T> copy) {
      this.annotationType = copy.annotationType;
      this.toStringFunction = copy.toStringFunction;
      this.filter = copy.filter;
      this.valueCalculator = copy.valueCalculator;
   }

   /**
    * Value calculator t.
    *
    * @param valueCalculator the value calculator
    * @return the t
    */
   public T valueCalculator(@NonNull ValueCalculator valueCalculator) {
      this.valueCalculator = valueCalculator;
      return Cast.as(this);
   }


   /**
    * Gets value calculator.
    *
    * @return the value calculator
    */
   public ValueCalculator getValueCalculator() {
      return valueCalculator;
   }

   /**
    * Annotation type t.
    *
    * @param annotationType the annotation type
    * @return the t
    */
   public T annotationType(@NonNull AnnotationType annotationType) {
      this.annotationType = annotationType;
      return Cast.as(this);
   }

   /**
    * Filter t.
    *
    * @param filter the filter
    * @return the t
    */
   public T filter(@NonNull SerializablePredicate<? super HString> filter) {
      this.filter = filter;
      return Cast.as(this);
   }

   /**
    * Gets annotation type.
    *
    * @return the annotation type
    */
   public AnnotationType getAnnotationType() {
      return annotationType;
   }

   /**
    * Gets filter.
    *
    * @return the filter
    */
   public SerializablePredicate<? super HString> getFilter() {
      return filter;
   }

   /**
    * Gets to string function.
    *
    * @return the to string function
    */
   public SerializableFunction<HString, String> getToStringFunction() {
      return toStringFunction;
   }

   /**
    * Ignore stop words t.
    *
    * @return the t
    */
   public T ignoreStopWords() {
      this.filter = StopWords.notHasStopWord();
      return Cast.as(this);
   }

   /**
    * Lower case t.
    *
    * @return the t
    */
   public T lowerCase() {
      this.toStringFunction = HString::toLowerCase;
      return Cast.as(this);
   }

   /**
    * Lemmatize t.
    *
    * @return the t
    */
   public T lemmatize() {
      toStringFunction = HString::getLemma;
      return Cast.as(this);
   }

   /**
    * To string function t.
    *
    * @param toStringFunction the to string function
    * @return the t
    */
   public T toStringFunction(@NonNull SerializableFunction<HString, String> toStringFunction) {
      this.toStringFunction = toStringFunction;
      return Cast.as(this);
   }


   /**
    * Get trim function serializable function.
    *
    * @return the serializable function
    */
   public SerializableFunction<HString, HString> getTrimFunction() {
      return this.trimFunction;
   }

   /**
    * Trim function t.
    *
    * @param trimFunction the trim function
    * @return the t
    */
   public T trimFunction(@NonNull SerializableFunction<HString, HString> trimFunction) {
      this.trimFunction = trimFunction;
      return Cast.as(this);
   }

}// END OF AbstractFeatureSpec
