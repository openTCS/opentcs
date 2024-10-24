// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base;

/**
 * Defines the allocation states a resource can be in.
 */
public enum AllocationState {

  /**
   * The resource is claimed by a vehicle.
   */
  CLAIMED,
  /**
   * The resource is allocated by a vehicle.
   */
  ALLOCATED,
  /**
   * The resource is allocated by a vehicle but its related transport order is withdrawn.
   */
  ALLOCATED_WITHDRAWN;
}
