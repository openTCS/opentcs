// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import java.io.Serializable;

/**
 * Provides the description for a vehicle comm adapter.
 */
public abstract class VehicleCommAdapterDescription
    implements
      Serializable {

  /**
   * Returns the description for a vehicle comm adapter.
   *
   * @return The description for a vehicle comm adapter.
   */
  public abstract String getDescription();

  /**
   * Whether the comm adapter is a simulating one.
   *
   * @return <code>true</code> if, and only if, the vehicle is a simulating one.
   */
  public abstract boolean isSimVehicleCommAdapter();

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof VehicleCommAdapterDescription)) {
      return false;
    }

    VehicleCommAdapterDescription other = (VehicleCommAdapterDescription) obj;
    return getDescription().equals(other.getDescription())
        && isSimVehicleCommAdapter() == other.isSimVehicleCommAdapter();
  }

  @Override
  public int hashCode() {
    return getDescription().hashCode();
  }
}
