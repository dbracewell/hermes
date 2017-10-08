package com.davidbracewell.hermes.corpus.processing;

import com.davidbracewell.apollo.ml.embedding.Embedding;
import com.davidbracewell.apollo.stat.measure.Similarity;
import com.davidbracewell.collection.counter.Counter;
import com.davidbracewell.collection.counter.Counters;
import com.davidbracewell.config.Config;
import com.davidbracewell.guava.common.base.Throwables;
import com.davidbracewell.hermes.Types;
import com.davidbracewell.hermes.corpus.Corpus;
import com.davidbracewell.hermes.extraction.TermExtractor;
import com.davidbracewell.hermes.lexicon.TrieWordList;
import com.davidbracewell.io.Resources;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.string.StringPredicates;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import static com.davidbracewell.tuple.Tuples.$;

/**
 * @author David B. Bracewell
 */
public class SpellcheckerModule implements ProcessingModule, Serializable {
    private static final long serialVersionUID = 1L;
    private final Embedding spellingEmbedding;
    private final TrieWordList dictionary;
    private final int maxCost;

    public SpellcheckerModule(@NonNull Embedding spellingEmbedding) {
        this(spellingEmbedding,
             Config
                 .get("SpellcheckerModule.dictionary")
                 .asResource(Resources.fromString()),
             2
        );
    }

    public SpellcheckerModule(@NonNull Embedding spellingEmbedding, @NonNull Resource dictionary, int maxCost) {
        this.spellingEmbedding = spellingEmbedding;
        try {
            this.dictionary = TrieWordList.read(dictionary, true);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        this.maxCost = maxCost;
    }

    @Override
    public Corpus process(Corpus corpus, ProcessorContext context) throws Exception {
        final Counter<String> unigrams = corpus.documentFrequencies(TermExtractor
                                                                        .create()
                                                                        .annotationType(Types.TOKEN)
                                                                        .filter(
                                                                            token -> StringPredicates.IS_LETTER_OR_WHITESPACE.test(
                                                                                token)
                                                                                         && token.length() >= 3
                                                                                         && spellingEmbedding.contains(
                                                                                token.toString())
                                                                        )
                                                                        .lemmatize()
        );

        final Map<String, String> spelling = corpus
                                                 .getStreamingContext()
                                                 .stream(unigrams.items())
                                                 .filter(w -> !dictionary.contains(w))
                                                 .mapToPair(oov -> {
                                                     Map<String, Integer> suggestions = dictionary.suggest(oov,
                                                                                                           maxCost);
                                                     final Counter<String> adjusted = Counters.newCounter();
                                                     int min = suggestions
                                                                   .values()
                                                                   .stream()
                                                                   .mapToInt(i -> i)
                                                                   .min()
                                                                   .orElse(2);
                                                     suggestions
                                                         .entrySet()
                                                         .stream()
                                                         .filter(e -> e.getValue() <= min)
                                                         .filter(e -> spellingEmbedding.contains(e.getKey()))
                                                         .filter(e -> unigrams.get(e.getKey()) >= 10)
                                                         .forEach(e -> {
                                                             double sim = Similarity.Cosine.calculate(
                                                                 spellingEmbedding.get(e.getKey()),
                                                                 spellingEmbedding.get(oov));
                                                             if (sim > 0) {
                                                                 adjusted.increment(e.getKey(), sim);
                                                             }
                                                         });
                                                     return $(oov, adjusted.max());
                                                 })
                                                 .collectAsMap();


        return corpus.map(document -> {
            document
                .tokenStream()
                .forEach(token -> {
                    if (spelling.containsKey(token.getLemma())) {
                        token.put(Types.SPELLING_CORRECTION, spelling.get(token.getLemma()));
                    }
                });
            return document;
        });
    }

}// END OF SpellcheckerModule
