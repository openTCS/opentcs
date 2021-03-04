/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.orderselection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;

/**
 * Selects an order for a given vehicle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleOrderSelectionStrategy {

  /**
   * Selects an order for a given vehicle.
   * 
   * @param vehicle The vehicle.
   * @return An instance of {@link VehicleOrderSelection} if a specific (or no) order should be
   * assigned to the vehicle; <code>null</code> if no applicable order was found by the
   * implementation.
   */
  @Nullable
  VehicleOrderSelection selectOrder(@Nonnull Vehicle vehicle);
}
