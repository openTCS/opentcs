/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback;

import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;

/**
 * A factory for various loopback specific instances.
 *
 * @author Martin Grzenia (Fraunhofer IML)
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
