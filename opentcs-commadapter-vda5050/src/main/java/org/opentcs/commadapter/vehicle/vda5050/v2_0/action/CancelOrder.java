// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.action;

import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;

/**
 * The VDA5050 {@code cancelOrder} action.
 */
public class CancelOrder
    extends
      Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "cancelOrder";

  public CancelOrder(
      @Nonnull
      String actionId,
      @Nonnull
      BlockingType blockingType
  ) {
    super(ACTION_TYPE, actionId, blockingType);
  }
}
