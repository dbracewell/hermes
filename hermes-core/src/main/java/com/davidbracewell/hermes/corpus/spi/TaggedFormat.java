package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.collection.map.Maps;
import com.davidbracewell.config.Config;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.AnnotationType;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.resource.Resource;
import lombok.NonNull;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(CorpusFormat.class)
public class TaggedFormat extends FileBasedFormat {
   public static final String TYPE_PROPERTY = "TaggedFormat.type";
   private static final Pattern TAG_PATTERN = Pattern.compile("<([a-z_]+)>([^<>]+)</\\1>", Pattern.CASE_INSENSITIVE);

   public static void setAnnotationType(@NonNull AnnotationType type) {
      Config.setProperty(TYPE_PROPERTY, type.canonicalName());
   }

   @Override
   public boolean isOnePerLine() {
      return true;
   }

   @Override
   public String name() {
      return "TAGGED";
   }

   @Override
   public Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
      String content = resource.readToString().trim();
      final AnnotationType annotationType = Config.get(TYPE_PROPERTY).as(AnnotationType.class, Types.ENTITY);
      int last = 0;
      List<Integer> startPositions = new ArrayList<>();
      List<Integer> endPositions = new ArrayList<>();
      List<String> types = new ArrayList<>();
      Matcher matcher = TAG_PATTERN.matcher(content);
      StringBuilder builder = new StringBuilder();
      while (matcher.find()) {
         if (matcher.start() != last) {
            builder.append(content.substring(last, matcher.start()));
         }
         last = matcher.end();
         startPositions.add(builder.length());
         endPositions.add(builder.length() + matcher.group(2).length());
         types.add(matcher.group(1));
         builder.append(matcher.group(2));
      }
      if (last != content.length()) {
         builder.append(content.substring(last, content.length()));
      }
      Document document = documentFactory.createRaw(builder.toString());
      for (int i = 0; i < startPositions.size(); i++) {
         document.createAnnotation(annotationType,
                                   startPositions.get(i),
                                   endPositions.get(i),
                                   Maps.map(annotationType.getTagAttribute(),
                                            Val.of(types.get(i))
                                               .as(annotationType.getTagAttribute().getValueType().getType()))
                                  );
      }
      return Collections.singleton(document);
   }

   @Override
   public String toString(Document document) {
      final AnnotationType annotationType = Config.get(TYPE_PROPERTY).as(AnnotationType.class, Types.ENTITY);
      return document.tag(annotationType,"UNKNOWN");
   }
}// END OF TaggedFormat
