package com.davidbracewell.hermes;

import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusFormats;
import com.davidbracewell.hermes.corpus.TermSpec;
import com.davidbracewell.io.Resources;

import java.io.Serializable;

public class SparkExample implements Serializable {
  private static final long serialVersionUID = 1L;

  public static void main(String[] args) throws Exception {
    Config.initialize("SparkExample");

    //Need to add the spark core jar file to the classpath for this to run
    //We will run it local, so we set the spark master to local[*]
    Config.setProperty("spark.master", "local[*]");

    //Build the corpus
    Corpus corpus = Corpus.builder()
      .distributed()
      .format(CorpusFormats.PLAIN_TEXT_OPL)
      //You can substitute the file for one you have. Here I am using a 1,000,000 sentence corpus from news articles with
      // one sentence (treated as a document) per line.
      .source(Resources.fromFile("/shared/data/corpora/en/Frequency/eng_news_2005_1M-sentences.txt")).build()
      .repartition(100)
      .annotate(Types.TOKEN);



    //Calculate term frequencies for the corpus. Note we are saying we want lemmatized versions, but have not
    //run the lemma annotator, instead it will just return the lowercase version of the content.
    Counter<String> counts = corpus.terms(TermSpec.create().lemmatize());
    counts.entries().forEach(entry -> System.out.println(entry.getKey() + " => " + entry.getValue()));
  }

}
