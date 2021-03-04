/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.user;

import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Defines the possible permission flags of kernel clients.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated User management via kernel interaction will not be supported in the future.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public enum UserPermission {

  /**
   * Indicates the client may retrieve any data from the kernel.
   */
  READ_DATA,
  /**
   * Indicates the client may change the kernel's state.
   */
  CHANGE_KERNEL_STATE,
  /**
   * Indicates the client may change the kernel's configuration items.
   */
  CHANGE_CONFIGURATION,
  /**
   * Indicates the client may create, modify and remove user accounts.
   *
   * @deprecated User management via kernel interaction will not be supported in the future.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  MANAGE_USERS,
  /**
   * Indicates the client may load another model.
   */
  LOAD_MODEL,
  /**
   * Indicates the client may save the current model (under any name).
   */
  SAVE_MODEL,
  /**
   * Indicates the client may modify any data of the current model.
   */
  MODIFY_MODEL,
  /**
   * Indicates the client may add or remove temporary path locks.
   */
  LOCK_PATH,
  /**
   * Indicates the client may move/place vehicles and modify their states
   * explicitly.
   */
  MODIFY_VEHICLES,
  /**
   * Indicates the client may create/modify transport orders.
   */
  MODIFY_ORDER,
  /**
   * Indicates the client may publish messages via the kernel.
   */
  PUBLISH_MESSAGES
}
