package com.davidbracewell.hermes.preprocessing;

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public class TraditionalToSimplifiedTest {

  @Test
  public void testPerformNormalization() throws Exception {
    Config.initializeTest();
    Document document = DocumentFactory.builder()
      .add(new TraditionalToSimplified())
      .defaultLanguage(Language.CHINESE)
      .build()
      .create("電腦是新的。");
    assertEquals("电脑是新的。", document.toString());
  }
}