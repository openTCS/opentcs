/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.algorithms;

import org.opentcs.data.model.Location;
import org.opentcs.data.model.Vehicle;

/**
 * A strategy for finding locations suitable for recharging vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface RechargeStrategy {

  /**
   * Returns a suitable location for recharging the given vehicle.
   *
   * @param vehicle The vehicle to be recharged.
   * @return A suitable location for recharging the given vehicle.
   */
  Location getRechargeLocation(Vehicle vehicle);
}
