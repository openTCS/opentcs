// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Vehicle;

/**
 * Unit tests for {@link CommAdapterFactory}.
 */
class VehicleHasRequiredPropertiesTest {

  private VehicleHasRequiredProperties hasRequiredProperties;

  @BeforeEach
  void setUp() {
    hasRequiredProperties = new VehicleHasRequiredProperties();
  }

  @Test
  void acceptVehicleWithAllProperties() {
    assertTrue(
        hasRequiredProperties.test(
            new Vehicle("Some vehicle")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_VERSION, "1.1")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME, "openTCS")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
        )
    );
    assertTrue(
        hasRequiredProperties.test(
            new Vehicle("Some vehicle")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_VERSION, "1.1")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_TOPIC_PREFIX, "some-prefix")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
        )
    );
  }

  @Test
  void refuseVehicleMissingBothInterfaceNameAndTopicPrefix() {
    assertFalse(
        hasRequiredProperties.test(
            new Vehicle("Some vehicle")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_VERSION, "1.1")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
        )
    );
  }

  @Test
  void refuseVehicleMissingManufacturer() {
    assertFalse(
        hasRequiredProperties.test(
            new Vehicle("Some vehicle")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_VERSION, "1.1")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME, "openTCS")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
        )
    );
    assertFalse(
        hasRequiredProperties.test(
            new Vehicle("Some vehicle")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_VERSION, "1.1")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_TOPIC_PREFIX, "some-prefix")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
        )
    );
  }

  @Test
  void refuseVehicleMissingSerialNumber() {
    assertFalse(
        hasRequiredProperties.test(
            new Vehicle("Some vehicle")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_VERSION, "1.1")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME, "openTCS")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
        )
    );
    assertFalse(
        hasRequiredProperties.test(
            new Vehicle("Some vehicle")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_VERSION, "1.1")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_TOPIC_PREFIX, "some-prefix")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
        )
    );
  }

  @Test
  void refuseVehicleMissingVersion() {
    assertFalse(
        hasRequiredProperties.test(
            new Vehicle("Some vehicle")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME, "openTCS")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
        )
    );
    assertFalse(
        hasRequiredProperties.test(
            new Vehicle("Some vehicle")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_TOPIC_PREFIX, "some-prefix")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
        )
    );
  }
}
