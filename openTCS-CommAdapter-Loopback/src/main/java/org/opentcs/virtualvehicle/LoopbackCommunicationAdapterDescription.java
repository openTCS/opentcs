/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import java.util.ResourceBundle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import static org.opentcs.virtualvehicle.I18nLoopbackCommAdapter.BUNDLE_PATH;

/**
 * The loopback adapter's {@link VehicleCommAdapterDescription}.
 */
public class LoopbackCommunicationAdapterDescription
    extends VehicleCommAdapterDescription {

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
