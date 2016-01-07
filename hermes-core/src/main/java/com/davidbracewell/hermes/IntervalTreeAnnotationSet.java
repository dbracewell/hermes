package com.davidbracewell.hermes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author David B. Bracewell
 */
public class IntervalTreeAnnotationSet implements AnnotationSet {

  private final IntervalTree tree = new IntervalTree();
  private final Map<AnnotationType, String> completed = new HashMap<>(4);
  private final Map<Long, Annotation> idAnnotationMap = new HashMap<>(4);

  @Override
  public List<Annotation> select(Span span, Predicate<? super Annotation> criteria) {
    return tree.overlapping(span).stream().filter(criteria).collect(Collectors.toList());
  }

  @Override
  public List<Annotation> select(Predicate<? super Annotation> criteria) {
    return tree.stream().filter(criteria).collect(Collectors.toList());
  }

  @Override
  public void setIsCompleted(AnnotationType type, boolean isCompleted, String annotatorInformation) {
    if (isCompleted) {
      completed.put(type, annotatorInformation);
    } else {
      completed.remove(type);
    }
  }

  @Override
  public boolean isCompleted(AnnotationType type) {
    return completed.containsKey(type);
  }

  @Override
  public String getAnnotationProvider(AnnotationType type) {
    return completed.get(type);
  }

  @Override
  public Set<AnnotationType> getCompleted() {
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
  public void remove(Annotation annotation) {
      tree.remove(annotation);
      idAnnotationMap.remove(annotation.getId());
  }

  @Override
  public void add(Annotation annotation) {
    tree.add(annotation);
    long id = idAnnotationMap.size();
    annotation.setId(id);
    idAnnotationMap.put(id,annotation);
  }

  @Override
  public Annotation next(Annotation annotation, AnnotationType type) {
    return tree.ceiling(annotation,type);
  }

  @Override
  public Annotation previous(Annotation annotation, AnnotationType type) {
    return tree.floor(annotation,type);
  }

  @Override
  public int size() {
    return tree.size();
  }

  @Override
  public Iterator<Annotation> iterator() {
    return tree.iterator();
  }

}// END OF IntervalTreeAnnotationSet
