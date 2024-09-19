/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.rerouting;

import java.util.List;
import java.util.Optional;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;

/**
 * A strategy for rerouting {@link Vehicle}s.
 */
public interface ReroutingStrategy {

  /**
   * Tries to calculate a new route for the given {@link Vehicle} and the {@link TransportOrder}
   * it's currently processing.
   * <p>
   * The new route should consider the given vehicle's transport order progress so that the returned
   * list of {@link DriveOrder}s doesn't contain any drive orders that the vehicle already finished.
   *
   * @param vehicle The vehicle to calculate a new route for.
   * @return An {@link Optional} containing the new drive orders or {@link Optional#EMPTY}, if
   * no new route could be calculated (e.g. because the given vehicle is not processing a transport
   * order).
   */
  Optional<List<DriveOrder>> reroute(Vehicle vehicle);
}
