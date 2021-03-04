/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import java.util.List;
import javax.annotation.Nonnull;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A strategy for finding locations suitable for recharging vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Implementation-specific interface does not belong into generic API.
 * Moved to implementation.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public interface RechargePositionSupplier
    extends Lifecycle {

  /**
   * Returns a sequence of destinations for recharging the given vehicle.
   * In most cases, the sequence will probably consist of only one destination. Some vehicles may
   * require to visit more than one destination for recharging, though, e.g. to drop off a battery
   * pack at one location and get a fresh one at another location.
   *
   * @param vehicle The vehicle to be recharged.
   * @return A sequence of destinations including operations for recharging the given vehicle. If
   * no suitable sequence was found, the returned sequence will be empty.
   */
  @Nonnull
  List<Destination> findRechargeSequence(@Nonnull Vehicle vehicle);
}
