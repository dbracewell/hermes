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

import com.davidbracewell.collection.Collect;
import com.davidbracewell.collection.IntervalTree;
import com.davidbracewell.collection.Span;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * <p>Annotation tree using a Red-Black backed Interval tree.</p>
 *
 * @author David B. Bracewell
 */
public class AnnotationTree extends IntervalTree<Annotation> {
   private static final long serialVersionUID = 1L;

   /**
    * The type Node iterator.
    */
   static class NodeIterator implements Iterator<Annotation> {
      private Deque<Node<Annotation>> stack = new LinkedList<>();
      private Span targetSpan;
      private AnnotationType type = null;
      private boolean goLeft;
      private LinkedList<Annotation> annotations = new LinkedList<>();


      /**
       * Instantiates a new Node iterator.
       *
       * @param node the node
       */
      public NodeIterator(Node<Annotation> node) {
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
      public NodeIterator(Node<Annotation> node, int min, int max, AnnotationType type, boolean goLeft) {
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
            Node<Annotation> node = stack.pop();
            if (goLeft) {
               if (node.right != null && !node.right.isNull()) {
                  Node<Annotation> nr = node.right;
                  while (!nr.isNull()) {
                     stack.push(nr);
                     nr = nr.left;
                  }
               }
            } else {
               if (node.left != null && !node.left.isNull()) {
                  Node<Annotation> nr = node.left;
                  while (!nr.isNull()) {
                     stack.push(nr);
                     nr = nr.right;
                  }
               }
            }


            if (node.span.overlaps(targetSpan)) {
               if (type == null) {
                  annotations.addAll(node.items);
               } else {
                  node.items.stream()
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
