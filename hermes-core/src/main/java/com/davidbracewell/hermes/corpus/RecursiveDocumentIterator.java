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

package com.davidbracewell.hermes.corpus;

import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Logger;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * The type Recursive document iterator.
 *
 * @author David B. Bracewell
 */
class RecursiveDocumentIterator implements Iterator<Resource> {
  private final static Logger log = Logger.getLogger(RecursiveDocumentIterator.class);
  private final Queue<Resource> queue = Lists.newLinkedList();
  private final Iterator<Resource> resourceIterator;

  /**
   * Instantiates a new Recursive document iterator.
   *
   * @param resource the resource
   */
  public RecursiveDocumentIterator(Resource resource) {
    this.resourceIterator = resource.isDirectory() ? resource.childIterator(true) : Collections.singleton(resource).iterator();
    advance();
  }

  private void advance() {
    while (resourceIterator.hasNext() && queue.isEmpty()) {
      Resource r = resourceIterator.next();
      if (!r.isDirectory()) {
        if (r.asFile() == null || !r.asFile().isHidden()) {
          queue.add(r);
        }
      }
    }
  }

  @Override
  public boolean hasNext() {
    return !queue.isEmpty();
  }

  @Override
  public Resource next() {
    if (queue.isEmpty()) {
      throw new NoSuchElementException();
    }
    Resource returnValue = queue.remove();
    advance();
    return returnValue;
  }

}//END OF DocumentIterator
