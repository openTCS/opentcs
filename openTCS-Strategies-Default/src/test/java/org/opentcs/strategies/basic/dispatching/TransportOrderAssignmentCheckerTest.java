/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.dipatching.TransportOrderAssignmentVeto;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;

/**
 */
class TransportOrderAssignmentCheckerTest {

  private TCSObjectService objectService;
  private OrderReservationPool orderReservationPool;
  private TransportOrderAssignmentChecker checker;
  private Vehicle vehicle;
  private TransportOrder transportOrder;

  @BeforeEach
  void setUp() {
    objectService = mock(TCSObjectService.class);
    orderReservationPool = new OrderReservationPool();
    checker = new TransportOrderAssignmentChecker(objectService, orderReservationPool);

    vehicle = new Vehicle("some-vehicle")
        .withProcState(Vehicle.ProcState.IDLE)
        .withState(Vehicle.State.IDLE)
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withCurrentPosition(new Point("some-point").getReference())
        .withOrderSequence(null);
    transportOrder = new TransportOrder("some-order", new ArrayList<>())
        .withState(TransportOrder.State.DISPATCHABLE)
        .withWrappingSequence(null)
        .withIntendedVehicle(vehicle.getReference());
  }

  @ParameterizedTest
  @EnumSource(
      value = TransportOrder.State.class,
      names = {"RAW", "ACTIVE", "BEING_PROCESSED", "WITHDRAWN", "FINISHED", "FAILED", "UNROUTABLE"}
  )
  void onlyAcceptDispatchableOrders(TransportOrder.State orderState) {
    transportOrder = transportOrder.withState(orderState);

    assertThat(checker.checkTransportOrderAssignment(transportOrder),
               is(TransportOrderAssignmentVeto.TRANSPORT_ORDER_STATE_INVALID));
  }

  @Test
  void onlyAcceptOrdersWithoutWrappingSequence() {
    transportOrder
        = transportOrder.withWrappingSequence(new OrderSequence("some-seq").getReference());

    assertThat(checker.checkTransportOrderAssignment(transportOrder),
               is(TransportOrderAssignmentVeto.TRANSPORT_ORDER_PART_OF_ORDER_SEQUENCE));
  }

  @Test
  void onlyAcceptOrdersWithIntendedVehicle() {
    transportOrder = transportOrder.withIntendedVehicle(null);

    assertThat(checker.checkTransportOrderAssignment(transportOrder),
               is(TransportOrderAssignmentVeto.TRANSPORT_ORDER_INTENDED_VEHICLE_NOT_SET));
  }

  @ParameterizedTest
  @EnumSource(
      value = Vehicle.ProcState.class,
      names = {"AWAITING_ORDER", "PROCESSING_ORDER"}
  )
  void onlyAcceptIntendedVehicleWithValidProcState(Vehicle.ProcState procState) {
    vehicle = vehicle.withProcState(procState);

    when(objectService.fetchObject(Vehicle.class, vehicle.getReference()))
        .thenReturn(vehicle);

    assertThat(checker.checkTransportOrderAssignment(transportOrder),
               is(TransportOrderAssignmentVeto.VEHICLE_PROCESSING_STATE_INVALID));
  }

  @ParameterizedTest
  @EnumSource(
      value = Vehicle.State.class,
      names = {"UNKNOWN", "UNAVAILABLE", "ERROR", "EXECUTING"}
  )
  void onlyAcceptIntendedVehicleWithValidState(Vehicle.State state) {
    vehicle = vehicle.withState(state);

    when(objectService.fetchObject(Vehicle.class, vehicle.getReference()))
        .thenReturn(vehicle);

    assertThat(checker.checkTransportOrderAssignment(transportOrder),
               is(TransportOrderAssignmentVeto.VEHICLE_STATE_INVALID));
  }

  @ParameterizedTest
  @EnumSource(
      value = Vehicle.IntegrationLevel.class,
      names = {"TO_BE_IGNORED", "TO_BE_NOTICED", "TO_BE_RESPECTED"}
  )
  void onlyAcceptIntendedVehicleWithValidIntegrationLevel(Vehicle.IntegrationLevel level) {
    vehicle = vehicle.withIntegrationLevel(level);

    when(objectService.fetchObject(Vehicle.class, vehicle.getReference()))
        .thenReturn(vehicle);

    assertThat(checker.checkTransportOrderAssignment(transportOrder),
               is(TransportOrderAssignmentVeto.VEHICLE_INTEGRATION_LEVEL_INVALID));
  }

  @Test
  void onlyAcceptIntendedVehicleWithKnownPosition() {
    vehicle = vehicle.withCurrentPosition(null);

    when(objectService.fetchObject(Vehicle.class, vehicle.getReference()))
        .thenReturn(vehicle);

    assertThat(checker.checkTransportOrderAssignment(transportOrder),
               is(TransportOrderAssignmentVeto.VEHICLE_CURRENT_POSITION_UNKNOWN));
  }

  @Test
  void onlyAcceptIntendedVehicleWithoutOrderSequence() {
    vehicle = vehicle.withOrderSequence(new OrderSequence("some-seq").getReference());

    when(objectService.fetchObject(Vehicle.class, vehicle.getReference()))
        .thenReturn(vehicle);

    assertThat(checker.checkTransportOrderAssignment(transportOrder),
               is(TransportOrderAssignmentVeto.VEHICLE_PROCESSING_ORDER_SEQUENCE));
  }

  @Test
  void onlyAcceptIntendedVehicleWithoutReservation() {
    orderReservationPool.addReservation(
        new TransportOrder("some-other-order", List.of()).getReference(),
        vehicle.getReference()
    );

    when(objectService.fetchObject(Vehicle.class, vehicle.getReference()))
        .thenReturn(vehicle);

    assertThat(checker.checkTransportOrderAssignment(transportOrder),
               is(TransportOrderAssignmentVeto.GENERIC_VETO));
  }

  @Test
  void acceptValidOrderAndVehicle() {
    // No changes to vehicle, transport order or reservation pool here.
    when(objectService.fetchObject(Vehicle.class, vehicle.getReference()))
        .thenReturn(vehicle);

    assertThat(checker.checkTransportOrderAssignment(transportOrder),
               is(TransportOrderAssignmentVeto.NO_VETO));
  }
}
