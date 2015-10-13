package com.davidbracewell.hermes.corpus;

import com.davidbracewell.tuple.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Created by david on 10/10/15.
 */
public interface CStream<T> {

  CStream<T> filter(Predicate<? super T> filter);

  <R> CStream<R> map(Function<? super T, ? extends R> mapper);

  <R> CStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

  <K, V> CStream<Tuple2<K, V>> mapToPair(Function<? super T, ? extends Tuple2<? extends K, ? extends V>> mapper);

  <V> CStream<Map.Entry<V, Iterable<T>>> groupBy(Function<? super T, ? extends V> mapper);

  Optional<T> first();

  CStream<T> sample(int number);

  Optional<T> reduce(BinaryOperator<T> reducer);

  long size();

  CStream<T> distinct();

  void forEach(Consumer<? super T> consumer);

  <R> R collect(Collector<? super T, T, R> collector);

  CStream<T> limit(long number);

  List<T> take(int n);

  CStream<T> skip(long n);

}
