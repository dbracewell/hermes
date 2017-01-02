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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A specialized annotation graph edge that stores relation type and value.
 *
 * @author David B. Bracewell
 */
@EqualsAndHashCode(callSuper = true)
@ToString(includeFieldNames = true)
public class RelationEdge extends Edge<Annotation> {
   private static final long serialVersionUID = 1L;
   @Getter
   @Setter
   private String relation;
   @Getter
   @Setter
   private RelationType relationType;

   /**
    * Instantiates a new Relation edge.
    *
    * @param source the source vertex
    * @param target the target vertex
    */
   public RelationEdge(Annotation source, Annotation target) {
      super(source, target);
   }

   @Override
   public boolean isDirected() {
      return true;
   }

}//END OF RelationEdge
