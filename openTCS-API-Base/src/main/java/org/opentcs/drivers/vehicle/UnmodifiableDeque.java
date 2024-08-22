/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Unmodifiable deque.
 *
 * @param <E> The type of elements in this deque.
 */
class UnmodifiableDeque<E>
    implements
      Deque<E> {

  private final Deque<E> deque;

  UnmodifiableDeque(Deque<E> deque) {
    this.deque = requireNonNull(deque, "deque");
  }

  @Override
  public void addFirst(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addLast(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean offerFirst(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean offerLast(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public E removeFirst() {
    throw new UnsupportedOperationException();
  }

  @Override
  public E removeLast() {
    throw new UnsupportedOperationException();
  }

  @Override
  public E pollFirst() {
    throw new UnsupportedOperationException();
  }

  @Override
  public E pollLast() {
    throw new UnsupportedOperationException();
  }

  @Override
  public E getFirst() {
    return this.iterator().next();
  }

  @Override
  public E getLast() {
    return this.reversed().iterator().next();
  }

  @Override
  public E peekFirst() {
    return deque.peekFirst();
  }

  @Override
  public E peekLast() {
    return deque.peekLast();
  }

  @Override
  public boolean removeFirstOccurrence(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeLastOccurrence(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean add(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean offer(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public E remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public E poll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public E element() {
    return deque.element();
  }

  @Override
  public E peek() {
    return deque.peek();
  }

  @Override
  public boolean addAll(Collection<? extends E> coll) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void push(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public E pop() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(Object o) {
    return deque.contains(o);
  }

  @Override
  public int size() {
    return deque.size();
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      private final Iterator<? extends E> i = deque.iterator();

      @Override
      public boolean hasNext() {
        return i.hasNext();
      }

      @Override
      public E next() {
        return i.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      @Override
      public void forEachRemaining(Consumer<? super E> action) {
        i.forEachRemaining(action);
      }
    };
  }

  @Override
  public Iterator<E> descendingIterator() {
    return new Iterator<E>() {
      private final Iterator<? extends E> i = deque.descendingIterator();

      @Override
      public boolean hasNext() {
        return i.hasNext();
      }

      @Override
      public E next() {
        return i.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      @Override
      public void forEachRemaining(Consumer<? super E> action) {
        i.forEachRemaining(action);
      }
    };
  }

  @Override
  public boolean isEmpty() {
    return deque.isEmpty();
  }

  @Override
  public Object[] toArray() {
    return deque.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return deque.toArray(a);
  }

  @Override
  public <T> T[] toArray(IntFunction<T[]> f) {
    return deque.toArray(f);
  }

  @Override
  public String toString() {
    return deque.toString();
  }

  @Override
  public boolean containsAll(Collection<?> coll) {
    return deque.containsAll(coll);
  }

  @Override
  public boolean removeAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    deque.forEach(action);
  }

  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Spliterator<E> spliterator() {
    return deque.spliterator();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Stream<E> stream() {
    return deque.stream();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Stream<E> parallelStream() {
    return deque.parallelStream();
  }
}
