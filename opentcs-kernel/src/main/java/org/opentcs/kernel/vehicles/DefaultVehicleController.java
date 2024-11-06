// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static java.util.Objects.requireNonNull;
import static org.opentcs.kernel.vehicles.MovementComparisons.equalsInMovement;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkState;

import com.google.inject.assistedinject.Assisted;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.components.kernel.ResourceAllocationException;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.IncomingPoseTransformer;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.MovementCommandTransformer;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleController;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.ProcessModelEvent;
import org.opentcs.kernel.KernelApplicationConfiguration;
import org.opentcs.kernel.vehicles.transformers.VehicleDataTransformerRegistry;
import org.opentcs.util.ExplainedBoolean;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Realizes a bidirectional connection between the kernel and a communication adapter controlling a
 * vehicle.
 */
public class DefaultVehicleController
    implements
      VehicleController,
      Scheduler.Client,
      PropertyChangeListener,
      EventHandler {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultVehicleController.class);
  /**
   * The kernel's vehicle service.
   */
  private final InternalVehicleService vehicleService;
  /**
   * The kernel's transport order service.
   */
  private final InternalTransportOrderService transportOrderService;
  /**
   * The kernel's notification service.
   */
  private final NotificationService notificationService;
  /**
   * The kernel's dispatcher service.
   */
  private final DispatcherService dispatcherService;
  /**
   * The scheduler maintaining the resources.
   */
  private final Scheduler scheduler;
  /**
   * The event bus we should register with and send events to.
   */
  private final EventBus eventBus;
  /**
   * The vehicle controlled by this controller/the communication adapter.
   */
  private final Vehicle vehicle;
  /**
   * The communication adapter controlling the physical vehicle.
   */
  private final VehicleCommAdapter commAdapter;
  /**
   * This controller's <em>enabled</em> flag.
   */
  private volatile boolean initialized;
  /**
   * Manages interactions with peripheral devices that are to be performed before or after the
   * execution of movement commands.
   */
  private final PeripheralInteractor peripheralInteractor;
  /**
   * Maps drive orders to movement commands.
   */
  private final MovementCommandMapper movementCommandMapper;
  /**
   * The configuration to use.
   */
  private final KernelApplicationConfiguration configuration;
  /**
   * The transport order that the vehicle is currently processing.
   */
  private volatile TransportOrder transportOrder;
  /**
   * The drive order that the vehicle currently has to process.
   */
  private volatile DriveOrder currentDriveOrder;
  /**
   * A flag indicating if the vehicle controller is allowed to send commands to the vehicle driver.
   */
  private boolean sendingCommandsAllowed;
  /**
   * Tracks processing of movement commands.
   */
  private final CommandProcessingTracker commandProcessingTracker;
  /**
   * A transformer transforming movement commands.
   */
  private final MovementCommandTransformer movementCommandTransformer;
  /**
   * A transformer transforming incoming poses.
   */
  private final IncomingPoseTransformer incomingPoseTransformer;
  /**
   * A map of transformed movement commands to their corresponding original ones.
   */
  private final Map<MovementCommand, MovementCommand> transformedToOriginalCommands
      = new HashMap<>();

  /**
   * Creates a new instance associated with the given vehicle.
   *
   * @param vehicle The vehicle this vehicle controller will be associated with.
   * @param adapter The communication adapter of the associated vehicle.
   * @param vehicleService The kernel's vehicle service.
   * @param transportOrderService The kernel's transport order service.
   * @param notificationService The kernel's notification service.
   * @param dispatcherService The kernel's dispatcher service.
   * @param scheduler The scheduler managing resource allocations.
   * @param eventBus The event bus this instance should register with and send events to.
   * @param componentsFactory A factory for various components related to a vehicle controller.
   * @param movementCommandMapper Maps drive orders to movement commands.
   * @param configuration The configuration to use.
   * @param commandProcessingTracker Track processing of movement commands.
   * @param dataTransformerRegistry A registry for data transformer factories.
   */
  @Inject
  public DefaultVehicleController(
      @Assisted
      @Nonnull
      Vehicle vehicle,
      @Assisted
      @Nonnull
      VehicleCommAdapter adapter,
      @Nonnull
      InternalVehicleService vehicleService,
      @Nonnull
      InternalTransportOrderService transportOrderService,
      @Nonnull
      NotificationService notificationService,
      @Nonnull
      DispatcherService dispatcherService,
      @Nonnull
      Scheduler scheduler,
      @Nonnull
      @ApplicationEventBus
      EventBus eventBus,
      @Nonnull
      VehicleControllerComponentsFactory componentsFactory,
      @Nonnull
      MovementCommandMapper movementCommandMapper,
      @Nonnull
      KernelApplicationConfiguration configuration,
      @Nonnull
      CommandProcessingTracker commandProcessingTracker,
      @Nonnull
      VehicleDataTransformerRegistry dataTransformerRegistry
  ) {
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.commAdapter = requireNonNull(adapter, "adapter");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.transportOrderService = requireNonNull(transportOrderService, "transportOrderService");
    this.notificationService = requireNonNull(notificationService, "notificationService");
    this.dispatcherService = requireNonNull(dispatcherService, "dispatcherService");
    this.scheduler = requireNonNull(scheduler, "scheduler");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    requireNonNull(componentsFactory, "componentsFactory");
    this.peripheralInteractor
        = componentsFactory.createPeripheralInteractor(vehicle.getReference());
    this.movementCommandMapper = requireNonNull(movementCommandMapper, "movementCommandMapper");
    this.configuration = requireNonNull(configuration, "configuration");
    this.commandProcessingTracker
        = requireNonNull(commandProcessingTracker, "commandProcessingTracker");
    requireNonNull(dataTransformerRegistry, "dataTransformerRegistry");
    this.movementCommandTransformer
        = dataTransformerRegistry
            .findFactoryFor(vehicle)
            .createMovementCommandTransformer(vehicle);
    this.incomingPoseTransformer
        = dataTransformerRegistry
            .findFactoryFor(vehicle)
            .createIncomingPoseTransformer(vehicle);
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    eventBus.subscribe(this);

    vehicleService.updateVehicleRechargeOperation(
        vehicle.getReference(),
        commAdapter.getRechargeOperation()
    );
    commAdapter.getProcessModel().addPropertyChangeListener(this);

    // Initialize standard attributes once.
    setVehiclePosition(commAdapter.getProcessModel().getPosition());
    updateVehiclePose(commAdapter.getProcessModel().getPose());
    vehicleService.updateVehicleEnergyLevel(
        vehicle.getReference(),
        commAdapter.getProcessModel().getEnergyLevel()
    );
    vehicleService.updateVehicleLoadHandlingDevices(
        vehicle.getReference(),
        commAdapter.getProcessModel().getLoadHandlingDevices()
    );
    updateVehicleState(commAdapter.getProcessModel().getState());
    updateVehicleBoundingBox(commAdapter.getProcessModel().getBoundingBox());

    commandProcessingTracker.clear();

    peripheralInteractor.initialize();

    sendingCommandsAllowed = true;

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    peripheralInteractor.terminate();

    commAdapter.getProcessModel().removePropertyChangeListener(this);
    // Reset the vehicle's position.
    updatePosition(null, null);
    updateVehiclePose(new Pose(null, Double.NaN));
    // Free all allocated resources.
    freeAllResources();

    updateVehicleState(Vehicle.State.UNKNOWN);

    eventBus.unsubscribe(this);

    sendingCommandsAllowed = false;

    initialized = false;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getSource() != commAdapter.getProcessModel()) {
      return;
    }

    handleProcessModelEvent(evt);
  }

  @Override
  public void onEvent(Object event) {
    if (!(event instanceof TCSObjectEvent)) {
      return;
    }

    TCSObjectEvent objectEvent = (TCSObjectEvent) event;
    if (objectEvent.getType() != TCSObjectEvent.Type.OBJECT_MODIFIED) {
      return;
    }

    if (!(objectEvent.getCurrentOrPreviousObjectState() instanceof Vehicle)) {
      return;
    }

    if (!(Objects.equals(
        objectEvent.getCurrentOrPreviousObjectState().getName(),
        vehicle.getName()
    ))) {
      return;
    }

    Vehicle prevVehicleState = (Vehicle) objectEvent.getPreviousObjectState();
    Vehicle currVehicleState = (Vehicle) objectEvent.getCurrentObjectState();

    if (prevVehicleState.getIntegrationLevel() != currVehicleState.getIntegrationLevel()) {
      onIntegrationLevelChange(prevVehicleState, currVehicleState);
    }
  }

  @Override
  public void setTransportOrder(
      @Nonnull
      TransportOrder newOrder
  )
      throws IllegalArgumentException {
    requireNonNull(newOrder, "newOrder");
    requireNonNull(newOrder.getCurrentDriveOrder(), "newOrder.getCurrentDriveOrder()");

    if (transportOrder == null
        || !Objects.equals(newOrder.getName(), transportOrder.getName())
        || newOrder.getCurrentDriveOrderIndex() != transportOrder.getCurrentDriveOrderIndex()) {
      // We received either a new transport order or the same transport order for its next drive
      // order.
      sendingCommandsAllowed = true;
      transformedToOriginalCommands.clear();
      transportOrder = newOrder;
      setDriveOrder(transportOrder.getCurrentDriveOrder(), transportOrder.getProperties());
    }
    else {
      // We received an update for a drive order we're already processing.
      transportOrder = newOrder;

      checkArgument(
          driveOrdersContinual(currentDriveOrder, transportOrder.getCurrentDriveOrder()),
          "The new and old drive orders are not considered continual."
      );

      if (isForcedRerouting(transportOrder.getCurrentDriveOrder())) {
        Vehicle currVehicle = vehicleService.fetchObject(Vehicle.class, vehicle.getReference());
        if (currVehicle.getCurrentPosition() == null) {
          throw new IllegalArgumentException("The vehicle's current position is unknown.");
        }

        sendingCommandsAllowed = true;
        transformedToOriginalCommands.clear();

        Point currPosition = vehicleService.fetchObject(
            Point.class,
            currVehicle.getCurrentPosition()
        );
        // Before interacting with the scheduler in any way, ensure that we will be able to
        // allocate the required resources.
        if (!mayAllocateNow(Set.of(currPosition))) {
          throw new IllegalArgumentException(
              "Resources for the vehicle's current position may not be allocated now."
          );
        }

        freeAllResources();
        try {
          // Allocate the resources for the vehicle's current position.
          scheduler.allocateNow(this, Set.of(currPosition));
          commandProcessingTracker.allocationReset(Set.of(currPosition));
          vehicleService.updateVehicleAllocatedResources(
              vehicle.getReference(),
              toListOfResourceSets(commandProcessingTracker.getAllocatedResources())
          );
        }
        catch (ResourceAllocationException ex) {
          // May never happen. The caller is expected to call mayAllocateNow() first before applying
          // forced rerouting.
          throw new IllegalArgumentException(
              "Unable to allocate resources for the vehicle's current position.",
              ex
          );
        }
      }

      updateDriveOrder(transportOrder.getCurrentDriveOrder(), transportOrder.getProperties());
    }
  }

  private void setDriveOrder(
      @Nonnull
      DriveOrder newOrder,
      @Nonnull
      Map<String, String> orderProperties
  )
      throws IllegalArgumentException {
    synchronized (commAdapter) {
      requireNonNull(newOrder, "newOrder");
      requireNonNull(orderProperties, "orderProperties");
      requireNonNull(newOrder.getRoute(), "newOrder.getRoute()");
      // Assert that there isn't still is a drive order that hasn't been finished/removed, yet.
      checkArgument(
          currentDriveOrder == null,
          "%s still has an order! Current order: %s, new order: %s",
          vehicle.getName(),
          currentDriveOrder,
          newOrder
      );

      LOG.debug("{}: Setting drive order: {}", vehicle.getName(), newOrder);

      currentDriveOrder = newOrder;

      commandProcessingTracker.driveOrderUpdated(
          movementCommandMapper.toMovementCommands(newOrder, transportOrder)
      );

      // Set the claim for (the remainder of) this transport order.
      List<Set<TCSResource<?>>> claim = currentClaim(transportOrder);
      scheduler.claim(this, claim);

      vehicleService.updateVehicleClaimedResources(
          vehicle.getReference(),
          toListOfResourceSets(claim)
      );

      if (canSendNextCommand()) {
        allocateForNextCommand();
      }

      // Set the vehicle's next expected position.
      Point nextPoint = newOrder.getRoute().getSteps().get(0).getDestinationPoint();
      vehicleService.updateVehicleNextPosition(
          vehicle.getReference(),
          nextPoint.getReference()
      );
    }
  }

  private void updateDriveOrder(
      @Nonnull
      DriveOrder newOrder,
      @Nonnull
      Map<String, String> orderProperties
  )
      throws IllegalArgumentException {
    synchronized (commAdapter) {
      requireNonNull(newOrder, "newOrder");
      checkArgument(currentDriveOrder != null, "There's no drive order to be updated");

      LOG.debug("{}: Updating drive order: {}", vehicle.getName(), newOrder);
      // Update the current drive order and future commands
      currentDriveOrder = newOrder;
      // There is a new drive order, so discard all the future/scheduled commands of the old one.
      discardFutureCommands();

      commandProcessingTracker.driveOrderUpdated(
          movementCommandMapper.toMovementCommands(newOrder, transportOrder)
      );

      // Update the claim.
      List<Set<TCSResource<?>>> claim = currentClaim(transportOrder);
      scheduler.claim(this, claim);

      vehicleService.updateVehicleClaimedResources(
          vehicle.getReference(),
          toListOfResourceSets(claim)
      );

      // The vehicle may now process previously restricted steps.
      if (canSendNextCommand()) {
        allocateForNextCommand();
      }
    }
  }

  private boolean driveOrdersContinual(DriveOrder oldOrder, DriveOrder newOrder) {
    LOG.debug(
        "{}: Checking drive order continuity for {} (old) and {} (new).",
        vehicle.getName(), oldOrder, newOrder
    );

    if (getLastCommandExecutedRouteIndex() == TransportOrder.ROUTE_STEP_INDEX_DEFAULT) {
      LOG.debug("{}: Drive orders continuous: No route progress, yet.", vehicle.getName());
      return true;
    }

    List<Step> oldSteps = oldOrder.getRoute().getSteps();
    List<Step> newSteps = newOrder.getRoute().getSteps();

    // Compare already processed steps (up to and including the last executed command) for equality.
    List<Step> oldProcessedSteps = oldSteps.subList(0, getLastCommandExecutedRouteIndex() + 1);
    List<Step> newProcessedSteps = newSteps.subList(0, getLastCommandExecutedRouteIndex() + 1);
    LOG.debug(
        "{}: Comparing already processed steps for equality: {} (old) and {} (new)",
        vehicle.getName(),
        oldProcessedSteps,
        newProcessedSteps
    );
    if (!equalsInMovement(oldProcessedSteps, newProcessedSteps)) {
      LOG.debug(
          "{}: Drive orders not continuous: Mismatching old and new processed steps.",
          vehicle.getName()
      );
      return false;
    }

    if (isForcedRerouting(newOrder)) {
      LOG.debug("{}: Drive orders continuous: New order with forced rerouting.", vehicle.getName());
      return true;
    }

    // Compare pending steps (after the last executed command) for equality.
    int futureOrCurrentPositionIndex = getFutureOrCurrentPositionIndex();
    List<Step> oldPendingSteps = oldSteps.subList(
        getLastCommandExecutedRouteIndex() + 1,
        futureOrCurrentPositionIndex + 1
    );
    List<Step> newPendingSteps = newSteps.subList(
        getLastCommandExecutedRouteIndex() + 1,
        futureOrCurrentPositionIndex + 1
    );
    LOG.debug(
        "{}: Comparing pending steps for equality: {} (old) and {} (new)",
        vehicle.getName(),
        oldPendingSteps,
        newPendingSteps
    );
    if (!equalsInMovement(oldPendingSteps, newPendingSteps)) {
      LOG.debug(
          "{}: Drive orders not continuous: Mismatching old and new pending steps.",
          vehicle.getName()
      );
      return false;
    }

    LOG.debug("{}: Drive orders continuous.", vehicle.getName());
    return true;
  }

  private int getFutureOrCurrentPositionIndex() {
    if (commandProcessingTracker.getSentCommands().isEmpty()
        && getInteractionsPendingCommand().isEmpty()) {
      LOG.debug(
          "{}: No commands expected to be executed. Last executed command route index: {}",
          vehicle.getName(),
          getLastCommandExecutedRouteIndex()
      );
      return getLastCommandExecutedRouteIndex();
    }

    if (getInteractionsPendingCommand().isPresent()) {
      LOG.debug(
          "{}: Command with pending peripheral operations present. Route index: {}",
          vehicle.getName(),
          getInteractionsPendingCommand().orElseThrow().getStep().getRouteIndex()
      );
      return getInteractionsPendingCommand().orElseThrow().getStep().getRouteIndex();
    }

    MovementCommand lastCommandSent = commandProcessingTracker.getSentCommands().getLast();
    LOG.debug(
        "{}: Using the last command sent to the communication adapter. Route index: {}",
        vehicle.getName(),
        lastCommandSent.getStep().getRouteIndex()
    );
    return lastCommandSent.getStep().getRouteIndex();
  }

  private void discardFutureCommands() {
    withdrawPendingResourceAllocations();
  }

  @Override
  public void abortTransportOrder(boolean immediate) {
    synchronized (commAdapter) {
      if (immediate) {
        clearDriveOrder();

        withdrawPendingResourceAllocations();

        scheduler.claim(this, List.of());
      }
      else {
        abortDriveOrder();

        withdrawPendingResourceAllocations();

        commandProcessingTracker.driveOrderAborted(false);

        scheduler.claim(this, List.of());

        checkForPendingCommands();
      }

      vehicleService.updateVehicleClaimedResources(vehicle.getReference(), List.of());
      vehicleService.updateVehicleAllocatedResources(
          vehicle.getReference(),
          toListOfResourceSets(commandProcessingTracker.getAllocatedResources())
      );
    }
  }

  private void clearDriveOrder() {
    synchronized (commAdapter) {
      currentDriveOrder = null;

      clearCommandQueue();
    }
  }

  private void abortDriveOrder() {
    synchronized (commAdapter) {
      if (currentDriveOrder == null) {
        LOG.debug("{}: No drive order to be aborted", vehicle.getName());
        return;
      }
    }
  }

  private void clearCommandQueue() {
    synchronized (commAdapter) {
      commAdapter.clearCommandQueue();

      Collection<Set<TCSResource<?>>> resourceToBeFreed
          = commandProcessingTracker.getAllocatedResourcesAhead();
      commandProcessingTracker.driveOrderAborted(true);

      peripheralInteractor.clear();

      for (Set<TCSResource<?>> resSet : resourceToBeFreed) {
        scheduler.free(this, resSet);
      }
    }
  }

  @Override
  @Nonnull
  public ExplainedBoolean canProcess(TransportOrder order) {
    requireNonNull(order, "order");

    synchronized (commAdapter) {
      return commAdapter.canProcess(order);
    }
  }

  @Override
  public void onVehiclePaused(boolean paused) {
    synchronized (commAdapter) {
      commAdapter.onVehiclePaused(paused);
    }
  }

  @Override
  public void sendCommAdapterMessage(
      @Nullable
      Object message
  ) {
    synchronized (commAdapter) {
      commAdapter.processMessage(message);
    }
  }

  @Override
  public void sendCommAdapterCommand(AdapterCommand command) {
    synchronized (commAdapter) {
      commAdapter.execute(command);
    }
  }

  @Override
  public Queue<MovementCommand> getCommandsSent() {
    return commandProcessingTracker.getSentCommands();
  }

  @Override
  public Optional<MovementCommand> getInteractionsPendingCommand() {
    return commandProcessingTracker.getSendingPendingCommand();
  }

  @Override
  public boolean mayAllocateNow(Set<TCSResource<?>> resources) {
    return scheduler.mayAllocateNow(this, resources);
  }

  @Override
  @Nonnull
  public String getId() {
    return vehicle.getName();
  }

  @Override
  public TCSObjectReference<Vehicle> getRelatedVehicle() {
    return vehicle.getReference();
  }

  @Override
  public boolean allocationSuccessful(
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(resources, "resources");

    synchronized (commAdapter) {
      // Check if we've actually been waiting for these resources now. If not,
      // let the scheduler know that we don't want them.
      if (!Objects.equals(
          resources,
          commandProcessingTracker.getAllocationPendingResources().orElse(null)
      )) {
        LOG.warn(
            "{}: Allocated resources ({}) != pending resources ({}), refusing them",
            vehicle.getName(),
            resources,
            commandProcessingTracker.getAllocationPendingResources()
        );
        return false;
      }

      LOG.debug("{}: Accepting allocated resources: {}", vehicle.getName(), resources);

      commandProcessingTracker.allocationConfirmed(resources);

      MovementCommand command = commandProcessingTracker.getSendingPendingCommand().orElseThrow();

      vehicleService.updateVehicleClaimedResources(
          vehicle.getReference(),
          toListOfResourceSets(currentClaim(transportOrder))
      );
      vehicleService.updateVehicleAllocatedResources(
          vehicle.getReference(),
          toListOfResourceSets(commandProcessingTracker.getAllocatedResources())
      );

      peripheralInteractor.prepareInteractions(transportOrder.getReference(), command);
      peripheralInteractor.startPreMovementInteractions(
          command,
          () -> sendCommandOrStopSending(command),
          this::onPreMovementInteractionFailed
      );
    }
    // Let the scheduler know we've accepted the resources given.
    return true;
  }

  @Override
  public void allocationFailed(
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(resources, "resources");
    throw new IllegalStateException("Failed to allocate: " + resources);
  }

  @Override
  public String toString() {
    return "DefaultVehicleController{" + "vehicleName=" + vehicle.getName() + '}';
  }

  private void sendCommandOrStopSending(MovementCommand command) {
    if (sendingCommandsAllowed) {
      sendCommand(command);
    }
    else {
      LOG.debug(
          "{}: Sending commands not allowed. Discarding movement command: {}",
          vehicle.getName(),
          command
      );
      commandProcessingTracker.commandSendingStopped(command);
    }
  }

  private void sendCommand(MovementCommand command)
      throws IllegalStateException {
    LOG.debug("{}: Enqueuing movement command with comm adapter: {}", vehicle.getName(), command);

    MovementCommand transformedCommand = movementCommandTransformer.apply(command);
    // Send the command to the communication adapter.
    checkState(
        commAdapter.enqueueCommand(transformedCommand),
        "Comm adapter did not accept command"
    );
    transformedToOriginalCommands.put(transformedCommand, command);
    commandProcessingTracker.commandSent(command);

    // Check if the communication adapter has capacity for another command.
    if (canSendNextCommand()) {
      allocateForNextCommand();
    }
  }

  private void onPreMovementInteractionFailed() {
    // Implementation remark: This method is called only for interactions where a peripheral job
    // with the completion required flag set has failed.
    LOG.warn("{}: Pre-movement interaction failed.", vehicle.getName());

    // With a failed pre-movement interaction, the movement command for the latest allocated
    // resources will not be sent to the vehicle. Therefore, free these resources.
    Set<TCSResource<?>> res = commandProcessingTracker.getAllocatedResources().peekLast();
    scheduler.free(this, res);
    commandProcessingTracker.allocationRevoked(res);
    vehicleService.updateVehicleAllocatedResources(
        vehicle.getReference(),
        toListOfResourceSets(commandProcessingTracker.getAllocatedResources())
    );

    dispatcherService.withdrawByVehicle(vehicle.getReference(), false);
  }

  private void onPostMovementInteractionFailed() {
    // Implementation remark: This method is called only for interactions where a peripheral job
    // with the completion required flag set has failed.
    LOG.warn("{}: Post-movement interaction failed.", vehicle.getName());

    dispatcherService.withdrawByVehicle(vehicle.getReference(), false);
  }

  @SuppressWarnings("unchecked")
  private void handleProcessModelEvent(PropertyChangeEvent evt) {
    eventBus.onEvent(
        new ProcessModelEvent(
            evt.getPropertyName(),
            commAdapter.createTransferableProcessModel()
        )
    );

    if (Objects.equals(evt.getPropertyName(), VehicleProcessModel.Attribute.POSITION.name())) {
      updateVehiclePosition((String) evt.getNewValue());
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.POSE.name()
    )) {
      if (vehicleService.fetchObject(Vehicle.class, vehicle.getReference()).getIntegrationLevel()
          != Vehicle.IntegrationLevel.TO_BE_IGNORED) {
        updateVehiclePose((Pose) evt.getNewValue());
      }
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.ENERGY_LEVEL.name()
    )) {
      vehicleService.updateVehicleEnergyLevel(vehicle.getReference(), (Integer) evt.getNewValue());
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.LOAD_HANDLING_DEVICES.name()
    )) {
      vehicleService.updateVehicleLoadHandlingDevices(
          vehicle.getReference(),
          (List<LoadHandlingDevice>) evt.getNewValue()
      );
    }
    else if (Objects.equals(evt.getPropertyName(), VehicleProcessModel.Attribute.STATE.name())) {
      updateVehicleState((Vehicle.State) evt.getNewValue());
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.BOUNDING_BOX.name()
    )) {
      updateVehicleBoundingBox((BoundingBox) evt.getNewValue());
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.COMMAND_EXECUTED.name()
    )) {
      commandExecuted((MovementCommand) evt.getNewValue());
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.COMMAND_FAILED.name()
    )) {
      commandFailed((MovementCommand) evt.getNewValue());
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.USER_NOTIFICATION.name()
    )) {
      notificationService.publishUserNotification((UserNotification) evt.getNewValue());
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.COMM_ADAPTER_EVENT.name()
    )) {
      eventBus.onEvent(evt.getNewValue());
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.VEHICLE_PROPERTY.name()
    )) {
      VehicleProcessModel.VehiclePropertyUpdate propUpdate
          = (VehicleProcessModel.VehiclePropertyUpdate) evt.getNewValue();
      vehicleService.updateObjectProperty(
          vehicle.getReference(),
          propUpdate.getKey(),
          propUpdate.getValue()
      );
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.TRANSPORT_ORDER_PROPERTY.name()
    )) {
      VehicleProcessModel.TransportOrderPropertyUpdate propUpdate
          = (VehicleProcessModel.TransportOrderPropertyUpdate) evt.getNewValue();
      if (currentDriveOrder != null) {
        vehicleService.updateObjectProperty(
            currentDriveOrder.getTransportOrder(),
            propUpdate.getKey(),
            propUpdate.getValue()
        );
      }
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.INTEGRATION_LEVEL_CHANGE_REQUESTED.name()
    )) {
      vehicleService.updateVehicleIntegrationLevel(
          vehicle.getReference(),
          (Vehicle.IntegrationLevel) evt.getNewValue()
      );
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.TRANSPORT_ORDER_WITHDRAWAL_REQUESTED.name()
    )) {
      dispatcherService.withdrawByVehicle(vehicle.getReference(), (Boolean) evt.getNewValue());
    }
  }

  private void withdrawPendingResourceAllocations() {
    scheduler.clearPendingAllocations(this);
  }

  private void updateVehiclePose(
      @Nonnull
      Pose pose
  )
      throws ObjectUnknownException {
    requireNonNull(pose, "pose");
    vehicleService.updateVehiclePose(vehicle.getReference(), incomingPoseTransformer.apply(pose));
  }

  private void updateVehiclePosition(String position) {
    // Get an up-to-date copy of the vehicle
    Vehicle currVehicle = vehicleService.fetchObject(Vehicle.class, vehicle.getReference());

    if (currVehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_RESPECTED
        || currVehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        || currVehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_NOTICED) {
      setVehiclePosition(position);
    }
  }

  private void setVehiclePosition(String position) {
    // Place the vehicle on the given position, regardless of what the kernel
    // might expect. The vehicle is physically there, even if it shouldn't be.
    // The same is true for null values - if the vehicle says it's not on any
    // known position, it has to be treated as a fact.
    Point point;
    if (position == null) {
      point = null;
    }
    else {
      point = vehicleService.fetchObject(Point.class, position);
      // If the new position is not in the model, ignore it. (Some vehicles/drivers send
      // intermediate positions that cannot be order destinations and thus do not exist in
      // the model.
      if (point == null) {
        LOG.warn("{}: At unknown position {}", vehicle.getName(), position);
        return;
      }
    }
    synchronized (commAdapter) {
      // If the current drive order is null, just set the vehicle's position.
      if (currentDriveOrder == null) {
        LOG.debug(
            "{}: Reported new position {} and we do not have a drive order.",
            vehicle.getName(),
            point
        );
        updatePositionWithoutOrder(point);
      }
      else {
        updatePositionWithOrder(point);
      }
    }
  }

  private void commandExecuted(MovementCommand executedCommand) {
    requireNonNull(executedCommand, "executedCommand");

    synchronized (commAdapter) {
      checkArgument(
          transformedToOriginalCommands.containsKey(executedCommand),
          "Unknown command reported as executed: %s",
          executedCommand
      );
      MovementCommand originalCommand = transformedToOriginalCommands.remove(executedCommand);

      LOG.debug(
          "{}: Communication adapter reports movement command as executed: {}",
          vehicle.getName(),
          originalCommand
      );

      commandProcessingTracker.commandExecuted(originalCommand);

      Point currentVehiclePosition = originalCommand.getStep().getDestinationPoint();
      Deque<Set<TCSResource<?>>> allocatedResources
          = commandProcessingTracker.getAllocatedResources();
      switch (configuration.vehicleResourceManagementType()) {
        case LENGTH_IGNORED:
          while (!allocatedResources.peek().contains(currentVehiclePosition)) {
            Set<TCSResource<?>> oldResources = allocatedResources.poll();
            LOG.debug("{}: Freeing resources: {}", vehicle.getName(), oldResources);
            scheduler.free(this, oldResources);
            commandProcessingTracker.allocationReleased(oldResources);
          }
          break;
        case LENGTH_RESPECTED:
          // Free resources allocated for executed commands, but keep as many as needed for the
          // vehicle's current length.
          int freeableResourceSetCount
              = ResourceMath.freeableResourceSetCount(
                  SplitResources.from(allocatedResources, Set.of(currentVehiclePosition))
                      .getResourcesPassed(),
                  commAdapter.getProcessModel().getBoundingBox().getLength()
              );
          for (int i = 0; i < freeableResourceSetCount; i++) {
            Set<TCSResource<?>> oldResources = allocatedResources.poll();
            LOG.debug("{}: Freeing resources: {}", vehicle.getName(), oldResources);
            scheduler.free(this, oldResources);
            commandProcessingTracker.allocationReleased(oldResources);
          }
          break;
        default:
          throw new IllegalArgumentException(
              "Unhandled resource deallocation method: "
                  + configuration.vehicleResourceManagementType().name()
          );
      }

      vehicleService.updateVehicleAllocatedResources(
          vehicle.getReference(),
          toListOfResourceSets(commandProcessingTracker.getAllocatedResources())
      );

      transportOrderService.updateTransportOrderCurrentRouteStepIndex(
          transportOrder.getReference(),
          originalCommand.getStep().getRouteIndex()
      );

      peripheralInteractor.startPostMovementInteractions(
          originalCommand,
          this::checkForPendingCommands,
          this::onPostMovementInteractionFailed
      );
    }
  }

  private void commandFailed(MovementCommand failedCommand) {
    LOG.debug(
        "{}: Communication adapter reports movement command as failed: {}",
        vehicle.getName(),
        failedCommand
    );
    dispatcherService.withdrawByVehicle(vehicle.getReference(), true);
  }

  private void checkForPendingCommands() {
    // Check if there are more commands to be processed for the current drive order.
    if (!commandProcessingTracker.hasCommandsToBeSent()) {
      LOG.debug("{}: No more commands in current drive order", vehicle.getName());
      // Check if there are still commands that have been sent to the communication adapter but
      // not yet executed. If not, the whole order has been executed completely - let the kernel
      // know about that so it can give us the next drive order.
      if (commandProcessingTracker.isDriveOrderFinished()) {
        LOG.debug("{}: Current drive order processed", vehicle.getName());
        currentDriveOrder = null;
        // Let the kernel/dispatcher know that the drive order has been processed completely (by
        // setting its state to AWAITING_ORDER).
        vehicleService.updateVehicleProcState(
            vehicle.getReference(),
            Vehicle.ProcState.AWAITING_ORDER
        );
      }
    }
    // There are more commands to be processed.
    // Check if we can send another command to the comm adapter.
    else if (canSendNextCommand()) {
      allocateForNextCommand();
    }
  }

  private void updateVehicleState(Vehicle.State newState) {
    requireNonNull(newState, "newState");
    vehicleService.updateVehicleState(vehicle.getReference(), newState);
  }

  private void updateVehicleBoundingBox(BoundingBox newBoundingBox) {
    requireNonNull(newBoundingBox, "newBoundingBox");
    vehicleService.updateVehicleBoundingBox(vehicle.getReference(), newBoundingBox);
  }

  /**
   * Checks if we can send another command to the communication adapter without
   * overflowing its capacity and with respect to the number of commands still
   * in our queue and allocation requests to the scheduler in progress.
   *
   * @return <code>true</code> if, and only if, we can send another command.
   */
  private boolean canSendNextCommand() {
    if (!commAdapter.canAcceptNextCommand()) {
      LOG.debug(
          "{}: Cannot send, comm adapter cannot accept any further commands.",
          vehicle.getName()
      );
      return false;
    }
    if (commandProcessingTracker.isWaitingForAllocation()) {
      LOG.debug(
          "{}: Cannot send, resource allocation is pending for: {}",
          vehicle.getName(),
          commandProcessingTracker.getAllocationPendingResources().orElse(null)
      );
      return false;
    }
    if (commandProcessingTracker.getNextAllocationCommand().isEmpty()) {
      LOG.debug("{}: Cannot send, no commands to be sent.", vehicle.getName());
      return false;
    }
    else {
      if (!commandProcessingTracker.getNextAllocationCommand().orElseThrow()
          .getStep().isExecutionAllowed()) {
        LOG.debug("{}: Cannot send, movement execution is not allowed", vehicle.getName());
        return false;
      }
    }
    if (peripheralInteractor.isWaitingForMovementInteractionsToFinish()) {
      LOG.debug(
          "{}: Cannot send, waiting for peripheral operations to be completed: {}",
          vehicle.getName(),
          peripheralInteractor.pendingRequiredInteractionsByDestination()
      );
      return false;
    }
    if (!sendingCommandsAllowed) {
      LOG.debug(
          "{}: Cannot send, unresolved report of an unexpected position.",
          vehicle.getName()
      );
      return false;
    }
    return true;
  }

  /**
   * Allocate the resources needed for executing the next command.
   */
  private void allocateForNextCommand() {
    checkState(
        !commandProcessingTracker.isWaitingForAllocation(),
        "%s: Already waiting for allocation: %s",
        vehicle.getName(),
        commandProcessingTracker.getAllocationPendingResources().orElse(null)
    );

    // Find out which resources are actually needed for the next command.
    Set<TCSResource<?>> nextAllocation
        = commandProcessingTracker.getNextAllocationResources().orElseThrow();
    LOG.debug("{}: Requesting allocation of resources: {}", vehicle.getName(), nextAllocation);
    scheduler.allocate(this, nextAllocation);
    commandProcessingTracker.allocationRequested(nextAllocation);
  }

  /**
   * Returns a set of resources needed for executing the given command.
   *
   * @param cmd The command for which to return the needed resources.
   * @return A set of resources needed for executing the given command.
   */
  @Nonnull
  private Set<TCSResource<?>> getNeededResources(MovementCommand cmd) {
    requireNonNull(cmd, "cmd");

    Set<TCSResource<?>> result = new HashSet<>();
    result.add(cmd.getStep().getDestinationPoint());
    if (cmd.getStep().getPath() != null) {
      result.add(cmd.getStep().getPath());
    }
    if (cmd.getOpLocation() != null) {
      result.add(cmd.getOpLocation());
    }

    return result;
  }

  /**
   * Frees all resources allocated for the vehicle.
   */
  private void freeAllResources() {
    scheduler.freeAll(this);
    commandProcessingTracker.allocationReset(Set.of());
    vehicleService.updateVehicleAllocatedResources(vehicle.getReference(), List.of());
  }

  /**
   * Returns the next command expected to be executed by the vehicle, skipping the current one.
   *
   * @return The next command expected to be executed by the vehicle.
   */
  private MovementCommand findNextCommand() {
    return commandProcessingTracker.getSentCommands().stream()
        .skip(1)
        .findFirst()
        .or(commandProcessingTracker::getSendingPendingCommand)
        .or(commandProcessingTracker::getAllocationPendingCommand)
        .or(commandProcessingTracker::getNextAllocationCommand)
        .orElse(null);
  }

  private void updatePositionWithoutOrder(Point point)
      throws IllegalArgumentException {
    if (point == null) {
      freeAllResources();
    }
    else {
      Set<TCSResource<?>> requiredResource = Set.of(point);

      // Before giving up the resources allocated, ensure that we will be able to allocate the
      // newly required resources.
      checkArgument(
          mayAllocateNow(requiredResource),
          "%s: Current position '%s' may not be allocated now - check other vehicles' allocations!",
          vehicle.getName(),
          point.getName()
      );
      freeAllResources();
      try {
        scheduler.allocateNow(this, requiredResource);
        commandProcessingTracker.allocationReset(requiredResource);
      }
      catch (ResourceAllocationException exc) {
        // May never happen. After a successful call to `mayAllocateNow` the allocation should
        // always succeed.
        LOG.error(
            "{}: Could not allocate now although permission previously granted: {}",
            vehicle.getName(),
            point.getName(),
            exc
        );
        throw new IllegalArgumentException(
            vehicle.getName()
                + ": Could not allocate now although permission previously granted: "
                + point.getName()
        );
      }
      vehicleService.updateVehicleAllocatedResources(
          vehicle.getReference(),
          toListOfResourceSets(commandProcessingTracker.getAllocatedResources())
      );
    }

    updatePosition(toReference(point), null);
  }

  private void updatePositionWithOrder(Point point) {
    if (commandProcessingTracker.getSentCommands().isEmpty()) {
      if (commandProcessingTracker.getAllocationPendingCommand().isPresent()) {
        LOG.warn(
            "{}: Reported new position {} but we are waiting for resource allocation for: {}",
            vehicle.getName(),
            point,
            commandProcessingTracker.getAllocationPendingCommand().orElse(null)
        );
      }
      else if (commandProcessingTracker.getSendingPendingCommand().isPresent()) {
        LOG.warn(
            "{}: Reported new position {} but we are waiting for command to be sent: {}",
            vehicle.getName(),
            point,
            commandProcessingTracker.getSendingPendingCommand().orElse(null)
        );
      }
      else {
        LOG.warn(
            "{}: Reported new position {} but we didn't send any commands of the drive order.",
            vehicle.getName(),
            point
        );
      }

      onUnexpectedPositionReported(point);

      // We have a drive order, but can't remember sending a command to the vehicle. Just set the
      // position without touching the resources, as that might cause even more damage when we
      // actually send commands to the vehicle.
      updatePosition(toReference(point), null);
    }
    else {
      if (point == null) {
        LOG.info("{}: Resetting position for vehicle", vehicle.getName());
      }
      else {
        // Check if the reported position belongs to any of the commands we sent.
        List<Point> expectedPoints = commandProcessingTracker.getSentCommands().stream()
            .map(cmd -> cmd.getStep().getDestinationPoint())
            .collect(Collectors.toList());

        if (!expectedPoints.contains(point)) {
          LOG.warn(
              "{}: Reported position: {}, expected one of: {}",
              vehicle.getName(),
              point.getName(),
              expectedPoints
          );
          onUnexpectedPositionReported(point);
        }
      }

      updatePosition(toReference(point), extractNextPosition(findNextCommand()));
    }
  }

  private void updatePosition(
      TCSObjectReference<Point> posRef,
      TCSObjectReference<Point> nextPosRef
  ) {
    vehicleService.updateVehiclePosition(vehicle.getReference(), posRef);
    vehicleService.updateVehicleNextPosition(vehicle.getReference(), nextPosRef);
  }

  private void onIntegrationLevelChange(
      Vehicle prevVehicleState,
      Vehicle currVehicleState
  ) {
    Vehicle.IntegrationLevel prevIntegrationLevel = prevVehicleState.getIntegrationLevel();
    Vehicle.IntegrationLevel currIntegrationLevel = currVehicleState.getIntegrationLevel();

    synchronized (commAdapter) {
      if (currIntegrationLevel == Vehicle.IntegrationLevel.TO_BE_IGNORED) {
        // Reset the vehicle's position to free all allocated resources
        resetVehiclePosition();
        updateVehiclePose(new Pose(null, Double.NaN));
      }
      else if (currIntegrationLevel == Vehicle.IntegrationLevel.TO_BE_NOTICED) {
        // Reset the vehicle's position to free all allocated resources
        resetVehiclePosition();

        // Update the vehicle's position in its model, but don't allocate any resources
        VehicleProcessModel processModel = commAdapter.getProcessModel();
        if (processModel.getPosition() != null) {
          Point point = vehicleService.fetchObject(Point.class, processModel.getPosition());
          vehicleService.updateVehiclePosition(vehicle.getReference(), point.getReference());
        }
        updateVehiclePose(processModel.getPose());
      }
      else if ((currIntegrationLevel == Vehicle.IntegrationLevel.TO_BE_RESPECTED
          || currIntegrationLevel == Vehicle.IntegrationLevel.TO_BE_UTILIZED)
          && (prevIntegrationLevel == Vehicle.IntegrationLevel.TO_BE_IGNORED
              || prevIntegrationLevel == Vehicle.IntegrationLevel.TO_BE_NOTICED)) {
                // Allocate the vehicle's current position and implicitly update its model's
                // position
                allocateVehiclePosition();
              }
    }
  }

  private void resetVehiclePosition() {
    synchronized (commAdapter) {
      checkState(currentDriveOrder == null, "%s: Vehicle has a drive order", vehicle.getName());
      checkState(
          !commandProcessingTracker.isWaitingForAllocation(),
          "%s: Vehicle is waiting for resource allocation (%s)",
          vehicle.getName(),
          commandProcessingTracker.getAllocationPendingResources()
      );

      setVehiclePosition(null);
    }
  }

  private void allocateVehiclePosition() {
    VehicleProcessModel processModel = commAdapter.getProcessModel();
    // We don't want to set the vehicle position right away, since the vehicle's currently
    // allocated resources would be freed in the first place. We need to check, if the vehicle's
    // current position is already part of it's allocated resources.
    if (!alreadyAllocated(processModel.getPosition())) {
      // Set vehicle's position to allocate the resources
      setVehiclePosition(processModel.getPosition());
      updateVehiclePose(processModel.getPose());
    }
  }

  private boolean alreadyAllocated(String position) {
    return commandProcessingTracker.getAllocatedResources().stream()
        .filter(resources -> resources != null)
        .flatMap(resources -> resources.stream())
        .anyMatch(resource -> resource.getName().equals(position));
  }

  private static TCSObjectReference<Point> toReference(Point point) {
    return point == null ? null : point.getReference();
  }

  private static TCSObjectReference<Point> extractNextPosition(MovementCommand nextCommand) {
    if (nextCommand == null) {
      return null;
    }
    else {
      return nextCommand.getStep().getDestinationPoint().getReference();
    }
  }

  private static List<Set<TCSResourceReference<?>>> toListOfResourceSets(
      Queue<Set<TCSResource<?>>> resources
  ) {
    List<Set<TCSResourceReference<?>>> result = new ArrayList<>(resources.size());

    for (Set<TCSResource<?>> resourceSet : resources) {
      result.add(
          resourceSet.stream()
              .map(resource -> resource.getReference())
              .collect(Collectors.toSet())
      );
    }

    return result;
  }

  private static List<Set<TCSResourceReference<?>>> toListOfResourceSets(
      List<Set<TCSResource<?>>> resources
  ) {
    List<Set<TCSResourceReference<?>>> result = new ArrayList<>(resources.size());

    for (Set<TCSResource<?>> resourceSet : resources) {
      result.add(
          resourceSet.stream()
              .map(TCSResource::getReference)
              .collect(Collectors.toSet())
      );
    }

    return result;
  }

  private List<Set<TCSResource<?>>> currentClaim(TransportOrder order) {
    List<Set<TCSResource<?>>> claim = new ArrayList<>();
    claim.addAll(commandProcessingTracker.getClaimedResources());
    claim.addAll(requiredClaimForFutureDriveOrders(transportOrder));
    return claim;
  }

  private List<Set<TCSResource<?>>> requiredClaimForFutureDriveOrders(TransportOrder order) {
    return order.getFutureDriveOrders().stream()
        .map(driveOrder -> movementCommandMapper.toMovementCommands(driveOrder, order))
        .flatMap(Collection::stream)
        .map(this::getNeededResources)
        .toList();
  }

  private boolean isForcedRerouting(DriveOrder newOrder) {
    // If it's a forced rerouting, the step after the one the vehicle executed last should be marked
    // accordingly.
    Step nextPendingStep
        = newOrder.getRoute().getSteps().get(getLastCommandExecutedRouteIndex() + 1);
    if (nextPendingStep.getReroutingType() == ReroutingType.FORCED) {
      return true;
    }

    return false;
  }

  private int getLastCommandExecutedRouteIndex() {
    if (commandProcessingTracker.getLastCommandExecuted().isEmpty()) {
      return TransportOrder.ROUTE_STEP_INDEX_DEFAULT;
    }

    if (!Objects.equals(
        currentDriveOrder,
        commandProcessingTracker.getLastCommandExecuted().orElseThrow().getDriveOrder()
    )) {
      return TransportOrder.ROUTE_STEP_INDEX_DEFAULT;
    }

    return commandProcessingTracker.getLastCommandExecuted().orElseThrow()
        .getStep().getRouteIndex();
  }

  private void onUnexpectedPositionReported(
      @Nullable
      Point point
  ) {
    sendingCommandsAllowed = false;

    notificationService.publishUserNotification(
        new UserNotification(
            vehicle.getName(),
            String.format(
                "Vehicle reported an unexpected position ('%s') while processing a transport order."
                    + " Its vehicle driver won't receive further movement commands until the"
                    + " vehicle is forcefully rerouted.",
                point == null ? "null" : point.getName()
            ),
            UserNotification.Level.IMPORTANT
        )
    );
  }
}
