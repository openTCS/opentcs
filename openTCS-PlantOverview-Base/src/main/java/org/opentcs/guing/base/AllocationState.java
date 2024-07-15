/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
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
