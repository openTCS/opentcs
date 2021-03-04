/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.vehicleselection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;

/**
 * Selects a vehicle for a given transport order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface OrderVehicleSelectionStrategy {

  /**
   * Selects a vehicle for a given transport order.
   *
   * @param order The transport order.
   * @return An instance of {@link VehicleOrderSelection} if a specific (or no) vehicle should be
   * assigned to the order; <code>null</code> if no applicable vehicle was found by the
   * implementation.
   */
  @Nullable
  VehicleOrderSelection selectVehicle(@Nonnull TransportOrder order);
}
