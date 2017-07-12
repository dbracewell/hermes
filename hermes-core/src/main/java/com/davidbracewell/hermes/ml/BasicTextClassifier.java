package com.davidbracewell.hermes.ml;

import com.davidbracewell.apollo.ml.classification.Classification;
import com.davidbracewell.apollo.ml.classification.Classifier;
import com.davidbracewell.hermes.HString;
import lombok.NonNull;

/**
 * @author David B. Bracewell
 */
public abstract class BasicTextClassifier extends TextClassifier {
   private static final long serialVersionUID = 1L;

   @Override
   public final void classify(@NonNull HString text) {
      onClassify(text, getModel().classify(getFeaturizer().extractInstance(text)));
   }

   protected abstract Classifier getModel();

   protected abstract void onClassify(HString text, Classification classification);

}// END OF BasicTextClassifier
