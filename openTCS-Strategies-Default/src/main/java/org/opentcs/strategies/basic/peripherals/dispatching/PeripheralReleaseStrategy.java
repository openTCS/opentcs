/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching;

import java.util.Collection;
import org.opentcs.data.model.Location;

/**
 * A strategy that determines peripherals whose reservations are to be released.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PeripheralReleaseStrategy {

  /**
   * Selects the peripherals whose reservations are to be released.
   *
   * @param locations The peripherals to select from.
   * @return The selected peripherals.
   */
  Collection<Location> selectPeripheralsToRelease(Collection<Location> locations);
}
