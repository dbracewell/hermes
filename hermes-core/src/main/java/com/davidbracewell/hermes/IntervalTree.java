package com.davidbracewell.hermes;

import com.davidbracewell.conversion.Cast;

import java.io.Serializable;
import java.util.*;

/**
 * @author David B. Bracewell
 */
public class IntervalTree implements Serializable, Collection<Annotation> {
  private static final long serialVersionUID = 1L;

  private Node root = NILL;


  public List<Annotation> overlapping(Span span) {
    if (span == null || root.isNull()) {
      return Collections.emptyList();
    }
    return overlapping(root, span, new ArrayList<>());
  }

  private List<Annotation> overlapping(Node node, Span span, List<Annotation> results) {
    if (node.span.overlaps(span)) {
      results.addAll(node.annotations);
    }
    if (!node.left.isNull() && node.left.max >= span.start()) {
      overlapping(node.left, span, results);
    }
    if (!node.right.isNull() && node.right.min <= span.end()) {
      overlapping(node.right, span, results);
    }
    return results;
  }

  @Override
  public int size() {
    return size(root);
  }

  private int size(Node n) {
    if (n.isNull()) {
      return 0;
    }
    return n.annotations.size() + size(n.left) + size(n.right);
  }

  @Override
  public boolean isEmpty() {
    return root.isNull();
  }

  @Override
  public boolean contains(Object o) {
    if (o instanceof Annotation) {
      Node n = get(Cast.as(o));
      return !n.isNull() && n.annotations.contains(o);
    }
    return false;
  }

  private List<Annotation> toList() {
    if (root.isNull()) {
      return Collections.emptyList();
    }
    return toList(root, new LinkedList<>());
  }

  private List<Annotation> toList(Node node, List<Annotation> list) {
    list.addAll(node.annotations);
    if (!node.left.isNull()) {
      toList(node.left, list);
    }
    if (!node.right.isNull()) {
      toList(node.right, list);
    }
    return list;
  }

  @Override
  public Iterator<Annotation> iterator() {
    return toList().iterator();
  }

  @Override
  public Object[] toArray() {
    return toList().toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return toList().toArray(a);
  }

  @Override
  public boolean add(Annotation annotation) {
    if (annotation != null) {

      if (root.isNull()) {
        root = new Node(annotation);
        return true;
      }
      Node x = treeInsert(annotation);

      if (x.annotations.size() > 1) {
        return true;
      }


      x.color = Node.RED;
      while (!x.isNull() && x != root && x.parent.color == Node.RED) {
        if (x.parent == x.parent.parent.left) {

          Node y = x.parent.parent.right;
          if (y.color == Node.RED) {
            x.parent.color = Node.BLACK;
            y.color = Node.BLACK;
            x.parent.parent.color = Node.RED;
            x = x.parent.parent;
          } else {
            if (x == x.parent.right) {
              x = x.parent;
              rotateLeft(x);
            }
            x.parent.color = Node.BLACK;
            x.parent.parent.color = Node.RED;
            rotateRight(x.parent.parent);
          }

        } else {

          Node y = x.parent.parent.left;
          if (y.color == Node.RED) {
            x.parent.color = Node.BLACK;
            y.color = Node.BLACK;
            x.parent.parent.color = Node.RED;
            x = x.parent.parent;
          } else {
            if (x == x.parent.left) {
              x = x.parent;
              rotateRight(x);
            }
            x.parent.color = Node.BLACK;
            x.parent.parent.color = Node.RED;
            rotateLeft(x.parent.parent);
          }

        }
      }

      root.color = Node.BLACK;
      return true;
    }

    return false;
  }

  private Node treeInsert(Annotation annotation) {
    Node newNode = new Node(annotation);

    if (root.isNull()) {
      root = newNode;
      return root;
    }

    Node iNode = root;
    Node parent = NILL;

    while (!iNode.isNull()) {
      parent = iNode;
      if (annotation.start() == iNode.span.start() && annotation.end() == iNode.span.end()) {
        iNode.annotations.add(annotation);
        return iNode;
      } else if (annotation.start() <= iNode.span.start()) {
        iNode = iNode.left;
      } else {
        iNode = iNode.right;
      }
    }


    newNode.parent = parent;

    if (parent.isNull()) {
      root = newNode;
    } else {
      if (annotation.start() <= parent.span.start()) {
        parent.left = newNode;
      } else {
        parent.right = newNode;
      }
    }

    update(newNode);
    return newNode;
  }

