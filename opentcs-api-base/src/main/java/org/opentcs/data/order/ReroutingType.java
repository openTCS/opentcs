// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
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
