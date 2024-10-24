// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.peripheral.loopback;

import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;

/**
 * A factory for various loopback specific instances.
 */
public interface LoopbackPeripheralAdapterComponentsFactory {

  /**
   * Creates a new loopback communication adapter for the given location.
   *
   * @param location The location.
   * @return A loopback communication adapter instance.
   */
  LoopbackPeripheralCommAdapter createLoopbackCommAdapter(TCSResourceReference<Location> location);
}
