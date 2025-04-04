// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.drivers.vehicle.VehicleController;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Null-object implementation of {@link VehicleController}.
 */
public class NullVehicleController
    implements
      VehicleController {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(NullVehicleController.class);
  /**
   * The associated vehicle's name.
   */
  private final String vehicleName;

  /**
   * Creates a new instance.
   *
   * @param vehicleName The associated vehicle's name.
   */
  public NullVehicleController(
      @Nonnull
      String vehicleName
  ) {
    this.vehicleName = requireNonNull(vehicleName, "vehicleName");
  }

  @Override
  public void initialize() {
  }

  @Override
  public boolean isInitialized() {
    return true;
  }

  @Override
  public void terminate() {
  }

  @Override
  public void setTransportOrder(TransportOrder newOrder) {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  public void abortTransportOrder(boolean immediate) {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  public ExplainedBoolean canProcess(TransportOrder order) {
    return new ExplainedBoolean(false, "NullVehicleController");
  }

  @Override
  @Deprecated
  public void sendCommAdapterMessage(Object message) {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  @Deprecated
  public void sendCommAdapterCommand(AdapterCommand command) {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  public void sendCommAdapterMessage(
      @Nonnull
      VehicleCommAdapterMessage message
  ) {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  public Queue<MovementCommand> getCommandsSent() {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
    return new ArrayDeque<>();
  }

  @Override
  public void onVehiclePaused(boolean paused) {
  }

  @Override
  public Optional<MovementCommand> getInteractionsPendingCommand() {
    return Optional.empty();
  }

  @Override
  public boolean mayAllocateNow(Set<TCSResource<?>> resources) {
    return false;
  }

}
