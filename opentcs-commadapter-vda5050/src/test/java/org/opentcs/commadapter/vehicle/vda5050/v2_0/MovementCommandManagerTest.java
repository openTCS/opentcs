// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.opentcs.commadapter.vehicle.vda5050.common.MovementCommandCompletedCondition.EDGE;
import static org.opentcs.commadapter.vehicle.vda5050.common.MovementCommandCompletedCondition.EDGE_AND_NODE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_LASTNODEID_REQUIRED_FOR_MOVEMENT_COMPLETION;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MOVEMENT_COMMAND_COMPLETED_CONDITION;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.commadapter.vehicle.vda5050.common.MovementCommandCompletedCondition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Edge;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionStatus;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.BatteryState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.EStop;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.EdgeState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.NodeState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.SafetyState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Unit tests for {@link MovementCommandManager}.
 */
public class MovementCommandManagerTest {

  /**
   * The movement command manager to test.
   */
  private MovementCommandManager manager;
  /**
   * The callback for finished commands.
   */
  private Consumer<MovementCommand> callback;

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void setup() {

    callback = mock(Consumer.class);
    manager = new MovementCommandManager(vehicleWithCondition(EDGE_AND_NODE));
  }

  @ParameterizedTest
  @EnumSource(MovementCommandCompletedCondition.class)
  public void finishMovementWhenNodeAndEdgeStatesAreEmpty(
      MovementCommandCompletedCondition completedCondition
  ) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        false
    )
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId()).build();

    // Both completion conditions should mark the movement command as finished if edge and node
    // states are empty
    manager = new MovementCommandManager(vehicleWithCondition(completedCondition));
    manager.enqueue(association);
    manager.onStateMessage(state, callback);
    verify(callback, times(1)).accept(association.getCommand());
  }

  @ParameterizedTest
  @EnumSource(MovementCommandCompletedCondition.class)
  public void finishMovementWithLastNodeIdBeingJustRight(
      MovementCommandCompletedCondition completedCondition
  ) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        false
    )
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withLastNodeId("dest-point")
        .build();
    manager = new MovementCommandManager(
        vehicleWithCondition(completedCondition)
            .withProperty(PROPKEY_VEHICLE_LASTNODEID_REQUIRED_FOR_MOVEMENT_COMPLETION, "true")
    );
    manager.enqueue(association);

    manager.onStateMessage(state, callback);

    verify(callback, times(1)).accept(association.getCommand());
  }

  @ParameterizedTest
  @EnumSource(MovementCommandCompletedCondition.class)
  public void finishMovementWithLastNodeIdBeingTheFollowingHop(
      MovementCommandCompletedCondition completedCondition
  ) {
    OrderAssociation association1 = new MovementCommandManagerTest.OrderBuilder(
        "some-order-id",
        "source-point-1",
        "dest-point-1",
        false
    )
        .build();
    OrderAssociation association2 = new MovementCommandManagerTest.OrderBuilder(
        "some-order-id",
        "dest-point-1",
        "dest-point-2",
        false
    )
        .build();
    State state = new StateBuilder(association1.getOrder().getOrderId())
        .withLastNodeId("dest-point-2")
        .build();
    manager = new MovementCommandManager(
        vehicleWithCondition(completedCondition)
            .withProperty(PROPKEY_VEHICLE_LASTNODEID_REQUIRED_FOR_MOVEMENT_COMPLETION, "true")
    );
    manager.enqueue(association1);
    manager.enqueue(association2);

    manager.onStateMessage(state, callback);

    verify(callback, times(1)).accept(association1.getCommand());
    verify(callback, times(1)).accept(association2.getCommand());
  }

  @ParameterizedTest
  @EnumSource(MovementCommandCompletedCondition.class)
  public void keepMovementWithRequiredButImplausibleLastNodeId(
      MovementCommandCompletedCondition completedCondition
  ) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        false
    )
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withLastNodeId("some-other-point")
        .build();
    manager = new MovementCommandManager(
        vehicleWithCondition(completedCondition)
            .withProperty(PROPKEY_VEHICLE_LASTNODEID_REQUIRED_FOR_MOVEMENT_COMPLETION, "true")
    );
    manager.enqueue(association);

    manager.onStateMessage(state, callback);

    verifyNoInteractions(callback);
  }

  @ParameterizedTest
  @EnumSource(MovementCommandCompletedCondition.class)
  public void keepMovementWhenEdgeStatesAreNotEmpty(
      MovementCommandCompletedCondition completedCondition
  ) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        false
    )
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withEdgeStatesFrom(association.getOrder())
        .build();

    // If the edge is not completed and remains in the edge states then both completion checks
    // should not mark the movement command as complete.
    manager = new MovementCommandManager(vehicleWithCondition(completedCondition));
    manager.enqueue(association);
    manager.onStateMessage(state, callback);
    verifyNoInteractions(callback);
  }

  @Test
  public void keepMovementWhenNodeStatesAreNotEmptyForEdgeAndNodeCondition() {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        false
    )
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withNodeStatesFrom(association.getOrder())
        .build();
    // When checking edge and node states the movement command should not be marked as completed
    // if the node states are not empty.
    manager = new MovementCommandManager(vehicleWithCondition(EDGE_AND_NODE));
    manager.enqueue(association);
    manager.onStateMessage(state, callback);
    verifyNoInteractions(callback);
  }

  @Test
  public void finishMovementWhenEdgeStatesAreEmptyForEdgeCondition() {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        false
    )
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withNodeStatesFrom(association.getOrder())
        .build();
    // When only checking edge states the movement command should be completed even if
    // node states of the order remain.
    manager = new MovementCommandManager(vehicleWithCondition(EDGE));
    manager.enqueue(association);
    manager.onStateMessage(state, callback);
    verify(callback, times(1)).accept(association.getCommand());
  }

  @ParameterizedTest
  @EnumSource(MovementCommandCompletedCondition.class)
  public void keepFinalMovementWhenEdgeStatesAreNotEmpty(
      MovementCommandCompletedCondition condition
  ) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source_point",
        "dest-point",
        true
    ).build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withEdgeStatesFrom(association.getOrder())
        .build();
    // A final movement command can only be finished if both edge and node states are empty
    // regardless of the completion condition
    manager = new MovementCommandManager(vehicleWithCondition(condition));
    manager.enqueue(association);
    manager.onStateMessage(state, callback);
    verifyNoInteractions(callback);
  }

  @ParameterizedTest
  @EnumSource(MovementCommandCompletedCondition.class)
  public void keepFinalMovementWhenNodeStatesAreNotEmpty(
      MovementCommandCompletedCondition condition
  ) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source_point",
        "dest-point",
        true
    ).build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withNodeStatesFrom(association.getOrder())
        .build();
    // A final movement command can only be finished if both edge and node states are empty
    // regardless of the completion condition
    manager = new MovementCommandManager(vehicleWithCondition(condition));
    manager.enqueue(association);
    manager.onStateMessage(state, callback);
    verifyNoInteractions(callback);
  }

  @ParameterizedTest
  @EnumSource(MovementCommandCompletedCondition.class)
  public void finishFinalMovementOnlyWhenBothEdgeAndNodeStatesAreEmpty(
      MovementCommandCompletedCondition condition
  ) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source_point",
        "dest-point",
        true
    ).build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .build();

    manager = new MovementCommandManager(vehicleWithCondition(condition));
    manager.enqueue(association);
    manager.onStateMessage(state, callback);
    verify(callback, times(1)).accept(association.getCommand());
  }

  @Test
  public void finishMovementIfStateContainsNodesAndEdgesWithDifferentSequenceIds() {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        false
    )
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withNodeState(new NodeState("source-point", 3L, true))
        .withNodeState(new NodeState("dest-point", 5L, true))
        .withEdgeState(new EdgeState("some-path", 4L, true))
        .build();
    // Sequence ids that are unrelated to the movement command should not block it from completing
    // even if the node ids match.
    manager.enqueue(association);
    manager.onStateMessage(state, callback);

    verify(callback, times(1)).accept(association.getCommand());
  }

  @Test
  public void keepMovementWithDifferentOrderId() {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        false
    )
        .build();
    State state = new StateBuilder("some-OTHER-order-id")
        .build();
    // The movement command should not be completed if the state responds with an unrelated order.
    manager.enqueue(association);
    manager.onStateMessage(state, callback);

    verifyNoInteractions(callback);
  }

  @ParameterizedTest
  @EnumSource(ActionStatus.class)
  public void finishMovementWithRelatedAction(ActionStatus actionStatus) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        false
    )
        .withActionAtDestPoint("some-action-type", "some-action-id", BlockingType.NONE)
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withActionStatesFrom(association.getOrder(), actionStatus)
        .build();
    // For non-final movement commands, the state of related actions should not block
    // the movement command from completing.
    manager.enqueue(association);
    manager.onStateMessage(state, callback);

    verify(callback, times(1)).accept(association.getCommand());
  }

  @ParameterizedTest
  @EnumSource(ActionStatus.class)
  public void finishMovementWithUnrelatedAction(ActionStatus actionStatus) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        false
    )
        .withActionAtDestPoint("some-action-type", "some-action-id", BlockingType.NONE)
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withActionState(
            new ActionState("some-OTHER-action-id", actionStatus)
                .setActionType("some-action-type")
        )
        .build();
    // Actions that are unrelated to the movement command should not block it from completing.
    manager.enqueue(association);
    manager.onStateMessage(state, callback);

    verify(callback, times(1)).accept(association.getCommand());
  }

  @ParameterizedTest
  @EnumSource(value = ActionStatus.class, names = {"INITIALIZING", "RUNNING", "WAITING"})
  public void keepFinalMovementWithRelatedUnfinishedAction(ActionStatus actionStatus) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        true
    )
        .withActionAtDestPoint("some-action-type", "some-action-id", BlockingType.NONE)
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withActionStatesFrom(association.getOrder(), actionStatus)
        .build();

    // Final movements must wait for its related actions to be completed before the movement
    // command can be completed.
    manager.enqueue(association);
    manager.onStateMessage(state, callback);

    verifyNoInteractions(callback);
  }

  @ParameterizedTest
  @EnumSource(value = ActionStatus.class, names = {"FINISHED", "FAILED"})
  public void finishFinalMovementWithRelatedFinishedAction(ActionStatus actionStatus) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        true
    )
        .withActionAtDestPoint("some-action-type", "some-action-id", BlockingType.NONE)
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withActionStatesFrom(association.getOrder(), actionStatus)
        .build();

    // Final movements must wait for its related actions to be completed before the movement
    // command can be completed.
    manager.enqueue(association);
    manager.onStateMessage(state, callback);

    verify(callback, times(1)).accept(association.getCommand());
  }

  @ParameterizedTest
  @EnumSource(ActionStatus.class)
  public void finishFinalMovementWithUnrelatedAction(ActionStatus actionStatus) {
    OrderAssociation association = new OrderBuilder(
        "some-order-id",
        "source-point",
        "dest-point",
        true
    )
        .build();
    State state = new StateBuilder(association.getOrder().getOrderId())
        .withActionState(
            new ActionState("some-OTHER-action-id", actionStatus)
                .setActionType("some-action-type")
        )
        .build();
    // Unrelated actions don't block a movement command from completing. Even for final movements.
    manager.enqueue(association);
    manager.onStateMessage(state, callback);

    verify(callback, times(1)).accept(association.getCommand());
  }

  private static Vehicle vehicleWithCondition(
      MovementCommandCompletedCondition completedCondition
  ) {
    return new Vehicle("vehicle-001")
        .withProperty(
            PROPKEY_VEHICLE_MOVEMENT_COMMAND_COMPLETED_CONDITION,
            completedCondition.name()
        );
  }

  private static MovementCommand createMovementCommand(
      Point source, Point dest, boolean finalMovement
  ) {
    Path path = null;
    if (source != null && dest != null) {
      path = new Path("Path-0001", source.getReference(), dest.getReference());
    }

    return new MovementCommand(
        new TransportOrder("1", List.of()),
        new DriveOrder("drive-order", new DriveOrder.Destination(dest.getReference())),
        new Route.Step(path, source, dest, Vehicle.Orientation.FORWARD, 0, 1),
        "NOP",
        null,
        finalMovement,
        null,
        dest,
        "NOP",
        Map.of()
    );
  }

  private static class OrderBuilder {

    private String orderId;
    private MovementCommand command;
    private List<Action> destActions = new ArrayList<>();

    OrderBuilder(String orderId, String sourcePoint, String destPoint, boolean isFinalMovement) {
      this.orderId = orderId;
      command = createMovementCommand(
          new Point(sourcePoint),
          new Point(destPoint),
          isFinalMovement
      );
    }

    public OrderBuilder withActionAtDestPoint(
        String actionType,
        String actionId,
        BlockingType blockingType
    ) {
      destActions.add(new Action(actionType, actionId, blockingType));
      return this;
    }

    public OrderAssociation build() {
      Order order
          = new Order(
              orderId,
              0L,
              List.of(
                  new Node(command.getStep().getSourcePoint().getName(), 0L, true, List.of()),
                  new Node(command.getStep().getDestinationPoint().getName(), 2L, true, destActions)
              ),
              List.of(
                  new Edge(
                      "some-path", 1L, true,
                      command.getStep().getSourcePoint().getName(),
                      command.getStep().getDestinationPoint().getName(),
                      List.of()
                  )
              )
          );
      return new OrderAssociation(order, command);
    }
  }

  private static class StateBuilder {

    private State state;

    StateBuilder(String orderId) {
      state = new State(
          orderId, 0L, "", 0L,
          new ArrayList<>(),
          new ArrayList<>(),
          false,
          new ArrayList<>(),
          new BatteryState(0.0, false), OperatingMode.AUTOMATIC, List.of(),
          new SafetyState(EStop.NONE, false)
      );
    }

    public StateBuilder withNodeStatesFrom(Order order) {
      order.getNodes().stream()
          .forEach(node -> {
            state.getNodeStates().add(new NodeState(node.getNodeId(), node.getSequenceId(), true));
          });
      return this;
    }

    public StateBuilder withNodeState(NodeState nodeState) {
      state.getNodeStates().add(nodeState);
      return this;
    }

    public StateBuilder withEdgeStatesFrom(Order order) {
      order.getEdges().stream()
          .forEach(edge -> {
            state.getEdgeStates().add(new EdgeState(edge.getEdgeId(), edge.getSequenceId(), true));
          });
      return this;
    }

    public StateBuilder withEdgeState(EdgeState edgeState) {
      state.getEdgeStates().add(edgeState);
      return this;
    }

    public StateBuilder withActionStatesFrom(Order order, ActionStatus actionStatus) {
      Stream.concat(
          order.getNodes().stream().flatMap(node -> node.getActions().stream()),
          order.getEdges().stream().flatMap(edge -> edge.getActions().stream())
      ).forEach(action -> {
        state.getActionStates().add(
            new ActionState(action.getActionId(), actionStatus)
                .setActionType(action.getActionType())
        );
      });
      return this;
    }

    public StateBuilder withActionState(ActionState actionState) {
      state.getActionStates().add(actionState);
      return this;
    }

    public StateBuilder withLastNodeId(String lastNodeId) {
      state.setLastNodeId(lastNodeId);
      return this;
    }

    public State build() {
      return state;
    }
  }
}
