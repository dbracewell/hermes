package com.davidbracewell.hermes.ml;

import com.davidbracewell.apollo.ml.Feature;
import com.davidbracewell.apollo.ml.sequence.Sequence;
import com.davidbracewell.apollo.ml.sequence.SequenceFeaturizer;
import com.davidbracewell.apollo.ml.sequence.SequenceLabelerLearner;
import com.davidbracewell.apollo.ml.sequence.linear.CRFTrainer;
import com.davidbracewell.apollo.ml.sequence.linear.Solver;
import com.davidbracewell.hermes.Annotation;
import com.davidbracewell.hermes.Types;

import java.util.LinkedList;
import java.util.List;

/**
 * @author David B. Bracewell
 */
public class PhraseChunkTrainer extends BIOTrainer {
  private static final long serialVersionUID = 1L;

  public PhraseChunkTrainer() {
    super("PhraseChunkTrainer", Types.PHRASE_CHUNK);
  }

  public static void main(String[] args) {
    args = new String[]{
      "--corpus", "/home/david/test.txt",
      "--format", "CONLL",
      "--model", "/home/david/pc.bin",
      "--minFeatureCount", "5",
      "--mode", "TEST"
    };
    new PhraseChunkTrainer().run(args);
  }

  @Override
  protected SequenceFeaturizer<Annotation> getFeaturizer() {
    return SequenceFeaturizer.of(itr -> {
      List<Feature> features = new LinkedList<>();
      String pPOS, ppPOS = null, pWord, ppWord = null;
      if (itr.hasContext(-1)) {
        pWord = itr.getPrevious(1).get().toString();
        pPOS = itr.getPrevious(1).get().getPOS().asString();
        if (itr.hasContext(-2)) {
          ppWord = itr.getPrevious(2).get().toString();
          ppPOS = itr.getPrevious(2).get().getPOS().asString();
        } else {
          ppPOS = Sequence.BOS;
          ppWord = Sequence.BOS;
        }
      } else {
        pPOS = Sequence.BOS;
        pWord = Sequence.BOS;
      }

      String nPOS, nnPOS = null, nWord, nnWord = null;
      if (itr.hasContext(1)) {
        nPOS = itr.getNext(1).get().getPOS().asString();
        nWord = itr.getNext(1).get().toString();
        if (itr.hasContext(2)) {
          nnPOS = itr.getNext(2).get().getPOS().asString();
          nnWord = itr.getNext(2).get().toString();
        } else {
          nnWord = Sequence.EOS;
          nnPOS = Sequence.EOS;
        }
      } else {
        nWord = Sequence.EOS;
        nPOS = Sequence.EOS;
      }

      String word = itr.getCurrent().toString();
      String pos = itr.getCurrent().getPOS().asString();
      features.add(Feature.TRUE("w[0]=", word));
      features.add(Feature.TRUE("pos[0]=", itr.getCurrent().getPOS().asString()));
      features.add(Feature.TRUE("pos[-1,0]=", pPOS + "," + pos));
      if (ppPOS != null) {
        features.add(Feature.TRUE("w[-2,-1,0]=", ppWord + "," + pWord + "," + word));
        features.add(Feature.TRUE("pos[-2,-1,0]=", ppPOS + "," + pPOS + "," + pos));
      }
      features.add(Feature.TRUE("pos[0,+1]=", pos + "," + nPOS));
      features.add(Feature.TRUE("w[0,+1]=", word + "," + nWord));
      if (nnPOS != null) {
        features.add(Feature.TRUE("w[0,+1,+2]=", word + "," + nWord + "," + nnWord));
        features.add(Feature.TRUE("pos[0,+1,+2]=", pos + "," + nPOS + "," + nnPOS));
      }
      return features;
    });
  }

  @Override
  protected SequenceLabelerLearner getLearner() {
    CRFTrainer trainer = new CRFTrainer();
    trainer.setSolver(Solver.LBFGS);
    trainer.setMaxIterations(100);
    return trainer;
  }

}// END OF PhraseChunkTrainer
