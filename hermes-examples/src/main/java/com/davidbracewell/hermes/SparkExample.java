package com.davidbracewell.hermes;

import com.davidbracewell.collection.Counter;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.DocumentFormats;
import com.davidbracewell.io.Resources;
import com.davidbracewell.logging.Logger;

import java.io.Serializable;

public class SparkExample implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(SparkExample.class);

  public static void main(String[] args) throws Exception {
    Config.initialize("SparkExample");

    //We will run it local, so we set the spark master to local[*]
    Config.setProperty("spark.master", "local[*]");

    //Build the corpus
    Corpus corpus = Corpus.builder()
      .distributed()
      .format(DocumentFormats.PLAIN_TEXT_OPL)
      //You can substitute the file for one you have. Here I am using a 10,000 sentence corpus fron news articles with
      // one sentence (treated as a document) per line.
      .source(Resources.fromFile("/data/corpora/en/eng_news_2005_10K-text/eng_news_2005_10K-sentences.txt")).build()
      .annotate(Types.TOKEN);

    //Calculate term frequencies for the corpus. Not we are saying we want lemmatized versions, but have not
    //ran the lemma annotator, instead it will just return the lowercase version of the content.
    Counter<String> counts = corpus.termFrequencies(true);
    counts.entries().forEach(entry -> System.out.println(entry.getKey() + " => " + entry.getValue()));

  }

}
