// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.simulation;

import com.google.common.base.Strings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonBinder;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.ConnectionEventListener;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.IncomingMessage;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.MqttClientManager;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.QualityOfService;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterImpl;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.AgvPosition;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.connection.ConnectionState;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.ActionState;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.ActionStatus;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.BatteryState;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.EStop;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.EdgeState;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.NodeState;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.SafetyState;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A standalone application to simulate communication between the {@link CommAdapterImpl} and a
 * vehicle.
 */
public class VehicleSimulator
    implements
      ConnectionEventListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleSimulator.class);
  /**
   * Manufacturer of this agv.
   */
  private static final String MANUFACTURER = "fraunhofer_iml";
  /**
   * Serial number.
   */
  private static final String SERIAL_NUMBER = "S123";
  /**
   * Version.
   */
  private static final String VERSION = "v1";
  /**
   * The base path for any topic.
   */
  private static final String TOPIC_BASE
      = "uagv/" + VERSION + "/" + MANUFACTURER + "/" + SERIAL_NUMBER;
  /**
   * Movement speed in milliseconds.
   */
  private static final long MOVEMENTSPEED = 3000;
  /**
   * The executor for the simulation.
   */
  private final ScheduledExecutorService simulationExecutor = Executors
      .newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "simulationExecutor"));
  /**
   * The executor for specific tasks.
   */
  private final ScheduledExecutorService taskExecutor = Executors
      .newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "taskExecutor"));
  /**
   * Header id.
   */
  private long headerId;
  /**
   * MQTT client manager.
   */
  private final MqttClientManager clientManager;
  /**
   * Current vehicle state.
   */
  private final State vehicleState;
  /**
   * Binds JSON strings to objects and vice versa.
   */
  private final JsonBinder jsonBinder = new JsonBinder();
  /**
   * Maps actionId's to their state and action.
   */
  private final Map<String, ActionTuple> actionMap = new HashMap<>();
  /**
   * Current node for this vehicle.
   */
  private String currentNode;
  /**
   * The current order that is being processed.
   */
  private Order currentOrder;
  /**
   * Simulation task.
   */
  private ScheduledFuture<?> movementTask;

  /**
   * Creates a new instance.
   */
  public VehicleSimulator() {
    clientManager = new MqttClientManager(new ConfigurationImpl(), taskExecutor);
    // initialise vehicle state.
    vehicleState = new State(
        "",
        0L,
        "",
        0L,
        new ArrayList<>(),
        new ArrayList<>(),
        false,
        new ArrayList<>(),
        new BatteryState(100.0, false),
        OperatingMode.AUTOMATIC,
        new ArrayList<>(),
        new ArrayList<>(),
        new SafetyState(EStop.NONE, true)
    );
    vehicleState.setAgvPosition(new AgvPosition(0.0, 0.0, 0.0, "map", true));
  }

  /**
   * Initialises the MQTT connection.
   */
  private void initialize() {
    clientManager.registerConnectionEventListener(this);
    clientManager.subscribe(TOPIC_BASE + "/instantActions", QualityOfService.AT_LEAST_ONCE, this);
    clientManager.subscribe(TOPIC_BASE + "/order", QualityOfService.AT_LEAST_ONCE, this);
    // set connection broke last will
    try {
      String message = jsonBinder.toJson(
          new Connection(
              headerId++,
              Instant.now(),
              VERSION,
              MANUFACTURER,
              SERIAL_NUMBER,
              ConnectionState.CONNECTIONBROKEN
          )
      );
      clientManager.setLastWill(
          TOPIC_BASE + "/connection",
          message,
          QualityOfService.AT_LEAST_ONCE,
          true
      );
    }
    catch (IllegalArgumentException exc) {
      LOG.error("Failed to set last will {}", exc);
    }
  }

  /**
   * Closes the MQTT connection gracefully.
   */
  private void terminate() {
    if (clientManager.isConnected()) {
      sendConnection(
          new Connection(
              headerId++,
              Instant.now(),
              VERSION,
              MANUFACTURER,
              SERIAL_NUMBER,
              ConnectionState.OFFLINE
          )
      );
    }
  }

  /**
   * Crashes the MQTT connection.
   */
  private void terminateCrash() {
  }

  @Override
  public void onIncomingMessage(IncomingMessage message) {
    if (message.getTopic().endsWith("/instantActions")) {
      try {
        InstantActions instantAction = jsonBinder.fromJson(
            message.getMessage(),
            InstantActions.class
        );
        instantAction.getInstantActions().forEach(action -> newInstantAction(action));
      }
      catch (IllegalArgumentException ex) {
        LOG.warn("Unable to read instant action: {}", ex);
      }
    }
    if (message.getTopic().endsWith("/order")) {
      try {
        Order order = jsonBinder.fromJson(message.getMessage(), Order.class);
        onOrder(order);
      }
      catch (IllegalArgumentException ex) {
        LOG.warn("Unable to read order: {}", ex);
      }
    }

  }

  private void newInstantAction(Action action) {
    ActionTuple tuple = new ActionTuple();
    tuple.action = action;
    tuple.state = new ActionState(
        action.getActionId(),
        ActionStatus.WAITING
    )
        .setActionType(action.getActionType());
    actionMap.put(action.getActionId(), tuple);

    switch (tuple.action.getActionType()) {
      case "stateRequest":
        LOG.info("Received state request");
        tuple.state.setActionStatus(ActionStatus.FINISHED);
        sendState();
        break;
      default:
        LOG.info("Received unknown action type: {}", tuple.action.getActionType());
        tuple.state.setActionStatus(ActionStatus.FAILED);
        break;
    }
  }

  public void onOrder(Order order) {
    if (currentOrder == null || !currentOrder.getOrderId().equals(order.getOrderId())) {
      // received a new order
      if (currentOrderFinished()) {
        if (isNodeReachable(order.getNodes().get(0))) {
          vehicleState.getActionStates().clear();
          acceptNewOrder(order);
        }
        else {
          // error noRouteError
        }
      }
      else {
        // error orderUpdateError
      }
    }
    else {
      if (order.getOrderUpdateId() > currentOrder.getOrderUpdateId()) {
        if (currentOrderFinished()) {
          if (Objects.equals(
              vehicleState.getLastNodeId(),
              order.getNodes().get(0).getNodeId()
          )
              && Objects.equals(
                  vehicleState.getLastNodeSequenceId(),
                  order.getNodes().get(0).getSequenceId()
              )) {
            acceptNewOrder(order);
          }
        }
        else {
          // is the start of the new base the end of the old base end.
          NodeState oldBaseEnd = vehicleState.getNodeStates()
              .get(vehicleState.getNodeStates().size() - 1);
          if (order.getNodes().get(0).getNodeId().equals(oldBaseEnd.getNodeId())) {
            acceptOrderUpdate(order);
          }
          else {
            // error orderUpdateError
          }
        }
      }
      else if (order.getOrderUpdateId() < currentOrder.getOrderUpdateId()) {
        // error orderUpdateError
      }
      // else ignore message
    }
  }

  private boolean currentOrderFinished() {
    // TODO: implement this correctly.
    return currentOrder == null
        || vehicleState.getNodeStates().isEmpty() && vehicleState.getEdgeStates().isEmpty();
  }

  private boolean isNodeReachable(Node node) {
    // TODO: implement this correctly.
    return currentNode == null || node.getNodeId().equals(currentNode);
  }

  private void acceptNewOrder(Order order) {
    LOG.info("Accepting new order {}", order.getOrderId());
    currentOrder = order;
    vehicleState.setOrderId(order.getOrderId());
    vehicleState.setOrderUpdateId(order.getOrderUpdateId());
    vehicleState.getNodeStates().clear();
    order.getNodes().forEach(node -> {
      if (!node.isReleased()) {
        return;
      }
      NodeState state = new NodeState(
          node.getNodeId(),
          node.getSequenceId(),
          node.isReleased()
      );
      state.setNodePosition(node.getNodePosition());
      vehicleState.getNodeStates().add(state);
      node.getActions().forEach(action -> {
        vehicleState.getActionStates().add(
            new ActionState(
                action.getActionId(),
                ActionStatus.WAITING
            )
                .setActionType(action.getActionType())
        );
      });
    });
    vehicleState.getEdgeStates().clear();
    order.getEdges().forEach(edge -> {
      if (!edge.isReleased()) {
        return;
      }
      vehicleState.getEdgeStates().add(
          new EdgeState(
              edge.getEdgeId(),
              edge.getSequenceId(),
              edge.isReleased()
          )
      );
      edge.getActions().forEach(action -> {
        vehicleState.getActionStates().add(
            new ActionState(
                action.getActionId(),
                ActionStatus.WAITING
            )
                .setActionType(action.getActionType())
        );
      });
    });
    sendState();
    // reschedule movement task
    if (movementTask != null) {
      movementTask.cancel(false);
    }
    movementTask = taskExecutor.schedule(
        this::simulateMovement,
        MOVEMENTSPEED,
        TimeUnit.MILLISECONDS
    );
  }

  private void acceptOrderUpdate(Order order) {
    LOG.info("Order update. id: {}", order.getOrderUpdateId());
    currentOrder = order;
    vehicleState.setOrderId(order.getOrderId());
    vehicleState.setOrderUpdateId(order.getOrderUpdateId());
    // skip first node because that node is already in the list.
    order.getNodes().subList(1, order.getNodes().size()).forEach(node -> {
      if (!node.isReleased()) {
        return;
      }
      NodeState state = new NodeState(
          node.getNodeId(),
          node.getSequenceId(),
          node.isReleased()
      );
      state.setNodePosition(node.getNodePosition());
      vehicleState.getNodeStates().add(state);
      node.getActions().forEach(action -> {
        vehicleState.getActionStates().add(
            new ActionState(
                action.getActionId(),
                ActionStatus.WAITING
            )
                .setActionType(action.getActionType())
        );
      });
    });
    order.getEdges().forEach(edge -> {
      if (!edge.isReleased()) {
        return;
      }
      vehicleState.getEdgeStates().add(
          new EdgeState(
              edge.getEdgeId(),
              edge.getSequenceId(),
              edge.isReleased()
          )
      );
      edge.getActions().forEach(action -> {
        vehicleState.getActionStates().add(
            new ActionState(
                action.getActionId(),
                ActionStatus.WAITING
            )
                .setActionType(action.getActionType())
        );
      });
    });
    sendState();
  }

  private void simulateMovement() {
    movementTask = null;

    //if node state are empty there is nothing to simulate.
    if (!vehicleState.getNodeStates().isEmpty()) {
      LOG.info("simulate movement");

      // move to next node and remove it from node states.
      NodeState nextNode = vehicleState.getNodeStates().remove(0);
      vehicleState.setLastNodeId(nextNode.getNodeId());
      vehicleState.setLastNodeSequenceId(nextNode.getSequenceId());
      vehicleState.getAgvPosition().setX(nextNode.getNodePosition().getX());
      vehicleState.getAgvPosition().setY(nextNode.getNodePosition().getY());

      // remove edge that leads to the next node
      if (!vehicleState.getEdgeStates().isEmpty()
          && vehicleState.getEdgeStates().get(0).getSequenceId() < nextNode.getSequenceId()) {
        vehicleState.getEdgeStates().remove(0);
      }

      sendState();
      if (!vehicleState.getNodeStates().isEmpty()) {
        movementTask = taskExecutor.schedule(
            this::simulateMovement,
            MOVEMENTSPEED,
            TimeUnit.MILLISECONDS
        );
      }
    }
  }

  @Override
  public void onConnect() {
    LOG.info("Simulator connected to broker.");
    sendConnection(
        new Connection(
            headerId++,
            Instant.now(),
            VERSION,
            MANUFACTURER,
            SERIAL_NUMBER,
            ConnectionState.ONLINE
        )
    );
  }

  @Override
  public void onFailedConnectionAttempt() {
  }

  @Override
  public void onDisconnect() {
    LOG.info("Communication adapter disconnected from vehicle.");
    terminate();
  }

  @Override
  public void onIdle() {
    LOG.info("Communication adapter is idle.");
  }

  private void sendConnection(Connection connection) {
    try {
      String message = jsonBinder.toJson(connection);
      clientManager.publish(
          TOPIC_BASE + "/connection",
          QualityOfService.AT_LEAST_ONCE,
          message,
          true
      );
    }
    catch (IllegalArgumentException exc) {
      LOG.error("Failed to convert Connection to JSON {}", exc);
    }
  }

  private void sendState() {
    // update state
    vehicleState.setHeaderId(headerId++);
    vehicleState.setTimestamp(Instant.now());
    vehicleState.setVersion(VERSION);
    vehicleState.setManufacturer(MANUFACTURER);
    vehicleState.setSerialNumber(SERIAL_NUMBER);
    // update action states.
    vehicleState.setActionStates(
        actionMap.values().stream()
            .map(tuple -> tuple.state)
            .collect(Collectors.toList())
    );
    try {
      String message = jsonBinder.toJson(vehicleState);
      clientManager.publish(
          TOPIC_BASE + "/state",
          QualityOfService.AT_MOST_ONCE,
          message,
          false
      );
    }
    catch (IllegalArgumentException ex) {
      LOG.error("Failed to convert connection to JSON {}", ex);
    }
  }

  private void startSimulationThread() {
    Runnable simulationTask = () -> {
      LOG.info("Starting simulation... (press the return key to stop the simulation)");
      initialize();
      Scanner scanner = new Scanner(System.in);
      boolean loop = true;
      while (loop) {
        String in = scanner.nextLine();
        if (Strings.isNullOrEmpty(in)) {
          loop = false;
        }
        // Cancels the connection to the broker to trigger the last will message.
        else if (in.equals("stop-connection")) {
          LOG.info("Crashing the connection to the broker.");
          loop = false;
          terminateCrash();
        }
        else if (in.startsWith("move ")) {
          String destination = in.replace("move ", "");
          LOG.info("Moving to point {}", destination);
          vehicleState.setLastNodeId(destination);
          sendState();
        }
        else if (in.startsWith("setX ")) {
          String xPosition = in.replace("setX ", "");
          try {
            double xPos = Float.parseFloat(xPosition);
            vehicleState.getAgvPosition().setX(xPos);
          }
          catch (NumberFormatException e) {
            LOG.error("{} is not a valid number.", xPosition);
          }
        }
        else if (in.startsWith("setY ")) {
          String yPosition = in.replace("setY ", "");
          try {
            double yPos = Float.parseFloat(yPosition);
            vehicleState.getAgvPosition().setY(yPos);
          }
          catch (NumberFormatException e) {
            LOG.error("{} is not a valid number.", yPosition);
          }
        }
        else if (in.startsWith("setPos ")) {
          String destination = in.replace("setPos ", "");
          vehicleState.setLastNodeId(destination);
        }
        else if (in.equals("state")) {
          sendState();
          LOG.info("Sending state message!");
        }
        else {
          LOG.info("unknown command: " + in);
        }
      }
      LOG.info("Stopping simulation...");
      taskExecutor.shutdownNow();
      terminate();
      System.exit(0);
    };
    simulationExecutor.schedule(simulationTask, 0, TimeUnit.SECONDS);
  }

  /**
   * Starts the simulation of the vehicle.
   *
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    LogManager logManager = LogManager.getLogManager();
    try {
      Properties prop = new Properties();
      prop.put(
          "handlers",
          "java.util.logging.ConsoleHandler"
      );
      prop.put(
          "java.util.logging.ConsoleHandler.level",
          "INFO"
      );
      prop.put(
          "java.util.logging.ConsoleHandler.formatter",
          "org.opentcs.util.logging.SingleLineFormatter"
      );
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      prop.store(out, "");
      logManager.readConfiguration(new ByteArrayInputStream(out.toByteArray()));
    }
    catch (IOException | SecurityException e) {
      LOG.error("error setting up logger", e);
    }

    VehicleSimulator simulator = new VehicleSimulator();
    simulator.startSimulationThread();
  }

  private class ActionTuple {

    private Action action;
    private ActionState state;

    /**
     * Creates a new instance.
     */
    ActionTuple() {
    }
  }
}
