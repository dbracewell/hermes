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
import com.davidbracewell.collection.Sets;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.davidbracewell.collection.map.Maps.map;

/**
 * A graph where vertices are annotations and edges represent relations. This allows for the implicit graph relation of
 * {@link HString} to be visualized and manipulated in a traditional graph framework.
 *
 * @author David B. Bracewell
 */
public class RelationGraph extends AdjacencyMatrix<Annotation> {
   private static final long serialVersionUID = 1L;
   private volatile transient Lazy<ShortestPath<Annotation>> lazyShortestPath =
      new Lazy<>(() -> new DijkstraShortestPath<>(this));
   private volatile transient Lazy<ShortestPath<Annotation>> lazyUnDirectedShortestPath =
      new Lazy<>(() -> new DijkstraShortestPath<>(this, true));

   /**
    * Instantiates a new Relation graph.
    */
   public RelationGraph() {
      super(new RelationEdgeFactory());
   }

   /**
    * Creates a RelationGraph from a collection of edges
    *
    * @param edges the edges
    * @return the relation graph
    */
   public static RelationGraph from(@NonNull Collection<RelationEdge> edges) {
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
   public Set<RelationEdge> edges() {
      return super.edges();
   }

   /**
    * Filters the graph by evaluating the edges using the given predicate. The edges passing the predicate and
    * their incident vertices are used to construct a new filtered RelationGraph.
    *
    * @param edgePredicate the edge predicate
    * @return the relation graph
    */
   public RelationGraph filterByEdge(@NonNull Predicate<RelationEdge> edgePredicate) {
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

   /**
    * Filters the graph by evaluating the edges using the given predicate. The vertices passing the predicate and
    * their incident edges are used to construct a new filtered RelationGraph.
    *
    * @param vertexPredicate the vertex predicate
    * @return the relation graph
    */
   public RelationGraph filterByVertex(@NonNull Predicate<? super Annotation> vertexPredicate) {
      RelationGraph gPrime = new RelationGraph();
      vertices().stream().filter(vertexPredicate).forEach(gPrime::addVertex);
      edges().stream()
             .filter(e -> gPrime.containsVertex(e.getFirstVertex()) && gPrime.containsVertex(e.getSecondVertex()))
             .forEach(gPrime::addEdge);
      return gPrime;
   }

   @Override
   @SuppressWarnings("unchecked")
   public RelationEdge getEdge(Annotation v1, Annotation v2) {
      return super.getEdge(v1, v2);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Set<RelationEdge> getEdges(Annotation vertex) {
      return super.getEdges(vertex);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Set<RelationEdge> getInEdges(Annotation vertex) {
      return super.getInEdges(vertex);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Set<RelationEdge> getOutEdges(Annotation vertex) {
      return super.getOutEdges(vertex);
   }

   @Override
   @SuppressWarnings("unchecked")
   public RelationEdge removeEdge(Annotation fromVertex, Annotation toVertex) {
      return super.removeEdge(fromVertex, toVertex);
   }

   /**
    * Renders the graph using {@link GraphViz} in PNG format to specified output location.
    *
    * @param output the PNG file to render the graph to
    * @throws IOException Something went wrong rendering the graph
    */
   public void render(@NonNull Resource output) throws IOException {
      render(output, GraphViz.Format.PNG);
   }

   /**
    * Renders the graph in the specified format to the specified output location.
    *
    * @param output the location to save the rendered graph.
    * @param format the graphic format to save the rendered graph in
    * @throws IOException Something went wrong rendering the graph
    */
   public void render(@NonNull Resource output, @NonNull GraphViz.Format format) throws IOException {
      GraphViz<Annotation> graphViz = new GraphViz<>();
      graphViz.setVertexEncoder(v -> new Vertex(v.toString() + "_" + v.getPOS().toString(), Collections.emptyMap()));
      graphViz.setEdgeEncoder(e -> map("label", Cast.<RelationEdge>as(e).getRelation()));
      graphViz.setFormat(format);
      graphViz.render(this, output);
   }

   /**
    * Determines the shortest connection (undirected) between the source and target annotation
    *
    * @param source the source annotation
    * @param target the target annotation
    * @return the list of shortest edges between the two annotations or an empty list if there is no path
    */
   public List<RelationEdge> shortestConnection(@NonNull Annotation source, @NonNull Annotation target) {
      return Cast.as(lazyUnDirectedShortestPath.get().path(source, target));
   }

   /**
    * Determines the shortest path (directed) between the source and target annotation
    *
    * @param source the source annotation
    * @param target the target annotation
    * @return the list of shortest edges between the two annotations or an empty list if there is no path
    */
   public List<RelationEdge> shortestPath(@NonNull Annotation source, @NonNull Annotation target) {
      return Cast.as(lazyShortestPath.get().path(source, target));
   }

   /**
    * Gets sub tree nodes.
    *
    * @param node the node
    * @return the sub tree nodes
    */
   public Set<Annotation> getSubTreeNodes(@NonNull Annotation node) {
      return getSubTreeNodes(node, null);
   }

   /**
    * Gets sub tree nodes.
    *
    * @param node           the node
    * @param childRelations the child relations
    * @return the sub tree nodes
    */
   public Set<Annotation> getSubTreeNodes(@NonNull Annotation node, String... childRelations) {
      Set<Annotation> children = new HashSet<>();
      Set<String> targetRel = childRelations == null ? Collections.emptySet() : Sets.asSet(Arrays.asList(childRelations));
      Predicate<RelationEdge> keep = edge -> targetRel.size() == 0 || targetRel.contains(edge.getRelation());

      Queue<RelationEdge> queue = new LinkedList<>(getInEdges(node).stream().filter(keep).collect(Collectors.toList()));
      while (!queue.isEmpty()) {
         RelationEdge n = queue.remove();
         if (!"relcl".equals(n.getRelation()) && !"parataxis".equals(n.getRelation())) {
            children.add(n.getFirstVertex());
            queue.addAll(getInEdges(n.getFirstVertex())
                            .stream()
                            .filter(e -> !children.contains(e.getFirstVertex()))
                            .collect(Collectors.toSet()));
         }
      }
      return children;
   }


   public HString getSubTreeText(Annotation node, boolean includeGiven) {
      Set<Annotation> children = getSubTreeNodes(node);
      if (includeGiven) {
         children.add(node);
      }
      return HString.union(children);
   }

}//END OF RelationGraph
