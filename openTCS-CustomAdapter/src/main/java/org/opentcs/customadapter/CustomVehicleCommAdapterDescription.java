package org.opentcs.customadapter;

import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

public class CustomVehicleCommAdapterDescription
    extends
      VehicleCommAdapterDescription {
  CustomVehicleCommAdapterDescription() {
  }

  @Override
  public String getDescription() {
    return "Custom Vehicle Communication Adapter";
  }

  @Override
  public boolean isSimVehicleCommAdapter() {
    return false;
  }
}
