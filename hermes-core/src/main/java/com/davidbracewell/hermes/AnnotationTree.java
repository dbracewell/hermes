/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.davidbracewell.hermes;

import com.clearspring.analytics.util.Lists;
import com.davidbracewell.collection.Collect;
import com.davidbracewell.conversion.Cast;

import java.io.Serializable;
import java.util.*;

/**
 * The type Annotation tree.
 *
 * @author David B. Bracewell
 */
public class AnnotationTree implements Serializable, Collection<Annotation> {
  private static final Node NULL = new Node(null, null, null, new Span(0, Integer.MAX_VALUE));
  private static final long serialVersionUID = 1L;
  private Node root = NULL;
  private int size = 0;

  private boolean isRed(Node node) {
    return !isNull(node) && node.isRed;
  }

  @Override
  public boolean add(Annotation annotation) {
    if (annotation != null) {

      Node z = new Node(annotation);

      //Empty tree, add this and return
      if (isNull(root)) {
        size++;
        root = z;
        root.setBlack();
        return true;
      }

      Node iNode = root;
      Node parent = NULL;
      while (!isNull(iNode)) {
        parent = iNode;
        if (annotation.start() == iNode.span.start() && annotation.end() == iNode.span.end()) {
          iNode.annotations.add(annotation);
          return true;
        } else if (annotation.start() <= iNode.span.start()) {
          iNode = iNode.left;
        } else {
          iNode = iNode.right;
        }
      }

      z.parent = parent;
      size++;
      if (isNull(parent)) {
        root = z;
      } else if (annotation.start() <= parent.span.start()) {
        parent.left = z;
      } else {
        parent.right = z;
      }

      update(z);
      balance(z);
      return true;
    }

    return false;
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

  private void balance(Node z) {
    Node y;

    while (!isNull(z) && z != root && !isNull(z.getParent()) && isRed(z.getParent())) {
      if (z.getParent() == z.getGrandparent().left) {
        y = z.getGrandparent().right;
        if (isRed(y)) {
          z.getParent().setBlack();
          y.setBlack();
          z.getGrandparent().setRed();
          z = z.getGrandparent();
        } else {
          if (z == z.getParent().right) {
            z = z.getParent();
            rotateLeft(z);
          }
          z.getParent().setBlack();
          z.getGrandparent().setBlack();
          rotateRight(z.getGrandparent());
        }
      } else {
        y = z.getGrandparent().left;
        if (isRed(y)) {
          z.getParent().setBlack();
          y.setBlack();
          z.getGrandparent().setRed();
          z = z.getGrandparent();
        } else {
          if (z == z.getParent().left) {
            z = z.getParent();
            rotateRight(z);
          }
          z.getParent().setBlack();
          z.getGrandparent().setRed();
          rotateLeft(z.getGrandparent());
        }
      }
    }
    root.setBlack();
  }

  @Override
  public void clear() {
    this.root = NULL;
  }

  @Override
  public boolean contains(Object o) {
    if (o instanceof Annotation) {
      Node n = findNode(root, Cast.as(o));
      return !n.isNull() && n.annotations.contains(o);
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
    return NULL;
  }

  @Override
  public boolean isEmpty() {
    return root == null || root.isNull();
  }

  private boolean isNull(Node node) {
    return node == null || node.isNull();
  }

  @Override
  public Iterator<Annotation> iterator() {
    return new NodeIterator(root);
  }

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
    if (isNull(node)) {
      return results;
    }
    if (node.span.overlaps(span)) {
      results.addAll(node.annotations);
    }
    if (!isNull(node.left) && node.left.max >= span.start()) {
      overlapping(node.left, span, results);
    }
    if (!isNull(node.right) && node.right.min <= span.end()) {
      overlapping(node.right, span, results);
    }
    return results;
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof Annotation) {
      Annotation annotation = Cast.as(o);
      Node n = findNode(root, annotation);

      //Not in the tree
      if (isNull(n) || !n.annotations.remove(annotation)) {
        return false;
      }
      size--;

      if (n.annotations.size() > 0) {
        return true;
      }

      Node x = NULL;
      Node y = NULL;

      //Leaf Node
      if (isNull(n.left) && isNull(n.left)) {
        if (y.getParent().left == y) {
          y.getParent().left = NULL;
        } else {
          y.getParent().right = NULL;
        }
        return true;
      }

      if (isNull(n.left) || isNull(n.left)) {
        y = n;
      }

      if (!isNull(n.left)) {
        x = y.left;
      } else {
        x = y.right;
      }

      x.parent = y.parent;

      if (isNull(y.parent)) {
        root = x;
      } else if (!isNull(y.getParent().left) && y.getParent().left == y) {
        y.getParent().left = x;
      } else if (!isNull(y.getParent().right) && y.getParent().right == y) {
        y.getParent().right = x;
      }

      update(x);
      update(y);

      return true;
    }
    return false;
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

  private void rotateLeft(Node x) {
    Node y = x.right;
    x.right = y.left;

    if (!isNull(y)) {
      y.left.parent = x;
    }
    y.parent = x.parent;

    if (x == root) {
      root = y;
    } else if (x.getParent().left == x) {
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

    if (!isNull(y)) {
      y.right.parent = x;
    }

    y.parent = x.parent;

    if (x == root) {
      root = y;
    } else if (x.getParent().right == x) {
      x.parent.right = y;
    } else {
      x.parent.left = y;
    }

    y.right = x;
    x.parent = y;
    update(x);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Object[] toArray() {
    return Collect.stream(iterator()).toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return toList().toArray(a);
  }

  private List<Annotation> toList() {
    return Lists.newArrayList(Collect.asIterable(this.iterator()));
  }

  private void update(Node node) {
    while (!node.isNull()) {
      node.max = Math.max(Math.max(node.left.max, node.right.max), node.span.end());
      node.min = Math.min(Math.min(node.left.min, node.right.min), node.span.start());
      node = node.parent;
    }
  }

  private static class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * The Annotations.
     */
    final List<Annotation> annotations = new LinkedList<>();
    /**
     * The Is red.
     */
    boolean isRed;
    /**
     * The Span.
     */
    Span span;
    /**
     * The Left.
     */
    Node left;
    /**
     * The Right.
     */
    Node right;
    /**
     * The Parent.
     */
    Node parent;
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
     *
     * @param left   the left
     * @param right  the right
     * @param parent the parent
     * @param span   the span
     */
    public Node(Node left, Node right, Node parent, Span span) {
      this.left = left;
      this.max = span.start();
      this.min = span.end();
      this.parent = parent;
      this.right = right;
      this.span = span;
      this.isRed = true;
    }

    /**
     * Instantiates a new Node.
     *
     * @param annotation the annotation
     */
    public Node(Annotation annotation) {
      this.left = NULL;
      this.right = NULL;
      this.parent = NULL;
      this.min = annotation.start();
      this.max = annotation.end();
      this.span = annotation;
      this.annotations.add(annotation);
      this.isRed = true;
    }

    /**
     * Gets grandparent.
     *
     * @return the grandparent
     */
    public Node getGrandparent() {
      return parent == null ? NULL : parent.parent == null ? NULL : parent.parent;
    }

    /**
     * Gets parent.
     *
     * @return the parent
     */
    public Node getParent() {
      return parent == null ? NULL : parent;
    }

    /**
     * Is null boolean.
     *
     * @return the boolean
     */
    public boolean isNull() {
      return this == NULL;
    }

    /**
     * Sets black.
     */
    public void setBlack() {
      this.isRed = false;
    }

    /**
     * Sets red.
     */
    public void setRed() {
      this.isRed = true;
    }

    @Override
    public String toString() {
      return "(" + span.start() + ", " + span.end() + ")";
    }

  }

  /**
   * The type Node iterator.
   */
  static class NodeIterator implements Iterator<Annotation> {
    private Deque<Node> stack = new LinkedList<>();
    private Span targetSpan;
    private AnnotationType type = null;
    private boolean goLeft;
    private LinkedList<Annotation> annotations = new LinkedList<>();


    /**
     * Instantiates a new Node iterator.
     *
     * @param node the node
     */
    public NodeIterator(Node node) {
      this(node, -1, Integer.MAX_VALUE, null, true);
    }

    /**
     * Instantiates a new Node iterator.
     *
     * @param node   the node
     * @param min    the min
     * @param max    the max
     * @param type   the type
     * @param goLeft the go left
     */
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

  static {
    NULL.left = NULL;
    NULL.right = NULL;
    NULL.parent = NULL;
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

}//END OF AnnotationTree
