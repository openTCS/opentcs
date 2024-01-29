/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.event.SimpleEventBus;

/**
 * Unit tests for {@link TransportOrderPoolManager}.
 */
class TransportOrderPoolManagerTest {

  /**
   * The object repository.
   */
  private TCSObjectRepository objectRepo;
  /**
   * Manages plant model data.
   */
  private PlantModelManager plantModelManager;
  /**
   * The order pool manager to be tested here.
   */
  private TransportOrderPoolManager orderPoolManager;

  @BeforeEach
  void setUp() {
    objectRepo = new TCSObjectRepository();
    plantModelManager = new PlantModelManager(objectRepo, new SimpleEventBus());
    orderPoolManager = new TransportOrderPoolManager(objectRepo,
                                                     new SimpleEventBus(),
                                                     new PrefixedUlidObjectNameProvider());

    // Set up a minimal plant model.
    plantModelManager.createPlantModelObjects(
        new PlantModelCreationTO("some-plant-model")
            .withLocationType(new LocationTypeCreationTO("some-location-type"))
            .withLocation(new LocationCreationTO("some-location",
                                                 "some-location-type",
                                                 new Triple(1, 2, 3))
                .withLink("some-point", new HashSet<>())
            )
            .withPoint(new PointCreationTO("some-point"))
    );
  }

  @Test
  void storeCreatedObjectsInRepo() {
    orderPoolManager.createTransportOrder(
        new TransportOrderCreationTO("some-order",
                                     List.of(new DestinationCreationTO("some-location", "NOP")))
            .withIncompleteName(false)
    );
    orderPoolManager.createOrderSequence(new OrderSequenceCreationTO("some-sequence"));

    assertThat(objectRepo.getObjects(TransportOrder.class), hasSize(1));
    assertThat(objectRepo.getObject(TransportOrder.class, "some-order"), is(notNullValue()));
    assertThat(objectRepo.getObjects(OrderSequence.class), hasSize(1));
    assertThat(objectRepo.getObject(OrderSequence.class, "some-sequence"), is(notNullValue()));
  }

  @Test
  void removeAllCreatedObjectsOnClear() {
    orderPoolManager.createTransportOrder(
        new TransportOrderCreationTO("some-order",
                                     List.of(new DestinationCreationTO("some-location", "NOP")))
    );
    orderPoolManager.createOrderSequence(new OrderSequenceCreationTO("some-sequence"));

    orderPoolManager.clear();

    assertThat(objectRepo.getObjects(TransportOrder.class), is(empty()));
    assertThat(objectRepo.getObjects(OrderSequence.class), is(empty()));
  }

  @ParameterizedTest
  @EnumSource(value = TransportOrder.State.class,
              names = {"RAW", "ACTIVE", "DISPATCHABLE"})
  void allowSettingIntendedVehicleOnUnassignedTransportOrder(TransportOrder.State state) {
    plantModelManager.createPlantModelObjects(
        new PlantModelCreationTO("some-model")
            .withPoint(new PointCreationTO("some-point"))
            .withLocationType(
                new LocationTypeCreationTO("some-location-type")
                    .withAllowedOperations(List.of("NOP"))
            )
            .withLocation(
                new LocationCreationTO("some-location", "some-location-type", new Triple(1, 2, 3))
                    .withLink("some-point", Set.of("NOP"))
            )
            .withVehicle(new VehicleCreationTO("some-vehicle"))
    );
    Vehicle vehicle = objectRepo.getObject(Vehicle.class, "some-vehicle");

    TransportOrder order = orderPoolManager.createTransportOrder(
        new TransportOrderCreationTO("some-order",
                                     List.of(new DestinationCreationTO("some-location", "NOP")))
    );
    orderPoolManager.setTransportOrderState(order.getReference(), state);

    TransportOrder result
        = orderPoolManager.setTransportOrderIntendedVehicle(order.getReference(),
                                                            vehicle.getReference());

    assertThat(result.getIntendedVehicle(), is(equalTo(vehicle.getReference())));
  }

  @ParameterizedTest
  @EnumSource(value = TransportOrder.State.class,
              names = {"BEING_PROCESSED", "WITHDRAWN", "FINISHED", "FAILED", "UNROUTABLE"})
  void disallowSettingIntendedVehicleOnAssignedTransportOrder(TransportOrder.State state) {
    plantModelManager.createPlantModelObjects(
        new PlantModelCreationTO("some-model")
            .withPoint(new PointCreationTO("some-point"))
            .withLocationType(
                new LocationTypeCreationTO("some-location-type")
                    .withAllowedOperations(List.of("NOP"))
            )
            .withLocation(
                new LocationCreationTO("some-location", "some-location-type", new Triple(1, 2, 3))
                    .withLink("some-point", Set.of("NOP"))
            )
            .withVehicle(new VehicleCreationTO("some-vehicle"))
    );
    Vehicle vehicle = objectRepo.getObject(Vehicle.class, "some-vehicle");

    TransportOrder order = orderPoolManager.createTransportOrder(
        new TransportOrderCreationTO("some-order",
                                     List.of(new DestinationCreationTO("some-location", "NOP")))
    );
    orderPoolManager.setTransportOrderState(order.getReference(), state);

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          orderPoolManager.setTransportOrderIntendedVehicle(order.getReference(),
                                                            vehicle.getReference());
        });
  }

  @ParameterizedTest
  @EnumSource(value = TransportOrder.State.class,
              names = {"FINISHED", "FAILED", "UNROUTABLE"})
  void removeSingleTransportOrderIfFinished(TransportOrder.State state) {
    TransportOrder order = orderPoolManager.createTransportOrder(
        new TransportOrderCreationTO("some-order",
                                     List.of(new DestinationCreationTO("some-location", "NOP")))
    );
    orderPoolManager.setTransportOrderState(order.getReference(), state);
    orderPoolManager.removeTransportOrder(order.getReference());

    assertThat(objectRepo.getObjects(TransportOrder.class), is(empty()));
  }

  @Test
  void removeSingleOrderSequence() {
    OrderSequence sequence = orderPoolManager.createOrderSequence(
        new OrderSequenceCreationTO("some-sequence")
    );

    orderPoolManager.removeOrderSequence(sequence.getReference());

    assertThat(objectRepo.getObjects(OrderSequence.class), is(empty()));
  }
}
