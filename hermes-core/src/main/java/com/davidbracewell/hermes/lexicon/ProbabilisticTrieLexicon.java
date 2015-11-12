package com.davidbracewell.hermes.lexicon;

import com.davidbracewell.collection.trie.TrieMatch;
import com.davidbracewell.hermes.Attrs;
import com.davidbracewell.hermes.HString;
import com.davidbracewell.io.CSV;
import com.davidbracewell.io.resource.Resource;
import com.davidbracewell.io.structured.csv.CSVReader;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;

/**
 * @author David B. Bracewell
 */
public class ProbabilisticTrieLexicon extends BaseTrieLexicon<Double> implements ProbabilisticLexicon {


  public ProbabilisticTrieLexicon(@NonNull Map<String, Double> lexicon, boolean isCaseSensitive) {
    super(lexicon, isCaseSensitive);
  }

  public ProbabilisticTrieLexicon(boolean isCaseSensitive) {
    super(isCaseSensitive);
  }

  public static ProbabilisticTrieLexicon read(@NonNull Resource resource, boolean isCaseSensitive) throws IOException {
    ProbabilisticTrieLexicon lexicon = new ProbabilisticTrieLexicon(isCaseSensitive);
    try (CSVReader reader = CSV.builder().reader(resource)) {
      reader.forEach(row -> {
        if (row.size() > 1) {
          lexicon.trie.put(lexicon.normalize(row.get(0)), Double.valueOf(row.get(1)));
        }
      });
    }
    return lexicon;
  }

  @Override
  protected HString createMatch(HString source, TrieMatch<Double> match) {
    HString m = source.substring(match.start, match.end);
    m.put(Attrs.CONFIDENCE, getProbability(source));
    return m;
  }

  @Override
  public double getProbability(HString hString) {
    return getMatch(hString).map(trie::get).orElse(0d);
  }

}// END OF ProbabilisticTrieLexicon
