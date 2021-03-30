/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.peripherals;

import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralControllerPool;

/**
 * Manages the attachment of peripheral controllers to locations and peripheral comm adapters.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LocalPeripheralControllerPool
    extends PeripheralControllerPool,
            Lifecycle {

  /**
   * Associates a peripheral controller with a location and a comm adapter.
   *
   * @param location The reference to the location.
   * @param commAdapter The comm adapter that is going to control the peripheral deivce.
   * @throws IllegalArgumentException If the referenced location does not exist.
   */
  void attachPeripheralController(TCSResourceReference<Location> location,
                                  PeripheralCommAdapter commAdapter)
      throws IllegalArgumentException;

  /**
   * Disassociates a peripheral controller and a comm adapter from a location.
   *
   * @param location The reference to the location.
   */
  void detachPeripheralController(TCSResourceReference<Location> location);
}
