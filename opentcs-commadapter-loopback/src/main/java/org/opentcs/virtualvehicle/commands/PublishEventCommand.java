// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.commands;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterEvent;

/**
 * A command to publish {@link VehicleCommAdapterEvent}s.
 */
public class PublishEventCommand
    implements
      AdapterCommand {

  /**
   * The event to publish.
   */
  private final VehicleCommAdapterEvent event;

  /**
   * Creates a new instance.
   *
   * @param event The event to publish.
   */
  public PublishEventCommand(
      @Nonnull
      VehicleCommAdapterEvent event
  ) {
    this.event = requireNonNull(event, "event");
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().publishEvent(event);
  }
}
