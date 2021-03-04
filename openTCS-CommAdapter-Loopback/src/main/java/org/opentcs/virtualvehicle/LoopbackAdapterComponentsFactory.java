/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import org.opentcs.data.model.Vehicle;

/**
 * A factory for various loopback specific instances.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LoopbackAdapterComponentsFactory {

  /**
   * Creates a new LoopbackCommunicationAdapter for the given vehicle.
   *
   * @param vehicle The vehicle.
   * @return A new LoopbackCommunicationAdapter for the given vehicle.
   */
  LoopbackCommunicationAdapter createLoopbackCommAdapter(Vehicle vehicle);

  /**
   * Creates a new panel for the given comm adapter.
   *
   * @param commAdapter The comm adapter to create a panel for.
   * @return A new panel for the given comm adapter.
   */
  @Deprecated
  LoopbackCommunicationAdapterPanel createPanel(LoopbackCommunicationAdapter commAdapter);
}
