/*
 * openTCS copyright information:
 * Copyright (c) 2009 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.algorithms;

import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * A strategy for finding parking positions for vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ParkingStrategy {

  /**
   * Returns a suitable parking position for the given vehicle.
   *
   * @param vehicle The vehicle to find a parking position for.
   * @return A parking position for the given vehicle, or <code>null</code>, if
   * no suitable parking position is available.
   */
  Point getParkingPosition(Vehicle vehicle);
}
