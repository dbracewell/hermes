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

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class CorpusBuilder {

  private boolean isDistributed = false;
  private int partitions = -1;
  private boolean isInMemory = false;
  private Resource resource = null;
  private List<Document> documents = new LinkedList<>();
  private DocumentFactory documentFactory = DocumentFactory.getInstance();
  private CorpusFormat corpusFormat = CorpusFormats.forName(CorpusFormats.JSON_OPL);


  public CorpusBuilder inMemory() {
    this.isDistributed = false;
    this.isInMemory = true;
    return this;
  }

  public CorpusBuilder offHeap() {
    this.isDistributed = false;
    this.isInMemory = false;
    return this;
  }

  public CorpusBuilder distributed() {
    this.isDistributed = true;
    this.isInMemory = false;
    return this;
  }

  public CorpusBuilder distributed(int numPartitions) {
    this.partitions = numPartitions;
    this.isDistributed = true;
    this.isInMemory = false;
    return this;
  }

  public CorpusBuilder format(@NonNull String format) {
    this.corpusFormat = CorpusFormats.forName(format);
    return this;
  }

  public CorpusBuilder format(@NonNull CorpusFormat format) {
    this.corpusFormat = format;
    return this;
  }

  public CorpusBuilder add(@NonNull Document document) {
    documents.add(document);
    return this;
  }

  public CorpusBuilder addAll(@NonNull Collection<Document> documentCollection) {
    documents.addAll(documentCollection);
    return this;
  }

  public CorpusBuilder source(@NonNull Resource resource) {
    this.resource = resource;
    return this;
  }

  public CorpusBuilder source(@NonNull String format, @NonNull Resource resource) {
    this.resource = resource;
    return format(format);
  }

  public CorpusBuilder from(@NonNull String format, @NonNull Resource resource, @NonNull DocumentFactory documentFactory) {
    this.resource = resource;
    this.documentFactory = documentFactory;
    return format(format);
  }

  public Corpus build() {

    if (isInMemory) {
      List<Document> dList = new LinkedList<>(documents);
      if (resource != null) {
        dList.addAll(new FileCorpus(corpusFormat, resource, documentFactory).stream().collect());
      }
      return new InMemoryCorpus(dList);
    }

    if (isDistributed) {
      Corpus corpus = null;
      if (resource != null) {
        corpus = new SparkCorpus(resource.descriptor(), corpusFormat, documentFactory);
        if (partitions > 0) {
          Cast.<SparkCorpus>as(corpus).repartition(partitions);
        }
      }

      if (corpus == null) {
        corpus = new SparkCorpus(documents);
        if (partitions > 0) {
          Cast.<SparkCorpus>as(corpus).repartition(partitions);
        }
      } else if (documents.size() > 0) {
        corpus = corpus.union(new InMemoryCorpus(documents));
      }

      return corpus;
    }


    if (resource == null && documents.size() > 0) {
      return new InMemoryCorpus(documents);
    }

    Corpus corpus = new FileCorpus(corpusFormat, resource, documentFactory);
    if (documents.size() > 0) {
      corpus.union(new InMemoryCorpus(documents));
    }

    return corpus;
  }


}//END OF CorpusBuilder
