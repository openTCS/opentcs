/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link UnmodifiableDeque}.
 */
public class UnmodifiableDequeTest {

  private Deque<String> modifiableDeque;
  private UnmodifiableDeque<String> unmodifiableDeque;

  @BeforeEach
  public void setUp() {
    modifiableDeque = new LinkedBlockingDeque<>();
    modifiableDeque.add("Element1");
    modifiableDeque.add("Element2");
    unmodifiableDeque = new UnmodifiableDeque<>(modifiableDeque);
  }

  @Test
  void addFirstThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.addFirst("NewElement")
    );
  }

  @Test
  void addLastThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.addLast("NewElement")
    );
  }

  @Test
  void offerFirstThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.offerFirst("NewElement")
    );
  }

  @Test
  void offerLastThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.offerLast("NewElement")
    );
  }

  @Test
  void removeFirstThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.removeFirst()
    );
  }

  @Test
  void removeLastThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.removeLast()
    );
  }

  @Test
  void pollFirstThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.pollFirst()
    );
  }

  @Test
  void pollLastThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.pollLast()
    );
  }

  @Test
  void getFirstReturnsTheFirstElement() {
    assertThat(unmodifiableDeque.getFirst(), is(equalTo("Element1")));
  }

  @Test
  void getLastReturnsTheLastElement() {
    assertThat(unmodifiableDeque.getLast(), is(equalTo("Element2")));
  }

  @Test
  void peekFirstReturnsTheFirstElementOrNullIfEmpty() {
    assertThat(unmodifiableDeque.peekFirst(), is(equalTo("Element1")));

    modifiableDeque.removeAll(modifiableDeque);
    UnmodifiableDeque<String> unmodifiableDequeEmpty = new UnmodifiableDeque<>(modifiableDeque);
    assertThat(unmodifiableDequeEmpty.peekFirst(), is(equalTo(null)));
  }

  @Test
  void peekLastReturnsTheLastElementOrNullIfEmpty() {
    assertThat(unmodifiableDeque.peekLast(), is(equalTo("Element2")));

    modifiableDeque.removeAll(modifiableDeque);
    UnmodifiableDeque<String> unmodifiableDequeEmpty = new UnmodifiableDeque<>(modifiableDeque);
    assertThat(unmodifiableDequeEmpty.peekLast(), is(equalTo(null)));
  }

  @Test
  void removeFirstOccurenceThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.removeFirstOccurrence("Element1")
    );
  }

  @Test
  void removeLastOccurenceThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.removeLastOccurrence("Element2")
    );
  }

  @Test
  void addThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.add("NewElement")
    );
  }

  @Test
  void offerThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.offer("NewElement")
    );
  }

  @Test
  void removeThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.remove()
    );
  }

  @Test
  void pollThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.poll()
    );
  }

  @Test
  void elementReturnsTheHeadOfThisDequeOrNoSuchElementExceptionIfEmpty() {
    assertThat(unmodifiableDeque.element(), is(equalTo("Element1")));

    modifiableDeque.removeAll(modifiableDeque);
    UnmodifiableDeque<String> unmodifiableDequeEmpty = new UnmodifiableDeque<>(modifiableDeque);
    assertThrows(
        NoSuchElementException.class,
        () -> unmodifiableDeque.element()
    );
  }

  @Test
  void peekReturnsTheHeadOfThisDequeOrNullIfEmpty() {
    assertThat(unmodifiableDeque.peek(), is(equalTo("Element1")));

    modifiableDeque.removeAll(modifiableDeque);
    UnmodifiableDeque<String> unmodifiableDequeEmpty = new UnmodifiableDeque<>(modifiableDeque);
    assertThat(unmodifiableDequeEmpty.peek(), is(equalTo(null)));
  }

  @Test
  void addAllThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.addAll(modifiableDeque)
    );
  }

  @Test
  void pushThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.push("NewElement")
    );
  }

  @Test
  void popThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.pop()
    );
  }

  @Test
  void booleanRemoveThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.remove("Element1")
    );
  }

  @Test
  void containsReturnsTrueIfTheDequeContainsTheElement() {
    assertThat(unmodifiableDeque.contains("Element1"), is(true));
  }

  @Test
  void sizeReturnsTheSizeOfADeque() {
    assertThat(unmodifiableDeque.size(), is(2));
  }

  @Test
  void iteratorReturnsAnIteratorOverTheElementsInADeque() {
    Iterator<String> iterator = unmodifiableDeque.iterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(equalTo("Element1")));

    Consumer<String> mockConsumer = mock();
    iterator.forEachRemaining(mockConsumer);
    verify(mockConsumer, times(1)).accept("Element2");
  }

  @Test
  void descendingIteratorReturnsAnIteratorOverTheElementsInADequeInReverse() {
    Iterator<String> iterator = unmodifiableDeque.descendingIterator();
    assertThat(iterator.hasNext(), is(true));
    assertThat(iterator.next(), is(equalTo("Element2")));

    Consumer<String> mockConsumer = mock();
    iterator.forEachRemaining(mockConsumer);

    verify(mockConsumer, times(1)).accept("Element1");
  }

  @Test
  void isEmptyReturnsTrueIfDequeIsEmpty() {
    modifiableDeque.removeAll(modifiableDeque);
    UnmodifiableDeque<String> unmodifiableDequeEmpty = new UnmodifiableDeque<>(modifiableDeque);
    assertThat(unmodifiableDequeEmpty.isEmpty(), is(true));
  }

  @Test
  void toArrayReturnsAnArrayWithAllElementsInTheDeque() {
    Object[] array = unmodifiableDeque.toArray();
    assertThat(array, arrayWithSize(2));
    assertThat(array[0], is(equalTo("Element1")));
    assertThat(array[1], is(equalTo("Element2")));
  }

  @Test
  void toArrayWithTypeReturnsAnArrayWithAllElementsInTheDeque() {
    String[] array = unmodifiableDeque.toArray(new String[2]);
    assertThat(array, arrayWithSize(2));
    assertThat(array[0], is(equalTo("Element1")));
    assertThat(array[1], is(equalTo("Element2")));
  }

  @Test
  void toArrayWithFunktionReturnsAnArrayWithAllElementsInTheDeque() {
    Object[] array = unmodifiableDeque.toArray(String[]::new);
    assertThat(array, arrayWithSize(2));
    assertThat(array[0], is(equalTo("Element1")));
    assertThat(array[1], is(equalTo("Element2")));
  }

  @Test
  void toStringReturnsAStringRepresentationOfTheDeque() {
    assertThat(unmodifiableDeque.toString(), is(equalTo("[Element1, Element2]")));
  }

  @Test
  void containsAllReturnsTrueIfTheDequeContainsAllElements() {
    assertThat(unmodifiableDeque.containsAll(modifiableDeque), is(true));
  }

  @Test
  void removeAllThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.removeAll(modifiableDeque)
    );
  }

  @Test
  void retainAllThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.retainAll(modifiableDeque)
    );
  }

  @Test
  void clearThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.clear()
    );
  }

  @Test
  void forEachPerformsTheGivenActionForEachElementOfTheDeque() {
    Consumer<String> mockConsumer = mock();
    unmodifiableDeque.forEach(mockConsumer);

    verify(mockConsumer, times(1)).accept("Element1");
    verify(mockConsumer, times(1)).accept("Element2");
  }

  @Test
  void removeIfThrowsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> unmodifiableDeque.removeIf(element -> element.equals("Element1"))
    );
  }

  @Test
  void spliteratorReturnsASpliteratorOverTheElementsInTheDeque() {
    Spliterator<String> splitIterator = unmodifiableDeque.spliterator();
    Consumer<String> mockConsumer = mock();
    splitIterator.forEachRemaining(mockConsumer);
    verify(mockConsumer, times(1)).accept("Element1");
    verify(mockConsumer, times(1)).accept("Element2");
  }

  @Test
  void streamReturnsASequentialStreamOverTheElementsInTheDeque() {
    List<String> list = unmodifiableDeque.stream().collect(Collectors.toList());
    assertThat(list, contains("Element1", "Element2"));
  }

  @Test
  void parallelStreamReturnsAParallelStreanOverTheElementsInTheDeque() {
    List<String> list = unmodifiableDeque.parallelStream().collect(Collectors.toList());
    assertThat(list, contains("Element1", "Element2"));
  }
}
