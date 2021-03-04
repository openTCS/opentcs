/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.commands;

import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set the {@link LoadHandlingDevice}s attached to a vehicle.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetLoadHandlingDevicesCommand
    implements AdapterCommand {

  /**
   * The list of load handling devices.
   */
  private final List<LoadHandlingDevice> devices;

  /**
   * Creates a new instance.
   *
   * @param devices The list of load handling devices.
   */
  public SetLoadHandlingDevicesCommand(@Nonnull List<LoadHandlingDevice> devices) {
    this.devices = requireNonNull(devices, "devices");
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().setVehicleLoadHandlingDevices(devices);
  }
}
