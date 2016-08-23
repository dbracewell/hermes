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

import com.davidbracewell.Lazy;
import com.davidbracewell.atlas.AdjacencyMatrix;
import com.davidbracewell.atlas.Vertex;
import com.davidbracewell.atlas.algorithms.DijkstraShortestPath;
import com.davidbracewell.atlas.algorithms.ShortestPath;
import com.davidbracewell.atlas.io.GraphViz;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.davidbracewell.collection.map.Maps.map;

/**
 * The type Relation graph.
 * @author David B. Bracewell
 */
public class RelationGraph extends AdjacencyMatrix<Annotation> {
   private static final long serialVersionUID = 1L;
   private volatile transient Lazy<ShortestPath<Annotation>> lazyShortestPath = new Lazy<>(
         () -> new DijkstraShortestPath<>(this));
   private volatile transient Lazy<ShortestPath<Annotation>> lazyUnDirectedShortestPath = new Lazy<>(
         () -> new DijkstraShortestPath<>(this, true));

   /**
    * Render.
    *
    * @param output the output
    * @throws IOException the io exception
    */
   public void render(@NonNull Resource output) throws IOException {
      render(output, GraphViz.Format.PNG);
   }

   /**
    * Render.
    *
    * @param output the output
    * @param format the format
    * @throws IOException the io exception
    */
   public void render(@NonNull Resource output, @NonNull GraphViz.Format format) throws IOException {
      GraphViz<Annotation> graphViz = new GraphViz<>();
      graphViz.setVertexEncoder(v -> new Vertex(v.toString() + "_" + v.getPOS().toString(), Collections.emptyMap()));
      graphViz.setEdgeEncoder(e -> map("label", Cast.<RelationEdge>as(e).getRelation()));
      graphViz.setFormat(format);
      graphViz.render(this, output);
   }

   private static class MatchState {
      private LinkedList<RelationEdge> visited = new LinkedList<>();
      private Set<RelationEdge> open = new HashSet<>();

      /**
       * Instantiates a new Match state.
       *
       * @param open the open
       */
      public MatchState(Set<RelationEdge> open) {
         this(Collections.emptyList(), open);
      }

      /**
       * Instantiates a new Match state.
       *
       * @param visited the visited
       * @param open the open
       */
      public MatchState(List<RelationEdge> visited, Set<RelationEdge> open) {
         this.visited.addAll(visited);
         this.open.addAll(open);
      }

      /**
       * Next list.
       *
       * @param p the p
       * @return the list
       */
      public List<MatchState> next(Predicate<RelationEdge> p) {
         return open.stream()
                    .filter(e -> (visited.isEmpty()
                          || e.getSecondVertex().equals(visited.getLast().getSecondVertex())
                          || e.getFirstVertex().equals(visited.getLast().getSecondVertex())) && p.test(e))
                    .map(edge -> {
                       MatchState prime = new MatchState(visited, open);
                       prime.visited.add(edge);
                       prime.open.remove(edge);
                       return prime;
                    }).collect(Collectors.toList());
      }

   }

   /**
    * Match list.
    *
    * @param predicates the predicates
    * @return the list
    */
   @SafeVarargs
   public final List<RelationEdge> match(Predicate<RelationEdge>... predicates) {
      List<MatchState> matches = new ArrayList<>();
      matches.add(new MatchState(edges()));
      for (Predicate<RelationEdge> predicate : predicates) {
         List<MatchState> prime = matches.stream().flatMap(m -> m.next(predicate).stream())
                                         .collect(Collectors.toList());
         matches.clear();
         matches.addAll(prime);
      }
      matches.forEach(m -> System.out.println(m.visited));
      return matches.get(0).visited;
   }

   /**
    * Instantiates a new Relation graph.
    */
   public RelationGraph() {
      super(new RelationEdgeFactory());
   }

   /**
    * Shortest path list.
    *
    * @param source the source
    * @param target the target
    * @return the list
    */
   public List<RelationEdge> shortestPath(Annotation source, Annotation target) {
      if (source == null || target == null) {
         return null;
      }
      return Cast.as(lazyShortestPath.get().path(source, target));
   }

   /**
    * Shortest connection list.
    *
    * @param source the source
    * @param target the target
    * @return the list
    */
   public List<RelationEdge> shortestConnection(Annotation source, Annotation target) {
      if (source == null || target == null) {
         return null;
      }
      return Cast.as(lazyUnDirectedShortestPath.get().path(source, target));
   }

   /**
    * From relation graph.
    *
    * @param edges the edges
    * @return the relation graph
    */
   public static RelationGraph from(Collection<RelationEdge> edges) {
      RelationGraph gPrime = new RelationGraph();
      edges.forEach(e -> {
         if (!gPrime.containsVertex(e.getFirstVertex())) {
            gPrime.addVertex(e.getFirstVertex());
         }
         if (!gPrime.containsVertex(e.getSecondVertex())) {
            gPrime.addVertex(e.getSecondVertex());
         }
         gPrime.addEdge(e);
      });
      return gPrime;
   }

   /**
    * Filter vertices relation graph.
    *
    * @param vertexPredicate the vertex predicate
    * @return the relation graph
    */
   public RelationGraph filterVertices(@NonNull Predicate<? super Annotation> vertexPredicate) {
      RelationGraph gPrime = new RelationGraph();
      vertices().stream().filter(vertexPredicate).forEach(gPrime::addVertex);
      edges().stream()
             .filter(e -> gPrime.containsVertex(e.getFirstVertex()) && gPrime.containsVertex(e.getSecondVertex()))
             .forEach(gPrime::addEdge);
      return gPrime;
   }

   /**
    * Filter edges relation graph.
    *
    * @param edgePredicate the edge predicate
    * @return the relation graph
    */
   public RelationGraph filterEdges(@NonNull Predicate<RelationEdge> edgePredicate) {
      RelationGraph gPrime = new RelationGraph();
      edges().stream().filter(edgePredicate)
             .forEach(e -> {
                if (!gPrime.containsVertex(e.getFirstVertex())) {
                   gPrime.addVertex(e.getFirstVertex());
                }
                if (!gPrime.containsVertex(e.getSecondVertex())) {
                   gPrime.addVertex(e.getSecondVertex());
                }
                gPrime.addEdge(e);
             });
      return gPrime;
   }

   @Override
   @SuppressWarnings("unchecked")
   public RelationEdge addEdge(Annotation fromVertex, Annotation toVertex) {
      return super.addEdge(fromVertex, toVertex);
   }

   @Override
   @SuppressWarnings("unchecked")
   public RelationEdge addEdge(Annotation fromVertex, Annotation toVertex, double weight) {
      return super.addEdge(fromVertex, toVertex, weight);
   }


   @Override
   @SuppressWarnings("unchecked")
   public RelationEdge getEdge(Annotation v1, Annotation v2) {
      return super.getEdge(v1, v2);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Set<RelationEdge> getInEdges(Annotation vertex) {
      return super.getInEdges(vertex);
   }

   @Override
   @SuppressWarnings("unchecked")
   public RelationEdge removeEdge(Annotation fromVertex, Annotation toVertex) {
      return super.removeEdge(fromVertex, toVertex);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Set<RelationEdge> getOutEdges(Annotation vertex) {
      return super.getOutEdges(vertex);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Set<RelationEdge> getEdges(Annotation vertex) {
      return super.getEdges(vertex);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Set<RelationEdge> edges() {
      return super.edges();
   }

}//END OF RelationGraph
