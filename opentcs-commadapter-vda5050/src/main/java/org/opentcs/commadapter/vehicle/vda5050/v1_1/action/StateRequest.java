// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.action;

import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.BlockingType;

/**
 * The VDA5050 {@code stateRequest} action.
 */
public class StateRequest
    extends
      Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "stateRequest";

  public StateRequest(
      @Nonnull
      String actionId,
      @Nonnull
      BlockingType blockingType
  ) {
    super(ACTION_TYPE, actionId, blockingType);
  }
}
