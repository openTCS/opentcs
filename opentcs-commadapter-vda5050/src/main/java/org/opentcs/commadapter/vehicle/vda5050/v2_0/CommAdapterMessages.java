// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import java.util.regex.Pattern;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;

/**
 * Defines {@link VehicleCommAdapterMessage} types (and their parameters) supported by
 * {@link CommAdapterImpl}.
 */
public class CommAdapterMessages {

  /**
   * A message for sending an {@link InstantActions} message with a singe {@link Action}.
   */
  public static final String SEND_INSTANT_ACTION_TYPE = "vda5050:sendInstantAction";
  /**
   * A parameter for the action type.
   * The parameter's value must be set to a string value.
   */
  public static final String SEND_INSTANT_ACTION_PARAM_ACTION_TYPE = "actionType";
  /**
   * A parameter for the action ID.
   * The parameter's value must be set to a string value.
   */
  public static final String SEND_INSTANT_ACTION_PARAM_ACTION_ID = "actionId";
  /**
   * A parameter for the action description.
   * The parameter's value must be set to a string value.
   */
  public static final String SEND_INSTANT_ACTION_PARAM_ACTION_DESCRIPTION = "actionDescription";
  /**
   * A parameter for the action's blocking type.
   * The parameter's value must be set to the name of the corresponding {@link BlockingType}.
   */
  public static final String SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE = "blockingType";
  /**
   * The prefix to use for defining an action parameter as a message parameter.
   * The message parameter's key must start with this prefix.
   * The string that follows the prefix is mapped to the action parameter's key.
   * The message parameter's value must be set to a string value and is mapped to the action
   * parameter's value.
   */
  public static final String SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX = "parameter.";
  /**
   * A pattern for matching message parameters that define action parameters.
   */
  public static final Pattern SEND_INSTANT_ACTION_PARAM_PARAMETER_PATTERN
      = Pattern.compile("parameter\\.(.+)");

  /**
   * A message for sending an {@link Order} message.
   */
  public static final String SEND_ORDER_TYPE = "vda5050:sendOrder";
  /**
   * A parameter for the order ID.
   * The parameter's value must be set to a string value.
   */
  public static final String SEND_ORDER_PARAM_ORDER_ID = "orderId";
  /**
   * A parameter for the order update ID.
   * The parameter's value must be set to a long value.
   */
  public static final String SEND_ORDER_PARAM_ORDER_UPDATE_ID = "orderUpdateId";
  /**
   * A parameter for the order's source node.
   * The parameter's value must be set to a string value.
   */
  public static final String SEND_ORDER_PARAM_SOURCE_NODE = "sourceNode";
  /**
   * A parameter for the order's destination node.
   * The parameter's value must be set to a string value.
   */
  public static final String SEND_ORDER_PARAM_DESTINATION_NODE = "destinationNode";
  /**
   * A parameter for the action type of the action at the destination node.
   * The parameter's value must be set to an integer value.
   */
  public static final String SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_TYPE = "actionType";
  /**
   * A parameter for the action ID of the action at the destination node.
   * The parameter's value must be set to a string value.
   */
  public static final String SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_ID = "actionId";
  /**
   * A parameter for the action description of the action at the destination node.
   * The parameter's value must be set to a string value.
   */
  public static final String SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_DESCRIPTION
      = "actionDescription";
  /**
   * A parameter for the blocking type of the action at the destination node.
   * The parameter's value must be set to the name of the corresponding {@link BlockingType}.
   */
  public static final String SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_BLOCKING_TYPE
      = "blockingType";
  /**
   * The prefix to use for defining an action parameter as a message parameter.
   * The message parameter's key must start with this prefix.
   * The string that follows the prefix is mapped to the action parameter's key.
   * The message parameter's value must be set to a string value and is mapped to the action
   * parameter's value.
   */
  public static final String SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_PARAMETER_PREFIX
      = "parameter.";
  /**
   * A pattern for matching message parameters that define action parameters.
   */
  public static final Pattern SEND_ORDER_PARAM_PARAMETER_PATTERN
      = Pattern.compile("parameter\\.(.+)");
  /**
   * A parameter for the order's edge.
   * The parameter's value must be set to a string value.
   */
  public static final String SEND_ORDER_PARAM_EDGE = "edge";

  /**
   * A message for requesting the extension of the deviation of the very first node in an order to
   * include the vehicle's position.
   */
  public static final String EXTEND_DEVIATION_ONCE_TYPE = "vda5050:extendDeviationOnce";

  private CommAdapterMessages() {
  }
}
