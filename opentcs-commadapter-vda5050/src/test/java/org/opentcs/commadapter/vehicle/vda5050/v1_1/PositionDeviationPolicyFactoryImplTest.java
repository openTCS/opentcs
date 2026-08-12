// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_VERSION;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Vehicle;

/**
 * Tests for {@link PositionDeviationPolicyImpl}.
 */
class PositionDeviationPolicyFactoryImplTest {

  private Vehicle vehicleWithRequiredProperties;

  private PositionDeviationPolicyFactoryImpl factory;

  @BeforeEach
  void setUp() {
    vehicleWithRequiredProperties = new Vehicle("vehicle-1")
        .withProperty(PROPKEY_VEHICLE_VERSION, "1.1")
        .withProperty(PROPKEY_VEHICLE_INTERFACE_NAME, "interface")
        .withProperty(PROPKEY_VEHICLE_MANUFACTURER, "manufacturer")
        .withProperty(PROPKEY_VEHICLE_SERIAL_NUMBER, "serialno");

    factory = new PositionDeviationPolicyFactoryImpl(new VehicleHasRequiredProperties());
  }

  @Test
  void provideEmptyForVehicleWithMissingProperties() {
    assertThat(factory.createPolicyFor(new Vehicle("some-vehicle")))
        .isEmpty();
  }

  @Test
  void providePolicyForVehicleWithRequiredProperties() {
    assertThat(factory.createPolicyFor(vehicleWithRequiredProperties)).isPresent();
  }

  @Test
  void provideEmptyForVehicleWithWrongVersionProperty() {
    assertThat(
        factory.createPolicyFor(
            vehicleWithRequiredProperties.withProperty(PROPKEY_VEHICLE_VERSION, "2.0")
        )
    )
        .isEmpty();
  }
}
