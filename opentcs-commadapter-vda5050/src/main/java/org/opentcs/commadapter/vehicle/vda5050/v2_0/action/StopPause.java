// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.action;

import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;

/**
 * The VDA5050 {@code stopPause} action.
 */
public class StopPause
    extends
      Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "stopPause";

  public StopPause(
      @Nonnull
      String actionId,
      @Nonnull
      BlockingType blockingType
  ) {
    super(ACTION_TYPE, actionId, blockingType);
  }
}