  private void update(Node node) {
    while (!node.isNull()) {
      node.max = Math.max(Math.max(node.left.max, node.right.max), node.span.end());
      node.min = Math.min(Math.min(node.left.min, node.right.min), node.span.start());
      node = node.parent;
    }
  }

  private void rotateLeft(Node x) {
    Node y = x.right;
    x.right = y.left;

    if (!y.left.isNull()) {
      y.left.parent = x;
    }
    y.parent = x.parent;

    if (x == root) {
      root = y;
    } else if (x.parent.left == x) {
      x.parent.left = y;
    } else {
      x.parent.right = y;
    }

    y.left = x;
    x.parent = y;

    update(x);
  }

  private void rotateRight(Node x) {
    Node y = x.left;
    x.left = y.right;

    if (!y.right.isNull()) {
      y.right.parent = x;
    }

    y.parent = x.parent;

    if (x == root) {
      root = y;
    } else if (x.parent.right == x) {
      x.parent.right = y;
    } else {
      x.parent.left = y;
    }

    y.right = x;
    x.parent = y;
    update(x);
  }

  @Override
  public boolean remove(Object o) {
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    if (c == null) {
      return false;
    }
    for (Object o : c) {
      if (!contains(o)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends Annotation> c) {
    if (c == null) {
      return false;
    }
    boolean added = true;
    for (Annotation a : c) {
      if (!add(a)) {
        added = false;
      }
    }
    return added;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    if (c == null) {
      return false;
    }
    boolean removed = true;
    for (Object o : c) {
      if (!this.remove(o)) {
        removed = false;
      }
    }
    return removed;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return false;
  }

  @Override
  public void clear() {
    this.root = NILL;
  }

  private Node get(Span span) {
    if (span == null) {
      return null;
    }
    return get(root, span);
  }

  private Node get(Node n, Span span) {
    while (!n.isNull()) {
      int cmp = span.compareTo(n.span);
      if (cmp < 0) {
        n = n.left;
      } else if (cmp > 0) {
        n = n.right;
      } else {
        return n;
      }
    }
    return NILL;
  }

  public Annotation floor(Annotation annotation, AnnotationType type) {
    if (annotation == null || type == null) {
      return Fragments.detachedEmptyAnnotation();
    }
    Node n = floor(root, annotation);
    if (n.isNull()) {
      return Fragments.detachedEmptyAnnotation();
    }
    return n.annotations.stream().filter(a -> a.isInstance(type)).filter(a -> a != annotation).findFirst().orElse(Fragments.detachedEmptyAnnotation());
  }

  private Node floor(Node n, Span span) {
    if (n.isNull()) {
      return NILL;
    }
    int cmp = span.compareTo(n.span);
    if (cmp == 0) {
      return n;
    }
    if (cmp < 0) {
      return floor(n.left, span);
    }
    Node t = floor(n.right, span);
    if (!t.isNull()) {
      return t;
    }
    return n;
  }

  public Annotation ceiling(Annotation annotation, AnnotationType type) {
    if (annotation == null || type == null) {
      return Fragments.detachedEmptyAnnotation();
    }
    Node n = ceiling(root, annotation);
    if (n.isNull()) {
      return Fragments.detachedEmptyAnnotation();
    }
    return n.annotations.stream().filter(a -> a.isInstance(type)).filter(a -> a != annotation).findFirst().orElse(Fragments.detachedEmptyAnnotation());
  }

  private Node ceiling(Node n, Span span) {
    if (n.isNull()) {
      return NILL;
    }
    int cmp = span.compareTo(n.span);
    if (cmp == 0) {
      return n;
    }
    if (cmp > 0) {
      return ceiling(n.right, span);
    }
    Node t = ceiling(n.left, span);
    if (!t.isNull()) {
      return t;
    }
    return n;
  }

  static Node NILL = new Node();

  static {
    NILL.color = Node.BLACK;
    NILL.parent = NILL;
    NILL.left = NILL;
    NILL.right = NILL;
  }

  static class Node implements Serializable {
    static final boolean RED = true;
    static final boolean BLACK = false;
    private static final long serialVersionUID = 1L;
    Span span;
    final List<Annotation> annotations = new LinkedList<>();
    boolean color = RED;
    Node left = NILL;
    Node right = NILL;
    Node parent = NILL;
    int min;
    int max;

    public Node() {

    }

    public Node(Annotation annotation) {
      this.span = annotation;
      this.annotations.add(annotation);
      this.min = annotation.start();
      this.max = annotation.end();
    }

    public boolean isNull() {
      return this == NILL;
    }

  }

}// END OF IntervalTree
