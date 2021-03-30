/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.peripherals;

import javax.annotation.Nonnull;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;

/**
 * Maintains associations between locations and peripheral controllers.
 *
 * @author Martin Grzenia (Fraunhofer IML)
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
