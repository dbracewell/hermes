package com.davidbracewell.hermes.annotator;

import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.Attribute;
import com.davidbracewell.hermes.attribute.AttributeValueCodec;
import com.davidbracewell.hermes.wordnet.Sense;
import com.davidbracewell.hermes.wordnet.WordNet;
import com.davidbracewell.io.structured.ElementType;
import com.davidbracewell.io.structured.StructuredReader;
import com.davidbracewell.io.structured.StructuredWriter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class SenseCodec implements AttributeValueCodec, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public void encode(StructuredWriter writer, Attribute attribute, Object value) throws IOException {
    List<Sense> senses = Cast.as(value);
    for (Sense sense : senses) {
      writer.writeValue(sense.toString());
    }
  }

  @Override
  public Object decode(StructuredReader reader, Attribute attribute, Object value) throws IOException {
    List<Sense> senses = new ArrayList<>();
    while (reader.peek() != ElementType.END_ARRAY) {
      Sense sense = WordNet.getInstance().getSenseFromID(reader.nextValue().asString());
      if (sense != null) {
        senses.add(sense);
      }
    }
    return senses;
  }

  @Override
  public boolean isArray() {
    return true;
  }

  @Override
  public boolean isObject() {
    return false;
  }

}// END OF SenseCodec
