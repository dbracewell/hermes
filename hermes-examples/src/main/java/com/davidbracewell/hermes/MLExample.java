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

import com.davidbracewell.Language;
import com.davidbracewell.apollo.ml.Featurizer;
import com.davidbracewell.apollo.ml.Instance;
import com.davidbracewell.apollo.ml.Learner;
import com.davidbracewell.apollo.ml.classification.ClassifierEvaluation;
import com.davidbracewell.apollo.ml.classification.ClassifierLearner;
import com.davidbracewell.apollo.ml.classification.NaiveBayes;
import com.davidbracewell.apollo.ml.classification.NaiveBayesLearner;
import com.davidbracewell.apollo.ml.data.Dataset;
import com.davidbracewell.config.Config;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.ml.feature.BagOfAnnotations;
import com.davidbracewell.hermes.ml.feature.ValueCalculator;
import com.davidbracewell.stream.StreamingContext;

import java.util.Random;
import java.util.function.Supplier;

import static com.davidbracewell.collection.map.Maps.map;

/**
 * @author David B. Bracewell
 */
public class MLExample {

   public static void main(String[] args) throws Exception {
      Config.initialize("MLExample");

      //Set of documents taken from the Rotten Tomato corpus
      String[][] training = {
         new String[]{"NEGATIVE", "simplistic , silly and tedious . "},
         new String[]{"NEGATIVE", "it's so laddish and juvenile , only teenage boys could possibly find it funny . "},
         new String[]{"NEGATIVE", "exploitative and largely devoid of the depth or sophistication that would make watching such a graphic treatment of the crimes bearable . "},
         new String[]{"NEGATIVE", "[garbus] discards the potential for pathological study , exhuming instead , the skewed melodrama of the circumstantial situation . "},
         new String[]{"NEGATIVE", "a visually flashy but narratively opaque and emotionally vapid exercise in style and mystification . "},
         new String[]{"NEGATIVE", "the story is also as unoriginal as they come , already having been recycled more times than i'd care to count . "},
         new String[]{"NEGATIVE", "about the only thing to give the movie points for is bravado -- to take an entirely stale concept and push it through the audience's meat grinder one more time . "},
         new String[]{"NEGATIVE", "not so much farcical as sour . "},
         new String[]{"NEGATIVE", "unfortunately the story and the actors are served with a hack script . "},
         new String[]{"NEGATIVE", "all the more disquieting for its relatively gore-free allusions to the serial murders , but it falls down in its attempts to humanize its subject . "},
         new String[]{"NEGATIVE", "a sentimental mess that never rings true . "},
         new String[]{"NEGATIVE", "while the performances are often engaging , this loose collection of largely improvised numbers would probably have worked better as a one-hour tv documentary . "},
         new String[]{"NEGATIVE", "interesting , but not compelling . "},
         new String[]{"NEGATIVE", "on a cutting room floor somewhere lies . . . footage that might have made no such thing a trenchant , ironic cultural satire instead of a frustrating misfire . "},
         new String[]{"NEGATIVE", "while the ensemble player who gained notice in guy ritchie's lock , stock and two smoking barrels and snatch has the bod , he's unlikely to become a household name on the basis of his first starring vehicle . "},
         new String[]{"NEGATIVE", "there is a difference between movies with the courage to go over the top and movies that don't care about being stupid"},
         new String[]{"NEGATIVE", "nothing here seems as funny as it did in analyze this , not even joe viterelli as de niro's right-hand goombah . "},
         new String[]{"NEGATIVE", "such master screenwriting comes courtesy of john pogue , the yale grad who previously gave us \"the skulls \" and last year's \" rollerball. \" enough said , except : film overboard ! "},
         new String[]{"NEGATIVE", "here , common sense flies out the window , along with the hail of bullets , none of which ever seem to hit sascha . "},
         new String[]{"NEGATIVE", "this 100-minute movie only has about 25 minutes of decent material . "},
         new String[]{"NEGATIVE", "the execution is so pedestrian that the most positive comment we can make is that rob schneider actually turns in a pretty convincing performance as a prissy teenage girl . "},
         new String[]{"NEGATIVE", "on its own , it's not very interesting . as a remake , it's a pale imitation . "},
         new String[]{"NEGATIVE", "it shows that some studios firmly believe that people have lost the ability to think and will forgive any shoddy product as long as there's a little girl-on-girl action . "},
         new String[]{"NEGATIVE", "a farce of a parody of a comedy of a premise , it isn't a comparison to reality so much as it is a commentary about our knowledge of films . "},
         new String[]{"NEGATIVE", "as exciting as all this exoticism might sound to the typical pax viewer , the rest of us will be lulled into a coma . "},
         new String[]{"NEGATIVE", "the party scenes deliver some tawdry kicks . the rest of the film . . . is dudsville . "},
         new String[]{"NEGATIVE", "our culture is headed down the toilet with the ferocity of a frozen burrito after an all-night tequila bender  and i know this because i've seen 'jackass : the movie . '"},
         new String[]{"NEGATIVE", "the criticism never rises above easy , cynical potshots at morally bankrupt characters . . . "},
         new String[]{"NEGATIVE", "the movie's something-borrowed construction feels less the product of loving , well integrated homage and more like a mere excuse for the wan , thinly sketched story . killing time , that's all that's going on here . "},
         new String[]{"NEGATIVE", "stupid , infantile , redundant , sloppy , over-the-top , and amateurish . yep , it's \"waking up in reno.\" go back to sleep . "},
         new String[]{"NEGATIVE", "somewhere in the middle , the film compels , as demme experiments he harvests a few movie moment gems , but the field of roughage dominates . "},
         new String[]{"NEGATIVE", "the action clichés just pile up . "},
         new String[]{"NEGATIVE", "payami tries to raise some serious issues about iran's electoral process , but the result is a film that's about as subtle as a party political broadcast . "},
         new String[]{"NEGATIVE", "the only surprise is that heavyweights joel silver and robert zemeckis agreed to produce this ; i assume the director has pictures of them cavorting in ladies' underwear . "},
         new String[]{"NEGATIVE", "another useless recycling of a brutal mid-'70s american sports movie . "},
         new String[]{"NEGATIVE", "i didn't laugh . i didn't smile . i survived . "},
         new String[]{"NEGATIVE", "please , someone , stop eric schaeffer before he makes another film . "},
         new String[]{"NEGATIVE", "most of the problems with the film don't derive from the screenplay , but rather the mediocre performances by most of the actors involved"},
         new String[]{"NEGATIVE", " . . . if you're just in the mood for a fun -- but bad -- movie , you might want to catch freaks as a matinee . "},
         new String[]{"NEGATIVE", "curling may be a unique sport but men with brooms is distinctly ordinary . "},
         new String[]{"NEGATIVE", "though the opera itself takes place mostly indoors , jacquot seems unsure of how to evoke any sort of naturalism on the set . "},
         new String[]{"NEGATIVE", "there's no getting around the fact that this is revenge of the nerds revisited -- again . "},
         new String[]{"NEGATIVE", "the effort is sincere and the results are honest , but the film is so bleak that it's hardly watchable . "},
         new String[]{"NEGATIVE", "analyze that regurgitates and waters down many of the previous film's successes , with a few new swings thrown in . "},
         new String[]{"NEGATIVE", "with flashbulb editing as cover for the absence of narrative continuity , undisputed is nearly incoherent , an excuse to get to the closing bout . . . by which time it's impossible to care who wins . "},
         new String[]{"NEGATIVE", "stinks from start to finish , like a wet burlap sack of gloom . "},
         new String[]{"NEGATIVE", "to the civilized mind , a movie like ballistic : ecks vs . sever is more of an ordeal than an amusement . "},
         new String[]{"NEGATIVE", "equlibrium could pass for a thirteen-year-old's book report on the totalitarian themes of 1984 and farenheit 451 . "},
         new String[]{"NEGATIVE", "the lack of naturalness makes everything seem self-consciously poetic and forced . . . it's a pity that [nelson's] achievement doesn't match his ambition . "},
         new String[]{"NEGATIVE", "everything is off . "},
         new String[]{"NEGATIVE", "when seagal appeared in an orange prison jumpsuit , i wanted to stand up in the theater and shout , 'hey , kool-aid ! '"},
         new String[]{"NEGATIVE", "an easy watch , except for the annoying demeanour of its lead character . "},
         new String[]{"NEGATIVE", "imagine the cleanflicks version of 'love story , ' with ali macgraw's profanities replaced by romance-novel platitudes . "},
         new String[]{"NEGATIVE", "pc stability notwithstanding , the film suffers from a simplistic narrative and a pat , fairy-tale conclusion . "},
         new String[]{"NEGATIVE", "forget the misleading title , what's with the unexplained baboon cameo ? "},
         new String[]{"NEGATIVE", "an odd , haphazard , and inconsequential romantic comedy . "},
         new String[]{"NEGATIVE", "though her fans will assuredly have their funny bones tickled , others will find their humor-seeking dollars best spent elsewhere . "},
         new String[]{"NEGATIVE", "pascale bailly's rom-com provides amélie's audrey tautou with another fabuleux destin -- i . e . , a banal spiritual quest . "},
         new String[]{"NEGATIVE", "a static and sugary little half-hour , after-school special about interfaith understanding , stretched out to 90 minutes . "},
         new String[]{"NEGATIVE", "watching the chemistry between freeman and judd , however , almost makes this movie worth seeing . almost . "},
         new String[]{"NEGATIVE", " . . . a pretentious and ultimately empty examination of a sick and evil woman ."},
         new String[]{"POSITIVE", "the rock is destined to be the 21st century's new \" conan \" and that he's going to make a splash even greater than arnold schwarzenegger , jean-claud van damme or steven segal . "},
         new String[]{"POSITIVE", "the gorgeously elaborate continuation of \" the lord of the rings \" trilogy is so huge that a column of words cannot adequately describe co-writer/director peter jackson's expanded vision of j . r . r . tolkien's middle-earth . "},
         new String[]{"POSITIVE", "effective but too-tepid biopic"},
         new String[]{"POSITIVE", "if you sometimes like to go to the movies to have fun , wasabi is a good place to start . "},
         new String[]{"POSITIVE", "emerges as something rare , an issue movie that's so honest and keenly observed that it doesn't feel like one . "},
         new String[]{"POSITIVE", "the film provides some great insight into the neurotic mindset of all comics -- even those who have reached the absolute top of the game . "},
         new String[]{"POSITIVE", "offers that rare combination of entertainment and education . "},
         new String[]{"POSITIVE", "perhaps no picture ever made has more literally showed that the road to hell is paved with good intentions . "},
         new String[]{"POSITIVE", "steers turns in a snappy screenplay that curls at the edges ; it's so clever you want to hate it . but he somehow pulls it off . "},
         new String[]{"POSITIVE", "take care of my cat offers a refreshingly different slice of asian cinema . "},
         new String[]{"POSITIVE", "this is a film well worth seeing , talking and singing heads and all . "},
         new String[]{"POSITIVE", "what really surprises about wisegirls is its low-key quality and genuine tenderness . "},
         new String[]{"POSITIVE", " ( wendigo is ) why we go to the cinema : to be fed through the eye , the heart , the mind . "},
         new String[]{"POSITIVE", "one of the greatest family-oriented , fantasy-adventure movies ever . "},
         new String[]{"POSITIVE", "ultimately , it ponders the reasons we need stories so much . "},
         new String[]{"POSITIVE", "an utterly compelling 'who wrote it' in which the reputation of the most famous author who ever lived comes into question . "},
         new String[]{"POSITIVE", "illuminating if overly talky documentary . "},
         new String[]{"POSITIVE", "a masterpiece four years in the making . "},
         new String[]{"POSITIVE", "the movie's ripe , enrapturing beauty will tempt those willing to probe its inscrutable mysteries . "},
         new String[]{"POSITIVE", "offers a breath of the fresh air of true sophistication . "},
         new String[]{"POSITIVE", "a thoughtful , provocative , insistently humanizing film . "},
         new String[]{"POSITIVE", "with a cast that includes some of the top actors working in independent film , lovely & amazing involves us because it is so incisive , so bleakly amusing about how we go about our lives . "},
         new String[]{"POSITIVE", "a disturbing and frighteningly evocative assembly of imagery and hypnotic music composed by philip glass . "},
         new String[]{"POSITIVE", "not for everyone , but for those with whom it will connect , it's a nice departure from standard moviegoing fare . "},
         new String[]{"POSITIVE", "scores a few points for doing what it does with a dedicated and good-hearted professionalism . "},
         new String[]{"POSITIVE", "occasionally melodramatic , it's also extremely effective . "},
         new String[]{"POSITIVE", "spiderman rocks"},
         new String[]{"POSITIVE", "an idealistic love story that brings out the latent 15-year-old romantic in everyone . "},
         new String[]{"POSITIVE", "at about 95 minutes , treasure planet maintains a brisk pace as it races through the familiar story . however , it lacks grandeur and that epic quality often associated with stevenson's tale as well as with earlier disney efforts . "},
         new String[]{"POSITIVE", "it helps that lil bow wow . . . tones down his pint-sized gangsta act to play someone who resembles a real kid . "},
         new String[]{"POSITIVE", "guaranteed to move anyone who ever shook , rattled , or rolled . "},
         new String[]{"POSITIVE", "a masterful film from a master filmmaker , unique in its deceptive grimness , compelling in its fatalist worldview . "},
         new String[]{"POSITIVE", "light , cute and forgettable . "},
         new String[]{"POSITIVE", "if there's a way to effectively teach kids about the dangers of drugs , i think it's in projects like the ( unfortunately r-rated ) paid . "},
         new String[]{"POSITIVE", "while it would be easy to give crush the new title of two weddings and a funeral , it's a far more thoughtful film than any slice of hugh grant whimsy . "},
         new String[]{"POSITIVE", "though everything might be literate and smart , it never took off and always seemed static . "},
         new String[]{"POSITIVE", "cantet perfectly captures the hotel lobbies , two-lane highways , and roadside cafes that permeate vincent's days"},
         new String[]{"POSITIVE", "ms . fulford-wierzbicki is almost spooky in her sulky , calculating lolita turn . "},
         new String[]{"POSITIVE", "though it is by no means his best work , laissez-passer is a distinguished and distinctive effort by a bona-fide master , a fascinating film replete with rewards to be had by all willing to make the effort to reap them . "},
         new String[]{"POSITIVE", "like most bond outings in recent years , some of the stunts are so outlandish that they border on being cartoonlike . a heavy reliance on cgi technology is beginning to creep into the series . "},
         new String[]{"POSITIVE", "newton draws our attention like a magnet , and acts circles around her better known co-star , mark wahlberg . "},
         new String[]{"POSITIVE", "the story loses its bite in a last-minute happy ending that's even less plausible than the rest of the picture . much of the way , though , this is a refreshingly novel ride . "},
         new String[]{"POSITIVE", "fuller would surely have called this gutsy and at times exhilarating movie a great yarn . "},
         new String[]{"POSITIVE", "'compleja e intelectualmente retadora , el ladrón de orquídeas es uno de esos filmes que vale la pena ver precisamente por su originalidad . '"},
         new String[]{"POSITIVE", "the film makes a strong case for the importance of the musicians in creating the motown sound . "},
         new String[]{"POSITIVE", "karmen moves like rhythm itself , her lips chanting to the beat , her long , braided hair doing little to wipe away the jeweled beads of sweat . "},
         new String[]{"POSITIVE", "gosling provides an amazing performance that dwarfs everything else in the film . "},
         new String[]{"POSITIVE", "a real movie , about real people , that gives us a rare glimpse into a culture most of us don't know . "},
         new String[]{"POSITIVE", "tender yet lacerating and darkly funny fable . "},
         new String[]{"POSITIVE", "may be spoofing an easy target -- those old '50's giant creature features -- but . . . it acknowledges and celebrates their cheesiness as the reason why people get a kick out of watching them today . "},
         new String[]{"POSITIVE", "an engaging overview of johnson's eccentric career . "},
         new String[]{"POSITIVE", "in its ragged , cheap and unassuming way , the movie works . "},
         new String[]{"POSITIVE", "some actors have so much charisma that you'd be happy to listen to them reading the phone book . hugh grant and sandra bullock are two such likeable actors . "},
         new String[]{"POSITIVE", "sandra nettelbeck beautifully orchestrates the transformation of the chilly , neurotic , and self-absorbed martha as her heart begins to open . "},
         new String[]{"POSITIVE", "behind the snow games and lovable siberian huskies ( plus one sheep dog ) , the picture hosts a parka-wrapped dose of heart . "},
         new String[]{"POSITIVE", "everytime you think undercover brother has run out of steam , it finds a new way to surprise and amuse . "},
         new String[]{"POSITIVE", "manages to be original , even though it rips off many of its ideas . "},
         new String[]{"POSITIVE", "singer/composer bryan adams contributes a slew of songs  a few potential hits , a few more simply intrusive to the story  but the whole package certainly captures the intended , er , spirit of the piece . "},
         new String[]{"POSITIVE", "you'd think by now america would have had enough of plucky british eccentrics with hearts of gold . yet the act is still charming here . "},
         new String[]{"POSITIVE", "whether or not you're enlightened by any of derrida's lectures on \" the other \" and \" the self , \" derrida is an undeniably fascinating and playful fellow . "},
         new String[]{"POSITIVE", "a pleasant enough movie , held together by skilled ensemble actors ."}
      };


      AttributeType label = Types.attribute("LABEL");

      //Simple binary featurizer that converts tokens to lower case and removes stop words
      Featurizer<HString> featurizer = Featurizer.chain(
         BagOfAnnotations.builder()
                         .valueCalculator(ValueCalculator.Binary)
                         .lowerCase()
                         .ignoreStopWords()
                         .build()
                                                       );

      //Build an in-memory dataset from a corpus constructed using the raw labels and documents in the String[][] above
      Dataset<Instance> dataset = Corpus.of(StreamingContext.local().stream(training)
                                                            .map(example ->
                                                                    Document.create(example[1],
                                                                                    Language.ENGLISH,
                                                                                    map(label, example[0])
                                                                                   )
                                                                )
                                           )
                                        .annotate(Types.TOKEN)
                                        .asClassificationDataSet(featurizer, label)
                                        .shuffle(new Random(1234));

      //Setup a supplier for a classification learner to use in cross validation
      Supplier<ClassifierLearner> supplier = Learner.classification()
                                                    .learnerClass(NaiveBayesLearner.class)
                                                    .parameter("modelType", NaiveBayes.ModelType.Bernoulli)
                                                    .supplier();

      //Perform 10-fold cross-validation and output the results to System.out
      new ClassifierEvaluation()
         .crossValidation(dataset, supplier, 10)
         .output(System.out, true);

   }

}//END OF MLExample
