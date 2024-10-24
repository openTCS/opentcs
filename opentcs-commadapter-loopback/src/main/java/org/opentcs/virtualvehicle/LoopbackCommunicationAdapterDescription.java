// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle;

import static org.opentcs.virtualvehicle.I18nLoopbackCommAdapter.BUNDLE_PATH;

import java.util.ResourceBundle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * The loopback adapter's {@link VehicleCommAdapterDescription}.
 */
public class LoopbackCommunicationAdapterDescription
    extends
      VehicleCommAdapterDescription {

  /**
   * Creates a new instance.
   */
  public LoopbackCommunicationAdapterDescription() {
  }

  @Override
  public String getDescription() {
    return ResourceBundle.getBundle(BUNDLE_PATH)
        .getString("loopbackCommunicationAdapterDescription.description");
  }

  @Override
  public boolean isSimVehicleCommAdapter() {
    return true;
  }

}
