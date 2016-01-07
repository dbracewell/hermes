package com.davidbracewell.hermes;

import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Cast;
import com.google.common.base.Stopwatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class IntervalTree implements Serializable, Collection<Annotation> {
  private static final long serialVersionUID = 1L;

  private Node root = null;

  public static void main(String[] args) {
    Config.initialize("");
    IntervalTree it = new IntervalTree();
    Document doc = DocumentFactory.getInstance().create("This is a simple test.");
    Pipeline.process(doc, Types.TOKEN);
    Annotation sentence = doc.createAnnotation(Types.SENTENCE, 0, doc.length());
    doc.createAnnotation(Types.ENTITY, 0, 4);
    doc.getAllAnnotations().forEach(it::add);
    Stopwatch sw = Stopwatch.createStarted();
    System.out.println(it.overlapping(sentence));
    sw.stop();
    System.out.println(sw);

    sw.reset();
    sw.start();
    System.out.println(sentence.getAllAnnotations());
    sw.stop();
    System.out.println(sw);

    System.out.println(it.overlapping(doc.tokenAt(0)));
  }


  public List<Annotation> overlapping(Span span) {
    if (span == null || root == null) {
      return Collections.emptyList();
    }
    return overlapping(root, span, new ArrayList<>());
  }

  private List<Annotation> overlapping(Node node, Span span, List<Annotation> results) {
    if (node.span.overlaps(span)) {
      results.addAll(node.annotations);
    }
    if (node.left != null && node.left.span.end() > span.start()) {
      overlapping(node.left, span, results);
    }
    if (node.right != null && node.right.span.start() < span.end()) {
      overlapping(node.right, span, results);
    }
    return results;
  }

  @Override
  public int size() {
    return size(root);
  }

  private int size(Node n) {
    if (n == null) {
      return 0;
    }
    return n.annotations.size() + size(n.left) + size(n.right);
  }

  @Override
  public boolean isEmpty() {
    return root == null;
  }

  @Override
  public boolean contains(Object o) {
    if (o instanceof Annotation) {
      Node n = get(Cast.as(o));
      return n != null && n.annotations.contains(o);
    }
    return false;
  }

  private List<Annotation> toList() {
    if (root == null) {
      return Collections.emptyList();
    }
    return toList(root, new ArrayList<>());
  }

  private List<Annotation> toList(Node node, List<Annotation> list) {
    list.addAll(node.annotations);
    if (node.left != null) {
      toList(node.left, list);
    }
    if (node.right != null) {
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
      Node x = treeInsert(annotation);

      if (x.color != Node.RED) {
        x.color = Node.RED;
        while (x != root && x.parent.color == Node.RED) {

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
      }
      return true;
    }
    return false;
  }

  private Node treeInsert(Annotation annotation) {
    if (root == null) {
      root = new Node(annotation);
      return root;
    }

    Node iNode = root;
    Node parent = null;

    while (iNode != null) {
      parent = iNode;
      int cmp = annotation.compareTo(iNode.span);
      if (cmp < 0) {
        iNode = iNode.left;
      } else if (cmp > 0) {
        iNode = iNode.right;
      } else {
        iNode.annotations.add(annotation);
        return iNode;
      }
    }

    Node newNode = new Node(annotation);
    newNode.parent = parent;

    int cmp = annotation.compareTo(parent.span);
    if (cmp < 0) {
      parent.left = newNode;
    } else {
      parent.right = newNode;
    }

    return newNode;
  }

  private void rotateLeft(Node x) {
    Node y = x.right;
    x.right = y.left;

    if (y.left != null) {
      y.left.parent = x;
    }
    y.parent = x.parent;

    if (x.parent == null) {
      this.root = y;
    } else {
      if (x.parent.left == x) {
        x.parent.left = y;
      } else {
        x.parent.right = y;
      }
    }

    y.left = x;
    x.parent = y;

  }

  private void rotateRight(Node x) {
    Node y = x.left;
    x.left = y.right;

    if (y.right != null) {
      y.right.parent = x;
    }

    y.parent = x.parent;

    if (x.parent == null) {
      root = y;
    } else {
      if (x.parent.right == x) {
        x.parent.right = y;
      } else {
        x.parent.left = y;
      }
    }

    y.right = x;
    x.parent = y;
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
    this.root = null;
  }

  private Node get(Span span) {
    if (span == null) {
      return null;
    }
    return get(root, span);
  }

  private Node get(Node n, Span span) {
    while (n != null) {
      int cmp = span.compareTo(n.span);
      if (cmp < 0) {
        n = n.left;
      } else if (cmp > 0) {
        n = n.right;
      } else {
        return n;
      }
    }
    return null;
  }

  static class Node implements Serializable {
    static final boolean RED = true;
    static final boolean BLACK = false;
    private static final long serialVersionUID = 1L;
    final Span span;
    final List<Annotation> annotations = new LinkedList<>();
    boolean color = RED;
    Node left = null;
    Node right = null;
    Node parent = null;

    public Node(Annotation annotation) {
      this.span = annotation;
      this.annotations.add(annotation);
    }


    public Node grandParent() {
      if (parent == null) {
        return null;
      }
      return parent.parent;
    }

  }


}// END OF IntervalTree
