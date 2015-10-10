package com.davidbracewell.hermes;

import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.hermes.corpus.SparkCorpus;
import com.davidbracewell.io.Resources;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by david on 10/9/15.
 */
public class SparkExample implements Serializable {

  public static void main(String[] args) throws Exception {
    Corpus.from("SPARK", Resources.from("/home/david/test.json_opl"))
      .annotate(Types.TOKEN)
      .forEach((Serializable & Consumer<? super Document>) (document -> System.out.println(document.tokens())));
  }

}
