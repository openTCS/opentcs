// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.action;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;

/**
 * The VDA5050 {@code logReport} action.
 */
public class LogReport
    extends
      Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "logReport";
  /**
   * The key of the (VDA5050) {@code reason} parameter for this action.
   */
  public static final String PARAMKEY_REASON = "reason";

  @SuppressWarnings("this-escape")
  public LogReport(
      @Nonnull
      String actionId,
      @Nonnull
      BlockingType blockingType,
      @Nonnull
      String paramValueReason
  ) {
    super(ACTION_TYPE, actionId, blockingType);

    List<ActionParameter> actionParameters = new ArrayList<>();
    requireNonNull(paramValueReason, "paramValueReason");
    actionParameters.add(new ActionParameter(PARAMKEY_REASON, paramValueReason));
    setActionParameters(actionParameters);
  }
}
