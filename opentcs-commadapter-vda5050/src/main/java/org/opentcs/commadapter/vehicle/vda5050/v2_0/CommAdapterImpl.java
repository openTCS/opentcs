// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getProperty;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getPropertyInteger;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getPropertyLong;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.MqttSetting.VERSION_MAJOR;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.MqttSetting.VERSION_MINOR;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.MqttSetting.VERSION_PATCH;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_ERRORS_FATAL;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_ERRORS_WARNING;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_INFORMATION_DEBUG;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_INFORMATION_INFO;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_LENGTH_LOADED;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_LENGTH_UNLOADED;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MAX_DISTANCE_IN_ADVANCE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MAX_IGNORED_REJECTIONS;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MAX_STEPS_BASE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MIN_VISU_INTERVAL;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_PAUSED;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_RECHARGE_OPERATION;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.StateMappings.toLoadHandlingDevices;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.StateMappings.toVehicleLength;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.StateMappings.toVehicleState;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import java.beans.PropertyChangeEvent;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.CommAdapterConfiguration;
import org.opentcs.commadapter.vehicle.vda5050.CommAdapterConfiguration.ConfigIntegrationLevel;
import org.opentcs.commadapter.vehicle.vda5050.CommAdapterConfiguration.ConfigOperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.common.DistanceInAdvanceController;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonBinder;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.ConnectionEventListener;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.IncomingMessage;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.MqttClientManager;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.QualityOfService;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter.ProcessModelImplTO;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.Header;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.AgvPosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.ConnectionState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorLevel;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.InfoLevel;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.visualization.Visualization;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping.ExecutableActionsTagsPredicate;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping.OrderMapper;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping.UnsupportedPropertiesExtractor;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The communication adapter implementation for VDA5050.
 */
