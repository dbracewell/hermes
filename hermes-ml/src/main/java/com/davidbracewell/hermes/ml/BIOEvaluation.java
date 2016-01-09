package com.davidbracewell.hermes.ml;

import com.davidbracewell.apollo.ml.Dataset;
import com.davidbracewell.apollo.ml.Evaluation;
import com.davidbracewell.apollo.ml.Instance;
import com.davidbracewell.apollo.ml.sequence.Labeling;
import com.davidbracewell.apollo.ml.sequence.Sequence;
import com.davidbracewell.apollo.ml.sequence.SequenceLabeler;
import com.davidbracewell.collection.Counter;
import com.davidbracewell.collection.Counters;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.string.StringUtils;
import com.davidbracewell.string.TableFormatter;
import com.davidbracewell.tuple.Tuple3;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.NonNull;

import java.io.PrintStream;
import java.util.*;

/**
 * @author David B. Bracewell
 */
public class BIOEvaluation implements Evaluation<Sequence, SequenceLabeler> {

  private final Counter<String> incorrect = Counters.newHashMapCounter();
  private final Counter<String> correct = Counters.newHashMapCounter();
  private final Counter<String> missed = Counters.newHashMapCounter();
  private final Set<String> tags = new HashSet<>();
  private double totalPhrasesGold = 0;
  private double totalPhrasesFound = 0;

  private Set<Tuple3<Integer, Integer, String>> tags(Sequence sequence) {
    Set<Tuple3<Integer, Integer, String>> tags = new HashSet<>();
    int start = 0;
    String tag = null;
    List<Instance> instances = sequence.asInstances();
    for (int i = 0; i < sequence.size(); i++) {
      String lbl = instances.get(i).getLabel().toString();
      if (lbl.startsWith("O") && tag != null) {
        tags.add(Tuple3.of(start, i, tag));
      } else if (lbl.startsWith("B-")) {
        if (tag != null) {
          tags.add(Tuple3.of(start, i, tag));
        }
        start = i;
        tag = lbl.substring(2);
      }
    }
    if (tag != null && start < sequence.size()) {
      tags.add(Tuple3.of(start, sequence.size(), tag));
    }
    return tags;
  }

  private Set<Tuple3<Integer, Integer, String>> tags(Labeling result) {
    Set<Tuple3<Integer, Integer, String>> tags = new HashSet<>();
    for (int i = 0; i < result.size(); ) {
      String lbl = result.getLabel(i);
      if (lbl.equals("O")) {
        i++;
      } else {
        String tag = lbl.substring(2);
        int start = i;
        i++;
        while (i < result.size() && result.getLabel(i).startsWith("I-") && result.getLabel(i).substring(2).equals(tag)) {
          i++;
        }
        tags.add(Tuple3.of(start, i, tag));
      }
    }
    return tags;
  }

  private void entry(Set<Tuple3<Integer, Integer, String>> gold, Set<Tuple3<Integer, Integer, String>> pred) {
    totalPhrasesFound += pred.size();
    totalPhrasesGold += gold.size();
    Sets.union(gold, pred).stream()
      .map(Tuple3::getV3)
      .forEach(tags::add);
    Sets.intersection(gold, pred).stream()
      .map(Tuple3::getV3)
      .forEach(correct::increment);
    Sets.difference(gold, pred).stream()
      .map(Tuple3::getV3)
      .forEach(missed::increment);
    Sets.difference(pred, gold).stream()
      .map(Tuple3::getV3)
      .forEach(incorrect::increment);
  }

  /**
   * Accuracy double.
   *
   * @return the double
   */
  public double accuracy() {
    return correct.sum() / (correct.sum() + incorrect.sum() + missed.sum());
  }

  public double microPrecision() {
    double c = correct.sum();
    double i = incorrect.sum();
    if (i + c <= 0) {
      return 1.0;
    }
    return c / (c + i);
  }


  private double f1(double p, double r) {
    if (p + r == 0) {
      return 0;
    }
    return (2 * p * r) / (p + r);
  }

  public double f1(String label) {
    return f1(precision(label), recall(label));
  }

  /**
   * Micro f 1 double.
   *
   * @return the double
   */
  public double microF1() {
    return f1(microPrecision(), microRecall());
  }


  public double precision(String label) {
    double c = correct.get(label);
    double i = incorrect.get(label);
    if (i + c <= 0) {
      return 1.0;
    }
    return c / (c + i);
  }

  public double microRecall() {
    double c = correct.sum();
    double m = missed.sum();
    if (m + c <= 0) {
      return 1.0;
    }
    return c / (c + m);
  }

  public double macroRecall() {
    AtomicDouble avg = new AtomicDouble(0);
    tags.forEach(t -> avg.addAndGet(recall(t)));
    return avg.get() / tags.size();
  }

  public double macroPrecision() {
    AtomicDouble avg = new AtomicDouble(0);
    tags.forEach(t -> avg.addAndGet(precision(t)));
    return avg.get() / tags.size();
  }

  public double macroF1() {
    AtomicDouble avg = new AtomicDouble(0);
    tags.forEach(t -> avg.addAndGet(f1(t)));
    return avg.get() / tags.size();
  }


  public double recall(String label) {
    double c = correct.get(label);
    double m = missed.get(label);
    if (m + c <= 0) {
      return 1.0;
    }
    return c / (c + m);
  }


  @Override
  public void evaluate(SequenceLabeler model, Dataset<Sequence> dataset) {
    dataset.forEach(sequence -> entry(tags(sequence), tags(model.label(sequence))));
  }

  @Override
  public void evaluate(SequenceLabeler model, Collection<Sequence> dataset) {
    dataset.forEach(sequence -> entry(tags(sequence), tags(model.label(sequence))));
  }

  @Override
  public void merge(@NonNull Evaluation<Sequence, SequenceLabeler> evaluation) {
    Preconditions.checkArgument(evaluation instanceof BIOEvaluation);
    BIOEvaluation other = Cast.as(evaluation);
    incorrect.merge(other.incorrect);
    correct.merge(other.correct);
    missed.merge(other.missed);
    tags.addAll(other.tags);
  }

  @Override
  public void output(@NonNull PrintStream printStream) {
    Set<String> sorted = new TreeSet<>(tags);
    printStream.println("Total Gold Phrases: " + totalPhrasesGold);
    printStream.println("Total Predicted Phrases: " + totalPhrasesFound);
    printStream.println("Total Correct: " + correct.sum());
    TableFormatter tableFormatter = new TableFormatter();
    tableFormatter
      .title("Tag Metrics")
      .header(Arrays.asList(StringUtils.EMPTY, "P", "R", "F1"));
    sorted.forEach(g ->
      tableFormatter.content(Arrays.asList(
        g,
        precision(g),
        recall(g),
        f1(g)
      ))
    );
    tableFormatter.content(Arrays.asList(
      "micro",
      microPrecision(),
      microRecall(),
      microF1()
    ));
    tableFormatter.content(Arrays.asList(
      "macro",
      macroPrecision(),
      macroRecall(),
      macroF1()
    ));
    tableFormatter.print(printStream);

  }
}// END OF SequenceEvaluation
