// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.action;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;

/**
 * The VDA5050 {@code detectObject} action.
 */
public class DetectObject
    extends
      Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "detectObject";
  /**
   * The key of the (VDA5050) {@code objectType} parameter for this action.
   */
  public static final String PARAMKEY_OBJECT_TYPE = "objectType";

  @SuppressWarnings("this-escape")
  public DetectObject(
      @Nonnull
      String actionId,
      @Nonnull
      BlockingType blockingType,
      @Nullable
      String paramValueObjectType
  ) {
    super(ACTION_TYPE, actionId, blockingType);

    List<ActionParameter> actionParameters = new ArrayList<>();
    if (paramValueObjectType != null) {
      actionParameters.add(new ActionParameter(PARAMKEY_OBJECT_TYPE, paramValueObjectType));
    }
    if (!actionParameters.isEmpty()) {
      setActionParameters(actionParameters);
    }
  }
}
