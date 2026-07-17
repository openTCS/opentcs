// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.auth;

import io.javalin.security.RouteRole;

/**
 * Defines the possible permissions for web API users.
 */
public enum UserPermission
    implements
      RouteRole {

  /**
   * Indicates the client may retrieve any data from the kernel.
   */
  READ_DATA,
  /**
   * Indicates the client may load another model.
   */
  LOAD_MODEL,
  /**
   * Indicates the client may add or remove temporary locks at paths or locations.
   */
  LOCK_RESOURCE,
  /**
   * Indicates the client may update the routing topology.
   */
  UPDATE_ROUTING_TOPOLOGY,
  /**
   * Indicates the client may move/place vehicles and modify their states
   * explicitly.
   */
  MODIFY_VEHICLE,
  /**
   * Indicates the client may create/modify transport orders.
   */
  MODIFY_ORDER,
  /**
   * Indicates the client may create/modify order sequences.
   */
  MODIFY_ORDER_SEQUENCE,
  /**
   * Indicates the client may modify peripheral states.
   */
  MODIFY_PERIPHERAL,
  /**
   * Indicates the client may create/modify peripheral jobs.
   */
  MODIFY_PERIPHERAL_JOB,
  /**
   * Indicates the client may create/modify environmental entities.
   */
  MODIFY_ENVIRONMENTAL_ENTITY,
  /**
   * Indicates the client may shut down the kernel.
   */
  SHUTDOWN_KERNEL
}
