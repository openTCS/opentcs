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
 * The VDA5050 {@code finePositioning} action.
 */
public class FinePositioning
    extends
      Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "finePositioning";
  /**
   * The key of the (VDA5050) {@code stationType} parameter for this action.
   */
  public static final String PARAMKEY_STATION_TYPE = "stationType";
  /**
   * The key of the (VDA5050) {@code stationName} parameter for this action.
   */
  public static final String PARAMKEY_STATION_NAME = "stationName";

  @SuppressWarnings("this-escape")
  public FinePositioning(
      @Nonnull
      String actionId,
      @Nonnull
      BlockingType blockingType,
      @Nullable
      String paramValueStationType,
      @Nullable
      String paramValueStationName
  ) {
    super(ACTION_TYPE, actionId, blockingType);

    List<ActionParameter> actionParameters = new ArrayList<>();
    if (paramValueStationType != null) {
      actionParameters.add(new ActionParameter(PARAMKEY_STATION_TYPE, paramValueStationType));
    }
    if (paramValueStationName != null) {
      actionParameters.add(new ActionParameter(PARAMKEY_STATION_NAME, paramValueStationName));
    }
    if (!actionParameters.isEmpty()) {
      setActionParameters(actionParameters);
    }
  }
}
