/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.davidbracewell.hermes.corpus.spi;

import com.davidbracewell.Language;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.conversion.Val;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.DocumentFactory;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.CorpusFormat;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.StructuredFormat;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
@MetaInfServices(CorpusFormat.class)
public class TwitterSearchFormat extends FileBasedFormat {
   private static final long serialVersionUID = 1L;

   @Override
   public Iterable<Document> read(Resource resource, DocumentFactory documentFactory) throws IOException {
      Map<String, ?> file = StructuredFormat.JSON.loads(resource);
      List<Document> documentList = new ArrayList<>();
      List<Map<String, ?>> statuses = Cast.as(Val.of(file.get("statuses")).asList(Map.class));
      statuses.forEach(status -> {
         String id = String.format("%.0f", Val.of(status.get("id")).asDoubleValue());
         String content = Val.of(status.get("text")).asString();
         Language language = Language.fromString(
            Val.of(status.get("metadata")).asMap(String.class, Val.class).get("iso_language_code").asString()
                                                );
         Document document = documentFactory.create(id, content, language);
         documentList.add(document);
         Map<String, Val> user = Val.of(status.get("user")).asMap(String.class, Val.class);
         document.put(Types.AUTHOR, user.get("name").asString());
         if (!Val.of(status.get("in_reply_to_screen_name")).isNull()) {
            document.put(Types.attribute("reply_to"), Val.of(status.get("in_reply_to_screen_name")).asString());
         }
         document.put(Types.attribute("is_retweet"), content.startsWith("RT"));
         document.put(Types.attribute("twitter_user_id"),
                      String.format("%.0f", Val.of(user.get("id")).asDoubleValue()));
         document.put(Types.attribute("followers"), user.get("followers_count").asIntegerValue());
         document.put(Types.attribute("created_at"), Val.of(status.get("created_at")).asString());
      });
      return documentList;
   }

   @Override
   public String name() {
      return "TWITTER_SEARCH";
   }

   @Override
   public String extension() {
      return "json";
   }

}//END OF TwitterSearchFormat
