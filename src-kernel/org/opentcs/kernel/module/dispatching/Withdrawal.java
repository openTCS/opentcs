/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.dispatching;

import java.util.Objects;
import org.opentcs.data.model.Vehicle;

/**
 * The description of a withdrawal.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
final class Withdrawal {

  /**
   * The vehicle whose order is to be withdrawn.
   */
  private final Vehicle vehicle;
  /**
   * Indicates whether the vehicle's processing state is to be set to
   * UNAVAILABLE.
   */
  private final boolean disableVehicle;

  /**
   * Creates a new Withdrawal.
   *
   * @param newVehicle The vehicle whose order is to be withdrawn.
   * @param disableVehicle Whether to disable the vehicle after withdrawing the
   * order.
   */
  Withdrawal(Vehicle newVehicle, boolean disableVehicle) {
    this.vehicle = Objects.requireNonNull(newVehicle, "newVehicle is null");
    this.disableVehicle = disableVehicle;
  }

  /**
   * Returns the vehicle whose order is to be withdrawn.
   *
   * @return The vehicle whose order is to be withdrawn.
   */
  public Vehicle getVehicle() {
    return vehicle;
  }

  /**
   * Indicates whether to disable the vehicle after withdrawing the order.
   *
   * @return <code>true</code> if, and only if, the vehicle is to be disabled
   * after withdrawing the order.
   */
  public boolean getDisableVehicle() {
    return disableVehicle;
  }
}
