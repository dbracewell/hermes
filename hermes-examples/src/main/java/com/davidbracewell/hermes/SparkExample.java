package com.davidbracewell.hermes;

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.io.Resources;
import com.davidbracewell.logging.Logger;

import java.io.Serializable;
import java.util.Map;

public class SparkExample implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(SparkExample.class);

  public static void main(String[] args) throws Exception {
    Config.initialize("");
    Config.setProperty("TESTING", "IT WORKED");
    Corpus corpus = Corpus.builder()
      .distributed()
      .source(Resources.fromFile("/home/david/test.json_opl")).build();


    corpus.stream().forEach(d -> log.info(Config.get("TESTING").asString("FAIL")));

    Map<String, Double> map = corpus.stream()
      .flatMapToPair(document -> document.count(Types.TOKEN, HString::toLowerCase).entries())
      .reduceByKey((x, y) -> x + y)
      .collectAsMap();

    map.forEach((word, value) -> System.out.println(word + " = " + value));


    corpus.write("XML_OPL", "hdfs://yaguchi:54310/user/david/tmp/");


  }

}
