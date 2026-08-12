// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getPropertyDouble;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_THETA;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_XY;

import javax.annotation.Nonnull;
import org.opentcs.components.kernel.PositionDeviationPolicy;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * A deviation policy for VDA5050 1.1 vehicles.
 */
public class PositionDeviationPolicyImpl
    implements
      PositionDeviationPolicy {

  private final Vehicle vehicle;

  /**
   * Creates a new instance.
   *
   * @param vehicle The vehicle
   */
  public PositionDeviationPolicyImpl(
      @Nonnull
      Vehicle vehicle
  ) {
    this.vehicle = requireNonNull(vehicle, "vehicle");
  }

  @Override
  public long allowedDeviationDistance(
      @Nonnull
      Point point
  ) {
    return getPropertyDouble(PROPKEY_VEHICLE_DEVIATION_XY, point, vehicle)
        .map(deviation -> (long) (deviation * 1000))
        .orElse(0L);
  }

  @Override
  public long allowedDeviationAngle(
      @Nonnull
      Point point
  ) {
    // XXX Ensure the angle is (positive and) within 0 and 180 degrees.
    return getPropertyDouble(PROPKEY_VEHICLE_DEVIATION_THETA, point, vehicle)
        .map(Double::longValue)
        .orElse(0L);
  }

}
