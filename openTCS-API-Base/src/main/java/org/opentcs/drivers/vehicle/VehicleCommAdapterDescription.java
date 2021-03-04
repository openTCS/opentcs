/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.io.Serializable;

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

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof VehicleCommAdapterDescription)) {
      return false;
    }
    return getDescription().equals(((VehicleCommAdapterDescription) obj).getDescription());
  }

  @Override
  public int hashCode() {
    return getDescription().hashCode();
  }
}
