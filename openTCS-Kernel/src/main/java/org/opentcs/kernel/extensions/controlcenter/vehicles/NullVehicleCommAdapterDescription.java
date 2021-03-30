/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.controlcenter.vehicles;

import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * A {@link VehicleCommAdapterDescription} for no comm adapter.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class NullVehicleCommAdapterDescription
    extends VehicleCommAdapterDescription {

  @Override
  public String getDescription() {
    return "-";
  }

  @Override
  public boolean isSimVehicleCommAdapter() {
    return false;
  }
}
