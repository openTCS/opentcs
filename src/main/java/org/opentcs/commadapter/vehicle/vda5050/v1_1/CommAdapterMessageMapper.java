// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_DESCRIPTION;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_PARAMETER_PATTERN;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_INSTANT_ACTION_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_BLOCKING_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_DESCRIPTION;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages.SEND_ORDER_PARAM_PARAMETER_PATTERN;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.action.InitPosition;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Edge;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping.NodeMapping;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.util.MapValueExtractor;

/**
 * Provide methods for mapping {@link VehicleCommAdapterMessage}s to other types.
 */
public class CommAdapterMessageMapper {

  private final Vehicle vehicle;
  private final MapValueExtractor mapValueExtractor;
  private final TCSObjectService objectService;
  private final NodeMapping nodeMapping;

  /**
   * Creates a new instance.
   *
   * @param vehicle The vehicle this mapper is used for.
   * @param mapValueExtractor Extracts values from maps.
   * @param objectService The service used for fetching objects.
   */
  @Inject
  public CommAdapterMessageMapper(
      @Assisted
      Vehicle vehicle,
      MapValueExtractor mapValueExtractor,
      TCSObjectService objectService,
      NodeMapping nodeMapping
  ) {
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.mapValueExtractor = requireNonNull(mapValueExtractor, "mapValueExtractor");
    this.objectService = requireNonNull(objectService, "objectService");
    this.nodeMapping = requireNonNull(nodeMapping, "nodeMapping");
  }

  /**
   * Tries to map a {@link VehicleCommAdapterMessage} to an {@link Order}.
   *
   * @param message The message to map.
   * @return An {@link Optional} containing the mapped {@link Order}, or an empty {@link Optional}
   * if the message could not be mapped.
   */
  public Optional<Order> toOrder(VehicleCommAdapterMessage message) {
    Optional<String> orderId = mapValueExtractor.extractString(
        CommAdapterMessages.SEND_ORDER_PARAM_ORDER_ID,
        message.getParameters()
    );
    Optional<Long> orderUpdateId = mapValueExtractor.extractLong(
        CommAdapterMessages.SEND_ORDER_PARAM_ORDER_UPDATE_ID,
        message.getParameters()
    );
    Optional<String> sourceNodeName = mapValueExtractor.extractString(
        CommAdapterMessages.SEND_ORDER_PARAM_SOURCE_NODE,
        message.getParameters()
    );
    Optional<String> destinationNodeName = mapValueExtractor.extractString(
        CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE,
        message.getParameters()
    );
    Optional<String> edgeName = mapValueExtractor.extractString(
        CommAdapterMessages.SEND_ORDER_PARAM_EDGE,
        message.getParameters()
    );

    if (orderId.isEmpty() || orderUpdateId.isEmpty() || sourceNodeName.isEmpty()
        || destinationNodeName.isEmpty() || edgeName.isEmpty()) {
      return Optional.empty();
    }

    Node sourceNode = createNode(
        sourceNodeName.get(),
        0
    );
    Node destinationNode = createNode(
        destinationNodeName.get(),
        1
    );
    boolean actionRequested = mapValueExtractor.extractString(
        SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_TYPE,
        message.getParameters()
    ).isPresent();
    Optional<Action> action = toAction(
        message.getParameters(),
        SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_TYPE,
        SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_ID,
        SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_DESCRIPTION,
        SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_BLOCKING_TYPE,
        SEND_ORDER_PARAM_PARAMETER_PATTERN
    );
    if (actionRequested && action.isEmpty()) {
      return Optional.empty();
    }
    action.ifPresent(a -> destinationNode.setActions(List.of(a)));

    Edge edge = createEdge(
        edgeName.get(),
        sourceNodeName.get(),
        destinationNodeName.get()
    );

    return Optional.of(
        new Order(
            orderId.get(),
            orderUpdateId.get(),
            List.of(sourceNode, destinationNode),
            List.of(edge)
        )
    );
  }

