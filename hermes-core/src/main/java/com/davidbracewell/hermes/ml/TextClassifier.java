package com.davidbracewell.hermes.ml;

import com.davidbracewell.hermes.HString;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.io.Serializable;

/**
 * The type Text classifier.
 *
 * @author David B. Bracewell
 */
public interface TextClassifier extends Serializable {

   /**
    * Read t.
    *
    * @param <T>      the type parameter
    * @param resource the resource
    * @return the t
    * @throws Exception the exception
    */
   static <T extends TextClassifier> T read(@NonNull Resource resource) throws Exception {
      return resource.readObject();
   }

   /**
    * Classify.
    *
    * @param text the text
    */
   void classify(HString text);


   /**
    * Write.
    *
    * @param resource the resource
    * @throws Exception the exception
    */
   default void write(@NonNull Resource resource) throws Exception {
      resource.setIsCompressed(true).writeObject(this);
   }

}// END OF TextClassifier
