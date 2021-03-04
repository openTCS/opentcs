/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.commands;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterEvent;

/**
 * A command to publish {@link VehicleCommAdapterEvent}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PublishEventCommand
    implements AdapterCommand {

  /**
   * The event to publish.
   */
  private final VehicleCommAdapterEvent event;

  /**
   * Creates a new instance.
   *
   * @param event The event to publish.
   */
  public PublishEventCommand(@Nonnull VehicleCommAdapterEvent event) {
    this.event = requireNonNull(event, "event");
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.getProcessModel().publishEvent(event);
  }
}
