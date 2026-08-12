// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Tests for {@link DeviationExtensionTrigger}
 */
class DeviationExtensionTriggerTest {

  @ParameterizedTest
  @ValueSource(strings = {"always", "never", "auto", "manually", "auto+manually"})
  void dontExtendWhenNotFirstMovementCommand(String triggerType) {
    Vehicle vehicle = new Vehicle("vehicle-1")
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_DEVIATION_EXTENSION_TRIGGER, triggerType);
    DeviationExtensionTrigger trigger = new DeviationExtensionTrigger(vehicle);
    trigger.onCancelOrderEnqueued();
    trigger.onExtensionRequestedManually();

    assertThat(trigger.extendDeviation(createMovementCommand(1))).isFalse();
  }

  @Test
  void extendAlwaysWithoutExplicitConfiguration() {
    Vehicle vehicle = new Vehicle("vehicle-1");
    DeviationExtensionTrigger trigger = new DeviationExtensionTrigger(vehicle);

    assertThat(trigger.extendDeviation(createMovementCommand(0))).isTrue();
    // Deviation should be extended for subsequent movement commands as long as they are the first
    // one in their respective drive order.
    assertThat(trigger.extendDeviation(createMovementCommand(0))).isTrue();
  }

  @Test
  void extendAlwaysWithExplicitConfiguration() {
    Vehicle vehicle = new Vehicle("vehicle-1")
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_DEVIATION_EXTENSION_TRIGGER, "always");
    DeviationExtensionTrigger trigger = new DeviationExtensionTrigger(vehicle);

    assertThat(trigger.extendDeviation(createMovementCommand(0))).isTrue();
    // Deviation should be extended for subsequent movement commands as long as they are the first
    // one in their respective drive order.
    assertThat(trigger.extendDeviation(createMovementCommand(0))).isTrue();
  }

  @Test
  void extendNever() {
    Vehicle vehicle = new Vehicle("vehicle-1")
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_DEVIATION_EXTENSION_TRIGGER, "never");
    DeviationExtensionTrigger trigger = new DeviationExtensionTrigger(vehicle);

    assertThat(trigger.extendDeviation(createMovementCommand(0))).isFalse();
    assertThat(trigger.extendDeviation(createMovementCommand(1))).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"auto", "auto+manually"})
  void extendAuto(String triggerType) {
    Vehicle vehicle = new Vehicle("vehicle-1")
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_DEVIATION_EXTENSION_TRIGGER, triggerType);
    DeviationExtensionTrigger trigger = new DeviationExtensionTrigger(vehicle);
    assertThat(trigger.extendDeviation(createMovementCommand(0))).isFalse();
    trigger.onCancelOrderEnqueued();
    assertThat(trigger.extendDeviation(createMovementCommand(0))).isTrue();
    // Deviation should no longer be extended once the trigger is reset.
    trigger.reset();
    assertThat(trigger.extendDeviation(createMovementCommand(0))).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"manually", "auto+manually"})
  void extendManually(String triggerType) {
    Vehicle vehicle = new Vehicle("vehicle-1")
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_DEVIATION_EXTENSION_TRIGGER, triggerType);
    DeviationExtensionTrigger trigger = new DeviationExtensionTrigger(vehicle);
    assertThat(trigger.extendDeviation(createMovementCommand(0))).isFalse();
    trigger.onExtensionRequestedManually();
    assertThat(trigger.extendDeviation(createMovementCommand(0))).isTrue();
    // Deviation should no longer be extended once the trigger is reset.
    trigger.reset();
    assertThat(trigger.extendDeviation(createMovementCommand(0))).isFalse();
  }

  private MovementCommand createMovementCommand(int routeIndex) {
    Point point = new Point("point-1");
    DriveOrder driveOrder = new DriveOrder(
        "drive-order",
        new DriveOrder.Destination(point.getReference())
    );
    return new MovementCommand(
        new TransportOrder("1", List.of()),
        driveOrder,
        new Route.Step(null, null, point, Vehicle.Orientation.FORWARD, routeIndex, 1),
        "NOP",
        null,
        true,
        null,
        point,
        "NOP",
        Map.of()
    );
  }
}
