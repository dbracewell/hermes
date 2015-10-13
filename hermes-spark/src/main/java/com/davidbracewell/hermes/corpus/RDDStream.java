package com.davidbracewell.hermes.corpus;

import lombok.NonNull;
import org.apache.spark.api.java.JavaRDD;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

/**
 * Created by david on 10/10/15.
 */
public class RDDStream<T> implements Stream<T> {

  public RDDStream(@NonNull JavaRDD<T> rdd) {
    this.rdd = rdd;
  }

  private final JavaRDD<T> rdd;

  @Override
  public Stream<T> filter(Predicate<? super T> predicate) {
    return new RDDStream<>(rdd.filter(predicate::test));
  }

  @Override
  public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
    return new RDDStream<>(rdd.map(mapper::apply));
  }

  @Override
  public IntStream mapToInt(ToIntFunction<? super T> mapper) {
    return null;
  }

  @Override
  public LongStream mapToLong(ToLongFunction<? super T> mapper) {
    return null;
  }

  @Override
  public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
    return null;
  }

  @Override
  public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
    return new RDDStream<>(rdd.flatMap(t -> mapper.apply(t).collect(Collectors.toList())));
  }

  @Override
  public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
    return null;
  }

  @Override
  public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
    return null;
  }

  @Override
  public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
    return null;
  }

  @Override
  public Stream<T> distinct() {
    return new RDDStream<>(rdd.distinct());
  }

  @Override
  public Stream<T> sorted() {
    return new RDDStream<>(rdd.sortBy(t -> t, true, 100));
  }

  @Override
  public Stream<T> sorted(Comparator<? super T> comparator) {
    return null;
  }

  @Override
  public Stream<T> peek(Consumer<? super T> action) {
    return null;
  }

  @Override
  public Stream<T> limit(long maxSize) {
    return rdd.take((int) maxSize).stream();
  }

  @Override
  public Stream<T> skip(long n) {
    return null;
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    rdd.foreach(action::accept);
  }

  @Override
  public void forEachOrdered(Consumer<? super T> action) {
    rdd.foreach(action::accept);
  }

  @Override
  public Object[] toArray() {
    return new Object[0];
  }

  @Override
  public <A> A[] toArray(IntFunction<A[]> generator) {
    return null;
  }

  @Override
  public T reduce(T identity, BinaryOperator<T> accumulator) {
    T value = rdd.reduce(accumulator::apply);
    return value == null ? identity : value;
  }

  @Override
  public Optional<T> reduce(BinaryOperator<T> accumulator) {
    return Optional.ofNullable(rdd.reduce(accumulator::apply));
  }

  @Override
  public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
    return null;
  }

  @Override
  public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
    return null;
  }

  @Override
  public <R, A> R collect(Collector<? super T, A, R> collector) {
    return null;
  }

  @Override
  public Optional<T> min(Comparator<? super T> comparator) {
    return null;
  }

  @Override
  public Optional<T> max(Comparator<? super T> comparator) {
    return null;
  }
m
  @Override
  public long count() {
    return 0;
  }

  @Override
  public boolean anyMatch(Predicate<? super T> predicate) {
    return false;
  }

  @Override
  public boolean allMatch(Predicate<? super T> predicate) {
    return false;
  }

  @Override
  public boolean noneMatch(Predicate<? super T> predicate) {
    return false;
  }

  @Override
  public Optional<T> findFirst() {
    return null;
  }

  @Override
  public Optional<T> findAny() {
    return null;
  }

  @Override
  public Iterator<T> iterator() {
    return null;
  }

  @Override
  public Spliterator<T> spliterator() {
    return null;
  }

  @Override
  public boolean isParallel() {
    return rdd.partitions().size() > 1;
  }

  @Override
  public Stream<T> sequential() {
    return new RDDStream<>(rdd.coalesce(1));
  }

  @Override
  public Stream<T> parallel() {
    return this;
  }

  @Override
  public Stream<T> unordered() {
    return null;
  }

  @Override
  public Stream<T> onClose(Runnable closeHandler) {
    return null;
  }

  @Override
  public void close() {

  }
}
