// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Action that is to be executed on a node or edge.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Action
    implements
      Serializable {

  /**
   * Name of action.
   * Identifies the function of the action.
   */
  private String actionType;
  /**
   * Unique ID to identify the action and map them to the actionState in the state.
   * Suggestion: Use UUIDs.
   */
  private String actionId;
  /**
   * [Optional] Additional information on the action.
   */
  private String actionDescription;
  /**
   * Blocking type of this action.
   */
  private BlockingType blockingType;
  /**
   * [Optional] Array of {@link ActionParameter} objects for the indicated action. (E.g. deviceId,
   * loadId or external triggers.)
   */
  private List<ActionParameter> actionParameters;

  @JsonCreator
  public Action(
      @Nonnull
      @JsonProperty(required = true, value = "actionType")
      String actionType,
      @Nonnull
      @JsonProperty(required = true, value = "actionId")
      String actionId,
      @Nonnull
      @JsonProperty(required = true, value = "blockingType")
      BlockingType blockingType
  ) {
    this.actionType = requireNonNull(actionType, "actionType");
    this.actionId = requireNonNull(actionId, "actionId");
    this.blockingType = requireNonNull(blockingType, "blockingType");
  }

  public String getActionType() {
    return actionType;
  }

  public Action setActionType(
      @Nonnull
      String actionType
  ) {
    this.actionType = requireNonNull(actionType, "actionType");
    return this;
  }

  public String getActionId() {
    return actionId;
  }

  public Action setActionId(
      @Nonnull
      String actionId
  ) {
    this.actionId = requireNonNull(actionId, "actionId");
    return this;
  }

  public String getActionDescription() {
    return actionDescription;
  }

  public Action setActionDescription(String actionDescription) {
    this.actionDescription = actionDescription;
    return this;
  }

  public BlockingType getBlockingType() {
    return blockingType;
  }

  public Action setBlockingType(BlockingType blockingType) {
    this.blockingType = blockingType;
    return this;
  }

  public List<ActionParameter> getActionParameters() {
    return actionParameters;
  }

  public Action setActionParameters(List<ActionParameter> actionParameters) {
    this.actionParameters = actionParameters;
    return this;
  }

  @Override
  public String toString() {
    return "Action{" + "actionType=" + actionType
        + ", actionId=" + actionId
        + ", actionDescription=" + actionDescription
        + ", blockingType=" + blockingType
        + ", actionParameters=" + actionParameters
        + '}';
  }

}
