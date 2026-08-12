// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_LASTNODEID_REQUIRED_FOR_MOVEMENT_COMPLETION;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MOVEMENT_COMMAND_COMPLETED_CONDITION;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.common.MovementCommandCompletedCondition;
import org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Edge;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionStatus;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.EdgeState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.NodeState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Tracks the progress of movement commands and reports back finished ones.
 */
public class MovementCommandManager {

  /**
   * A list of currently tracked orders.
   */
  private final Queue<OrderAssociation> trackedOrders = new ArrayDeque<>();
  /**
   * The movement command completed condition.
   */
  private final MovementCommandCompletedCondition completedCondition;
  /**
   * Whether lastNodeId should be considered for determining whether a movement command is complete.
   */
  private final boolean lastNodeIdRequiredForMovementCompletion;

  /**
   * Construct a new MovementCommandManager.
   *
   * @param vehicle The vehicle to create the manager for.
   */
  @Inject
  public MovementCommandManager(
      @Assisted
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");
    this.completedCondition = PropertyExtractions.getMovementCommandCompletedCondition(
        PROPKEY_VEHICLE_MOVEMENT_COMMAND_COMPLETED_CONDITION, vehicle
    ).orElse(MovementCommandCompletedCondition.EDGE_AND_NODE);
    this.lastNodeIdRequiredForMovementCompletion = PropertyExtractions.getPropertyBoolean(
        PROPKEY_VEHICLE_LASTNODEID_REQUIRED_FOR_MOVEMENT_COMPLETION, vehicle
    ).orElse(false);
  }

  /**
   * Adds a movement command and an order to be tracked.
   *
   * @param orderAssociation The order association to track.
   */
  public void enqueue(
      @Nonnull
      OrderAssociation orderAssociation
  ) {
    requireNonNull(orderAssociation, "orderAssociation");

    trackedOrders.add(orderAssociation);
  }

  /**
   * Clears tracked order associations.
   */
  public void clear() {
    trackedOrders.clear();
  }

  /**
   * Notifies this instance about a new state reported by the vehicle.
   * Calls the callback function for any movement commands considered to be completed.
   *
   * @param currentState The current state message.
   * @param callback The callback for completed movement commands.
   */
  public void onStateMessage(
      @Nonnull
      State currentState,
      @Nonnull
      Consumer<MovementCommand> callback
  ) {
    requireNonNull(currentState, "currentState");
    requireNonNull(callback, "callback");

    if (!reportsOrderIdForCurrentDriveOrder(currentState)) {
      return;
    }

    Iterator<OrderAssociation> iter = trackedOrders.iterator();
    while (iter.hasNext() && checkForCompletionAndReport(iter.next(), currentState, callback)) {
      iter.remove();
    }
  }

  private boolean checkForCompletionAndReport(
      OrderAssociation association,
      State state,
      Consumer<MovementCommand> callback
  ) {
    if (orderComplete(association, state)) {
      callback.accept(association.getCommand());
      return true;
    }
    else {
      return false;
    }
  }

  private boolean orderComplete(OrderAssociation association, State state) {
    return movementComplete(association, state)
        && (!association.getCommand().isFinalMovement()
            || actionsComplete(association.getOrder(), state));
  }

  private boolean movementComplete(OrderAssociation association, State state) {
    if (!lastNodeIdPlausibleForMovementCompletion(state)) {
      return false;
    }

    if (association.getCommand().isFinalMovement()) {
      return edgesComplete(association.getOrder(), state)
          && nodesComplete(association.getOrder(), state);
    }
    switch (completedCondition) {
      case EDGE:
        return edgesComplete(association.getOrder(), state);
      case EDGE_AND_NODE:
      default:
        return edgesComplete(association.getOrder(), state)
            && nodesComplete(association.getOrder(), state);
    }
  }

  private boolean lastNodeIdPlausibleForMovementCompletion(State state) {
    if (!lastNodeIdRequiredForMovementCompletion) {
      return true;
    }

    // lastNodeId is acceptable if it matches the name of any remaining destination point on the
    // route that has already been released to the vehicle.
    return trackedOrders.stream()
        .anyMatch(
            association -> Objects.equals(
                association.getCommand().getStep().getDestinationPoint().getName(),
                state.getLastNodeId()
            )
        );
  }

  private boolean edgesComplete(Order order, State state) {
    return order.getEdges().stream()
        .filter(edge -> edge.isReleased())
        .allMatch(edge -> edgeComplete(edge, state.getEdgeStates()));
  }

  private boolean nodesComplete(Order order, State state) {
    return order.getNodes().stream()
        .filter(node -> node.isReleased())
        .allMatch(node -> nodeComplete(node, state.getNodeStates()));
  }

  private boolean edgeComplete(Edge edge, List<EdgeState> edgeStates) {
    // An edge is complete if it is not in the list of edge states (any more).
    return edgeStates.stream().noneMatch(edgeState -> {
      return Objects.equals(edgeState.getEdgeId(), edge.getEdgeId())
          && Objects.equals(edgeState.getSequenceId(), edge.getSequenceId());
    });
  }

  private boolean nodeComplete(Node node, List<NodeState> nodeStates) {
    // A node is complete if it is not in the list of node states (any more).
    return nodeStates.stream().noneMatch(nodeState -> {
      return Objects.equals(nodeState.getNodeId(), node.getNodeId())
          && Objects.equals(nodeState.getSequenceId(), node.getSequenceId());
    });
  }

  private boolean actionsComplete(Order order, State state) {
    return Stream.concat(
        order.getNodes().stream().flatMap(node -> node.getActions().stream()),
        order.getEdges().stream().flatMap(edge -> edge.getActions().stream())
    )
        .allMatch(action -> actionComplete(action, state.getActionStates()));
  }

  private boolean actionComplete(Action action, List<ActionState> actionStates) {
    return actionStates.stream()
        .anyMatch(actionState -> {
          return Objects.equals(actionState.getActionId(), action.getActionId())
              && isFinalActionStatus(actionState.getActionStatus());
        });
  }

  private boolean isFinalActionStatus(ActionStatus actionStatus) {
    return actionStatus == ActionStatus.FAILED
        || actionStatus == ActionStatus.FINISHED;
  }

  private boolean reportsOrderIdForCurrentDriveOrder(
      @Nonnull
      State state
  ) {
    return Objects.equals(
        state.getOrderId(),
        trackedOrders.stream()
            .findAny()
            .map(orderAssociation -> orderAssociation.getOrder().getOrderId())
            .orElse("")
    );
  }
}
