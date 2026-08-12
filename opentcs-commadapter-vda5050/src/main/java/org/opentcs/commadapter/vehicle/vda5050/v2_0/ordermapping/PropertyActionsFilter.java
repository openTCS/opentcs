// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * Filters property actions based on whether the vehicle can execute them.
 */
public class PropertyActionsFilter
    implements
      Predicate<PropertyAction> {

  /**
   * Action tags that are executable by the vehicle.
   */
  private final Predicate<String> vehicleActionsFilter;
  /**
   * Action tags that are executable for this command.
   */
  private final Predicate<String> commandActionsFilter;
  /**
   * Action tags that are executable after an edge.
   */
  private final Predicate<String> edgeActionFilter;
  /**
   * The accepted action triggers.
   */
  private final EnumSet<ActionTrigger> actionTriggers;

  /**
   * Creates a new executable actions predicate from a vehicle.
   *
   * @param vehicleActions The vehicle actions.
   * @param commandActionFilter The command actions.
   * @param edgeActionFilter The edge actions.
   * @param actionTriggers The accepted action triggers.
   */
  public PropertyActionsFilter(
      Predicate<String> vehicleActions,
      Predicate<String> commandActionFilter,
      Predicate<String> edgeActionFilter,
      EnumSet<ActionTrigger> actionTriggers
  ) {
    this.vehicleActionsFilter = requireNonNull(vehicleActions, "vehicleActions");
    this.commandActionsFilter = requireNonNull(commandActionFilter, "commandActionsFilter");
    this.edgeActionFilter = requireNonNull(edgeActionFilter, "edgeActionFilter");
    this.actionTriggers = requireNonNull(actionTriggers, "actionTriggers");
  }

  @Override
  public boolean test(PropertyAction propertyAction) {
    return isActionTagAccepted(propertyAction)
        && !Collections.disjoint(propertyAction.getTrigger(), actionTriggers);
  }

  private boolean isActionTagAccepted(PropertyAction action) {
    if (action.getTags().isEmpty()) {
      return true;
    }
    return action.getTags().stream().anyMatch(
        tag -> vehicleActionsFilter.test(tag)
            && commandActionsFilter.test(tag)
            && edgeActionFilter.test(tag)
    );
  }
}
