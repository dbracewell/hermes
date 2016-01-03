package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.DocumentFormat;
import com.davidbracewell.hermes.tag.POS;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.StringUtils;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(DocumentFormat.class)
public class POSTrainFormat extends FileBasedFormat {
  private static final long serialVersionUID = 1L;

  private Document processLine(String line, DocumentFactory documentFactory) {
    List<String> tokens = new LinkedList<>();
    List<String> pos = new ArrayList<>();
    String[] parts = line.split("\\s+");
    for (String part : parts) {
      int lpos = part.lastIndexOf('_');
      String w = part.substring(0, lpos);
      String p = part.substring(lpos + 1);
      w = POSCorrection.word(w, p);
      p = POSCorrection.pos(w, p);
      if (!StringUtils.isNullOrBlank(w)) {
        tokens.add(w);
        pos.add(p);
      }
    }
    Document document = documentFactory.fromTokens(tokens);
    for (int i = 0; i < tokens.size(); i++) {
      document.tokenAt(i).put(Attrs.PART_OF_SPEECH, POS.fromString(pos.get(i)));
    }
    document.createAnnotation(Types.SENTENCE, 0, document.length());
    document.getAnnotationSet().setIsCompleted(Types.SENTENCE, true, "PROVIDED");
    document.getAnnotationSet().setIsCompleted(Types.TOKEN, true, "PROVIDED");
    document.getAnnotationSet().setIsCompleted(Types.PART_OF_SPEECH, true, "PROVIDED");
    return document;
  }

  @Override
  public Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
    List<String> lines = resource.readLines();
    List<Document> documents = new ArrayList<>();
    lines.stream()
      .filter(s -> !StringUtils.isNullOrBlank(s))
      .forEach(line -> {
        Document document = processLine(line, documentFactory);
        if (document != null && document.tokenLength() > 0) {
          document.put(Attrs.FILE, resource.descriptor());
          documents.add(document);
        }
      });
    return documents;
  }

  @Override
  public String name() {
    return "POS";
  }

}// END OF POSTrainFormat
