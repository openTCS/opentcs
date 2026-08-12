// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.action;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.BlockingType;

/**
 * The VDA5050 {@code drop} action.
 */
public class Drop
    extends
      Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "drop";
  /**
   * The key of the (VDA5050) {@code lhd} parameter for this action.
   */
  public static final String PARAMKEY_LOAD_HANDLING_DEVICE = "lhd";
  /**
   * The key of the (VDA5050) {@code stationType} parameter for this action.
   */
  public static final String PARAMKEY_STATION_TYPE = "stationType";
  /**
   * The key of the (VDA5050) {@code stationName} parameter for this action.
   */
  public static final String PARAMKEY_STATION_NAME = "stationName";
  /**
   * The key of the (VDA5050) {@code loadType} parameter for this action.
   */
  public static final String PARAMKEY_LOAD_TYPE = "loadType";
  /**
   * The key of the (VDA5050) {@code loadId} parameter for this action.
   */
  public static final String PARAMKEY_LOAD_ID = "loadId";
  /**
   * The key of the (VDA5050) {@code height} parameter for this action.
   */
  public static final String PARAMKEY_HEIGHT = "height";
  /**
   * The key of the (VDA5050) {@code depth} parameter for this action.
   */
  public static final String PARAMKEY_DEPTH = "depth";
  /**
   * The key of the (VDA5050) {@code side} parameter for this action.
   */
  public static final String PARAMKEY_SIDE = "side";

  @SuppressWarnings("this-escape")
  public Drop(
      @Nonnull
      String actionId,
      @Nonnull
      BlockingType blockingType,
      @Nullable
      String paramValueLoadHandlingDevice,
      @Nullable
      String paramValueStationType,
      @Nullable
      String paramValueStationName,
      @Nullable
      String paramValueLoadType,
      @Nullable
      String paramValueLoadId,
      @Nullable
      Float paramValueHeight,
      @Nullable
      Float paramValueDepth,
      @Nullable
      String paramValueSide
  ) {
    super(ACTION_TYPE, actionId, blockingType);

    List<ActionParameter> actionParameters = new ArrayList<>();
    if (paramValueLoadHandlingDevice != null) {
      actionParameters.add(
          new ActionParameter(
              PARAMKEY_LOAD_HANDLING_DEVICE,
              paramValueLoadHandlingDevice
          )
      );
    }
    if (paramValueStationType != null) {
      actionParameters.add(new ActionParameter(PARAMKEY_STATION_TYPE, paramValueStationType));
    }
    if (paramValueStationName != null) {
      actionParameters.add(new ActionParameter(PARAMKEY_STATION_NAME, paramValueStationName));
    }
    if (paramValueLoadType != null) {
      actionParameters.add(new ActionParameter(PARAMKEY_LOAD_TYPE, paramValueLoadType));
    }
    if (paramValueLoadId != null) {
      actionParameters.add(new ActionParameter(PARAMKEY_LOAD_ID, paramValueLoadId));
    }
    if (paramValueHeight != null) {
      actionParameters.add(new ActionParameter(PARAMKEY_HEIGHT, paramValueHeight));
    }
    if (paramValueDepth != null) {
      actionParameters.add(new ActionParameter(PARAMKEY_DEPTH, paramValueDepth));
    }
    if (paramValueSide != null) {
      actionParameters.add(new ActionParameter(PARAMKEY_SIDE, paramValueSide));
    }
    if (!actionParameters.isEmpty()) {
      setActionParameters(actionParameters);
    }
  }
}
