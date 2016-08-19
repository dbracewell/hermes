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

import com.davidbracewell.apollo.ml.clustering.topic.GibbsLDA;
import com.davidbracewell.apollo.ml.clustering.topic.LDAModel;
import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusFormats;
import com.davidbracewell.hermes.corpus.NGramSpec;
import com.davidbracewell.hermes.corpus.TermSpec;
import com.davidbracewell.hermes.ml.feature.BagOfAnnotations;
import com.davidbracewell.io.Resources;
import com.davidbracewell.tuple.Tuple;

/**
 * @author David B. Bracewell
 */
public class CorpusExample {

  public static void main(String[] args) throws Exception {
    //Initializes configuration settings
    Hermes.initializeApplication(args);

    //A corpus is a collection of documents which we want to do some type of analysis on. Corpora in Hermes act like
    //a stream or iterator of documents. Corpora in hermes can be in memory (all documents are loaded into memory),
    //on disk (documents are streamed from disk as needed), or distributed (documents are loaded into Apache Spark for
    //distributed processing).
    //Corpora are created using a CorpusBuilder
    Corpus corpus = Corpus.builder()

      //.distributed(100) <= Creates a distributed corpus using 100 partitions (Apache Spark)
      //.offHeap() <= Creates a disk-based corpus
      .inMemory() //Our corpus is small, so we will load it into memory

      //Corpora may have their documents store in various formats, e.g. plain text, Conll, json, xml, etc. These formats
      //are defined as a DocumentFormat. Formats have a unique name (defined as String) such as TEXT for plain text.
      //In addition, if a format is appended with "_opl" it is treated as one per line, so TEXT_OPL would mean that
      //documents are defined as plain text with one document per line.
      //The source method will load documents in the given format from the given resource (by default the Hermes json
      //format is assumed.)
      .source(CorpusFormats.PLAIN_TEXT_OPL, Resources.fromClasspath("com/davidbracewell/hermes/example_docs.txt"))

      //Documents may also be specified via add and addAll methods. This is useful for building in memory corpora.
      //.add(Document.create("Document 1"))
      //.addAll(Arrays.asList(
      //  Document.create("Document 2"),
      //  Document.create("Document 3"),
      //  Document.create("Document 4")
      //))

      //Finally, like all other Java builders, we construct the corpus with the build command
      .build();


    corpus.query("[LANGUAGE]:ENGLISH").forEach(System.out::println);

    //Now that we have corpus, we can do some analysis.

    //First we might want to add some annotations.
    //Annotation of corpora is done using multiple threads for in memory and on disk corpora
    //Since we are modifying the corpus we should capture the returned corpus as on disk and distributed corpora
    //would require.
    corpus = corpus.annotate(Types.TOKEN, Types.SENTENCE, Types.LEMMA);

    //One thing we might want to do is get term frequencies. We can define our "term" to be any annotation and can
    //provide a function to convert to string form (for example if you want to lower or upper case).
    //Here we will use a convenience method which will use the standard definition of term (i.e. a token) and
    //we will tell it to use the lemma version of the tokens. Since we annotated lemmas, we will actually get lemmas.
    //If we had not annotated with lemmas, it would simply lowercase the tokens.
    Counter<String> termFrequencies = corpus.terms(TermSpec.create().lemmatize());

    //Lets print out the top 10 terms
    System.out.println("Top 10 by Term Frequency");
    termFrequencies.topN(10)
      .itemsByCount(false)
      .forEach(term -> System.out.println(term + ": " + termFrequencies.get(term)));
    System.out.println();

    //We can also calculate the document frequency
    //For this corpus there will not be much difference.
    Counter<String> docFrequencies = corpus.documentFrequencies(true);

    //Lets print out the top 10 terms
    System.out.println("Top 10 by Document Frequency");
    docFrequencies.topN(10)
      .itemsByCount(false)
      .forEach(term -> System.out.println(term + ": " + termFrequencies.get(term)));
    System.out.println();


    //Another thing we might want to do is extract all the bigrams in the corpus.
    //We will ignore bigrams with a stop word (e.g. the, it, of, etc.) and lowercase the output
    Counter<Tuple> bigrams = corpus.ngrams(NGramSpec.create().order(2).ignoreStopWords().lowerCase());

    //Lets print out the top 10 bigrams
    System.out.println("Top 10 Bigrams");
    bigrams.topN(10)
      .itemsByCount(false)
      .forEach(tuple -> System.out.println(tuple + ": " + bigrams.get(tuple)));
    System.out.println();

    //We can do other things on corpora like query it using simple boolean logic
    System.out.println(corpus.query("younger AND (brother OR sister)").size() + " documents match (younger AND (brother OR sister))");
    System.out.println();

    //We can take a sample of documents
    System.out.println(corpus.sample(1).iterator().next());
    System.out.println();

    //Filter documents (in this case documents that have more than four tokens (words)
    System.out.println(corpus.filter(d -> d.tokenLength() > 4).size() + " documents have more than four words.");

    System.out.println();
    System.out.println();
    //Finally, we can build a topic model
    //Lets construct an Gibbs sampled LDA with 2 topics
    GibbsLDA lda = new GibbsLDA();
    lda.setK(2);
    lda.setVerbose(false); //Turn off the messages building the model

    //We can train the topic on corpus by converting it into a classification data set with BagOfAnnotation features.
    //In this case our annotations will be tokens, we will lower case them, accept only non-stop words, and
    //since lda doesn't care about word count we will make the features binary (1 if word is present, 0 if not)
    LDAModel model = lda.train(
      corpus.asClassificationDataSet(
        BagOfAnnotations.builder().ignoreStopWords().lowerCase().build()
      )
    );

    for (int i = 0; i < model.size(); i++) {
      System.out.println("Topic " + i);
      Counter<String> topicWords = model.getTopicWords(i);
      topicWords.topN(10).itemsByCount(false).forEach(word -> System.out.println(word + ": " + topicWords.get(word)));
      System.out.println();
    }

    //Results might not be interesting on this dataset, but you get the idea!

  }

}//END OF CorpusExample
