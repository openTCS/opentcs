// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_TOPIC_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_VERSION;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.opentcs.data.model.Vehicle;

/**
 * Indicates whether a vehicle has all required properties to be handled by this comm adapter.
 */
public class VehicleHasRequiredProperties
    implements
      Predicate<Vehicle> {

  /**
   * Creates a new instance.
   */
  public VehicleHasRequiredProperties() {
  }

  @Override
  public boolean test(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    return Objects.equals(
        Optional.ofNullable(vehicle.getProperty(PROPKEY_VEHICLE_VERSION))
            .map(String::strip)
            .orElse(null),
        "1.1"
    )
        && (vehicle.getProperty(PROPKEY_VEHICLE_TOPIC_PREFIX) != null
            || vehicle.getProperty(PROPKEY_VEHICLE_INTERFACE_NAME) != null)
        && vehicle.getProperty(PROPKEY_VEHICLE_MANUFACTURER) != null
        && vehicle.getProperty(PROPKEY_VEHICLE_SERIAL_NUMBER) != null;
  }
}
