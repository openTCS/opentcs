/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection.vehicles;

import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.data.order.OrderSequence;

/**
 * Test for {@link IsRereparkable}.
 */
class IsReparkableTest {

  private TCSObjectService objectService;
  private IsReparkable isReparkable;
  private Vehicle reparkableVehicle;
  private Point p1;

  @BeforeEach
  void setUp() {
    objectService = mock();
    isReparkable = new IsReparkable(objectService);
    p1 = new Point("p1").withType(Point.Type.PARK_POSITION);
    reparkableVehicle = new Vehicle("V1")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withState(Vehicle.State.IDLE)
        .withProcState(Vehicle.ProcState.IDLE)
        .withCurrentPosition(p1.getReference())
        .withAllowedOrderTypes(Set.of(OrderConstants.TYPE_ANY));

    given(objectService.fetchObject(Point.class, p1.getReference()))
        .willReturn(p1);
  }

  @ParameterizedTest
  @ValueSource(strings={OrderConstants.TYPE_ANY,OrderConstants.TYPE_PARK})
  void checkVehicleIsReparkable(String type) {
    Vehicle vehicle = reparkableVehicle
        .withAllowedOrderTypes(Set.of(type));
    assertThat(isReparkable.apply(vehicle), hasSize(0));
  }

  @ParameterizedTest
  @EnumSource(value = Vehicle.IntegrationLevel.class,
              names = {"TO_BE_IGNORED", "TO_BE_NOTICED", "TO_BE_RESPECTED"})
  void checkVehicleIsNotFullyIntegrated(Vehicle.IntegrationLevel integrationLevel) {
    Vehicle vehicle = reparkableVehicle.withIntegrationLevel(integrationLevel);

    assertThat(isReparkable.apply(vehicle), hasSize(1));
  }

  @Test
  void checkVehicleHasParkingPosition() {
    p1 = p1.withType(Point.Type.HALT_POSITION);
    Vehicle vehicle = reparkableVehicle.withCurrentPosition(p1.getReference());

    given(objectService.fetchObject(Point.class, p1.getReference()))
        .willReturn(p1);

    assertThat(isReparkable.apply(vehicle), hasSize(1));
  }

  @ParameterizedTest
  @EnumSource(value = Vehicle.State.class,
              names = {"UNKNOWN", "UNAVAILABLE", "ERROR", "EXECUTING", "CHARGING"})
  void checkVehicleHasIncorrectState(Vehicle.State state) {
    Vehicle vehicle = reparkableVehicle.withState(state);

    assertThat(isReparkable.apply(vehicle), hasSize(1));
  }

  @ParameterizedTest
  @EnumSource(value = Vehicle.ProcState.class,
              names = {"AWAITING_ORDER", "PROCESSING_ORDER"})
  void checkVehicleHasIncorrectProcState(Vehicle.ProcState procState) {
    Vehicle vehicle = reparkableVehicle.withProcState(procState);

    assertThat(isReparkable.apply(vehicle), hasSize(1));
  }

  @Test
  void checkVehicleHasOrderSequence() {
    Vehicle vehicle = reparkableVehicle
        .withOrderSequence(new OrderSequence("OS").getReference());

    assertThat(isReparkable.apply(vehicle), hasSize(1));
  }

  @Test
  void checkVehicleIsNotAllowedToPark() {
    Vehicle vehicle = reparkableVehicle
        .withAllowedOrderTypes(Set.of(OrderConstants.TYPE_CHARGE));

    assertThat(isReparkable.apply(vehicle), hasSize(1));
  }
}
