/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.routing;

/**
 * Elements of this enumeration indicate how the costs for routes shall be
 * computed.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
enum CostType {

  /**
   * Indicates that the costs for a route/path are measured based on the
   * number of hops/paths travelled along the route.
   */
  HOP_BASED,
  /**
   * Indicates that the costs for a route/path are measured based on the
   * length of the route.
   */
  LENGTH_BASED,
  /**
   * Indicates that the costs for a route/path are measured based on the
   * time a vehicle spends travelling the route.
   */
  TIME_BASED,
  /**
   * Indicates that the costs for a route/path are taken directly from the
   * routing cost attribute of the involved paths on the route.
   */
  EXPLICIT
}
