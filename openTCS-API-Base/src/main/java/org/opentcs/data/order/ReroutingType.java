/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

import org.opentcs.data.model.Vehicle;

/**
 * Defines the different types {@link Vehicle}s can be rerouted.
 */
public enum ReroutingType {

  /**
   * Vehicles get rerouted with respect to their current resource allocations.
   */
  REGULAR,
  /**
   * Vehicles get (forcefully) rerouted from their current position (disregarding any resources that
   * might have already been allocated).
   */
  FORCED;
}
