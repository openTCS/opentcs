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
 * The VDA5050 {@code initPosition} action.
 */
public class InitPosition
    extends
      Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "initPosition";
  /**
   * The key of the (VDA5050) {@code x} parameter for this action.
   */
  public static final String PARAMKEY_X = "x";
  /**
   * The key of the (VDA5050) {@code y} parameter for this action.
   */
  public static final String PARAMKEY_Y = "y";
  /**
   * The key of the (VDA5050) {@code theta} parameter for this action.
   */
  public static final String PARAMKEY_THETA = "theta";
  /**
   * The key of the (VDA5050) {@code mapId} parameter for this action.
   */
  public static final String PARAMKEY_MAP_ID = "mapId";
  /**
   * The key of the (VDA5050) {@code lastNodeId} parameter for this action.
   */
  public static final String PARAMKEY_LAST_NODE_ID = "lastNodeId";

  @SuppressWarnings("this-escape")
  public InitPosition(
      @Nonnull
      String actionId,
      @Nonnull
      BlockingType blockingType,
      @Nonnull
      String paramValueX,
      @Nonnull
      String paramValueY,
      @Nonnull
      String paramValueTheta,
      @Nonnull
      String paramValueMapId,
      @Nonnull
      String paramValueLastNodeId
  ) {
    super(ACTION_TYPE, actionId, blockingType);

    List<ActionParameter> actionParameters = new ArrayList<>();
    requireNonNull(paramValueX, "paramValueX");
    actionParameters.add(new ActionParameter(PARAMKEY_X, paramValueX));
    requireNonNull(paramValueY, "paramValueY");
    actionParameters.add(new ActionParameter(PARAMKEY_Y, paramValueY));
    requireNonNull(paramValueTheta, "paramValueTheta");
    actionParameters.add(new ActionParameter(PARAMKEY_THETA, paramValueTheta));
    requireNonNull(paramValueMapId, "paramValueMapId");
    actionParameters.add(new ActionParameter(PARAMKEY_MAP_ID, paramValueMapId));
    requireNonNull(paramValueLastNodeId, "paramValueLastNodeId");
    actionParameters.add(new ActionParameter(PARAMKEY_LAST_NODE_ID, paramValueLastNodeId));
    setActionParameters(actionParameters);
  }
}
