package com.davidbracewell.hermes.corpus;

import com.davidbracewell.collection.Streams;
import com.davidbracewell.conversion.Cast;
import com.davidbracewell.hermes.Document;
import com.davidbracewell.tuple.Tuple2;
import com.google.common.collect.Iterables;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by david on 10/10/15.
 */
public class JavaStream<T> implements CStream<T> {

  private final Stream<T> stream;

  public JavaStream(Stream<T> stream) {
    this.stream = stream;
  }

  public JavaStream(Iterable<T> iterable) {
    this.stream = Streams.from(iterable);
  }

  @Override
  public CStream<T> filter(Predicate<? super T> filter) {
    return new JavaStream<>(stream.filter(filter));
  }

  @Override
  public <R> CStream<R> map(Function<? super T, ? extends R> mapper) {
    return new JavaStream<>(stream.map(mapper));
  }

  @Override
  public <R> CStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
    return new JavaStream<>(stream.flatMap(mapper));
  }

  @Override
  public <K, V> CStream<Tuple2<K, V>> mapToPair(Function<? super T, ? extends Tuple2<? extends K, ? extends V>> mapper) {
    return new JavaStream<>(stream.map(Cast.as(mapper)));
  }

  @Override
  public <V> CStream<Map.Entry<V, Iterable<T>>> groupBy(Function<? super T, ? extends V> mapper) {
    return new JavaStream<>(
      Cast.<Stream<Map.Entry<V, Iterable<T>>>>as(stream.collect(Collectors.groupingBy(mapper)).entrySet().stream())
    );
  }

  @Override
  public Optional<T> reduce(BinaryOperator<T> reducer) {
    return stream.reduce(reducer);
  }

  @Override
  public long size() {
    return stream.count();
  }

  @Override
  public CStream<T> distinct() {
    return new JavaStream<>(stream.distinct());
  }

  @Override
  public void forEach(Consumer<? super T> consumer) {
    stream.forEach(consumer);
  }

  @Override
  public <R> R collect(Collector<? super T, T, R> collector) {
    return stream.collect(collector);
  }

  @Override
  public CStream<T> limit(long number) {
    return new JavaStream<>(stream.limit(number));
  }

  @Override
  public List<T> take(int n) {
    return stream.limit(n).collect(Collectors.toList());
  }

  @Override
  public CStream<T> skip(long n) {
    return new JavaStream<>(stream.skip(n));
  }

  @Override
  public Optional<T> first() {
    return stream.findFirst();
  }

  @Override
  public CStream<T> sample(int count) {
    if (count <= 0) {
      return new JavaStream<>(Collections.emptyList());
    }
    Random random = new Random();
    List<T> sample = stream.limit(count).collect(Collectors.toList());
    AtomicInteger k = new AtomicInteger(count + 1);
    stream.skip(count).forEach(document -> {
      int rndIndex = random.nextInt(k.getAndIncrement());
      if (rndIndex < count) {
        sample.set(rndIndex, document);
      }
    });
    return new JavaStream<>(sample.parallelStream());
  }

  public static void main(String[] args) {
    CStream<String> stream = new JavaStream<>(Arrays.asList("1", "2", "3", "4", "5", "1").stream());

    stream.groupBy(Integer::parseInt)
      .map(e -> Tuple2.of(e.getKey(), Iterables.size(e.getValue())))
      .forEach(System.out::println);


  }

}
