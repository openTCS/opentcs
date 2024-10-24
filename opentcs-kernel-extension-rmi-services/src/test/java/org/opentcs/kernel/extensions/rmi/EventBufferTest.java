// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.rmi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Unit tests for {@link EventBuffer}.
 */
class EventBufferTest {

  private EventBuffer eventBuffer;

  @BeforeEach
  void setUp() {
    eventBuffer = new EventBuffer(event -> true);
  }

  @Test
  void checkGetEventsShouldReturnCorrectAmountOfEvents() {
    eventBuffer.onEvent(new Object());
    eventBuffer.onEvent(new Object());
    eventBuffer.onEvent(new Object());

    assertThat(eventBuffer.getEvents(0), hasSize(3));
  }

  @Test
  void checkGetEventsShouldReturnEmptyList() {
    eventBuffer.onEvent(new Object());
    eventBuffer.onEvent(new Object());
    eventBuffer.onEvent(new Object());

    assertThat(eventBuffer.getEvents(0), hasSize(3));
    assertThat(eventBuffer.getEvents(0), is(empty()));
  }

  @Test
  void checkSetEventFilterShouldChangeEventFilter() {
    eventBuffer.setEventFilter(i -> false);

    eventBuffer.onEvent(new Object());
    eventBuffer.onEvent(new Object());
    eventBuffer.onEvent(new Object());

    assertThat(eventBuffer.getEvents(0), is(empty()));
  }

  @Test
  void checkGetEventsShouldWorkWhenTimeoutGreaterThanZero() {
    eventBuffer.onEvent(new Object());

    assertThat(eventBuffer.getEvents(1000), hasSize(1));
    assertFalse(eventBuffer.hasWaitingClient());
  }

  @Test
  void aggregateConsecutiveTcsObjectEventsForSameObjects() {
    Point point = new Point("point");
    Point pointA = point.withType(Point.Type.PARK_POSITION);
    Point pointB = pointA.withProperty("some-key", "some-value");
    Point pointC = pointB.withProperty("some-other-key", "some-other-value");
    TCSObjectEvent event1 = new TCSObjectEvent(
        pointA,
        point,
        TCSObjectEvent.Type.OBJECT_MODIFIED
    );
    TCSObjectEvent event5 = new TCSObjectEvent(
        pointB,
        pointA,
        TCSObjectEvent.Type.OBJECT_MODIFIED
    );
    TCSObjectEvent event6 = new TCSObjectEvent(
        pointC,
        pointB,
        TCSObjectEvent.Type.OBJECT_MODIFIED
    );

    Vehicle vehicle = new Vehicle("vehicle");
    Vehicle vehicleA = vehicle.withEnergyLevel(42);
    Vehicle vehicleB = vehicleA.withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED);
    Vehicle vehicleC = vehicleB.withBoundingBox(new BoundingBox(1382, 1000, 1000));

    TCSObjectEvent event2 = new TCSObjectEvent(
        vehicleA,
        vehicle,
        TCSObjectEvent.Type.OBJECT_MODIFIED
    );
    TCSObjectEvent event3 = new TCSObjectEvent(
        vehicleB,
        vehicleA,
        TCSObjectEvent.Type.OBJECT_MODIFIED
    );
    TCSObjectEvent event4 = new TCSObjectEvent(
        vehicleC,
        vehicleB,
        TCSObjectEvent.Type.OBJECT_MODIFIED
    );

    eventBuffer.onEvent(event1);
    eventBuffer.onEvent(event2);
    eventBuffer.onEvent(event3);
    eventBuffer.onEvent(event4);
    eventBuffer.onEvent(event5);
    eventBuffer.onEvent(event6);

    List<Object> result = eventBuffer.getEvents(0);
    assertThat(result, hasSize(3));
    assertThat(result.get(0), is(equalTo(event1)));
    assertThat(result.get(1), is(equalTo(event4)));
    assertThat(result.get(2), is(equalTo(event6)));
  }

  @Test
  void dontAggregateEventsOfTypeCreateOrRemoved() {
    Vehicle vehicle = new Vehicle("vehicle");
    Vehicle vehicleA = vehicle.withEnergyLevel(42);

    TCSObjectEvent event1 = new TCSObjectEvent(
        vehicle,
        null,
        TCSObjectEvent.Type.OBJECT_CREATED
    );
    TCSObjectEvent event2 = new TCSObjectEvent(
        vehicleA,
        vehicle,
        TCSObjectEvent.Type.OBJECT_MODIFIED
    );
    TCSObjectEvent event3 = new TCSObjectEvent(
        null,
        vehicleA,
        TCSObjectEvent.Type.OBJECT_REMOVED
    );

    eventBuffer.onEvent(event1);
    eventBuffer.onEvent(event2);
    eventBuffer.onEvent(event3);

    List<Object> result = eventBuffer.getEvents(0);
    assertThat(result, hasSize(3));
    assertThat(result.get(0), is(equalTo(event1)));
    assertThat(result.get(1), is(equalTo(event2)));
    assertThat(result.get(2), is(equalTo(event3)));
  }
}
