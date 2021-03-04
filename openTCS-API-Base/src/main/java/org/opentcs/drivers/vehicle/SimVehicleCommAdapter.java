/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import javax.annotation.Nullable;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * This interface declares methods that a vehicle driver intended for simulation
 * must implement.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface SimVehicleCommAdapter
    extends VehicleCommAdapter {

  /**
   * Sets a time factor for the simulation.
   * Note that 1.0 is considered to be normal/real time, values lower than 1.0
   * slow motion and values higher than 1.0 accelerated motion. Values of 0.0 or
   * smaller are not allowed.
   *
   * @param factor The time factor.
   * @throws IllegalArgumentException If the given value is 0.0 or smaller.
   * @deprecated Simulation is out of scope for the openTCS project. Simulation-related components
   * should be configured individually.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  default void setSimTimeFactor(double factor)
      throws IllegalArgumentException {
  }

  /**
   * Sets an initial vehicle position.
   * This method should not be called while the communication adapter is
   * simulating order execution for the attached vehicle; the resulting
   * behaviour is undefined.
   *
   * @param newPos The new position.
   */
  void initVehiclePosition(@Nullable String newPos);
}