public class CommAdapterImpl
    extends
      BasicVehicleCommAdapter
    implements
      ConnectionEventListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CommAdapterImpl.class);
  /**
   * Maps movement commands from openTCS to the telegrams sent to the attached vehicle.
   */
  private OrderMapper orderMapper;
  /**
   * Manages the client's connection to an MQTT broker.
   */
  private final MqttClientManager clientManager;
  /**
   * Matches a state messages with sent order messages to confirm their delivery.
   */
  private final MessageResponseMatcher messageResponseMatcher;
  /**
   * Manages the completion of movement commands.
   */
  private final MovementCommandManager movementCommandManager;
  /**
   * Factory for creating adapter components.
   */
  private final CommAdapterComponentsFactory componentsFactory;
  /**
   * The minimum visualizationInterval.
   */
  private final int minVisualizationInterval;
  /**
   * The vehicle's length when loaded.
   */
  private final long vehicleLengthLoaded;
  /**
   * The vehicle's length when unloaded.
   */
  private final long vehicleLengthUnloaded;
  /**
   * Validates messages against JSON schemas.
   */
  private final MessageValidator messageValidator;
  /**
   * Checks whether incoming messages should be accepted.
   */
  private final IncomingMessageFilter incomingMessageFilter;
  /**
   * Binds JSON strings to objects and vice versa.
   */
  private final JsonBinder jsonBinder;
  /**
   * Header id counter for message topics.
   */
  private final Map<String, Long> headerIdCounter = new HashMap<>();
  /**
   * The MQTT settings for this vehicle.
   */
  private final MqttSetting mqttSetting;
  /**
   * Timestamp of the last visualization message.
   */
  private long lastVisualizationMessageTimestamp;
  /**
   * Predicate to test if an action is executable by the vehicle.
   */
  private final ExecutableActionsTagsPredicate isActionExecutable;
  /**
   * The comm adapter configuration.
   */
  private final CommAdapterConfiguration configuration;
  /**
   * Limits the amount of orders sent to the vehicle in advance.
   */
  private final DistanceInAdvanceController distanceInAdvanceController;
  /**
   * Determines whether the deviation of nodes should be extended.
   */
  private final DeviationExtensionTrigger deviationExtensionTrigger;
  /**
   * Maps {@link VehicleCommAdapterMessage} to other types.
   */
  private final CommAdapterMessageMapper commAdapterMessageMapper;

  /**
   * Creates a new instance.
   *
   * @param vehicle The attached vehicle.
   * @param kernelExecutor The kernel's executor service.
   * @param componentsFactory A factory for our components.
   * @param clientManager The MQTT client manager to use.
   * @param messageValidator Validates messages against JSON schemas.
   * @param incomingMessageFilter Checks whether incoming messages should be accepted.
   * @param jsonBinder Binds JSON strings to objects and vice versa.
   * @param configuration The adapter configuration.
   * @param unsupportedPropertiesExtractor Extracts unsupported optional fields from the vehicle.
   */
  @SuppressWarnings("this-escape")
  @Inject
  public CommAdapterImpl(
      @Assisted
      Vehicle vehicle,
      @Assisted
      MqttSetting mqttSetting,
      @Assisted
      MessageValidator messageValidator,
      @KernelExecutor
      ScheduledExecutorService kernelExecutor,
      CommAdapterComponentsFactory componentsFactory,
      MqttClientManager clientManager,
      IncomingMessageFilter incomingMessageFilter,
      JsonBinder jsonBinder,
      CommAdapterConfiguration configuration,
      UnsupportedPropertiesExtractor unsupportedPropertiesExtractor
  ) {
    super(
        new ProcessModelImpl(vehicle),
        getPropertyInteger(PROPKEY_VEHICLE_MAX_STEPS_BASE, vehicle).orElse(2) + 1,
        getProperty(PROPKEY_VEHICLE_RECHARGE_OPERATION, vehicle)
            .orElse(DestinationOperations.CHARGE),
        kernelExecutor
    );
    this.mqttSetting = requireNonNull(mqttSetting, "mqttSetting");
    this.messageValidator = requireNonNull(messageValidator, "messageValidator");
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
    this.minVisualizationInterval
        = getPropertyInteger(PROPKEY_VEHICLE_MIN_VISU_INTERVAL, vehicle).orElse(500);
    this.vehicleLengthLoaded = getPropertyLong(PROPKEY_VEHICLE_LENGTH_LOADED, vehicle)
        .orElse(vehicle.getBoundingBox().getLength());
    this.vehicleLengthUnloaded = getPropertyLong(PROPKEY_VEHICLE_LENGTH_UNLOADED, vehicle)
        .orElse(vehicle.getBoundingBox().getLength());
    this.clientManager = requireNonNull(clientManager, "clientManager");
    this.incomingMessageFilter = requireNonNull(incomingMessageFilter, "incomingMessageFilter");
    this.jsonBinder = requireNonNull(jsonBinder, "jsonBinder");
    this.configuration = requireNonNull(configuration, "configuration");
    requireNonNull(unsupportedPropertiesExtractor, "unsupportedPropertiesExtractor");

    movementCommandManager = componentsFactory.createMovementCommandManager(vehicle);
    this.jsonBinder.setFilter(
        componentsFactory.createUnsupportedPropertiesFilter(
            vehicle, unsupportedPropertiesExtractor
        )
    );
    distanceInAdvanceController = componentsFactory.createDistanceInAdvanceController(
        getPropertyLong(PROPKEY_VEHICLE_MAX_DISTANCE_IN_ADVANCE, vehicle).orElse(Long.MAX_VALUE)
    );
    commAdapterMessageMapper = componentsFactory.createCommAdapterMessageMapper(vehicle);

    messageResponseMatcher = new MessageResponseMatcher(
        this.getName(),
        this::sendOrder,
        this::sendInstantAction,
        this::orderAccepted,
        getPropertyInteger(PROPKEY_VEHICLE_MAX_IGNORED_REJECTIONS, vehicle).orElse(0)
    );

    getProcessModel().setTopicPrefix(mqttSetting.topicNamePrefix());

    this.isActionExecutable = new ExecutableActionsTagsPredicate(vehicle);
    this.deviationExtensionTrigger = componentsFactory.createDeviationExtensionTrigger(vehicle);
  }

  @Override
  public void initialize() {
    super.initialize();
    orderMapper = componentsFactory.createOrderMapper(
        getProcessModel().getReference(),
        isActionExecutable,
        deviationExtensionTrigger
    );
  }

  @Override
  public void terminate() {
    super.terminate();
  }

  @Override
  public synchronized void enable() {
    if (isEnabled()) {
      return;
    }

    super.enable();

    clientManager.registerConnectionEventListener(this);
    clientManager.subscribe(
        mqttSetting.connectionTopicName(), mqttSetting.connectionTopicQos(), this
    );
    clientManager.subscribe(
        mqttSetting.stateTopicName(), mqttSetting.stateTopicQos(), this
    );
    clientManager.subscribe(
        mqttSetting.visualizationTopicName(), mqttSetting.visualizationTopicQos(), this
    );
    clientManager.subscribe(
        mqttSetting.factsheetTopicName(), mqttSetting.factsheetTopicQos(), this
    );

    // The client manager may have already been connected to the broker prior to this adapter
    // instance being enabled. Therefore, we have to actively check the broker connection state.
    if (clientManager.isConnected()) {
      onConnect();
    }

    messageResponseMatcher.clear();
    movementCommandManager.clear();
  }

  @Override
  public synchronized void disable() {
    if (!isEnabled()) {
      return;
    }

    clientManager.unsubscribe(mqttSetting.connectionTopicName(), this);
    clientManager.unsubscribe(mqttSetting.stateTopicName(), this);
    clientManager.unsubscribe(mqttSetting.visualizationTopicName(), this);
    clientManager.unsubscribe(mqttSetting.factsheetTopicName(), this);
    clientManager.unregisterConnectionEventListener(this);

    // With unregistering from the client manager, we will no longer receive any update regarding
    // the broker connection. Therefore, treat a disabled adapter instance as disconnected.
    onDisconnect();

    super.disable();
  }

  @Override
  public synchronized void clearCommandQueue() {
    super.clearCommandQueue();
    movementCommandManager.clear();
    messageResponseMatcher.clear();

    enqueueCancelOrder();
  }

  @Override
  protected synchronized void connectVehicle() {
  }

  @Override
  protected synchronized void disconnectVehicle() {
  }

  @Override
  protected synchronized boolean isVehicleConnected() {
    return getProcessModel().isBrokerConnected() && getProcessModel().isCommAdapterConnected();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    super.propertyChange(evt);
    if (!(evt.getSource() instanceof ProcessModelImpl)) {
      return;
    }

    // Handling of events from the vehicle gui panels start here
  }

  @Override
  public final ProcessModelImpl getProcessModel() {
    return (ProcessModelImpl) super.getProcessModel();
  }

  @Override
  protected VehicleProcessModelTO createCustomTransferableProcessModel() {
    return new ProcessModelImplTO()
        .setVehicleRef(getProcessModel().getReference())
        .setCurrentState(getProcessModel().getCurrentState())
        .setPreviousState(getProcessModel().getPreviousState())
        .setLastOrderSent(getProcessModel().getLastOrderSent())
        .setLastInstantActionsSent(getProcessModel().getLastInstantActionsSent())
        .setVehicleIdle(getProcessModel().isVehicleIdle())
        .setBrokerConnected(getProcessModel().isBrokerConnected())
        .setTopicPrefix(getProcessModel().getTopicPrefix())
        .setCurrentConnection(getProcessModel().getCurrentConnection())
        .setCurrentVisualization(getProcessModel().getCurrentVisualization());
  }

  @Override
  public synchronized void sendCommand(MovementCommand cmd)
      throws IllegalArgumentException {
    requireNonNull(cmd, "cmd");

    // If this is a drive order's first movement command AND the vehicle reports still having edges
    // or nodes, ensure they are cleared first by sending a cancelOrder action.
    if (cmd.getStep().getRouteIndex() == 0
        && (!getProcessModel().getCurrentState().getNodeStates().isEmpty()
            || !getProcessModel().getCurrentState().getEdgeStates().isEmpty())) {
      enqueueCancelOrder();
    }

    messageResponseMatcher.enqueueCommand(orderMapper.toOrder(cmd), cmd);
    deviationExtensionTrigger.reset();
  }

  @Override
  public boolean canAcceptNextCommand() {
    return super.canAcceptNextCommand() && distanceInAdvanceController.canAcceptNextCommand(
        Stream
            .concat(getUnsentCommands().stream(), getSentCommands().stream())
            .collect(Collectors.toList())
    );
  }

  @Override
  public synchronized ExplainedBoolean canProcess(TransportOrder order) {
    requireNonNull(order, "order");

    return canProcessList(
        order.getFutureDriveOrders().stream()
            .map(driveOrder -> driveOrder.getDestination().getOperation())
            .collect(Collectors.toList())
    );
  }

  private synchronized ExplainedBoolean canProcessList(List<String> operations) {
    requireNonNull(operations, "operations");

    boolean canProcess = true;
    String reason = "";
    if (!isEnabled()) {
      canProcess = false;
      reason = "Adapter not enabled";
    }
    if (canProcess && !isVehicleConnected()) {
      canProcess = false;
      reason = "Vehicle does not seem to be connected";
    }

    for (String operation : operations) {
      if (!isStandardOperation(operation)
          && !operation.equals(getRechargeOperation())
          && !isActionExecutable.test(operation)) {
        canProcess = false;
        reason = "Vehicle cannot process operation " + operation + ".";
        break;
      }
    }

    return new ExplainedBoolean(canProcess, reason);
  }

  private boolean isStandardOperation(String operation) {
    return operation.equals(DriveOrder.Destination.OP_NOP)
        || operation.equals(DriveOrder.Destination.OP_MOVE)
        || operation.equals(DriveOrder.Destination.OP_PARK);
  }

  @Override
  public void processMessage(
      @Nonnull
      VehicleCommAdapterMessage message
  ) {
    switch (message.getType()) {
      case CommAdapterMessages.SEND_ORDER_TYPE -> handleSendOrder(message);
      case CommAdapterMessages.SEND_INSTANT_ACTION_TYPE -> handleSendInstantAction(message);
      case CommAdapterMessages.EXTEND_DEVIATION_ONCE_TYPE -> handleExtendDeviationOnce();
      default -> LOG.warn("Ignoring unknown message type: {}", message.getType());
    }
  }

  //ConnectionEventListener
  @Override
  public void onConnect() {
    if (!isEnabled()) {
      return;
    }
    LOG.debug("{}: Connected to broker.", getName());

    getExecutor().execute(() -> getProcessModel().setBrokerConnected(true));
  }

  @Override
  public void onFailedConnectionAttempt() {
    if (!isEnabled()) {
      return;
    }

    getExecutor().execute(() -> {
      getProcessModel().setBrokerConnected(false);
      getProcessModel().setCommAdapterConnected(false);
    });
  }

  @Override
  public void onDisconnect() {
    LOG.debug("{}: Disconnected from broker.", getName());

    getExecutor().execute(() -> {
      incomingMessageFilter.reset();
      getProcessModel().setBrokerConnected(false);
      getProcessModel().setCommAdapterConnected(false);
      getProcessModel().setVehicleIdle(true);
      getProcessModel().setState(Vehicle.State.UNKNOWN);
    });
  }

  @Override
  public void onIdle() {
    LOG.debug("{}: Idle", getName());

    getExecutor().execute(() -> getProcessModel().setVehicleIdle(true));
  }

  @Override
  public synchronized void onIncomingMessage(IncomingMessage message) {
    requireNonNull(message, "message");

    if (Objects.equals(message.getTopic(), mqttSetting.connectionTopicName())) {
      try {
        messageValidator.validate(message.getMessage(), Connection.class);
        Connection connectionMessage = jsonBinder.fromJson(message.getMessage(), Connection.class);
        getExecutor().execute(() -> onConnectionMessage(connectionMessage));
      }
      catch (IllegalArgumentException ex) {
        LOG.warn("Cannot parse connection message: {}", message.getMessage(), ex);
      }
    }
    else if (Objects.equals(message.getTopic(), mqttSetting.stateTopicName())) {
      try {
        messageValidator.validate(message.getMessage(), State.class);
        State stateMessage = jsonBinder.fromJson(message.getMessage(), State.class);
        getExecutor().execute(() -> onStateMessage(stateMessage));
      }
      catch (IllegalArgumentException ex) {
        LOG.warn("Cannot parse state message: {}", message.getMessage(), ex);
      }
    }
    else if (Objects.equals(message.getTopic(), mqttSetting.visualizationTopicName())) {
      try {
        messageValidator.validate(message.getMessage(), Visualization.class);
        Visualization vis = jsonBinder.fromJson(message.getMessage(), Visualization.class);
        getExecutor().execute(() -> onVisualizationMessage(vis));
      }
      catch (IllegalArgumentException ex) {
        LOG.warn("Cannot parse visualization message: {}", message.getMessage(), ex);
      }
    }
    else if (Objects.equals(message.getTopic(), mqttSetting.factsheetTopicName())) {
      LOG.info("Received factsheet from vehicle, ignoring it.");
    }
    else {
      LOG.warn(
          "Incoming message on unhandled topic '{}': {}",
          message.getTopic(),
          message.getMessage()
      );
    }
  }

  @Override
  public void onVehiclePaused(boolean paused) {
    Action pauseAction = new Action(
        paused ? "startPause" : "stopPause",
        UUID.randomUUID().toString(),
        BlockingType.NONE
    );
    InstantActions instantAction = new InstantActions();
    instantAction.setActions(List.of(pauseAction));
    messageResponseMatcher.enqueueAction(instantAction);
  }

  private void onVisualizationMessage(Visualization vis) {
    LOG.debug("{}: Received a new visualization message: {}", getName(), vis);
    getProcessModel().setVehicleIdle(false);

    if (!incomingMessageFilter.accept(vis)) {
      LOG.warn("Discarding unacceptable visualization message: {}", vis);
      return;
    }

    long now = System.currentTimeMillis();
    if (now - lastVisualizationMessageTimestamp < minVisualizationInterval) {
      LOG.trace(
          "Visualization message discarded - last one was {} ms ago.",
          now - lastVisualizationMessageTimestamp
      );
      return;
    }
    lastVisualizationMessageTimestamp = now;

    if (vis.getAgvPosition() != null) {
      processVehiclePosition(vis.getAgvPosition());
    }
    getProcessModel().setCurrentVisualization(vis);
  }

  private void onConnectionMessage(Connection message) {
    LOG.debug("{}: Received a new connection message: {}", getName(), message);
    getProcessModel().setVehicleIdle(false);

    if (!incomingMessageFilter.accept(message)) {
      LOG.warn("Discarding unacceptable connection message: {}", message);
      return;
    }

    if (message.getConnectionState() == ConnectionState.OFFLINE
        || message.getConnectionState() == ConnectionState.CONNECTIONBROKEN) {
      getProcessModel().setCommAdapterConnected(false);
      getProcessModel().setState(Vehicle.State.UNKNOWN);
    }
    else if (message.getConnectionState() == ConnectionState.ONLINE) {
      getProcessModel().setCommAdapterConnected(true);
      getProcessModel().setState(Vehicle.State.IDLE);
    }
    getProcessModel().setCurrentConnection(message);
  }

  private void onStateMessage(State state) {
    LOG.debug("{}: Received a new state message: {}", getName(), state);
    getProcessModel().setVehicleIdle(false);

    if (!incomingMessageFilter.accept(state)) {
      LOG.warn("Discarding unacceptable state message: {}", state);
      return;
    }

    messageResponseMatcher.onStateMessage(state);

    // Update the vehicle's current state and remember the old one.
    getProcessModel().setPreviousState(getProcessModel().getCurrentState());
    getProcessModel().setCurrentState(state);

    boolean orderRejectedNow = StateMappings.vehicleRejectsOrder(state);
    boolean orderRejectedBefore = StateMappings.vehicleRejectsOrder(
        getProcessModel().getPreviousState()
    );

    if (orderRejectedNow != orderRejectedBefore) {
      if (orderRejectedNow) {
        LOG.info("{}: Vehicle indicates order rejection.", getName());
        getProcessModel().publishUserNotification(
            new UserNotification(
                getProcessModel().getName(),
                "Vehicle rejects its current VDA5050 order",
                UserNotification.Level.IMPORTANT
            )
        );
      }
      else {
        LOG.info("{}: Vehicle no longer indicates order rejection.", getName());
        getProcessModel().publishUserNotification(
            new UserNotification(
                getProcessModel().getName(),
                "Vehicle no longer rejects its current VDA5050 order",
                UserNotification.Level.IMPORTANT
            )
        );
      }
    }

    if (state.getAgvPosition() != null) {
      processVehiclePosition(state.getAgvPosition());
    }

    if (state.getLastNodeId() != null && !state.getLastNodeId().isBlank()) {
      String newVehiclePosition = state.getLastNodeId();
      if (!Objects.equals(newVehiclePosition, getProcessModel().getPosition())) {
        LOG.debug("{}: Vehicle is now at point {}", getName(), newVehiclePosition);
        getProcessModel().setPosition(newVehiclePosition);
      }
    }
    else if (state.getAgvPosition() != null) {
      getProcessModel().positionResolutionRequested(getProcessModel().getPose());
    }

    getProcessModel().setLoadHandlingDevices(toLoadHandlingDevices(state));
    getProcessModel().setEnergyLevel(state.getBatteryState().getBatteryCharge().intValue());
    getProcessModel().setProperty(
        PROPKEY_VEHICLE_ERRORS_FATAL,
        StateMappings.toErrorPropertyValue(state, ErrorLevel.FATAL)
    );
    getProcessModel().setProperty(
        PROPKEY_VEHICLE_ERRORS_WARNING,
        StateMappings.toErrorPropertyValue(state, ErrorLevel.WARNING)
    );
    getProcessModel().setProperty(
        PROPKEY_VEHICLE_INFORMATION_INFO,
        StateMappings.toInfoPropertyValue(state, InfoLevel.INFO)
    );
    getProcessModel().setProperty(
        PROPKEY_VEHICLE_INFORMATION_DEBUG,
        StateMappings.toInfoPropertyValue(state, InfoLevel.DEBUG)
    );
    getProcessModel().setProperty(
        PROPKEY_VEHICLE_PAUSED,
        StateMappings.toPausedPropertyValue(state)
    );
    getProcessModel().setState(toVehicleState(state));
    getProcessModel().setBoundingBox(
        getProcessModel().getBoundingBox().withLength(
            toVehicleLength(state, vehicleLengthUnloaded, vehicleLengthLoaded)
        )
    );

    processVehicleOperatingMode(state);

    movementCommandManager.onStateMessage(state, this::onMovementCommandExecuted);
  }

  private void onMovementCommandExecuted(
      @Nonnull
      MovementCommand finishedCommand
  ) {
    requireNonNull(finishedCommand, "finishedCommand");

    MovementCommand oldestCommand = getSentCommands().peek();
    if (Objects.equals(finishedCommand, oldestCommand)) {
      getSentCommands().poll();
      getProcessModel().commandExecuted(oldestCommand);
    }
    else {
      LOG.warn("Not oldest movement command: {} != {}", finishedCommand, oldestCommand);
    }
  }

  private void processVehicleOperatingMode(State state) {
    if (getProcessModel().getPreviousState().getOperatingMode() == state.getOperatingMode()) {
      return;
    }

    if (configuration.onOpModeChangeDoWithdrawOrder()
        .getOrDefault(mapToConfigOperatingMode(state.getOperatingMode()), Boolean.FALSE)) {
      getProcessModel().transportOrderWithdrawalRequested(true);
    }

    if (configuration.onOpModeChangeDoResetPosition()
        .getOrDefault(mapToConfigOperatingMode(state.getOperatingMode()), Boolean.FALSE)) {
      LOG.debug(
          "{}: Resetting last known vehicle position due to op mode change to {}...",
          getName(),
          state.getOperatingMode()
      );
      getProcessModel().setPosition(null);
    }

    configuration.onOpModeChangeDoUpdateIntegrationLevel()
        .getOrDefault(
            mapToConfigOperatingMode(state.getOperatingMode()),
            ConfigIntegrationLevel.LEAVE_UNCHANGED
        )
        .toIntegrationLevel()
        .ifPresent((integrationLevel) -> {
          getExecutor().execute(() -> {
            getProcessModel().integrationLevelChangeRequested(integrationLevel);
          });
        });
  }

  private ConfigOperatingMode mapToConfigOperatingMode(OperatingMode opMode) {
    switch (opMode) {
      case AUTOMATIC:
        return ConfigOperatingMode.AUTOMATIC;
      case SEMIAUTOMATIC:
        return ConfigOperatingMode.SEMIAUTOMATIC;
      case MANUAL:
        return ConfigOperatingMode.MANUAL;
      case SERVICE:
        return ConfigOperatingMode.SERVICE;
      case TEACHIN:
        return ConfigOperatingMode.TEACHIN;
      default:
        throw new IllegalArgumentException("Unmapped operating mode " + opMode.name());
    }
  }

  private void processVehiclePosition(AgvPosition position) {
    getProcessModel().setPose(
        new Pose(
            new Triple(
                (long) (position.getX() * 1000.0),
                (long) (position.getY() * 1000.0),
                0
            ),
            Math.toDegrees(position.getTheta())
        )
    );
  }

  /**
   * Sends an order to the vehicle.
   *
   * @param order the order to send.
   */
  public void sendOrder(Order order) {
    sendMessage(order, mqttSetting.orderTopicName(), mqttSetting.orderTopicQos());
    getProcessModel().setLastOrderSent(order);
  }

  /**
   * Sends an instant action to the vehicle.
   *
   * @param instantActions the action to send.
   */
  public void sendInstantAction(InstantActions instantActions) {
    sendMessage(
        instantActions,
        mqttSetting.instantActionsTopicName(),
        mqttSetting.instantActionsTopicQos()
    );
    getProcessModel().setLastInstantActionsSent(instantActions);
  }

  private void sendMessage(Header messageObject, String topic, QualityOfService qos) {
    // increment header id for this topic
    long headerId = headerIdCounter.getOrDefault(topic, 0L);
    headerIdCounter.put(topic, headerId + 1);

    // set header for message object
    messageObject.setHeaderId(headerId);
    messageObject.setTimestamp(Instant.now());
    messageObject.setVersion(VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH);
    messageObject.setManufacturer(mqttSetting.vehicleManufacturer());
    messageObject.setSerialNumber(mqttSetting.vehicleSerialNumber());
    try {
      String message = jsonBinder.toJson(messageObject);
      messageValidator.validate(message, messageObject.getClass());
      LOG.debug("{}: Sending message to '{}': {}", getName(), topic, message);
      clientManager.publish(topic, qos, message, false);
    }
    catch (IllegalArgumentException exc) {
      LOG.error("{}: Failed to convert to JSON {}", getName(), messageObject, exc);
    }
  }

  private void orderAccepted(OrderAssociation order) {
    movementCommandManager.enqueue(order);
  }

  private void enqueueCancelOrder() {
    Action cancelOrderAction = new Action(
        "cancelOrder",
        UUID.randomUUID().toString(),
        BlockingType.NONE
    );
    InstantActions instantAction = new InstantActions();
    instantAction.setActions(List.of(cancelOrderAction));
    messageResponseMatcher.enqueueAction(instantAction);
    deviationExtensionTrigger.onCancelOrderEnqueued();
  }

  private void handleSendOrder(VehicleCommAdapterMessage message) {
    commAdapterMessageMapper.toOrder(message)
        .ifPresent(this::sendOrder);
  }

  private void handleSendInstantAction(VehicleCommAdapterMessage message) {
    commAdapterMessageMapper.toAction(message)
        .ifPresent(
            action -> messageResponseMatcher.enqueueAction(
                new InstantActions().setActions(List.of(action))
            )
        );
  }

  private void handleExtendDeviationOnce() {
    deviationExtensionTrigger.onExtensionRequestedManually();
  }
}
