/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import org.opentcs.data.model.Vehicle;

/**
 * Implementations of this interface construct routing tables for given models.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface RoutingTableBuilder {

  /**
   * Computes a routing table for the given vehicle in the given model.
   *
   * @param vehicle The vehicle.
   * @return The computed routing table.
   */
  RoutingTable computeTable(Vehicle vehicle);
}
