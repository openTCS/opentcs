/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EventBuffer}.
 */
class EventBufferTest {

  @Test
  void checkGetEventsShouldReturnCorrectAmountOfEvents() {
    EventBuffer testobject = new EventBuffer(i -> true);

    testobject.onEvent(new Object());
    testobject.onEvent(new Object());
    testobject.onEvent(new Object());

    assertThat(testobject.getEvents(0), hasSize(3));
  }

  @Test
  void checkGetEventsShouldReturnEmptyList() {
    EventBuffer testobject = new EventBuffer(i -> true);

    testobject.onEvent(new Object());
    testobject.onEvent(new Object());
    testobject.onEvent(new Object());

    assertThat(testobject.getEvents(0), hasSize(3));
    assertThat(testobject.getEvents(0), is(empty()));
  }

  @Test
  void checkSetEventFilterShouldChangeEventFilter() {
    EventBuffer testobject = new EventBuffer(i -> true);

    testobject.setEventFilter(i -> false);

    testobject.onEvent(new Object());
    testobject.onEvent(new Object());
    testobject.onEvent(new Object());

    assertThat(testobject.getEvents(0), is(empty()));
  }

  @Test
  void checkGetEventsShouldWorkWhenTimeoutGreaterThanZero() {
    EventBuffer testobject = new EventBuffer(i -> true);

    testobject.onEvent(new Object());

    assertThat(testobject.getEvents(1000), hasSize(1));
    assertFalse(testobject.hasWaitingClient());
  }

}
