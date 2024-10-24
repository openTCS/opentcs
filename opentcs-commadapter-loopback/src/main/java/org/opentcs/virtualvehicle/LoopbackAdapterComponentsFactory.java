// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle;

import org.opentcs.data.model.Vehicle;

/**
 * A factory for various loopback specific instances.
 */
public interface LoopbackAdapterComponentsFactory {

  /**
   * Creates a new LoopbackCommunicationAdapter for the given vehicle.
   *
   * @param vehicle The vehicle.
   * @return A new LoopbackCommunicationAdapter for the given vehicle.
   */
  LoopbackCommunicationAdapter createLoopbackCommAdapter(Vehicle vehicle);
}
