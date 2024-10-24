// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.parking;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * A strategy for finding parking positions for vehicles.
 */
public interface ParkingPositionSupplier
    extends
      Lifecycle {

  /**
   * Returns a suitable parking position for the given vehicle.
   *
   * @param vehicle The vehicle to find a parking position for.
   * @return A parking position for the given vehicle, or an empty Optional, if no suitable parking
   * position is available.
   */
  @Nonnull
  Optional<Point> findParkingPosition(
      @Nonnull
      Vehicle vehicle
  );
}
