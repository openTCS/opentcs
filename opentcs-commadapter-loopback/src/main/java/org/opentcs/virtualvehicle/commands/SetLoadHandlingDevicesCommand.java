// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.commands;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.List;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set the {@link LoadHandlingDevice}s attached to a vehicle.
 */
public class SetLoadHandlingDevicesCommand
    implements
      AdapterCommand {

  /**
   * The list of load handling devices.
   */
  private final List<LoadHandlingDevice> devices;

  /**
   * Creates a new instance.
   *
   * @param devices The list of load handling devices.
   */
  public SetLoadHandlingDevicesCommand(
      @Nonnull
      List<LoadHandlingDevice> devices
  ) {
    this.devices = requireNonNull(devices, "devices");
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().setLoadHandlingDevices(devices);
  }
}
