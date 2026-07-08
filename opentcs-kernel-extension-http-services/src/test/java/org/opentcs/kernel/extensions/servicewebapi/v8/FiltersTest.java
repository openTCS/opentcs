// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;

/**
 * Unit tests for {@link Filters}.
 */
class FiltersTest {

  @Test
  void acceptTransportOrdersRegardlessOfIntendedVehicle() {
    assertThat(
        Filters.transportOrderWithIntendedVehicle(null)
            .test(
                new TransportOrder("some-order", List.of())
                    .withIntendedVehicle(null)
            ),
        is(true)
    );

    assertThat(
        Filters.transportOrderWithIntendedVehicle(null)
            .test(
                new TransportOrder("some-order", List.of())
                    .withIntendedVehicle(new Vehicle("some-vehicle").getReference())
            ),
        is(true)
    );
  }

  @Test
  void acceptTransportOrdersWithGivenIntendedVehicle() {
    Vehicle vehicle = new Vehicle("some-vehicle");

    assertThat(
        Filters.transportOrderWithIntendedVehicle(vehicle.getReference())
            .test(
                new TransportOrder("some-order", List.of())
                    .withIntendedVehicle(null)
            ),
        is(false)
    );

    assertThat(
        Filters.transportOrderWithIntendedVehicle(null)
            .test(
                new TransportOrder("some-order", List.of())
                    .withIntendedVehicle(vehicle.getReference())
            ),
        is(true)
    );
  }

  @Test
  void acceptOrderSequencesRegardlessOfIntendedVehicle() {
    assertThat(
        Filters.orderSequenceWithIntendedVehicle(null)
            .test(
                new OrderSequence("some-sequence")
                    .withIntendedVehicle(null)
            ),
        is(true)
    );

    assertThat(
        Filters.orderSequenceWithIntendedVehicle(null)
            .test(
                new OrderSequence("some-sequence")
                    .withIntendedVehicle(new Vehicle("some-vehicle").getReference())
            ),
        is(true)
    );
  }

  @Test
  void acceptOrderSequenceWithGivenIntendedVehicle() {
    Vehicle vehicle = new Vehicle("some-vehicle");

    assertThat(
        Filters.orderSequenceWithIntendedVehicle(vehicle.getReference())
            .test(
                new OrderSequence("some-sequence")
                    .withIntendedVehicle(null)
            ),
        is(false)
    );

    assertThat(
        Filters.orderSequenceWithIntendedVehicle(null)
            .test(
                new OrderSequence("some-sequence")
                    .withIntendedVehicle(new Vehicle("some-vehicle").getReference())
            ),
        is(true)
    );
  }

  @Test
  void acceptPeripheralJobsRegardlessOfRelatedVehicle() {
    Location location
        = new Location("some-location", new LocationType("some-location-type").getReference());
    PeripheralJob job = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    );

    assertThat(
        Filters.peripheralJobWithRelatedVehicle(null)
            .test(
                job.withRelatedVehicle(null)
            ),
        is(true)
    );

