/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.io.Serializable;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides the description for a vehicle comm adapter.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public abstract class VehicleCommAdapterDescription
    implements Serializable {

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
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  public boolean isSimVehicleCommAdapter() {
    return false;
  }

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
