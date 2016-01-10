package com.davidbracewell.hermes;

import com.davidbracewell.collection.Collect;
import com.davidbracewell.conversion.Cast;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The type Interval tree.
 *
 * @author David B. Bracewell
 */
public class IntervalTree implements Serializable, Collection<Annotation> {
  private static final long serialVersionUID = 1L;
  /**
   * The Nill.
   */
  static Node NILL = new Node();

  static {
    NILL.color = Node.BLACK;
    NILL.parent = NILL;
    NILL.left = NILL;
    NILL.right = NILL;
  }

  private Node root = NILL;
  private int size = 0;

  /**
   * Overlapping list.
   *
   * @param span the span
   * @return the list
   */
  public List<Annotation> overlapping(Span span) {
    if (span == null || root.isNull()) {
      return Collections.emptyList();
    }
    return overlapping(root, span, new ArrayList<>());
  }

  private List<Annotation> overlapping(Node node, Span span, List<Annotation> results) {
    if (node == null || node.isNull()) {
      return results;
    }
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
    return size;
  }

  @Override
  public boolean isEmpty() {
    return root.isNull();
  }

  @Override
  public boolean contains(Object o) {
    if (o instanceof Annotation) {
      Node n = findNode(root, Cast.as(o));
      return !n.isNull() && n.annotations.contains(o);
    }
    return false;
  }

  private List<Annotation> toList() {
    return Lists.newArrayList(iterator());
  }

  @Override
  public Iterator<Annotation> iterator() {
    return new NodeIterator(root);
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
      size++;

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
    if (o instanceof Annotation) {
      Annotation annotation = Cast.as(o);
      Node n = findNode(root, annotation);
      if (n != null && !n.isNull() && n.annotations.contains(annotation)) {
        size--;
        n.annotations.remove(annotation);
        if (n.annotations.isEmpty()) {
          //case 1: Leaf Node
          if (n.left.isNull() && n.right.isNull()) {
            if (n.parent.left == n) {
              n.parent.left = NILL;
            } else {
              n.parent.right = NILL;
            }
          }
        }
        return true;
      }
    }
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
    if (c != null) {
      Collection<Annotation> toRemove = Collect.difference(toList(), Cast.cast(c));
      toRemove.forEach(this::remove);
      return containsAll(c);
    }
    return false;
  }

  @Override
  public void clear() {
    this.root = NILL;
  }

  private Node findNode(Node n, Span span) {
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

  /**
   * Floor annotation.
   *
   * @param annotation the annotation
   * @param type       the type
   * @return the annotation
   */
  public Annotation floor(Annotation annotation, AnnotationType type) {
    if (annotation == null || type == null) {
      return Fragments.detachedEmptyAnnotation();
    }
    for (Annotation a : Collect.asIterable(new NodeIterator(root, -1, annotation.start(), type, false))) {
      if (a.isInstance(type) && a != annotation) {
        return a;
      }
    }
    return Fragments.detachedEmptyAnnotation();
  }


  /**
   * Ceiling annotation.
   *
   * @param annotation the annotation
   * @param type       the type
   * @return the annotation
   */
  public Annotation ceiling(Annotation annotation, AnnotationType type) {
    if (annotation == null || type == null) {
      return Fragments.detachedEmptyAnnotation();
    }
    for (Annotation a : Collect.asIterable(new NodeIterator(root, annotation.end(), Integer.MAX_VALUE, type, true))) {
      if (a.isInstance(type) && a != annotation) {
        return a;
      }
    }
    return Fragments.detachedEmptyAnnotation();
  }

  static class NodeIterator implements Iterator<Annotation> {
    private Deque<Node> stack = new LinkedList<>();
    private Span targetSpan;
    private AnnotationType type = null;
    private boolean goLeft;
    private LinkedList<Annotation> annotations = new LinkedList<>();


    public NodeIterator(Node node) {
      this(node, -1, Integer.MAX_VALUE, null, true);
    }

    public NodeIterator(Node node, int min, int max, AnnotationType type, boolean goLeft) {
      this.type = type;
      this.goLeft = goLeft;
      this.targetSpan = new Span(min, max);
      while (node != null && !node.isNull()) {
        stack.push(node);
        node = goLeft ? node.left : node.right;
      }
    }

    private boolean advance() {
      while (annotations.isEmpty()) {
        if (stack.isEmpty()) {
          return false;
        }
        Node node = stack.pop();
        if (goLeft) {
          if (node.right != null && !node.right.isNull()) {
            Node nr = node.right;
            while (!nr.isNull()) {
              stack.push(nr);
              nr = nr.left;
            }
          }
        } else {
          if (node.left != null && !node.left.isNull()) {
            Node nr = node.left;
            while (!nr.isNull()) {
              stack.push(nr);
              nr = nr.right;
            }
          }
        }


        if (node.span.overlaps(targetSpan)) {
          if (type == null) {
            annotations.addAll(node.annotations);
          } else {
            node.annotations.stream()
              .filter(a -> a.isInstance(type))
              .forEach(annotations::add);
          }
        }
      }

      return annotations.size() > 0;
    }

    @Override
    public boolean hasNext() {
      return advance();
    }

    @Override
    public Annotation next() {
      if (!advance()) {
        throw new NoSuchElementException();
      }
      return annotations.removeFirst();
    }
  }


  /**
   * The type Node.
   */
  static class Node implements Serializable {
    /**
     * The Red.
     */
    static final boolean RED = true;
    /**
     * The Black.
     */
    static final boolean BLACK = false;
    private static final long serialVersionUID = 1L;
    /**
     * The Annotations.
     */
    final List<Annotation> annotations = new LinkedList<>();
    /**
     * The Span.
     */
    Span span;
    /**
     * The Color.
     */
    boolean color = RED;
    /**
     * The Left.
     */
    Node left = NILL;
    /**
     * The Right.
     */
    Node right = NILL;
    /**
     * The Parent.
     */
    Node parent = NILL;
    /**
     * The Min.
     */
    int min;
    /**
     * The Max.
     */
    int max;

    /**
     * Instantiates a new Node.
     */
    public Node() {

    }

    /**
     * Instantiates a new Node.
     *
     * @param annotation the annotation
     */
    public Node(Annotation annotation) {
      this.span = annotation;
      this.annotations.add(annotation);
      this.min = annotation.start();
      this.max = annotation.end();
    }

    /**
     * Is null boolean.
     *
     * @return the boolean
     */
    public boolean isNull() {
      return this == NILL;
    }

  }

}// END OF IntervalTree