    assertThat(
        Filters.peripheralJobWithRelatedVehicle(null)
            .test(
                job.withRelatedVehicle(new Vehicle("some-vehicle").getReference())
            ),
        is(true)
    );
  }

  @Test
  void acceptPeripheralJobsWithGivenRelatedVehicle() {
    Location location
        = new Location("some-location", new LocationType("some-location-type").getReference());
    PeripheralJob job = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    );
    Vehicle vehicle = new Vehicle("some-vehicle");

    assertThat(
        Filters.peripheralJobWithRelatedVehicle(vehicle.getReference())
            .test(
                job.withRelatedVehicle(null)
            ),
        is(false)
    );

    assertThat(
        Filters.peripheralJobWithRelatedVehicle(vehicle.getReference())
            .test(
                job.withRelatedVehicle(vehicle.getReference())
            ),
        is(true)
    );
  }

  @Test
  void acceptPeripheralJobsRegardlessOfRelatedTransportOrder() {
    Location location
        = new Location("some-location", new LocationType("some-location-type").getReference());
    PeripheralJob job = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    );

    assertThat(
        Filters.peripheralJobWithRelatedTransportOrder(null)
            .test(
                job.withRelatedTransportOrder(null)
            ),
        is(true)
    );

    assertThat(
        Filters.peripheralJobWithRelatedTransportOrder(null)
            .test(
                job.withRelatedTransportOrder(
                    new TransportOrder("some-order", List.of()).getReference()
                )
            ),
        is(true)
    );
  }

  @Test
  void acceptPeripheralJobsWithGivenRelatedTransportOrder() {
    Location location
        = new Location("some-location", new LocationType("some-location-type").getReference());
    PeripheralJob job = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    );
    TransportOrder order = new TransportOrder("some-order", List.of());

    assertThat(
        Filters.peripheralJobWithRelatedTransportOrder(order.getReference())
            .test(
                job.withRelatedTransportOrder(null)
            ),
        is(false)
    );

    assertThat(
        Filters.peripheralJobWithRelatedTransportOrder(order.getReference())
            .test(
                job.withRelatedTransportOrder(order.getReference())
            ),
        is(true)
    );
  }

  @ParameterizedTest
  @EnumSource(Vehicle.ProcState.class)
  void acceptVehiclesWithAnyProcState(Vehicle.ProcState procState) {
    assertThat(
        Filters.vehicleWithProcState(null)
            .test(
                new Vehicle("some-vehicle")
                    .withProcState(procState)
            ),
        is(true)
    );
  }

  @Test
  void acceptVehiclesWithGivenProcStateOnly() {
    assertThat(
        Filters.vehicleWithProcState(Vehicle.ProcState.IDLE)
            .test(
                new Vehicle("some-vehicle")
                    .withProcState(Vehicle.ProcState.IDLE)
            ),
        is(true)
    );

    assertThat(
        Filters.vehicleWithProcState(Vehicle.ProcState.IDLE)
            .test(
                new Vehicle("some-vehicle")
                    .withProcState(Vehicle.ProcState.AWAITING_ORDER)
            ),
        is(false)
    );
    assertThat(
        Filters.vehicleWithProcState(Vehicle.ProcState.IDLE)
            .test(
                new Vehicle("some-vehicle")
                    .withProcState(Vehicle.ProcState.PROCESSING_ORDER)
            ),
        is(false)
    );
  }

  @Test
  void acceptOnlyUserNotificationsCreatedAfterTimeThreshold() {
    assertThat(
        Filters.userNotificationCreatedAfter(Instant.ofEpochSecond(1000))
            .test(
                new DummyNotification(Instant.ofEpochSecond(1500))
            ),
        is(true)
    );
    assertThat(
        Filters.userNotificationCreatedAfter(Instant.ofEpochSecond(1000))
            .test(
                new DummyNotification(Instant.ofEpochSecond(500))
            ),
        is(false)
    );
  }

  @Test
  void acceptObjectsWithGivenNamesOnly() {
    Predicate<TCSObject<?>> filter = Filters.objectNameMatchesOneOf(
        List.of("some-point", "some-path", "some-location")
    );

    assertThat(filter.test(new Point("some-point")), is(true));
    assertThat(
        filter.test(
            new Path(
                "some-path",
                new Point("some-point").getReference(),
                new Point("some-other-point").getReference()
            )
        ), is(true)
    );
    assertThat(
        filter.test(
            new Location(
                "some-location",
                new LocationType("").getReference()
            )
        ), is(true)
    );

    assertThat(filter.test(new Point("some-other-point")), is(false));
  }

  private static class DummyNotification
      extends
        UserNotification {
    private final Instant timestamp;

    DummyNotification(Instant timestamp) {
      super("source", "notificationText", Level.INFORMATIONAL);
      this.timestamp = timestamp;
    }

    @Override
    public Instant getTimestamp() {
      return timestamp;
    }
  }
}
