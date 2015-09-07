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

package com.davidbracewell.hermes.stopword;

import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.tag.POS;
import com.davidbracewell.string.StringUtils;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * @author David B. Bracewell
 */
public class JapaneseStopWords extends StopWords {

  ImmutableSet<String> stopWords = ImmutableSet.<String>builder()
      .add("これ")
      .add("それ")
      .add("あれ")
      .add("この")
      .add("その")
      .add("あの")
      .add("ここ")
      .add("そこ")
      .add("あそこ")
      .add("こちら")
      .add("どこ")
      .add("だれ")
      .add("なに")
      .add("なん")
      .add("何")
      .add("私")
      .add("貴方")
      .add("貴方方")
      .add("我々")
      .add("私達")
      .add("あの人")
      .add("あのかた")
      .add("彼女")
      .add("彼")
      .add("です")
      .add("あります")
      .add("おります")
      .add("います")
      .add("は")
      .add("が")
      .add("の")
      .add("に")
      .add("を")
      .add("で")
      .add("え")
      .add("から")
      .add("まで")
      .add("より")
      .add("も")
      .add("どの")
      .add("と")
      .add("し")
      .add("それで")
      .add("しかし")
      .build();

  @Override
  protected boolean isTokenStopWord(Annotation token) {
    if (token.hasAttribute(Attrs.PART_OF_SPEECH)) {
      POS tag = token.getAttribute(Attrs.PART_OF_SPEECH).as(POS.class);
      if (tag != null) {
        return tag.isConjunction() ||
            tag.isNumber() ||
            tag.isAdposition() ||
            tag.isDeterminer() ||
            tag.isParticle() ||
            tag.isOther() ||
            tag.isPunctuation() ||
            isStopWord(token.toString()) ||
            isStopWord(token.getLemma());
      }
    }
    return isStopWord(token.toString()) || isStopWord(token.getLemma());
  }

  @Override
  public boolean isStopWord(String word) {
    return Strings.isNullOrEmpty(word) ||
        stopWords.contains(word) ||
        StringUtils.isNonAlphaNumeric(word);
  }

}//END OF JapaneseStopWords
