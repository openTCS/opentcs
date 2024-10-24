// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.peripherals;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;

/**
 * Maintains associations between locations and peripheral controllers.
 */
public interface PeripheralControllerPool {

  /**
   * Returns the peripheral controller associated with the given location.
   *
   * @param location The reference to the location.
   * @return The peripheral controller associated with the given location.
   * @throws IllegalArgumentException If no peripheral controller is associated with the given
   * location or if the referenced location does not exist.
   */
  @Nonnull
  PeripheralController getPeripheralController(TCSResourceReference<Location> location)
      throws IllegalArgumentException;
}
