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

import com.davidbracewell.collection.Collect;
import com.davidbracewell.hermes.Annotatable;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.hermes.Types;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.text.BreakIterator;
import java.util.Collections;
import java.util.Set;

/**
 * <p> A <code>BreakIterator</code> backed sentence annotator that has limited knowledge of abbreviations. </p>
 *
 * @author David B. Bracewell
 */
public class DefaultSentenceAnnotator implements Annotator, Serializable {
  private static final long serialVersionUID = 1L;

  private static CharMatcher BAD_EOS = CharMatcher.INVISIBLE.and(CharMatcher.WHITESPACE).and(CharMatcher.BREAKING_WHITESPACE);
  private final Set<String> noSentenceBreak = ImmutableSet.<String>builder()
    .add("mr.")
    .add("mrs.")
    .add("dr.")
    .add("gen.")
    .add("dr.")
    .add("mr.")
    .add("mrs.")
    .add("ms.")
    .add("col.")
    .add("lt.")
    .add("cmdr.")
    .add("admr.")
    .add("sgt.")
    .add("cpl.")
    .add("gen.")
    .add("maj.")
    .add("pvt.")
    .add("jr.")
    .add("sr.")
    .add("prof.")
    .add("sens.")
    .add("reps.")
    .add("sen.")
    .add("rep.")
    .add("gov.")
    .add("atty.")
    .add("attys.")
    .add("supt.")
    .add("det.")
    .add("rev.")
    .add("inc.")
    .add("ltd.")
    .add("co.")
    .add("corp.")
    .add("vs.")
    .add("etc.")
    .add("esp.")
    .add("arc.")
    .add("al.")
    .add("ave.")
    .add("blvd.")
    .add("bld.")
    .add("cl.")
    .add("ct.")
    .add("cres.")
    .add("dr.")
    .add("expy.")
    .add("exp.")
    .add("dist.")
    .add("mt.")
    .add("ft.")
    .add("fw?y.")
    .add("fy.")
    .add("hway.")
    .add("hwy.")
    .add("la.")
    .add("pde?.")
    .add("pd.")
    .add("pl.")
    .add("plz.")
    .add("rd.")
    .add("st.")
    .add("tce.")
    .add("ala.")
    .add("ariz.")
    .add("ark.")
    .add("cal.")
    .add("calif.")
    .add("col.")
    .add("colo.")
    .add("conn.")
    .add("del.")
    .add("fed.")
    .add("fla.")
    .add("ga.")
    .add("ida.")
    .add("id.")
    .add("ill.")
    .add("ind.")
    .add("ia.")
    .add("kan.")
    .add("kans.")
    .add("ken.")
    .add("ky.")
    .add("la.")
    .add("me.")
    .add("md.")
    .add("is.")
    .add("mass.")
    .add("mich.")
    .add("minn.")
    .add("miss.")
    .add("mo.")
    .add("mont.")
    .add("neb.")
    .add("nebr.")
    .add("nev.")
    .add("mex.")
    .add("okla.")
    .add("ok.")
    .add("ore.")
    .add("penna.")
    .add("penn.")
    .add("pa.")
    .add("dak.")
    .add("tenn.")
    .add("tex.")
    .add("tx.")
    .add("ut.")
    .add("vt.")
    .add("va.")
    .add("wash.")
    .add("wis.")
    .add("wisc.")
    .add("wy.")
    .add("wyo.")
    .add("usafa.")
    .add("usa.")
    .add("u.s.a.")
    .add("u.s.")
    .add("us.")
    .add("alta.")
    .add("man.")
    .add("ont.")
    .add("qué.")
    .add("sask.")
    .add("yuk.")
    .build();

  @Override
  public void annotate(Document doc) {
    int index = 0;
    BreakIterator iterator = BreakIterator.getSentenceInstance(doc.getLanguage().asLocale());
    iterator.setText(doc.toString());
    for (int end = iterator.next(), start = 0; end != BreakIterator.DONE; end = iterator.next()) {
      //Trim whitespace the front and end of the entence
      while (start < doc.length() && BAD_EOS.matches(doc.charAt(start))) {
        start++;
      }
      while (end > 0 && BAD_EOS.matches(doc.charAt(end - 1))) {
        end--;
      }

      //Make sure it is still a valid sentence
      if (end <= start || start >= doc.length()) {
        start = end;
        continue;
      }

      String sentence = doc.subSequence(start, end).toString();
      int idx = sentence.lastIndexOf(' ');
      if (idx != -1) {
        sentence = sentence.substring(idx + 1).toLowerCase();
      }
      if (!noSentenceBreak.contains(sentence)) {
        doc.createAnnotation(Types.SENTENCE,
          start,
          end,
          Collect.map(Attrs.INDEX, index)
        );
        index++;
        start = end;
      }

    }

  }

  @Override
  public Set<Annotatable> satisfies() {
    return Collections.singleton(Types.SENTENCE);
  }


}//END OF DefaultSentenceAnnotator
