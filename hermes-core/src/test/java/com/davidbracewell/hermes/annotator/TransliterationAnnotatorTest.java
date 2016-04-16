package com.davidbracewell.hermes.annotator;

import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class TransliterationAnnotatorTest {

  @Test
  public void testAnnotate() throws Exception {
    Config.initializeTest();
    Document document = DocumentProvider.getChineseDocument();
    TransliterationAnnotator annotator = new TransliterationAnnotator("Han-Latin");
    annotator.annotate(document);
    List<Annotation> tokens = document.tokens();
    assertEquals("wǒ", tokens.get(0).get(Types.TRANSLITERATION).get());
    assertEquals("ài", tokens.get(1).get(Types.TRANSLITERATION).get());
    assertEquals("nǐ", tokens.get(2).get(Types.TRANSLITERATION).get());
  }
}