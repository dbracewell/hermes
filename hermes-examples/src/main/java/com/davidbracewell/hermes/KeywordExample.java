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

package com.davidbracewell.hermes;

import com.davidbracewell.hermes.attribute.POS;
import com.davidbracewell.hermes.corpus.TermSpec;
import com.davidbracewell.hermes.filter.StopWords;
import com.davidbracewell.hermes.keyword.NPClusteringKeywordExtractor;
import com.davidbracewell.hermes.keyword.TFKeywordExtractor;

/**
 * @author David B. Bracewell
 */
public class KeywordExample {


   public static void main(String[] args) throws Exception {
      Hermes.initializeApplication("", args);
      Document document = Document.create(
         "Producing a major update to the browser that helped launch the Web has proven a difficult labor for Netscape.\n" +
            "Last year the company decided to toss out the spaghetti code that was going to be Navigator 5, and started over to build Netscape 6 around a new rendering engine code-named Gecko that would include contributions from open-source developers.\n" +
            "Since that time, America Online acquired the browser-side of the company, and its server-side initiatives are tied to Sun Microsystems. Disruption on that scale tends to really hurt a company's development efforts.\n" +
            "That said, its not surprising that Netscape 6 Preview Release 1 is very much a work in progress. The browser, which was introduced last week at  Spring Internet World , has some basic stability problems that need to be addressed before it's ready for everyday use.\n" +
            "Preview Release 1 is far from feature complete. Netscape is more concerned with users test driving what's under the hood than what's built around it. As a result, there's a lot of basic features missing, including keyboard commands, a history of Web sites you visited, and the auto-completion of Web addresses. You can't even navigate backwards or forwards with the keyboard.\n" +
            "The company instead focused on making its rendering engine compatible with emerging Internet standards. Netscape 6 supports Extensible Markup Language (XML), Cascading Style Sheets 1 (CSS1), and HTML 4.0. There's also partial support of CSS2 and 3, the Document Object Model (DOM), JavaScript 1.5 and Java 2. It's supposed to be Java compliant, but not all Java-based chat rooms, such as the one on Net radio station KNAC.com ,  work.\n" +
            "Netscape 6 (the Communicator and Navigator monikers that accompanied previous versions have been dropped) is a rather slow browser. At the Internet World introduction, Netscape made a big deal touting how quickly pages loaded, but their demo loaded files that were cached on the PC.\n" +
            "The browser is only slightly faster than previous version Navigator 4.72 when pulling pages off the Web, but it's clearly not in the league of Internet Explorer 5.01. Although text-heavy sites load quickly as expected, sites with substantial graphics such as CNBC can cause it to choke, even on a cable modem connection.\n" +
            "The new Cookie Manager feature is little more than a browser that enables you to preview the information in each cookie before deciding if you want to delete it, and it allows you to block cookies from certain domains.\n" +
            "Unfortunately, it would not save my membership information for the AnandTech Forums , so Netscape's cookies are crumbling a little.\n" +
            "There's also a Password Manager, which can save and encrypt your passwords to consumer sites so you don't have to re-enter them. At this point, all you can do is delete passwords, you can't edit or change them.\n" +
            "AOL's Instant Messenger (AIM) is integrated into the browser, but it did not detect the installation of AIM on my system, so I had to recreate my buddy list. When it's finally working properly the integration of AIM with Netscape Mail will be a nifty feature. If an AIM user who is online sends you a letter, you have the option of sending an instant message instead of writing an email back. The Netscape Mail client is smarter than AIM in this release in that it will import Eudora, Outlook and Outlook Express address books, saved mail, and other settings.\n" +
            "The browser, email client, and AIM interfaces all look very primitive compared to the previous browser and are in dire need of a makeover. Netscape plans to introduce skins shortly, customizable interfaces for the browser. Although no skins are out yet, the Client Customization Kit should provide a lot of ways to change the look of the browser.\n" +
            "For international users, Netscape 6 has a built-in translation engine that interprets pages to a user's language of choice. Translation is actually performed by translation site Teletranslator, which takes a while to process.\n" +
            "The translation of the German site of Guano Apes , (Germany's answer to Korn) was slow, but it only missed a few words. The engine smartly detected the German language, and defaulted to an English translation. Similarly, Teletranslator was successful in translating a Japanese site.\n" +
            "For Netscape 6, the key phrase is proceed with caution -- only visit Java-rich sites if you're prepared to submit error reports and restart your browser frequently, and don't uninstall Navigator 4.72 just yet. Netscape 6 will import your Navigator 4.72 settings and bookmarks, and it won't mess with Navigator or IE 5 installations. While there's no uninstall program, you can remove Netscape 6 just by deleting its folder.\n");
      document.annotate(Types.TOKEN, Types.SENTENCE, Types.PHRASE_CHUNK);
      NPClusteringKeywordExtractor ke = new NPClusteringKeywordExtractor();
      System.out.println("   NPClusteringKeywordExtractor");
      ke.extract(document).topN(10).forEach((kw, score) -> System.out.println(kw + "\t" + score));
      System.out.println();


      System.out.println("   TFKeywordExtractor");
      TFKeywordExtractor tfke = new TFKeywordExtractor(TermSpec.create()
                                                               .annotationType(Types.PHRASE_CHUNK)
                                                               .toStringFunction(
                                                                  pc -> pc.trim(StopWords.isStopWord()).getLemma())
                                                               .filter(pc -> pc.getPOS().isInstance(POS.NOUN))
      );
      tfke.extract(document).topN(10).forEach((kw, score) -> System.out.println(kw + "\t" + score));


   }


}//END OF KeywordExample