  /**
   * Tries to map a {@link VehicleCommAdapterMessage} to an {@link Action}.
   *
   * @param message The message to map.
   * @return An {@link Optional} containing the mapped {@link Action}, or an empty {@link Optional}
   * if the message could not be mapped.
   */
  public Optional<Action> toAction(VehicleCommAdapterMessage message) {
    if (!Objects.equals(message.getType(), SEND_INSTANT_ACTION_TYPE)) {
      return Optional.empty();
    }

    return toAction(
        message.getParameters(),
        SEND_INSTANT_ACTION_PARAM_ACTION_TYPE,
        SEND_INSTANT_ACTION_PARAM_ACTION_ID,
        SEND_INSTANT_ACTION_PARAM_ACTION_DESCRIPTION,
        SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE,
        SEND_INSTANT_ACTION_PARAM_PARAMETER_PATTERN
    );
  }

  private Optional<Action> toAction(
      Map<String, String> messageParameters,
      String actionTypeKey,
      String actionIdKey,
      String actionDescriptionKey,
      String blockingTypeKey,
      Pattern actionParameterPattern
  ) {
    Optional<String> actionType = mapValueExtractor.extractString(
        actionTypeKey,
        messageParameters
    );
    Optional<String> actionId = mapValueExtractor.extractString(
        actionIdKey,
        messageParameters
    );
    Optional<String> actionDescription = mapValueExtractor.extractString(
        actionDescriptionKey,
        messageParameters
    );
    Optional<BlockingType> blockingType = mapValueExtractor.extractEnum(
        blockingTypeKey,
        messageParameters,
        BlockingType.class
    );

    if (actionType.isEmpty() || actionId.isEmpty() || blockingType.isEmpty()) {
      return Optional.empty();
    }

    Action action = new Action(
        actionType.get(),
        actionId.get(),
        blockingType.get()
    );

    actionDescription.ifPresent(action::setActionDescription);

    List<ActionParameter> actionParameters = new ArrayList<>();
    for (Map.Entry<String, String> entry : messageParameters.entrySet()) {
      Matcher matcher = actionParameterPattern.matcher(entry.getKey());
      if (!matcher.matches()) {
        continue;
      }

      String paramKey = matcher.group(1);
      Optional<Object> parsedValue = parseParameterValue(
          action.getActionType(), paramKey, entry.getValue()
      );
      if (parsedValue.isEmpty()) {
        return Optional.empty();
      }
      actionParameters.add(new ActionParameter(paramKey, parsedValue.get()));
    }
    action.setActionParameters(actionParameters);

    return Optional.of(action);
  }

  private Node createNode(String pointName, long sequenceId) {
    return new Node(
        pointName,
        sequenceId,
        true,
        List.of()
    ).setNodePosition(
        nodeMapping.toNodePosition(
            objectService.fetch(Point.class, pointName).orElseThrow(),
            vehicle,
            false
        )
    );
  }

  private Edge createEdge(String pathName, String startNodeId, String endNodeId) {
    return new Edge(
        pathName,
        0L,
        true,
        startNodeId,
        endNodeId,
        List.of()
    );
  }

  private Optional<Object> parseParameterValue(String actionType, String paramKey, String value) {
    // Only parse numeric parameters for well-known action types/keys. Prevent accidental
    // conversion of string fields that just look like numbers (e.g., mapId = "1.0").
    if (Objects.equals(actionType, InitPosition.ACTION_TYPE)
        && (Objects.equals(paramKey, InitPosition.PARAMKEY_X)
            || Objects.equals(paramKey, InitPosition.PARAMKEY_Y)
            || Objects.equals(paramKey, InitPosition.PARAMKEY_THETA))) {
      try {
        double parsed = Double.parseDouble(value);
        if (!Double.isFinite(parsed)) {
          return Optional.empty();
        }
        return Optional.of(parsed);
      }
      catch (NumberFormatException e) {
        return Optional.empty();
      }
    }

    return Optional.of(value);
  }
}
