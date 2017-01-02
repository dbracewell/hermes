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

package com.davidbracewell.hermes.wordnet.io;

import com.davidbracewell.Language;
import com.davidbracewell.config.Config;
import com.davidbracewell.guava.common.base.Charsets;
import com.davidbracewell.guava.common.base.Function;
import com.davidbracewell.guava.common.base.Strings;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.hermes.wordnet.*;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.tuple.Tuple2;
import com.davidbracewell.tuple.Tuple3;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * @author David B. Bracewell
 */
public class PrincentonWordNetLoader implements WordNetLoader {

  private WordNetDB db;
  private Map<Tuple2<String, String>, Integer> synsetSenseToSenseNumber = new HashMap<>(100000);
  private Set<Tuple3<String, String, WordNetRelation>> senseRelations = new HashSet<>(10000);
  private Map<String, Sense> senseMap = new HashMap<>(100000);

  private void processFiles(Resource root, String prefix, Function<String, Object> parser) throws IOException {
    for (WordNetPOS posFILE : WordNetPOS.values()) {
      if (posFILE == WordNetPOS.ANY) continue;
      Resource file = root.getChild(prefix + "." + posFILE.getShortForm().toLowerCase());
      file.setCharset(Charsets.ISO_8859_1);
      try (BufferedReader reader = new BufferedReader(file.reader())) {
        String line;
        while ((line = reader.readLine()) != null) {
          parser.apply(line);
        }
      }
    }
  }

