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

package com.davidbracewell.hermes.annotator;

import com.davidbracewell.Language;
import com.davidbracewell.SystemInfo;
import com.davidbracewell.config.Config;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.guava.common.collect.ImmutableSet;
import com.davidbracewell.hermes.AnnotatableType;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Relation;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.FileResource;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.logging.Loggable;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentMaltParserService;
import org.maltparser.concurrent.graph.ConcurrentDependencyEdge;
import org.maltparser.concurrent.graph.ConcurrentDependencyGraph;
import org.maltparser.concurrent.graph.ConcurrentDependencyNode;
import org.maltparser.core.exception.MaltChainedException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author David B. Bracewell
 */
public class DefaultDependencyAnnotator extends SentenceLevelAnnotator implements Loggable {
   private static final long serialVersionUID = 1L;
   private static volatile Map<Language, ConcurrentMaltParserModel> models = new ConcurrentHashMap<>();

   @Override
   public void annotate(Annotation sentence) {
      ConcurrentMaltParserModel model = getModel(sentence.getLanguage());
      List<Annotation> tokens = sentence.tokens();
      String[] input = new String[tokens.size()];
      for (int i = 0; i < tokens.size(); i++) {
         Annotation token = tokens.get(i);
         input[i] = i + "\t" + token.toString() + "\t" + token.getLemma() + "\t" + token.getPOS()
                                                                                        .asString() + "\t" + token.getPOS()
                                                                                                                  .asString() + "\t-";
      }
      try {
         ConcurrentDependencyGraph graph = model.parse(input);
         for (int i = 1; i <= graph.nTokenNodes(); i++) {
            ConcurrentDependencyNode node = graph.getTokenNode(i);
            ConcurrentDependencyEdge edge = node.getHeadEdge();

            Annotation child = tokens.get(node.getIndex() - 1);
            if (edge.getSource().getIndex() != 0) {
               Annotation parent = tokens.get(edge.getSource().getIndex() - 1);
               child.add(new Relation(Types.DEPENDENCY, edge.getLabel("DEPREL"), parent.getId()));
            }
         }
      } catch (MaltChainedException e) {
         throw Throwables.propagate(e);
      }
   }

   @Override
   protected Set<AnnotatableType> furtherRequires() {
      return ImmutableSet.of(Types.PART_OF_SPEECH, Types.LEMMA);
   }

   private ConcurrentMaltParserModel getModel(Language language) {
      if (!models.containsKey(language)) {
         synchronized (this) {
            if (!models.containsKey(language)) {
               String langCode = language.getCode().toLowerCase();
               Resource modelDir = Config.get("models.dir").asResource(Resources.from(SystemInfo.USER_HOME));
               Resource classpathDir = Resources.fromClasspath("hermes/models/");
               String configProperty = "Relation.DEPENDENCY";
               String modelName = "dependency.mco";
               Exception thrownException = null;

               for (Resource r : new Resource[]{
                  Config.get("", language, "model").asResource(),
                  classpathDir.getChild(langCode).getChild(modelName),
                  modelDir.getChild(langCode).getChild(modelName),
                  Config.get(configProperty, "model").asResource(),
                  classpathDir.getChild(modelName),
                  modelDir.getChild(modelName)
               }) {
                  if (r != null && r.exists()) {
                     if (!(r instanceof FileResource)) {
                        Resource tmpLocation = Resources.temporaryFile();
                        tmpLocation.deleteOnExit();
                        try {
                           logFine("Writing dependency model to temporary file [{0}].", tmpLocation);
                           tmpLocation.write(r.readBytes());
                           r = tmpLocation;
                        } catch (IOException e) {
                           //no opt
                        }
                     }
                     if (r instanceof FileResource) {
                        try {
                           models.put(language,
                                      ConcurrentMaltParserService.initializeParserModel(r.asURL().get()));
                           return models.get(language);
                        } catch (Exception e) {
                           thrownException = e;
                        }
                     }
                  }
               }
               if (thrownException == null) {
                  throw new RuntimeException(modelName + " does not exist");
               } else {
                  throw Throwables.propagate(thrownException);
               }
            }
         }
      }
      return models.get(language);
   }

   @Override
   public Set<AnnotatableType> satisfies() {
      return Collections.singleton(Types.DEPENDENCY);
   }
}//END OF MaltParserAnnotator
