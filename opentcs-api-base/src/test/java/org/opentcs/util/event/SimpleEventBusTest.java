// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SimpleEventBus}.
 */
class SimpleEventBusTest {

  private SimpleEventBus eventBus;

  @BeforeEach
  void setUp() {
    eventBus = new SimpleEventBus();
  }

  @Test
  void forwardEventToSubscribers() {
    List<Object> receivedObjects = new ArrayList<>();
    EventHandler eventHandler = (object) -> receivedObjects.add(object);

    eventBus.subscribe(eventHandler);

    eventBus.onEvent(new Object());

    assertThat(receivedObjects, hasSize(1));

    eventBus.unsubscribe(eventHandler);
    receivedObjects.clear();

    eventBus.onEvent(new Object());

    assertThat(receivedObjects, is(empty()));
  }
}
