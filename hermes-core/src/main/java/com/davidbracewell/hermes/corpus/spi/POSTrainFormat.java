package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.DocumentFormat;
import com.davidbracewell.hermes.tag.POS;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.tuple.Tuple2;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(DocumentFormat.class)
public class POSTrainFormat extends FileBasedFormat {
  private static final long serialVersionUID = 1L;

  @Override
  public Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
    List<String> lines = resource.readLines();
    List<Tuple2<Integer, Integer>> sentences = new LinkedList<>();
    List<String> tokens = new LinkedList<>();
    List<String> pos = new ArrayList<>();
    AtomicInteger start = new AtomicInteger(0);
    AtomicInteger end = new AtomicInteger(0);

    List<Document> documents = new ArrayList<>();
    lines.forEach(line -> {
      line = line.trim();
      if (!StringUtils.isNullOrBlank(line)) {
        String[] parts = line.split("\\s+");
        for (String part : parts) {
          int lpos = part.lastIndexOf('_');
          String w = part.substring(0, lpos);
          String p = part.substring(lpos + 1);

          switch (w) {
//            case "%":
//              p = "SYM";
//              break;
            case "``":
              w = "\"";
              break;
            case "''":
              w = "\"";
              break;
            case "-LRB-":
              w = "(";
              break;
            case "-LSB-":
              w = "[";
              break;
            case "-LCB-":
              w = "{";
              break;
            case "-RRB-":
              w = ")";
              break;
            case "-RCB-":
              w = "}";
              break;
            case "-RSB-":
              w = "]";
              break;

          }
          end.addAndGet(w.length() + 1);
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
