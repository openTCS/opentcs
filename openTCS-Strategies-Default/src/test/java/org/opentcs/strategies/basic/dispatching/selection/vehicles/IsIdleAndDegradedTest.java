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
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.data.order.OrderSequence;

/**
 * Test for {@link IsIdleAndDegraded}.
 */
class IsIdleAndDegradedTest {

  private Vehicle idleAndDegradedVehicle;
  private IsIdleAndDegraded isIdleAndDegraded;

  @BeforeEach
  void setUp() {
    isIdleAndDegraded = new IsIdleAndDegraded();
    idleAndDegradedVehicle = new Vehicle("V1")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withState(Vehicle.State.IDLE)
        .withProcState(Vehicle.ProcState.IDLE)
        .withCurrentPosition(new Point("p1").getReference())
        .withEnergyLevel(10)
        .withAllowedOrderTypes(Set.of(OrderConstants.TYPE_ANY));
  }

  @ParameterizedTest
  @ValueSource(strings = {OrderConstants.TYPE_ANY, OrderConstants.TYPE_CHARGE})
  void checkVehicleIsIdleAndDegraded(String type) {
    Vehicle vehicle = idleAndDegradedVehicle
        .withAllowedOrderTypes(Set.of(type));
    assertThat(isIdleAndDegraded.apply(vehicle), hasSize(0));
  }

  @ParameterizedTest
  @EnumSource(value = Vehicle.IntegrationLevel.class,
              names = {"TO_BE_IGNORED", "TO_BE_NOTICED", "TO_BE_RESPECTED"})
  void checkVehicleIsNotFullyIntegrated(Vehicle.IntegrationLevel integrationLevel) {
    Vehicle vehicle = idleAndDegradedVehicle.withIntegrationLevel(integrationLevel);

    assertThat(isIdleAndDegraded.apply(vehicle), hasSize(1));
  }

  @Test
  void checkVehicleHasNoPosition() {
    Vehicle vehicle = idleAndDegradedVehicle.withCurrentPosition(null);

    assertThat(isIdleAndDegraded.apply(vehicle), hasSize(1));
  }

  @ParameterizedTest
  @EnumSource(value = Vehicle.State.class,
              names = {"UNKNOWN", "UNAVAILABLE", "ERROR", "EXECUTING", "CHARGING"})
  void checkVehicleHasIncorrectState(Vehicle.State state) {
    Vehicle vehicle = idleAndDegradedVehicle.withState(state);

    assertThat(isIdleAndDegraded.apply(vehicle), hasSize(1));
  }

  @ParameterizedTest
  @EnumSource(value = Vehicle.ProcState.class,
              names = {"AWAITING_ORDER", "PROCESSING_ORDER"})
  void checkVehicleHasIncorrectProcState(Vehicle.ProcState procState) {
    Vehicle vehicle = idleAndDegradedVehicle.withProcState(procState);

    assertThat(isIdleAndDegraded.apply(vehicle), hasSize(1));
  }

  @Test
  void checkVehicleHasOrderSequence() {
    Vehicle vehicle = idleAndDegradedVehicle
        .withOrderSequence(new OrderSequence("OS").getReference());

    assertThat(isIdleAndDegraded.apply(vehicle), hasSize(1));
  }

  @Test
  void checkEnergyLevelIsNotDegraded() {
    Vehicle vehicle = idleAndDegradedVehicle
        .withEnergyLevel(100);

    assertThat(isIdleAndDegraded.apply(vehicle), hasSize(1));
  }

  @Test
  void checkVehicleIsNotAllowedToCharge() {
    Vehicle vehicle = idleAndDegradedVehicle
        .withAllowedOrderTypes(Set.of(OrderConstants.TYPE_PARK));

    assertThat(isIdleAndDegraded.apply(vehicle), hasSize(1));
  }
}
