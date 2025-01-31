// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection.vehicles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.util.TimeProvider;

/**
 * Test for {@link IsIdleAndDegraded}.
 */
class IsIdleAndDegradedTest {

  private DefaultDispatcherConfiguration configuration;
  private TimeProvider timeProvider;
  private Vehicle idleAndDegradedVehicle;
  private IsIdleAndDegraded isIdleAndDegraded;

  @BeforeEach
  void setUp() {
    configuration = mock();
    timeProvider = mock();
    isIdleAndDegraded = new IsIdleAndDegraded(configuration, timeProvider);
    idleAndDegradedVehicle = new Vehicle("V1")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withState(Vehicle.State.IDLE)
        .withProcState(Vehicle.ProcState.IDLE)
        .withCurrentPosition(new Point("p1").getReference())
        .withEnergyLevel(10)
        .withAcceptableOrderTypes(
            Set.of(new AcceptableOrderType(OrderConstants.TYPE_ANY, 0))
        );

    long rechargeIdleVehiclesDelay = 60000;
    given(configuration.rechargeIdleVehiclesDelay()).willReturn(rechargeIdleVehiclesDelay);
    given(timeProvider.getCurrentTimeInstant())
        .willReturn(
            idleAndDegradedVehicle.getProcStateTimestamp()
                .plusMillis(rechargeIdleVehiclesDelay + 1)
        );
  }

  @ParameterizedTest
  @ValueSource(strings = {OrderConstants.TYPE_ANY, OrderConstants.TYPE_CHARGE})
  void checkVehicleIsIdleAndDegraded(String type) {
    Vehicle vehicle = idleAndDegradedVehicle
        .withAcceptableOrderTypes(Set.of(new AcceptableOrderType(type, 0)));
    assertThat(isIdleAndDegraded.apply(vehicle), hasSize(0));
  }

  @ParameterizedTest
  @EnumSource(
      value = Vehicle.IntegrationLevel.class,
      names = {"TO_BE_IGNORED", "TO_BE_NOTICED", "TO_BE_RESPECTED"}
  )
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
  @EnumSource(
      value = Vehicle.State.class,
      names = {"UNKNOWN", "UNAVAILABLE", "ERROR", "EXECUTING", "CHARGING"}
  )
  void checkVehicleHasIncorrectState(Vehicle.State state) {
    Vehicle vehicle = idleAndDegradedVehicle.withState(state);

    assertThat(isIdleAndDegraded.apply(vehicle), hasSize(1));
  }

  @ParameterizedTest
  @EnumSource(
      value = Vehicle.ProcState.class,
      names = {"AWAITING_ORDER", "PROCESSING_ORDER"}
  )
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
        .withAcceptableOrderTypes(
            Set.of(new AcceptableOrderType(OrderConstants.TYPE_PARK, 0))
        );

    assertThat(isIdleAndDegraded.apply(vehicle), hasSize(1));
  }

  @Test
  void checkVehicleIsNotIdleLongEnough() {
    given(timeProvider.getCurrentTimeInstant())
        .willReturn(idleAndDegradedVehicle.getProcStateTimestamp().plusMillis(30000));
    assertThat(isIdleAndDegraded.apply(idleAndDegradedVehicle), hasSize(1));
  }
}