  @Override
  public void load(WordNetDB db) {
    this.db = db;
    try {
      processFiles(Config.get("PrincentonWordNetLoader.dictionary").asResource(), "index", new IndexParser());
      processFiles(Config.get("PrincentonWordNetLoader.dictionary").asResource(), "data", new DataParser());
      for (Tuple3<String, String, WordNetRelation> cell : senseRelations) {
        db.putRelation(senseMap.get(cell.getV1()), senseMap.get(cell.getV2()), cell.getV3());
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }


  private class IndexParser implements Function<String, Object> {

    @Override
    public Object apply(String line) {
      if (Character.isWhitespace(line.charAt(0))) return null; //Header

      StringTokenizer tokenizer = new StringTokenizer(line, " ");

      String lemma = tokenizer.nextToken();
      WordNetPOS pos = WordNetPOS.fromString(tokenizer.nextToken());
      int synset_cnt = Integer.parseInt(tokenizer.nextToken());

      int p_cnt = Integer.parseInt(tokenizer.nextToken());
      for (int i = 0; i < p_cnt; i++) {
        tokenizer.nextToken();
      }

      Integer.parseInt(tokenizer.nextToken());
      Integer.parseInt(tokenizer.nextToken());

      for (int i = 0; i < synset_cnt; i++) {
        String synset_offset = tokenizer.nextToken();
        synsetSenseToSenseNumber.put(Tuple2.of(synset_offset + pos.getTag(), lemma), i + 1);
      }
      return null;
    }
  }

  private static String toSenseIndex(String synset, int position) {
    return synset + "%%" + Integer.toString(position);
  }

  private class DataParser implements Function<String, Object> {
    private String toSynsetId(int offset, WordNetPOS pos) {
      return Strings.padStart(Integer.toString(offset), 8, '0') + pos.getTag();
    }


    @Override
    public Object apply(String line) {
      if (Character.isWhitespace(line.charAt(0))) return null;

      StringTokenizer tokenizer = new StringTokenizer(line, " ");

      //http://wordnet.princeton.edu/man/wndb.5WN.html
      String synset_offset = tokenizer.nextToken();
      int lex_filenum = Integer.parseInt(tokenizer.nextToken());
      String ss_type = tokenizer.nextToken().toUpperCase();

      WordNetPOS synsetPOS = WordNetPOS.fromString(ss_type);
      final String synsetId = synset_offset + synsetPOS.getTag();


      SynsetImpl synset = new SynsetImpl();
      synset.setId(synsetId);
      synset.setPartOfSpeech(synsetPOS);
      synset.setLexicographerFile(LexicographerFile.fromId(lex_filenum));
      synset.setAdjectiveSatelite(ss_type.equalsIgnoreCase("S"));
      db.putSynset(synsetId, synset);

      int w_cnt = Integer.parseInt(tokenizer.nextToken(), 16);

      SenseImpl[] senses = new SenseImpl[w_cnt];
      //Process the senses and add them
      for (int i = 0; i < w_cnt; i++) {
        String lemma = tokenizer.nextToken();

        AdjectiveMarker marker = null;
        if (synsetPOS == WordNetPOS.ADJECTIVE) {
          for (AdjectiveMarker adjMarker : AdjectiveMarker.values()) {
            if (lemma.endsWith(adjMarker.getTag())) {
              marker = adjMarker;
              lemma = lemma.substring(0, lemma.length() - adjMarker.getTag().length());
              break;
            }
          }
        }


        int lex_id = Integer.parseInt(tokenizer.nextToken(), 16) + 1;

        SenseImpl sense = new SenseImpl();
        sense.setPartOfSpeech(synsetPOS);
        sense.setSynsetPosition(i);
        sense.setLanguage(Language.ENGLISH);
        sense.setLemma(lemma);
        sense.setLexicalId(lex_id);
        sense.setSense(synsetSenseToSenseNumber.get(Tuple2.of(synsetId, lemma.toLowerCase())));
        sense.setSynset(synset);
        sense.setAdjectiveMarker(marker);
        sense.setId(synsetId + "%" + lemma + "%" + Strings.padStart(Integer.toString(lex_id), 2, '0'));
        db.putSense(lemma, sense);
        senseMap.put(toSenseIndex(synsetId, i + 1), sense);
        senses[i] = sense;
      }

      synset.setSenses(senses);

      //Process the synsetRelations
      int p_cnt = Integer.parseInt(tokenizer.nextToken());
      boolean hasHypernym = false;
      for (int i = 0; i < p_cnt; i++) {
        WordNetRelation relation = WordNetRelation.forCode(synsetPOS, tokenizer.nextToken());
        int targetOffset = Integer.parseInt(tokenizer.nextToken());
        WordNetPOS targetPOS = WordNetPOS.fromString(tokenizer.nextToken());
        String sn = tokenizer.nextToken();
        int source_target = Integer.parseInt(sn, 16);
        if (source_target == 0) { //synset relation
          db.putRelation(synsetId, toSynsetId(targetOffset, targetPOS), relation);
          if (relation == WordNetRelation.HYPERNYM || relation == WordNetRelation.HYPERNYM_INSTANCE) {
            hasHypernym = true;
          }
        } else { //sense relation
          int source_num = Integer.parseInt(sn.substring(0, 2), 16);
          int target_num = Integer.parseInt(sn.substring(2), 16);
          senseRelations.add(
            Tuple3.of(
              toSenseIndex(synsetId, source_num),
              toSenseIndex(toSynsetId(targetOffset, targetPOS), target_num),
              relation
            )
          );
        }
      }

      if (!hasHypernym) {
        db.addRoot(synset);
      }

      if (synsetPOS == WordNetPOS.VERB) {
        int numFrames = Integer.parseInt(tokenizer.nextToken());
        for (int i = 0; i < numFrames; i++) {
          tokenizer.nextToken(); // consume the +
          VerbFrame frame = VerbFrame.forId(Integer.parseInt(tokenizer.nextToken()) - 1);
          int lemma_num = Integer.parseInt(tokenizer.nextToken(), 16);
          if (lemma_num == 0) {
            for (SenseImpl s : senses) {
              s.addVerbFrame(frame);
            }
          } else {
            senses[lemma_num - 1].addVerbFrame(frame);
          }
        }
      }


      tokenizer.nextToken(); //start of gloss
      String gloss = "";
      while (tokenizer.hasMoreTokens()) {
        gloss += " " + tokenizer.nextToken();
      }
      synset.setGloss(gloss.trim());


      return null;
    }
  }


}//END OF WordNetGraphGenerator
