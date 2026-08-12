// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

/**
 * The different conditions when a movement command is considered completed.
 */
public enum MovementCommandCompletedCondition {

  /**
   * A movement command is considered completed when its associated edge state has disappeared from
   * the vehicle state.
   */
  EDGE,
  /**
   * A movement command is considered completed when its associated edge state and node state have
   * both disappeared from the vehicle state.
   */
  EDGE_AND_NODE;
}
