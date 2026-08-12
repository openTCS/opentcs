// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A {@link Vda5050CommAdapterFactory} that does not provide any adapters.
 */
public class DisabledVda5050CommAdapterFactory
    implements
      Vda5050CommAdapterFactory {

  /**
   * Creates a new instance.
   */
  public DisabledVda5050CommAdapterFactory() {
  }

  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
    return false;
  }

  @Override
  public VehicleCommAdapter getAdapterFor(Vehicle vehicle) {
    throw new UnsupportedOperationException("Does not provide adapters.");
  }

}
