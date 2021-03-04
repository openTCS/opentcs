/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A strategy for finding parking positions for vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Implementation-specific interface does not belong into generic API.
 * Moved to implementation.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public interface ParkingPositionSupplier
    extends Lifecycle {

  /**
   * Returns a suitable parking position for the given vehicle.
   *
   * @param vehicle The vehicle to find a parking position for.
   * @return A parking position for the given vehicle, or an empty Optional, if no suitable parking
   * position is available.
   */
  @Nonnull
  Optional<Point> findParkingPosition(@Nonnull Vehicle vehicle);
}
