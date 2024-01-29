/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.event;

import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
