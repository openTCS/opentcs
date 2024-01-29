/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelStateTransitionEvent;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.ServiceWebApiConfiguration;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetEventsResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.OrderStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.PeripheralJobStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.VehicleStatusMessage;
import org.opentcs.util.event.EventSource;
import org.opentcs.util.event.SimpleEventBus;

/**
 * Unit tests for {@link StatusEventDispatcher}.
 */
class StatusEventDispatcherTest {

  private ServiceWebApiConfiguration configuration;
  private EventSource eventSource;
  private StatusEventDispatcher statusEventDispatcher;

  @BeforeEach
  void setUp() {
    configuration = mock(ServiceWebApiConfiguration.class);
    eventSource = new SimpleEventBus();
    statusEventDispatcher = new StatusEventDispatcher(configuration, eventSource);

    given(configuration.statusEventsCapacity())
        .willReturn(10);

    statusEventDispatcher.initialize();
  }

  @AfterEach
  void tearDown() {
    statusEventDispatcher.terminate();
  }

  @Test
  void returnEmptyListInitially() {
    GetEventsResponseTO result = statusEventDispatcher.fetchEvents(0, Long.MAX_VALUE, 1);

    assertThat(result.getStatusMessages()).isEmpty();
  }

  @Test
  void suppressEventCollectionInModellingMode() {
    // Arrange
    statusEventDispatcher.onEvent(
        new KernelStateTransitionEvent(Kernel.State.MODELLING, Kernel.State.OPERATING, true)
    );
    statusEventDispatcher.onEvent(
        new KernelStateTransitionEvent(Kernel.State.OPERATING, Kernel.State.MODELLING, true)
    );

    TransportOrder order = new TransportOrder("some-order", List.of());
    for (int i = 0; i < 5; i++) {
      statusEventDispatcher.onEvent(
          new TCSObjectEvent(order, order, TCSObjectEvent.Type.OBJECT_MODIFIED)
      );
    }

    // Act
    GetEventsResponseTO result = statusEventDispatcher.fetchEvents(0, Long.MAX_VALUE, 1);

    // Assert
    assertThat(result.getStatusMessages()).isEmpty();
  }

  @Test
  void respectConfiguredCapacity() {
    // Arrange
    statusEventDispatcher.onEvent(
        new KernelStateTransitionEvent(Kernel.State.MODELLING, Kernel.State.OPERATING, true)
    );

    TransportOrder order = new TransportOrder("some-order", List.of());
    for (int i = 0; i < 20; i++) {
      statusEventDispatcher.onEvent(
          new TCSObjectEvent(order, order, TCSObjectEvent.Type.OBJECT_MODIFIED)
      );
    }

    // Act
    GetEventsResponseTO result = statusEventDispatcher.fetchEvents(0, Long.MAX_VALUE, 1);

    // Assert
    assertThat(result.getStatusMessages()).hasSize(10);
    assertThat(result.getStatusMessages().get(9).getSequenceNumber()).isEqualTo(19);
  }

  @Test
  void processEventsForRelatedObjects() {
    // Arrange
    statusEventDispatcher.onEvent(
        new KernelStateTransitionEvent(Kernel.State.MODELLING, Kernel.State.OPERATING, true)
    );

    TransportOrder order = new TransportOrder("some-order", List.of());
    Vehicle vehicle = new Vehicle("some-vehicle");
    PeripheralJob job = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            new Location(
                "some-location",
                new LocationType("some-location-type").getReference()
            ).getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    );
    Point point = new Point("some-point");

    statusEventDispatcher.onEvent(
        new TCSObjectEvent(order, order, TCSObjectEvent.Type.OBJECT_MODIFIED)
    );
    statusEventDispatcher.onEvent(
        new TCSObjectEvent(vehicle, vehicle, TCSObjectEvent.Type.OBJECT_MODIFIED)
    );
    statusEventDispatcher.onEvent(
        new TCSObjectEvent(job, job, TCSObjectEvent.Type.OBJECT_MODIFIED)
    );
    // Events for other object types, e.g. points, should be ignored.
    statusEventDispatcher.onEvent(
        new TCSObjectEvent(point, point, TCSObjectEvent.Type.OBJECT_MODIFIED)
    );

    // Act
    GetEventsResponseTO list = statusEventDispatcher.fetchEvents(0, Long.MAX_VALUE, 1);

    // Assert
    assertThat(list.getStatusMessages()).hasSize(3);
    assertThat(list.getStatusMessages().get(0))
        .isInstanceOf(OrderStatusMessage.class)
        .matches(msg -> msg.getSequenceNumber() == 0);
    assertThat(list.getStatusMessages().get(1))
        .isInstanceOf(VehicleStatusMessage.class)
        .matches(msg -> msg.getSequenceNumber() == 1);
    assertThat(list.getStatusMessages().get(2))
        .isInstanceOf(PeripheralJobStatusMessage.class)
        .matches(msg -> msg.getSequenceNumber() == 2);
  }
}
