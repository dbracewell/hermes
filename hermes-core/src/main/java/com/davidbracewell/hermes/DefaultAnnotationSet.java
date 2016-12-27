package com.davidbracewell.hermes;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * <p> A default implementation of an <code>AnnotationSet</code> that uses an {@link AnnotationTree} which is an
 * Red-Black backed Interval Tree. </p>
 *
 * @author David B. Bracewell
 */
public class DefaultAnnotationSet implements AnnotationSet, Serializable {
   private static final long serialVersionUID = 1L;
   private final AnnotationTree tree = new AnnotationTree();
   private final Map<AnnotatableType, String> completed = new HashMap<>(4);
   private final Map<Long, Annotation> idAnnotationMap = new HashMap<>(4);

   @Override
   public List<Annotation> select(Span span, Predicate<? super Annotation> criteria) {
      return tree.overlapping(span).stream().filter(criteria).sorted().collect(Collectors.toList());
   }

   @Override
   public List<Annotation> select(Predicate<? super Annotation> criteria) {
      return tree.stream().filter(criteria).sorted().collect(Collectors.toList());
   }

   @Override
   public void setIsCompleted(AnnotatableType type, boolean isCompleted, String annotatorInformation) {
      if (isCompleted) {
         completed.put(type, annotatorInformation);
      } else {
         completed.remove(type);
      }
   }

   @Override
   public boolean isCompleted(AnnotatableType type) {
      return completed.containsKey(type);
   }

   @Override
   public String getAnnotationProvider(AnnotatableType type) {
      return completed.get(type);
   }

   @Override
   public Set<AnnotatableType> getCompleted() {
      return completed.keySet();
   }

   @Override
   public Annotation get(long id) {
      return idAnnotationMap.get(id);
   }

   @Override
   public boolean contains(Annotation annotation) {
      return tree.contains(annotation);
   }

   @Override
   public boolean remove(Annotation annotation) {
      boolean removed = tree.remove(annotation);
      if (removed) {
         idAnnotationMap.remove(annotation.getId());
      }
      return removed;
   }

   @Override
   public void add(Annotation annotation) {
      tree.add(annotation);
      idAnnotationMap.put(annotation.getId(), annotation);
   }

   @Override
   public Annotation next(Annotation annotation, AnnotationType type) {
      return tree.ceiling(annotation, type);
   }

   @Override
   public Annotation previous(Annotation annotation, AnnotationType type) {
      return tree.floor(annotation, type);
   }

   @Override
   public int size() {
      return tree.size();
   }

   @Override
   public Iterator<Annotation> iterator() {
      return tree.iterator();
   }

}// END OF DefaultAnnotationSet
