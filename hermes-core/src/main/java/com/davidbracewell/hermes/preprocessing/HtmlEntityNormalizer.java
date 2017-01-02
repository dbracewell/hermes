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

package com.davidbracewell.hermes.preprocessing;

import com.davidbracewell.Language;
import com.davidbracewell.guava.common.collect.ImmutableMap;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Normalizes xml and html entities, such as <code>&amp;</code></p>
 *
 * @author David B. Bracewell
 */
public class HtmlEntityNormalizer extends TextNormalizer {
   private static final long serialVersionUID = 1L;
   private final Pattern decimalEntity = Pattern.compile("(?i)&#(\\d+);");
   private final Pattern hexEntity = Pattern.compile("(?i)&#x([\\d|A-F]+);");
   private final Map<String, Character> entityMap = ImmutableMap.<String, Character>builder()
                                                       .put(Pattern.quote("&quot;"), (char) 34)
                                                       .put(Pattern.quote("&amp;"), (char) 38)
                                                       .put(Pattern.quote("&apos;"), (char) 39)
                                                       .put(Pattern.quote("&lt;"), (char) 60)
                                                       .put(Pattern.quote("&gt;"), (char) 62)
                                                       .put(Pattern.quote("&nbsp;"), (char) 32)
                                                       .put(Pattern.quote("&iexcl;"), (char) 161)
                                                       .put(Pattern.quote("&cent;"), (char) 162)
                                                       .put(Pattern.quote("&pound;"), (char) 163)
                                                       .put(Pattern.quote("&curren;"), (char) 164)
                                                       .put(Pattern.quote("&yen;"), (char) 165)
                                                       .put(Pattern.quote("&brvbar;"), (char) 166)
                                                       .put(Pattern.quote("&sect;"), (char) 167)
                                                       .put(Pattern.quote("&uml;"), (char) 168)
                                                       .put(Pattern.quote("&copy;"), (char) 169)
                                                       .put(Pattern.quote("&ordf;"), (char) 170)
                                                       .put(Pattern.quote("&laquo;"), (char) 171)
                                                       .put(Pattern.quote("&not;"), (char) 172)
                                                       .put(Pattern.quote("&shy;"), (char) 173)
                                                       .put(Pattern.quote("&reg;"), (char) 174)
                                                       .put(Pattern.quote("&macr;"), (char) 175)
                                                       .put(Pattern.quote("&deg;"), (char) 176)
                                                       .put(Pattern.quote("&plusmn;"), (char) 177)
                                                       .put(Pattern.quote("&sup2;"), (char) 178)
                                                       .put(Pattern.quote("&sup3;"), (char) 179)
                                                       .put(Pattern.quote("&acute;"), (char) 180)
                                                       .put(Pattern.quote("&micro;"), (char) 181)
                                                       .put(Pattern.quote("&para;"), (char) 182)
                                                       .put(Pattern.quote("&middot;"), (char) 183)
                                                       .put(Pattern.quote("&cedil;"), (char) 184)
                                                       .put(Pattern.quote("&sup1;"), (char) 185)
                                                       .put(Pattern.quote("&ordm;"), (char) 186)
                                                       .put(Pattern.quote("&raquo;"), (char) 187)
                                                       .put(Pattern.quote("&frac14;"), (char) 188)
                                                       .put(Pattern.quote("&frac12;"), (char) 189)
                                                       .put(Pattern.quote("&frac34;"), (char) 190)
                                                       .put(Pattern.quote("&iquest;"), (char) 191)
                                                       .put(Pattern.quote("&Agrave;"), (char) 192)
                                                       .put(Pattern.quote("&Aacute;"), (char) 193)
                                                       .put(Pattern.quote("&Acirc;"), (char) 194)
                                                       .put(Pattern.quote("&Atilde;"), (char) 195)
                                                       .put(Pattern.quote("&Auml;"), (char) 196)
                                                       .put(Pattern.quote("&Aring;"), (char) 197)
                                                       .put(Pattern.quote("&AElig;"), (char) 198)
                                                       .put(Pattern.quote("&Ccedil;"), (char) 199)
                                                       .put(Pattern.quote("&Egrave;"), (char) 200)
                                                       .put(Pattern.quote("&Eacute;"), (char) 201)
                                                       .put(Pattern.quote("&Ecirc;"), (char) 202)
                                                       .put(Pattern.quote("&Euml;"), (char) 203)
                                                       .put(Pattern.quote("&Igrave;"), (char) 204)
                                                       .put(Pattern.quote("&Iacute;"), (char) 205)
                                                       .put(Pattern.quote("&Icirc;"), (char) 206)
                                                       .put(Pattern.quote("&Iuml;"), (char) 207)
                                                       .put(Pattern.quote("&ETH;"), (char) 208)
                                                       .put(Pattern.quote("&Ntilde;"), (char) 209)
                                                       .put(Pattern.quote("&Ograve;"), (char) 210)
                                                       .put(Pattern.quote("&Oacute;"), (char) 211)
                                                       .put(Pattern.quote("&Ocirc;"), (char) 212)
                                                       .put(Pattern.quote("&Otilde;"), (char) 213)
                                                       .put(Pattern.quote("&Ouml;"), (char) 214)
                                                       .put(Pattern.quote("&times;"), (char) 215)
                                                       .put(Pattern.quote("&Oslash;"), (char) 216)
                                                       .put(Pattern.quote("&Ugrave;"), (char) 217)
                                                       .put(Pattern.quote("&Uacute;"), (char) 218)
                                                       .put(Pattern.quote("&Ucirc;"), (char) 219)
                                                       .put(Pattern.quote("&Uuml;"), (char) 220)
                                                       .put(Pattern.quote("&Yacute;"), (char) 221)
                                                       .put(Pattern.quote("&THORN;"), (char) 222)
                                                       .put(Pattern.quote("&szlig;"), (char) 223)
                                                       .put(Pattern.quote("&agrave;"), (char) 224)
                                                       .put(Pattern.quote("&aacute;"), (char) 225)
                                                       .put(Pattern.quote("&acirc;"), (char) 226)
                                                       .put(Pattern.quote("&atilde;"), (char) 227)
                                                       .put(Pattern.quote("&auml;"), (char) 228)
                                                       .put(Pattern.quote("&aring;"), (char) 229)
                                                       .put(Pattern.quote("&aelig;"), (char) 230)
                                                       .put(Pattern.quote("&ccedil;"), (char) 231)
                                                       .put(Pattern.quote("&egrave;"), (char) 232)
                                                       .put(Pattern.quote("&eacute;"), (char) 233)
                                                       .put(Pattern.quote("&ecirc;"), (char) 234)
                                                       .put(Pattern.quote("&euml;"), (char) 235)
                                                       .put(Pattern.quote("&igrave;"), (char) 236)
                                                       .put(Pattern.quote("&iacute;"), (char) 237)
                                                       .put(Pattern.quote("&icirc;"), (char) 238)
                                                       .put(Pattern.quote("&iuml;"), (char) 239)
                                                       .put(Pattern.quote("&eth;"), (char) 240)
                                                       .put(Pattern.quote("&ntilde;"), (char) 241)
                                                       .put(Pattern.quote("&ograve;"), (char) 242)
                                                       .put(Pattern.quote("&oacute;"), (char) 243)
                                                       .put(Pattern.quote("&ocirc;"), (char) 244)
                                                       .put(Pattern.quote("&otilde;"), (char) 245)
                                                       .put(Pattern.quote("&ouml;"), (char) 246)
                                                       .put(Pattern.quote("&divide;"), (char) 247)
                                                       .put(Pattern.quote("&oslash;"), (char) 248)
                                                       .put(Pattern.quote("&ugrave;"), (char) 249)
                                                       .put(Pattern.quote("&uacute;"), (char) 250)
                                                       .put(Pattern.quote("&ucirc;"), (char) 251)
                                                       .put(Pattern.quote("&uuml;"), (char) 252)
                                                       .put(Pattern.quote("&yacute;"), (char) 253)
                                                       .put(Pattern.quote("&thorn;"), (char) 254)
                                                       .put(Pattern.quote("&yuml;"), (char) 255)
                                                       .put(Pattern.quote("&OElig;"), (char) 338)
                                                       .put(Pattern.quote("&oelig;"), (char) 339)
                                                       .put(Pattern.quote("&Scaron;"), (char) 352)
                                                       .put(Pattern.quote("&scaron;"), (char) 353)
                                                       .put(Pattern.quote("&Yuml;"), (char) 376)
                                                       .put(Pattern.quote("&fnof;"), (char) 402)
                                                       .put(Pattern.quote("&circ;"), (char) 710)
                                                       .put(Pattern.quote("&tilde;"), (char) 732)
                                                       .put(Pattern.quote("&Alpha;"), (char) 913)
                                                       .put(Pattern.quote("&Beta;"), (char) 914)
                                                       .put(Pattern.quote("&Gamma;"), (char) 915)
                                                       .put(Pattern.quote("&Delta;"), (char) 916)
                                                       .put(Pattern.quote("&Epsilon;"), (char) 917)
                                                       .put(Pattern.quote("&Zeta;"), (char) 918)
                                                       .put(Pattern.quote("&Eta;"), (char) 919)
                                                       .put(Pattern.quote("&Theta;"), (char) 920)
                                                       .put(Pattern.quote("&Iota;"), (char) 921)
                                                       .put(Pattern.quote("&Kappa;"), (char) 922)
                                                       .put(Pattern.quote("&Lambda;"), (char) 923)
                                                       .put(Pattern.quote("&Mu;"), (char) 924)
                                                       .put(Pattern.quote("&Nu;"), (char) 925)
                                                       .put(Pattern.quote("&Xi;"), (char) 926)
                                                       .put(Pattern.quote("&Omicron;"), (char) 927)
                                                       .put(Pattern.quote("&Pi;"), (char) 928)
                                                       .put(Pattern.quote("&Rho;"), (char) 929)
                                                       .put(Pattern.quote("&Sigma;"), (char) 931)
                                                       .put(Pattern.quote("&Tau;"), (char) 932)
                                                       .put(Pattern.quote("&Upsilon;"), (char) 933)
                                                       .put(Pattern.quote("&Phi;"), (char) 934)
                                                       .put(Pattern.quote("&Chi;"), (char) 935)
                                                       .put(Pattern.quote("&Psi;"), (char) 936)
                                                       .put(Pattern.quote("&Omega;"), (char) 937)
                                                       .put(Pattern.quote("&alpha;"), (char) 945)
                                                       .put(Pattern.quote("&beta;"), (char) 946)
                                                       .put(Pattern.quote("&gamma;"), (char) 947)
                                                       .put(Pattern.quote("&delta;"), (char) 948)
                                                       .put(Pattern.quote("&epsilon;"), (char) 949)
                                                       .put(Pattern.quote("&zeta;"), (char) 950)
                                                       .put(Pattern.quote("&eta;"), (char) 951)
                                                       .put(Pattern.quote("&theta;"), (char) 952)
                                                       .put(Pattern.quote("&iota;"), (char) 953)
                                                       .put(Pattern.quote("&kappa;"), (char) 954)
                                                       .put(Pattern.quote("&lambda;"), (char) 955)
                                                       .put(Pattern.quote("&mu;"), (char) 956)
                                                       .put(Pattern.quote("&nu;"), (char) 957)
                                                       .put(Pattern.quote("&xi;"), (char) 958)
                                                       .put(Pattern.quote("&omicron;"), (char) 959)
                                                       .put(Pattern.quote("&pi;"), (char) 960)
                                                       .put(Pattern.quote("&rho;"), (char) 961)
                                                       .put(Pattern.quote("&sigmaf;"), (char) 962)
                                                       .put(Pattern.quote("&sigma;"), (char) 963)
                                                       .put(Pattern.quote("&tau;"), (char) 964)
                                                       .put(Pattern.quote("&upsilon;"), (char) 965)
                                                       .put(Pattern.quote("&phi;"), (char) 966)
                                                       .put(Pattern.quote("&chi;"), (char) 967)
                                                       .put(Pattern.quote("&psi;"), (char) 968)
                                                       .put(Pattern.quote("&omega;"), (char) 969)
                                                       .put(Pattern.quote("&thetasym;"), (char) 977)
                                                       .put(Pattern.quote("&upsih;"), (char) 978)
                                                       .put(Pattern.quote("&piv;"), (char) 982)
                                                       .put(Pattern.quote("&ensp;"), (char) 8194)
                                                       .put(Pattern.quote("&emsp;"), (char) 8195)
                                                       .put(Pattern.quote("&thinsp;"), (char) 8201)
                                                       .put(Pattern.quote("&zwnj;"), (char) 8204)
                                                       .put(Pattern.quote("&zwj;"), (char) 8205)
                                                       .put(Pattern.quote("&lrm;"), (char) 8206)
                                                       .put(Pattern.quote("&rlm;"), (char) 8207)
                                                       .put(Pattern.quote("&ndash;"), (char) 8211)
                                                       .put(Pattern.quote("&mdash;"), (char) 8212)
                                                       .put(Pattern.quote("&lsquo;"), (char) 8216)
                                                       .put(Pattern.quote("&rsquo;"), (char) 8217)
                                                       .put(Pattern.quote("&sbquo;"), (char) 8218)
                                                       .put(Pattern.quote("&ldquo;"), (char) 8220)
                                                       .put(Pattern.quote("&rdquo;"), (char) 8221)
                                                       .put(Pattern.quote("&bdquo;"), (char) 8222)
                                                       .put(Pattern.quote("&dagger;"), (char) 8224)
                                                       .put(Pattern.quote("&Dagger;"), (char) 8225)
                                                       .put(Pattern.quote("&bull;"), (char) 8226)
                                                       .put(Pattern.quote("&hellip;"), (char) 8230)
                                                       .put(Pattern.quote("&permil;"), (char) 8240)
                                                       .put(Pattern.quote("&prime;"), (char) 8242)
                                                       .put(Pattern.quote("&Prime;"), (char) 8243)
                                                       .put(Pattern.quote("&lsaquo;"), (char) 8249)
                                                       .put(Pattern.quote("&rsaquo;"), (char) 8250)
                                                       .put(Pattern.quote("&oline;"), (char) 8254)
                                                       .put(Pattern.quote("&frasl;"), (char) 8260)
                                                       .put(Pattern.quote("&euro;"), (char) 8364)
                                                       .put(Pattern.quote("&image;"), (char) 8465)
                                                       .put(Pattern.quote("&weierp;"), (char) 8472)
                                                       .put(Pattern.quote("&real;"), (char) 8476)
                                                       .put(Pattern.quote("&trade;"), (char) 8482)
                                                       .put(Pattern.quote("&alefsym;"), (char) 8501)
                                                       .put(Pattern.quote("&larr;"), (char) 8592)
                                                       .put(Pattern.quote("&uarr;"), (char) 8593)
                                                       .put(Pattern.quote("&rarr;"), (char) 8594)
                                                       .put(Pattern.quote("&darr;"), (char) 8595)
                                                       .put(Pattern.quote("&harr;"), (char) 8596)
                                                       .put(Pattern.quote("&crarr;"), (char) 8629)
                                                       .put(Pattern.quote("&lArr;"), (char) 8656)
                                                       .put(Pattern.quote("&uArr;"), (char) 8657)
                                                       .put(Pattern.quote("&rArr;"), (char) 8658)
                                                       .put(Pattern.quote("&dArr;"), (char) 8659)
                                                       .put(Pattern.quote("&hArr;"), (char) 8660)
                                                       .put(Pattern.quote("&forall;"), (char) 8704)
                                                       .put(Pattern.quote("&part;"), (char) 8706)
                                                       .put(Pattern.quote("&exist;"), (char) 8707)
                                                       .put(Pattern.quote("&empty;"), (char) 8709)
                                                       .put(Pattern.quote("&nabla;"), (char) 8711)
                                                       .put(Pattern.quote("&isin;"), (char) 8712)
                                                       .put(Pattern.quote("&notin;"), (char) 8713)
                                                       .put(Pattern.quote("&ni;"), (char) 8715)
                                                       .put(Pattern.quote("&prod;"), (char) 8719)
                                                       .put(Pattern.quote("&sum;"), (char) 8721)
                                                       .put(Pattern.quote("&minus;"), (char) 8722)
                                                       .put(Pattern.quote("&lowast;"), (char) 8727)
                                                       .put(Pattern.quote("&radic;"), (char) 8730)
                                                       .put(Pattern.quote("&prop;"), (char) 8733)
                                                       .put(Pattern.quote("&infin;"), (char) 8734)
                                                       .put(Pattern.quote("&ang;"), (char) 8736)
                                                       .put(Pattern.quote("&and;"), (char) 8743)
                                                       .put(Pattern.quote("&or;"), (char) 8744)
                                                       .put(Pattern.quote("&cap;"), (char) 8745)
                                                       .put(Pattern.quote("&cup;"), (char) 8746)
                                                       .put(Pattern.quote("&int;"), (char) 8747)
                                                       .put(Pattern.quote("&there4;"), (char) 8756)
                                                       .put(Pattern.quote("&sim;"), (char) 8764)
                                                       .put(Pattern.quote("&cong;"), (char) 8773)
                                                       .put(Pattern.quote("&asymp;"), (char) 8776)
                                                       .put(Pattern.quote("&ne;"), (char) 8800)
                                                       .put(Pattern.quote("&equiv;"), (char) 8801)
                                                       .put(Pattern.quote("&le;"), (char) 8804)
                                                       .put(Pattern.quote("&ge;"), (char) 8805)
                                                       .put(Pattern.quote("&sub;"), (char) 8834)
                                                       .put(Pattern.quote("&sup;"), (char) 8835)
                                                       .put(Pattern.quote("&nsub;"), (char) 8836)
                                                       .put(Pattern.quote("&sube;"), (char) 8838)
                                                       .put(Pattern.quote("&supe;"), (char) 8839)
                                                       .put(Pattern.quote("&oplus;"), (char) 8853)
                                                       .put(Pattern.quote("&otimes;"), (char) 8855)
                                                       .put(Pattern.quote("&perp;"), (char) 8869)
                                                       .put(Pattern.quote("&sdot;"), (char) 8901)
                                                       .put(Pattern.quote("&lceil;"), (char) 8968)
                                                       .put(Pattern.quote("&rceil;"), (char) 8969)
                                                       .put(Pattern.quote("&lfloor;"), (char) 8970)
                                                       .put(Pattern.quote("&rfloor;"), (char) 8971)
                                                       .put(Pattern.quote("&lang;"), (char) 9001)
                                                       .put(Pattern.quote("&rang;"), (char) 9002)
                                                       .put(Pattern.quote("&loz;"), (char) 9674)
                                                       .put(Pattern.quote("&spades;"), (char) 9824)
                                                       .put(Pattern.quote("&clubs;"), (char) 9827)
                                                       .put(Pattern.quote("&hearts;"), (char) 9829)
                                                       .put(Pattern.quote("&diams;"), (char) 9830)
                                                       .build();

   @Override
   protected String performNormalization(String input, Language language) {
      if (input == null) {
         return null;
      }

      String r = input;
      for (String e : entityMap.keySet()) {
         r = r.replaceAll(e, entityMap.get(e).toString());
      }

      Matcher m = decimalEntity.matcher(r);
      while (m.find()) {
         String num = m.group(1).trim();
         r = r.replaceAll("(?i)&#" + num + ";", Character.toString((char) Integer.parseInt(num)));
      }

      m = hexEntity.matcher(r);
      while (m.find()) {
         String num = m.group(1).trim();
         r = r.replaceAll("(?i)&#x" + num + ";", Character.toString((char) Integer.parseInt(num, 16)));
      }

      return r;
   }
}//END OF HtmlEntityNormalizer
