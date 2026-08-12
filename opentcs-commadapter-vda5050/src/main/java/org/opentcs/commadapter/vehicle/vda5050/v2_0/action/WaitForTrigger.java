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
 * The VDA5050 {@code waitForTrigger} action.
 */
public class WaitForTrigger
    extends
      Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "waitForTrigger";
  /**
   * The key of the (VDA5050) {@code triggerType} parameter for this action.
   */
  public static final String PARAMKEY_TRIGGER_TYPE = "triggerType";

  @SuppressWarnings("this-escape")
  public WaitForTrigger(
      @Nonnull
      String actionId,
      @Nonnull
      BlockingType blockingType,
      @Nonnull
      String paramValueTriggerType
  ) {
    super(ACTION_TYPE, actionId, blockingType);

    List<ActionParameter> actionParameters = new ArrayList<>();
    requireNonNull(paramValueTriggerType, "paramValueTriggerType");
    actionParameters.add(new ActionParameter(PARAMKEY_TRIGGER_TYPE, paramValueTriggerType));
    setActionParameters(actionParameters);
  }
}
