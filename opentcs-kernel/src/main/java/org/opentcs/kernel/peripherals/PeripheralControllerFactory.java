// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.peripherals;

import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralController;

/**
 * A factory for {@link PeripheralController} instances.
 */
public interface PeripheralControllerFactory {

  /**
   * Creates a new peripheral controller for the given location and communication adapter.
   *
   * @param location The location.
   * @param commAdapter The communication adapter.
   * @return A new peripheral controller.
   */
  DefaultPeripheralController createPeripheralController(
      TCSResourceReference<Location> location,
      PeripheralCommAdapter commAdapter
  );
}
