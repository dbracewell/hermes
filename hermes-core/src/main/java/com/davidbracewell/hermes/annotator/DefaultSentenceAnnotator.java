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
import com.davidbracewell.hermes.AnnotationType;
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
 * <p>
 * A <code>BreakIterator</code> backed sentence annotator that has limited knowledge of abbreviations.
 * </p>
 *
 * @author David B. Bracewell
 */
public class DefaultSentenceAnnotator implements Annotator, Serializable {
  private static final long serialVersionUID = 1L;

  private static CharMatcher BAD_EOS = CharMatcher.INVISIBLE.and(CharMatcher.WHITESPACE).and(CharMatcher.BREAKING_WHITESPACE);
  private final Set<String> noSentenceBreak = ImmutableSet.<String>builder()
    .add("Mr.")
    .add("Mrs.")
    .add("Dr.")
    .add("Gen.")
    .add("Dr.")
    .add("Mr.")
    .add("Mrs.")
    .add("Ms.")
    .add("Col.")
    .add("Lt.")
    .add("Cmdr.")
    .add("Admr.")
    .add("Sgt.")
    .add("Cpl.")
    .add("Gen.")
    .add("Maj.")
    .add("Pvt.")
    .add("Jr.")
    .add("Sr.")
    .add("Prof.")
    .add("Sens?.")
    .add("Reps?.")
    .add("Gov.")
    .add("Attys?.")
    .add("Supt.")
    .add("Det.")
    .add("Rev.")
    .add("Inc.")
    .add("Ltd.")
    .add("Co.")
    .add("Corp.")
    .add("vs.")
    .add("etc.")
    .add("esp.")
    .add("arc.")
    .add("al.")
    .add("ave.")
    .add("blv?d.")
    .add("cl.")
    .add("ct.")
    .add("cres.")
    .add("dr.")
    .add("expy?.")
    .add("exp.")
    .add("dist.")
    .add("mt.")
    .add("ft.")
    .add("fw?y.")
    .add("hwa?y.")
    .add("la.")
    .add("pde?.")
    .add("pl.")
    .add("plz.")
    .add("rd.")
    .add("st.")
    .add("tce.")
    .add("Ala.")
    .add("Ariz.")
    .add("Ark.")
    .add("Cal.")
    .add("Calif.")
    .add("Col.")
    .add("Colo.")
    .add("Conn.")
    .add("Del.")
    .add("Fed.")
    .add("Fla.")
    .add("Ga.")
    .add("Ida.")
    .add("Id.")
    .add("Ill.")
    .add("Ind.")
    .add("Ia.")
    .add("Kan.")
    .add("Kans.")
    .add("Ken.")
    .add("Ky.")
    .add("La.")
    .add("Me.")
    .add("Md.")
    .add("Is.")
    .add("Mass.")
    .add("Mich.")
    .add("Minn.")
    .add("Miss.")
    .add("Mo.")
    .add("Mont.")
    .add("Neb.")
    .add("Nebr.")
    .add("Nev.")
    .add("Mex.")
    .add("Okla.")
    .add("Ok.")
    .add("Ore.")
    .add("Penna.")
    .add("Penn.")
    .add("Pa.")
    .add("Dak.")
    .add("Tenn.")
    .add("Tex.")
    .add("Ut.")
    .add("Vt.")
    .add("Va.")
    .add("Wash.")
    .add("Wis.")
    .add("Wisc.")
    .add("Wy.")
    .add("Wyo.")
    .add("USAFA.")
    .add("Alta.")
    .add("Man.")
    .add("Ont.")
    .add("Qué.")
    .add("Sask.")
    .add("Yuk.")
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

      if (!noSentenceBreak.contains(doc.substring(start, end))) {
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
  public Set<AnnotationType> satisfies() {
    return Collections.singleton(Types.SENTENCE);
  }


}//END OF DefaultSentenceAnnotator
