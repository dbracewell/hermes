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

import com.davidbracewell.function.SerializableFunction;
import com.davidbracewell.function.SerializablePredicate;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.stream.MStream;
import com.google.common.collect.Iterators;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @author David B. Bracewell
 */
class UnionCorpus implements Corpus, Serializable {
   private static final long serialVersionUID = 1L;
   private final Corpus c1;
   private final Corpus c2;

   UnionCorpus(Corpus c1, Corpus c2) {
      this.c1 = c1;
      this.c2 = c2;
   }

   @Override
   public Corpus annotate(AnnotatableType... types) {
      return new UnionCorpus(c1.annotate(types), c2.annotate(types));
   }

   @Override
   public void close() throws Exception {

   }

   @Override
   public DocumentFactory getDocumentFactory() {
      return c1.getDocumentFactory();
   }

   @Override
   public MStream<Document> stream() {
      return c1.stream().union(c2.stream());
   }

   @Override
   public CorpusType getCorpusType() {
      return CorpusType.STREAM;
   }

   @Override
   public Corpus cache() {
      return new UnionCorpus(c1.cache(), c2.cache());
   }

   @Override
   public Corpus filter(@NonNull SerializablePredicate<? super Document> filter) {
      return new UnionCorpus(c1.filter(filter), c2.filter(filter));
   }

   @Override
   public long size() {
      return c1.size() + c2.size();
   }

   @Override
   public boolean isEmpty() {
      return c1.isEmpty() && c2.isEmpty();
   }

   @Override
   public Iterator<Document> iterator() {
      return Iterators.concat(c1.iterator(), c2.iterator());
   }

   @Override
   public Corpus map(@NonNull SerializableFunction<Document, Document> function) {
      return new MStreamCorpus(stream().map(function), DocumentFactory.getInstance());
   }
}//END OF UnionCorpus
