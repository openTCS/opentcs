/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.CyclicTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically checks for idle vehicles that could be dispatched.
 * The main purpose of doing this is retrying to dispatch vehicles that were not in a dispatchable
 * state when dispatching them was last tried.
 * A potential reason for this is that a vehicle temporarily reported an error because a safety
 * sensor was triggered.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PeriodicVehicleRedispatchingTask
    extends CyclicTask {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeriodicVehicleRedispatchingTask.class);

  private final LocalKernel kernel;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel used to dispatch vehicles.
   * @param dispatchInterval The dispatch interval. May not be less than zero.
   */
  public PeriodicVehicleRedispatchingTask(@Nonnull LocalKernel kernel, long dispatchInterval) {
    super(dispatchInterval);
    this.kernel = requireNonNull(kernel, "kernel");
  }

  @Override
  protected void runActualTask() {
    for (Vehicle vehicle : idleVehicles()) {
      LOG.debug("Redispatching {}...", vehicle);
      kernel.dispatchVehicle(vehicle.getReference(), false);
    }
  }

  private Set<Vehicle> idleVehicles() {
    return kernel.getTCSObjects(Vehicle.class,
                                vehicle -> vehicle.hasProcState(Vehicle.ProcState.IDLE)
                                && !vehicle.isProcessingOrder()
                                && vehicle.hasState(Vehicle.State.IDLE)
                                && vehicle.getCurrentPosition() != null);
  }

}
