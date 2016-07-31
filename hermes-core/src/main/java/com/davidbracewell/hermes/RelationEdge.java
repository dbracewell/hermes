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

import com.davidbracewell.atlas.Edge;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author David B. Bracewell
 */
public class RelationEdge extends Edge<Annotation> {
  private static final long serialVersionUID = 1L;
  @Getter
  @Setter
  public String relation;
  @Getter
  @Setter
  public RelationType relationType;

  public RelationEdge(Annotation vertex1, Annotation vertex2) {
    super(vertex1, vertex2);
  }

  @Override
  public boolean isDirected() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RelationEdge)) return false;
    RelationEdge edge = (RelationEdge) o;
    return Objects.equals(relation, edge.relation) &&
      Objects.equals(relationType, edge.relationType) &&
      Objects.equals(getFirstVertex(), edge.getFirstVertex()) &&
      Objects.equals(getSecondVertex(), edge.getSecondVertex());
  }

  @Override
  public int hashCode() {
    return Objects.hash(relation, relationType, getFirstVertex(), getSecondVertex());
  }

  @Override
  public String toString() {
    return "RelationEdge{" +
      "source='" + getFirstVertex() + "', " +
      "target='" + getSecondVertex() + "', " +
      "relation='" + relation + '\'' +
      ", relationType=" + relationType +
      '}';
  }
}//END OF RelationEdge
