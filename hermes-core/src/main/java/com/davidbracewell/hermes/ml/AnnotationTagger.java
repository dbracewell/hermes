package com.davidbracewell.hermes.ml;

import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.io.Serializable;

/**
 * @author David B. Bracewell
 */
public abstract class AnnotationTagger implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Read t.
   *
   * @param resource the resource
   * @return the t
   * @throws Exception the exception
   */
  public static <T extends AnnotationTagger> T read(@NonNull Resource resource) throws Exception {
    return resource.readObject();
  }


  public abstract void tag(Annotation sentence);

  /**
   * Write.
   *
   * @param resource the resource
   * @throws Exception the exception
   */
  public void write(@NonNull Resource resource) throws Exception {
    resource.setIsCompressed(true).writeObject(this);
  }


}// END OF AnnotationTagger
