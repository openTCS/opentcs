// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.controlcenter.vehicles;

import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * A {@link VehicleCommAdapterDescription} for no comm adapter.
 */
public class NullVehicleCommAdapterDescription
    extends
      VehicleCommAdapterDescription {

  /**
   * Creates a new instance.
   */
  public NullVehicleCommAdapterDescription() {
  }

  @Override
  public String getDescription() {
    return "-";
  }

  @Override
  public boolean isSimVehicleCommAdapter() {
    return false;
  }
}
