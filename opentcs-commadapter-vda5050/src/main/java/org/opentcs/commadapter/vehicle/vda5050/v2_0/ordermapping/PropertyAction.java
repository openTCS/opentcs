// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping;

import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;

/**
 * Represents an action that was defined in the properties of a TCS resource or a movement command.
 */
public class PropertyAction {

  /**
   * The action type of the action.
   */
  private final String actionType;
  /**
   * The action id for the action.
   */
  private final String actionId;
  /**
   * The blocking type for the action.
   */
  private final BlockingType blockingType;
  /**
   * The action parameters.
   */
  private final List<ActionParameter> actionParameters;
  /**
   * When the action should be executed.
   */
  private final EnumSet<ActionTrigger> trigger;
  /**
   * Set of tags for this action.
   */
  private final Set<String> tags;

  /**
   * Create a new instance.
   *
   * @param actionType The action type.
   * @param actionId The action id.
   * @param blockingType the blocking type.
   * @param actionParameters The action parameters.
   * @param trigger The execution trigger of the action.
   * @param tags The tags for this action.
   */
  public PropertyAction(
      String actionType,
      String actionId,
      BlockingType blockingType,
      List<ActionParameter> actionParameters,
      EnumSet<ActionTrigger> trigger,
      Set<String> tags
  ) {
    this.actionType = requireNonNull(actionType, "actionType");
    this.actionId = requireNonNull(actionId, "actionId");
    this.blockingType = requireNonNull(blockingType, "blockingType");
    this.actionParameters = requireNonNull(actionParameters, "actionParameters");
    this.trigger = requireNonNull(trigger, "trigger");
    this.tags = requireNonNull(tags, "tags");
  }

  public String getActionType() {
    return actionType;
  }

  public String getActionId() {
    return actionId;
  }

  public BlockingType getBlockingType() {
    return blockingType;
  }

  public List<ActionParameter> getActionParameters() {
    return actionParameters;
  }

  public EnumSet<ActionTrigger> getTrigger() {
    return trigger;
  }

  public Set<String> getTags() {
    return tags;
  }

}
