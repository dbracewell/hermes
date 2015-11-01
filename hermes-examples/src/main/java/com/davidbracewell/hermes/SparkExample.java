package com.davidbracewell.hermes;

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.io.Resources;

import java.io.Serializable;

public class SparkExample implements Serializable {
  private static final long serialVersionUID = 1L;

  public static void main(String[] args) throws Exception {
    Config.initialize("");
    Config.setProperty("TESTING", "IT WORKED");
    Corpus corpus = Corpus.builder()
      .distributed()
      .source(Resources.fromFile("/home/david/test.json_opl")).build();

    corpus.stream().forEach(d -> System.out.println(Config.get("hermes.DefaultLanguage").asString("DID NOT WORK")));


  }

}
