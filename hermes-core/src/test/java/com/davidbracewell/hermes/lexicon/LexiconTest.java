package com.davidbracewell.hermes.lexicon;

import com.davidbracewell.hermes.Fragments;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.tag.Entities;
import com.davidbracewell.hermes.tag.StringTag;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author David B. Bracewell
 */
public class LexiconTest {


  @Test
  public void test() {
    Lexicon lexicon = new TrieLexicon(true, true, Types.TAG);
    lexicon.add("test"); //Add an entry with no tag and no probability
    lexicon.add("testing", 0.8, new StringTag("TEST"));
    lexicon.add("bark", 0.8);
    lexicon.add("barking", new StringTag("TEST"));


    //Items in the lexicon
    assertTrue(lexicon.test(Fragments.string("test")));
    assertTrue(lexicon.test(Fragments.string("testing")));
    assertTrue(lexicon.test(Fragments.string("bark")));
    assertTrue(lexicon.test(Fragments.string("barking")));

    //Items not in the lexicon
    assertFalse(lexicon.test(Fragments.string("BARK")));
    assertFalse(lexicon.test(Fragments.string("missing")));

    //Tags
    assertEquals(new StringTag("TEST"), lexicon.getTag("testing").get());
    assertEquals(new StringTag("TEST"), lexicon.getTag("barking").get());

    //No Tags
    assertFalse(lexicon.getTag("test").isPresent());
    assertFalse(lexicon.getTag("bark").isPresent());
    assertFalse(lexicon.getTag("missing").isPresent());

    //Words that exist
    assertEquals(1.0d, lexicon.getProbability("test"), 0d);
    assertEquals(0.8d, lexicon.getProbability("testing"), 0d);
    assertEquals(0.8d, lexicon.getProbability("testing", new StringTag("TEST")), 0d);
    assertEquals(0.8d, lexicon.getProbability("bark"), 0d);
    assertEquals(1.0d, lexicon.getProbability("barking"), 0d);

    //Words that are missing
    assertEquals(0d, lexicon.getProbability("missing"), 0d);
    assertEquals(0d, lexicon.getProbability("test", Entities.DATE), 0d);


  }


}