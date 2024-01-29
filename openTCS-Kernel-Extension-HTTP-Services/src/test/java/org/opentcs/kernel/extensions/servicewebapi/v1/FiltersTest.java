/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
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
}
