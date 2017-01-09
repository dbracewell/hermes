package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.SystemInfo;
import com.davidbracewell.guava.common.base.Preconditions;
import com.davidbracewell.hermes.*;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.StringUtils;
import lombok.NonNull;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(CorpusFormat.class)
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
      boolean complete = false;
      for (int i = 0; i < tokens.size(); i++) {
         POS p = POS.fromString(pos.get(i));
         if (p != null && !p.isTag(POS.ANY)) {
            complete = true;
            Preconditions.checkArgument(!p.isPhraseTag(), p.asString());
            document.tokenAt(i).put(Types.PART_OF_SPEECH, p);
         }
      }
      document.createAnnotation(Types.SENTENCE, 0, document.length());
      document.getAnnotationSet().setIsCompleted(Types.SENTENCE, true, "PROVIDED");
      document.getAnnotationSet().setIsCompleted(Types.TOKEN, true, "PROVIDED");
      document.getAnnotationSet().setIsCompleted(Types.PART_OF_SPEECH, complete, "PROVIDED");
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
                 document.put(Types.FILE, resource.descriptor());
                 documents.add(document);
              }
           });
      return documents;
   }

   @Override
   public String name() {
      return "POS";
   }

//  @Override
//  public void write(@NonNull Resource resource, @NonNull Document document) throws IOException {
//    if( document.getAnnotationSet().isCompleted(Types.PART_OF_SPEECH)) {
//      try (BufferedWriter writer = new BufferedWriter(resource.writer())) {
//        for (Annotation sentence : document.sentences()) {
//          writer.write(sentence.toPOSString('_'));
//          writer.write(SystemInfo.LINE_SEPARATOR);
//        }
//      }
//    }
//  }

   @Override
   public String toString(@NonNull Document document) {
      StringBuilder builder = new StringBuilder();
      if (document.getAnnotationSet().isCompleted(Types.PART_OF_SPEECH)) {
         for (Annotation sentence : document.sentences()) {
            builder.append(sentence.toPOSString('_'));
            builder.append(SystemInfo.LINE_SEPARATOR);
         }
      }
      return builder.toString();
   }

   @Override
   public boolean isOnePerLine() {
      return true;
   }
}// END OF POSTrainFormat
