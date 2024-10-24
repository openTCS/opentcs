// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.peripherals.dispatching;

import java.util.Collection;
import org.opentcs.data.model.Location;

/**
 * A strategy that determines peripherals whose reservations are to be released.
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
