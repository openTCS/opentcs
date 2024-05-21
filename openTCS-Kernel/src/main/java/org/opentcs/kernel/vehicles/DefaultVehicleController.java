/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import com.google.inject.assistedinject.Assisted;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
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
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleController;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.ProcessModelEvent;
import org.opentcs.kernel.KernelApplicationConfiguration;
import static org.opentcs.kernel.vehicles.MovementComparisons.equalsInMovement;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkState;
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
    implements VehicleController,
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
   * A list of commands that still need to be sent to the communication adapter.
   */
  private final Deque<MovementCommand> futureCommands = new ArrayDeque<>();
  /**
   * A command for which a resource allocation is pending and which has not yet been sent to the
   * adapter.
   */
  private volatile MovementCommand pendingCommand;
  /**
   * A set of resources for which allocation is pending.
   */
  private volatile Set<TCSResource<?>> pendingResources;
  /**
   * A command for which the execution of peripheral operations is pending.
   */
  private volatile MovementCommand interactionsPendingCommand;
  /**
   * A list of commands that have been sent to the communication adapter.
   */
  private final Deque<MovementCommand> commandsSent = new ArrayDeque<>();
  /**
   * The last command that has been executed.
   */
  private MovementCommand lastCommandExecuted;
  /**
   * The route index of the last command that has been executed.
   */
  private int lastCommandExecutedRouteIndex = TransportOrder.ROUTE_STEP_INDEX_DEFAULT;
  /**
   * The resources this controller has claimed for future allocation.
   * <p>
   * For every movement command, a single element is added to this queue containing all resources
   * associated with the respective movement command (i.e., a path (if any), a point and a
   * location (if any)).
   * </p>
   */
  private final Deque<Set<TCSResource<?>>> claimedResources = new ArrayDeque<>();
  /**
   * The resources this controller has allocated for each command.
   * <p>
   * For every movement command, two elements are added to this queue - one element containing only
   * the path (if any) associated with the respective movement command, and another element
   * containing the point and the location (if any) associated with the respective movement command.
   * </p>
   */
  private final Deque<Set<TCSResource<?>>> allocatedResources = new ArrayDeque<>();
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
   */
  @Inject
  public DefaultVehicleController(@Assisted @Nonnull Vehicle vehicle,
                                  @Assisted @Nonnull VehicleCommAdapter adapter,
                                  @Nonnull InternalVehicleService vehicleService,
                                  @Nonnull InternalTransportOrderService transportOrderService,
                                  @Nonnull NotificationService notificationService,
                                  @Nonnull DispatcherService dispatcherService,
                                  @Nonnull Scheduler scheduler,
                                  @Nonnull @ApplicationEventBus EventBus eventBus,
                                  @Nonnull VehicleControllerComponentsFactory componentsFactory,
                                  @Nonnull MovementCommandMapper movementCommandMapper,
                                  @Nonnull KernelApplicationConfiguration configuration) {
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

    vehicleService.updateVehicleRechargeOperation(vehicle.getReference(),
                                                  commAdapter.getRechargeOperation());
    commAdapter.getProcessModel().addPropertyChangeListener(this);

    // Initialize standard attributes once.
    setVehiclePosition(commAdapter.getProcessModel().getPosition());
    vehicleService.updateVehiclePrecisePosition(
        vehicle.getReference(),
        commAdapter.getProcessModel().getPrecisePosition()
    );
    vehicleService.updateVehicleOrientationAngle(
        vehicle.getReference(),
        commAdapter.getProcessModel().getOrientationAngle()
    );
    vehicleService.updateVehicleEnergyLevel(vehicle.getReference(),
                                            commAdapter.getProcessModel().getEnergyLevel());
    vehicleService.updateVehicleLoadHandlingDevices(
        vehicle.getReference(),
        commAdapter.getProcessModel().getLoadHandlingDevices()
    );
    updateVehicleState(commAdapter.getProcessModel().getState());
    updateVehicleLength(commAdapter.getProcessModel().getLength());

    claimedResources.clear();
    allocatedResources.clear();

    peripheralInteractor.initialize();

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
    vehicleService.updateVehiclePrecisePosition(vehicle.getReference(), null);
    // Free all allocated resources.
    freeAllResources();

    updateVehicleState(Vehicle.State.UNKNOWN);

    eventBus.unsubscribe(this);

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

    if (!(Objects.equals(objectEvent.getCurrentOrPreviousObjectState().getName(),
                         vehicle.getName()))) {
      return;
    }

    Vehicle prevVehicleState = (Vehicle) objectEvent.getPreviousObjectState();
    Vehicle currVehicleState = (Vehicle) objectEvent.getCurrentObjectState();

    if (prevVehicleState.getIntegrationLevel() != currVehicleState.getIntegrationLevel()) {
      onIntegrationLevelChange(prevVehicleState, currVehicleState);
    }
  }

  @Override
  public void setTransportOrder(@Nonnull TransportOrder newOrder)
      throws IllegalArgumentException {
    requireNonNull(newOrder, "newOrder");
    requireNonNull(newOrder.getCurrentDriveOrder(), "newOrder.getCurrentDriveOrder()");

    if (transportOrder == null
        || !Objects.equals(newOrder.getName(), transportOrder.getName())
        || newOrder.getCurrentDriveOrderIndex() != transportOrder.getCurrentDriveOrderIndex()) {
      // We received either a new transport order or the same transport order for its next drive
      // order.
      transportOrder = newOrder;
      setDriveOrder(transportOrder.getCurrentDriveOrder(), transportOrder.getProperties());
    }
    else {
      // We received an update for a drive order we're already processing.
      transportOrder = newOrder;

      checkArgument(driveOrdersContinual(currentDriveOrder, transportOrder.getCurrentDriveOrder()),
                    "The new and old drive orders are not considered continual.");

      if (isForcedRerouting(transportOrder.getCurrentDriveOrder())) {
        Vehicle currVehicle = vehicleService.fetchObject(Vehicle.class, vehicle.getReference());
        if (currVehicle.getCurrentPosition() == null) {
          throw new IllegalArgumentException("The vehicle's current position is unknown.");
        }
        Point currPosition = vehicleService.fetchObject(Point.class,
                                                        currVehicle.getCurrentPosition());
        // Before interacting with the scheduler in any way, ensure that the we will be able to
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
          allocatedResources.add(Set.of(currPosition));
          vehicleService.updateVehicleAllocatedResources(vehicle.getReference(),
                                                         toListOfResourceSets(allocatedResources));
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

  @Override
  @Deprecated
  public void setDriveOrder(@Nonnull DriveOrder newOrder,
                            @Nonnull Map<String, String> orderProperties)
      throws IllegalArgumentException {
    synchronized (commAdapter) {
      requireNonNull(newOrder, "newOrder");
      requireNonNull(orderProperties, "orderProperties");
      requireNonNull(newOrder.getRoute(), "newOrder.getRoute()");
      // Assert that there isn't still is a drive order that hasn't been finished/removed, yet.
      checkArgument(currentDriveOrder == null,
                    "%s still has an order! Current order: %s, new order: %s",
                    vehicle.getName(),
                    currentDriveOrder,
                    newOrder);

      LOG.debug("{}: Setting drive order: {}", vehicle.getName(), newOrder);

      currentDriveOrder = newOrder;
      lastCommandExecutedRouteIndex = TransportOrder.ROUTE_STEP_INDEX_DEFAULT;

      vehicleService.updateVehicleRouteProgressIndex(vehicle.getReference(),
                                                     Vehicle.ROUTE_INDEX_DEFAULT);

      // Set the claim for (the remainder of) this transport order.
      List<Set<TCSResource<?>>> claim = remainingRequiredClaim(transportOrder);
      scheduler.claim(this, claim);
      claimedResources.clear();
      claimedResources.addAll(claim);

      vehicleService.updateVehicleClaimedResources(vehicle.getReference(),
                                                   toListOfResourceSets(claimedResources));

      futureCommands.addAll(movementCommandMapper.toMovementCommands(newOrder, transportOrder));

      if (canSendNextCommand()) {
        allocateForNextCommand();
      }

      // Set the vehicle's next expected position.
      Point nextPoint = newOrder.getRoute().getSteps().get(0).getDestinationPoint();
      vehicleService.updateVehicleNextPosition(vehicle.getReference(),
                                               nextPoint.getReference());
    }
  }

  @Override
  @Deprecated
  public void updateDriveOrder(@Nonnull DriveOrder newOrder,
                               @Nonnull Map<String, String> orderProperties)
      throws IllegalArgumentException {
    synchronized (commAdapter) {
      requireNonNull(newOrder, "newOrder");
      checkArgument(currentDriveOrder != null, "There's no drive order to be updated");

      LOG.debug("{}: Updating drive order: {}", vehicle.getName(), newOrder);
      // Update the current drive order and future commands
      currentDriveOrder = newOrder;
      // There is a new drive order, so discard all the future/scheduled commands of the old one.
      discardFutureCommands();

      // Update the claim.
      List<Set<TCSResource<?>>> claim = remainingRequiredClaim(transportOrder);
      scheduler.claim(this, claim);
      claimedResources.clear();
      claimedResources.addAll(claim);

      vehicleService.updateVehicleClaimedResources(vehicle.getReference(),
                                                   toListOfResourceSets(claimedResources));

      futureCommands.addAll(movementCommandMapper.toMovementCommands(newOrder, transportOrder));
      // The current drive order got updated but our queue of future commands now contains commands
      // that have already been processed, so discard these.
      discardProcessedFutureCommands();

      // Get an up-tp-date copy of the vehicle
      Vehicle updatedVehicle = vehicleService.fetchObject(Vehicle.class, vehicle.getReference());
      // Trigger the vehicle's route to be re-drawn
      vehicleService.updateVehicleRouteProgressIndex(vehicle.getReference(),
                                                     updatedVehicle.getRouteProgressIndex());

      // The vehicle may now process previously restricted steps.
      if (canSendNextCommand()) {
        allocateForNextCommand();
      }
    }
  }

  private boolean driveOrdersContinual(DriveOrder oldOrder, DriveOrder newOrder) {
    LOG.debug("{}: Checking drive order continuity for {} (old) and {} (new).",
              vehicle.getName(), oldOrder, newOrder);

    if (lastCommandExecutedRouteIndex == TransportOrder.ROUTE_STEP_INDEX_DEFAULT) {
      LOG.debug("{}: Drive orders continuous: No route progress, yet.", vehicle.getName());
      return true;
    }

    List<Step> oldSteps = oldOrder.getRoute().getSteps();
    List<Step> newSteps = newOrder.getRoute().getSteps();

    // Compare already processed steps (up to and including the last executed command) for equality.
    List<Step> oldProcessedSteps = oldSteps.subList(0, lastCommandExecutedRouteIndex + 1);
    List<Step> newProcessedSteps = newSteps.subList(0, lastCommandExecutedRouteIndex + 1);
    LOG.debug("{}: Comparing already processed steps for equality: {} (old) and {} (new)",
              vehicle.getName(),
              oldProcessedSteps,
              newProcessedSteps);
    if (!equalsInMovement(oldProcessedSteps, newProcessedSteps)) {
      LOG.debug("{}: Drive orders not continuous: Mismatching old and new processed steps.",
                vehicle.getName());
      return false;
    }

    if (isForcedRerouting(newOrder)) {
      LOG.debug("{}: Drive orders continuous: New order with forced rerouting.", vehicle.getName());
      return true;
    }

    // Compare pending steps (after the last executed command) for equality.
    int futureOrCurrentPositionIndex = getFutureOrCurrentPositionIndex();
    List<Step> oldPendingSteps = oldSteps.subList(lastCommandExecutedRouteIndex + 1,
                                                  futureOrCurrentPositionIndex + 1);
    List<Step> newPendingSteps = newSteps.subList(lastCommandExecutedRouteIndex + 1,
                                                  futureOrCurrentPositionIndex + 1);
    LOG.debug("{}: Comparing pending steps for equality: {} (old) and {} (new)",
              vehicle.getName(),
              oldPendingSteps,
              newPendingSteps);
    if (!equalsInMovement(oldPendingSteps, newPendingSteps)) {
      LOG.debug("{}: Drive orders not continuous: Mismatching old and new pending steps.",
                vehicle.getName());
      return false;
    }

    LOG.debug("{}: Drive orders continuous.", vehicle.getName());
    return true;
  }

  private int getFutureOrCurrentPositionIndex() {
    if (commandsSent.isEmpty() && getInteractionsPendingCommand().isEmpty()) {
      LOG.debug("{}: No commands expected to be executed. Last executed command route index: {}",
                vehicle.getName(),
                lastCommandExecutedRouteIndex);
      return lastCommandExecutedRouteIndex;
    }

    if (getInteractionsPendingCommand().isPresent()) {
      LOG.debug("{}: Command with pending peripheral operations present. Route index: {}",
                vehicle.getName(),
                getInteractionsPendingCommand().get().getStep().getRouteIndex());
      return getInteractionsPendingCommand().get().getStep().getRouteIndex();
    }

    MovementCommand lastCommandSent = commandsSent.getLast();
    LOG.debug("{}: Using the last command sent to the communication adapter. Route index: {}",
              vehicle.getName(),
              lastCommandSent.getStep().getRouteIndex());
    return lastCommandSent.getStep().getRouteIndex();
  }

  private void discardFutureCommands() {
    futureCommands.clear();
    withdrawPendingResourceAllocations();
  }

  private void discardProcessedFutureCommands() {
    MovementCommand lastCommandProcessed = lastCommandProcessed();
    if (lastCommandProcessed == null) {
      // There are no commands to be discarded.
      return;
    }

    LOG.debug("{}: Discarding future commands up to '{}' (inclusively): {}",
              vehicle.getName(),
              lastCommandProcessed,
              futureCommands);
    // Discard commands up to lastCommandSent...
    while (!lastCommandProcessed.equalsInMovement(futureCommands.peek())) {
      futureCommands.poll();
    }
    // ...and also discard lastCommandSent itself.
    futureCommands.poll();
  }

  @Override
  public void abortTransportOrder(boolean immediate) {
    synchronized (commAdapter) {
      if (immediate) {
        clearDriveOrder();

        withdrawPendingResourceAllocations();

        claimedResources.clear();
        scheduler.claim(this, List.of());
      }
      else {
        abortDriveOrder();

        List<Set<TCSResource<?>>> newClaim
            = pendingResources == null
                ? List.of()
                : List.of(pendingResources);

        withdrawPendingResourceAllocations();

        claimedResources.clear();
        claimedResources.addAll(newClaim);
        scheduler.claim(this, newClaim);

        checkForPendingCommands();
      }

      vehicleService.updateVehicleClaimedResources(vehicle.getReference(),
                                                   toListOfResourceSets(claimedResources));
      vehicleService.updateVehicleAllocatedResources(vehicle.getReference(),
                                                     toListOfResourceSets(allocatedResources));
    }
  }

  @Override
  @Deprecated
  public void clearDriveOrder() {
    synchronized (commAdapter) {
      currentDriveOrder = null;

      // XXX May be removed after this method has been made private / integrated into
      // abortTransportOrder().
      resetPendingResourceAllocations();

      vehicleService.updateVehicleRouteProgressIndex(vehicle.getReference(),
                                                     Vehicle.ROUTE_INDEX_DEFAULT);

      clearCommandQueue();
    }
  }

  @Override
  @Deprecated
  public void abortDriveOrder() {
    synchronized (commAdapter) {
      if (currentDriveOrder == null) {
        LOG.debug("{}: No drive order to be aborted", vehicle.getName());
        return;
      }
      futureCommands.clear();
    }
  }

  @Override
  @Deprecated
  public void clearCommandQueue() {
    synchronized (commAdapter) {
      commAdapter.clearCommandQueue();
      commandsSent.clear();
      futureCommands.clear();
      // XXX May be removed after this method has been made private / integrated into
      // abortTransportOrder().
      resetPendingResourceAllocations();
      interactionsPendingCommand = null;
      peripheralInteractor.clear();

      // Free all resource sets that were reserved for future commands, except the current one and
      // the ones the vehicle is still covering with its length.
      SplitResources splitResources;
      if (lastCommandExecuted != null) {
        splitResources = SplitResources.from(
            allocatedResources,
            Set.of(lastCommandExecuted.getStep().getDestinationPoint())
        );
      }
      else {
        // Happens if the very first transport order was withdrawn before the vehicle has made
        // any movement. Ensure the resources for the current command/position are retained.
        splitResources = SplitResources.from(
            allocatedResources,
            Set.of(allocatedResources.stream()
                .flatMap(resSet -> resSet.stream())
                .filter(res -> res instanceof Point)
                .findFirst()
                .get())
        );
      }
      for (Set<TCSResource<?>> resSet : splitResources.getResourcesAhead()) {
        scheduler.free(this, resSet);
      }
      allocatedResources.clear();
      allocatedResources.addAll(splitResources.getResourcesPassed());
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
  @Deprecated
  @Nonnull
  public ExplainedBoolean canProcess(@Nonnull List<String> operations) {
    requireNonNull(operations, "operations");

    synchronized (commAdapter) {
      return commAdapter.canProcess(operations);
    }
  }

  @Override
  public void onVehiclePaused(boolean paused) {
    synchronized (commAdapter) {
      commAdapter.onVehiclePaused(paused);
    }
  }

  @Override
  public void sendCommAdapterMessage(@Nullable Object message) {
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
    return new ArrayDeque<>(commandsSent);
  }

  @Override
  public Optional<MovementCommand> getInteractionsPendingCommand() {
    return Optional.ofNullable(interactionsPendingCommand);
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
  public boolean allocationSuccessful(@Nonnull Set<TCSResource<?>> resources) {
    requireNonNull(resources, "resources");

    synchronized (commAdapter) {
      // Check if we've actually been waiting for these resources now. If not,
      // let the scheduler know that we don't want them.
      if (!Objects.equals(resources, pendingResources)) {
        LOG.warn("{}: Allocated resources ({}) != pending resources ({}), refusing them",
                 vehicle.getName(),
                 resources,
                 pendingResources);
        return false;
      }

      LOG.debug("{}: Accepting allocated resources: {}", vehicle.getName(), resources);
      allocatedResources.add(extractPath(resources));
      allocatedResources.add(extractPointAndLocation(resources));
      claimedResources.poll();

      MovementCommand command = pendingCommand;
      resetPendingResourceAllocations();

      vehicleService.updateVehicleClaimedResources(vehicle.getReference(),
                                                   toListOfResourceSets(claimedResources));
      vehicleService.updateVehicleAllocatedResources(vehicle.getReference(),
                                                     toListOfResourceSets(allocatedResources));

      interactionsPendingCommand = command;

      peripheralInteractor.prepareInteractions(transportOrder.getReference(), command);
      peripheralInteractor.startPreMovementInteractions(command,
                                                        () -> sendCommand(command),
                                                        this::onPreMovementInteractionFailed);
    }
    // Let the scheduler know we've accepted the resources given.
    return true;
  }

  @Override
  public void allocationFailed(@Nonnull Set<TCSResource<?>> resources) {
    requireNonNull(resources, "resources");
    throw new IllegalStateException("Failed to allocate: " + resources);
  }

  @Override
  public String toString() {
    return "DefaultVehicleController{" + "vehicleName=" + vehicle.getName() + '}';
  }

  private void sendCommand(MovementCommand command)
      throws IllegalStateException {
    LOG.debug("{}: Enqueuing movement command with comm adapter: {}", vehicle.getName(), command);
    // Send the command to the communication adapter.
    checkState(commAdapter.enqueueCommand(command),
               "Comm adapter did not accept command");
    commandsSent.add(command);
    interactionsPendingCommand = null;

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
    Set<TCSResource<?>> res = allocatedResources.pollLast();
    scheduler.free(this, res);
    vehicleService.updateVehicleAllocatedResources(vehicle.getReference(),
                                                   toListOfResourceSets(allocatedResources));

    // Ensure there is no command with pending interactions that would otherwise prevent the vehicle
    // state from being set properly in checkForPendingCommands().
    interactionsPendingCommand = null;

    dispatcherService.withdrawByVehicle(vehicle.getReference(), false);
  }

  private void onPostMovementInteractionFailed() {
    // Implementation remark: This method is called only for interactions where a peripheral job
    // with the completion required flag set has failed.
    LOG.warn("{}: Post-movement interaction failed.", vehicle.getName());

    // Ensure there is no command with pending interactions that would otherwise prevent the vehicle
    // state from being set properly in checkForPendingCommands().
    interactionsPendingCommand = null;

    dispatcherService.withdrawByVehicle(vehicle.getReference(), false);
  }

  @SuppressWarnings("unchecked")
  private void handleProcessModelEvent(PropertyChangeEvent evt) {
    eventBus.onEvent(new ProcessModelEvent(evt.getPropertyName(),
                                           commAdapter.createTransferableProcessModel()));

    if (Objects.equals(evt.getPropertyName(), VehicleProcessModel.Attribute.POSITION.name())) {
      updateVehiclePosition((String) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.PRECISE_POSITION.name())) {
      updateVehiclePrecisePosition((Triple) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.ORIENTATION_ANGLE.name())) {
      vehicleService.updateVehicleOrientationAngle(vehicle.getReference(),
                                                   (Double) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.ENERGY_LEVEL.name())) {
      vehicleService.updateVehicleEnergyLevel(vehicle.getReference(), (Integer) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.LOAD_HANDLING_DEVICES.name())) {
      vehicleService.updateVehicleLoadHandlingDevices(vehicle.getReference(),
                                                      (List<LoadHandlingDevice>) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(), VehicleProcessModel.Attribute.STATE.name())) {
      updateVehicleState((Vehicle.State) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(), VehicleProcessModel.Attribute.LENGTH.name())) {
      updateVehicleLength((int) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.COMMAND_EXECUTED.name())) {
      commandExecuted((MovementCommand) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.COMMAND_FAILED.name())) {
      commandFailed((MovementCommand) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.USER_NOTIFICATION.name())) {
      notificationService.publishUserNotification((UserNotification) evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.COMM_ADAPTER_EVENT.name())) {
      eventBus.onEvent(evt.getNewValue());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.VEHICLE_PROPERTY.name())) {
      VehicleProcessModel.VehiclePropertyUpdate propUpdate
          = (VehicleProcessModel.VehiclePropertyUpdate) evt.getNewValue();
      vehicleService.updateObjectProperty(vehicle.getReference(),
                                          propUpdate.getKey(),
                                          propUpdate.getValue());
    }
    else if (Objects.equals(evt.getPropertyName(),
                            VehicleProcessModel.Attribute.TRANSPORT_ORDER_PROPERTY.name())) {
      VehicleProcessModel.TransportOrderPropertyUpdate propUpdate
          = (VehicleProcessModel.TransportOrderPropertyUpdate) evt.getNewValue();
      if (currentDriveOrder != null) {
        vehicleService.updateObjectProperty(currentDriveOrder.getTransportOrder(),
                                            propUpdate.getKey(),
                                            propUpdate.getValue());
      }
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.INTEGRATION_LEVEL_CHANGE_REQUESTED.name())) {
      vehicleService.updateVehicleIntegrationLevel(vehicle.getReference(),
                                                   (Vehicle.IntegrationLevel) evt.getNewValue());
    }
    else if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.TRANSPORT_ORDER_WITHDRAWAL_REQUESTED.name())) {
      dispatcherService.withdrawByVehicle(vehicle.getReference(), (Boolean) evt.getNewValue());
    }
  }

  private void withdrawPendingResourceAllocations() {
    scheduler.clearPendingAllocations(this);
    resetPendingResourceAllocations();
  }

  private void resetPendingResourceAllocations() {
    pendingResources = null;
    pendingCommand = null;
  }

  /**
   * Indicates whether we're currently waiting for resources to be allocated by the scheduler.
   *
   * @return <code>true</code> if we're currently waiting for resources to be allocated.
   */
  private boolean isWaitingForAllocation() {
    return pendingResources != null;
  }

  private void updateVehiclePrecisePosition(Triple precisePosition)
      throws ObjectUnknownException {
    // Get an up-to-date copy of the vehicle
    Vehicle currVehicle = vehicleService.fetchObject(Vehicle.class, vehicle.getReference());

    if (currVehicle.getIntegrationLevel() != Vehicle.IntegrationLevel.TO_BE_IGNORED) {
      vehicleService.updateVehiclePrecisePosition(vehicle.getReference(), precisePosition);
    }
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
        LOG.debug("{}: Reported new position {} and we do not have a drive order.",
                  vehicle.getName(),
                  point);
        updatePositionWithoutOrder(point);
      }
      else if (commandsSent.isEmpty()) {
        // We have a drive order, but can't remember sending a command to the
        // vehicle. Just set the position without touching the resources, as
        // that might cause even more damage when we actually send commands
        // to the vehicle.
        LOG.debug("{}: Reported new position {} and we didn't send any commands of drive order.",
                  vehicle.getName(),
                  point);
        updatePosition(toReference(point), null);
      }
      else {
        updatePositionWithOrder(position, point);
      }
    }
  }

  private void commandExecuted(MovementCommand executedCommand) {
    requireNonNull(executedCommand, "executedCommand");

    LOG.debug("{}: Communication adapter reports movement command as executed: {}",
              vehicle.getName(),
              executedCommand);

    synchronized (commAdapter) {
      // Check if the executed command is the one we expect at this point.
      MovementCommand expectedCommand = commandsSent.peek();
      checkArgument(Objects.equals(expectedCommand, executedCommand),
                    "%s: Communication adapter executed unexpected command: %s != %s",
                    vehicle.getName(),
                    executedCommand,
                    expectedCommand);

      // Remove the command from the queue, since it has been processed successfully.
      lastCommandExecuted = commandsSent.remove();
      lastCommandExecutedRouteIndex = lastCommandExecuted.getStep().getRouteIndex();

      Point currentVehiclePosition = lastCommandExecuted.getStep().getDestinationPoint();
      switch (configuration.vehicleResourceManagementType()) {
        case LENGTH_IGNORED:
          while (!allocatedResources.peek().contains(currentVehiclePosition)) {
            Set<TCSResource<?>> oldResources = allocatedResources.poll();
            LOG.debug("{}: Freeing resources: {}", vehicle.getName(), oldResources);
            scheduler.free(this, oldResources);
          }
          break;
        case LENGTH_RESPECTED:
          // Free resources allocated for executed commands, but keep as many as needed for the
          // vehicle's current length.
          int freeableResourceSetCount
              = ResourceMath.freeableResourceSetCount(
                  SplitResources.from(allocatedResources, Set.of(currentVehiclePosition))
                      .getResourcesPassed(),
                  commAdapter.getProcessModel().getLength()
              );
          for (int i = 0; i < freeableResourceSetCount; i++) {
            Set<TCSResource<?>> oldResources = allocatedResources.poll();
            LOG.debug("{}: Freeing resources: {}", vehicle.getName(), oldResources);
            scheduler.free(this, oldResources);
          }
          break;
        default:
          throw new IllegalArgumentException(
              "Unhandled resource deallocation method: "
              + configuration.vehicleResourceManagementType().name()
          );
      }

      vehicleService.updateVehicleAllocatedResources(vehicle.getReference(),
                                                     toListOfResourceSets(allocatedResources));

      transportOrderService.updateTransportOrderCurrentRouteStepIndex(
          transportOrder.getReference(),
          executedCommand.getStep().getRouteIndex()
      );

      peripheralInteractor.startPostMovementInteractions(executedCommand,
                                                         this::checkForPendingCommands,
                                                         this::onPostMovementInteractionFailed);
    }
  }

  private void commandFailed(MovementCommand failedCommand) {
    LOG.debug("{}: Communication adapter reports movement command as failed: {}",
              vehicle.getName(),
              failedCommand);
    dispatcherService.withdrawByVehicle(vehicle.getReference(), true);
  }

  @SuppressWarnings("deprecation")
  private void checkForPendingCommands() {
    // Check if there are more commands to be processed for the current drive order.
    if (interactionsPendingCommand == null
        && !isWaitingForAllocation()
        && futureCommands.isEmpty()) {
      LOG.debug("{}: No more commands in current drive order", vehicle.getName());
      // Check if there are still commands that have been sent to the communication adapter but
      // not yet executed. If not, the whole order has been executed completely - let the kernel
      // know about that so it can give us the next drive order.
      if (commandsSent.isEmpty()) {
        LOG.debug("{}: Current drive order processed", vehicle.getName());
        currentDriveOrder = null;
        // Let the kernel/dispatcher know that the drive order has been processed completely (by
        // setting its state to AWAITING_ORDER).
        vehicleService.updateVehicleRouteProgressIndex(vehicle.getReference(),
                                                       Vehicle.ROUTE_INDEX_DEFAULT);
        vehicleService.updateVehicleProcState(vehicle.getReference(),
                                              Vehicle.ProcState.AWAITING_ORDER);
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

  private void updateVehicleLength(int newLength) {
    vehicleService.updateVehicleLength(vehicle.getReference(), newLength);
  }

  /**
   * Checks if we can send another command to the communication adapter without
   * overflowing its capacity and with respect to the number of commands still
   * in our queue and allocation requests to the scheduler in progress.
   *
   * @return <code>true</code> if, and only if, we can send another command.
   */
  private boolean canSendNextCommand() {
    if (futureCommands.isEmpty()) {
      LOG.debug("{}: Cannot send, no commands to be sent.", vehicle.getName());
      return false;
    }
    if (!commAdapter.canAcceptNextCommand()) {
      LOG.debug("{}: Cannot send, comm adapter cannot accept any further commands.",
                vehicle.getName());
      return false;
    }
    if (!futureCommands.peek().getStep().isExecutionAllowed()) {
      LOG.debug("{}: Cannot send, movement execution is not allowed", vehicle.getName());
      return false;
    }
    if (isWaitingForAllocation()) {
      LOG.debug("{}: Cannot send, resource allocation is pending for: {}",
                vehicle.getName(),
                pendingCommand);
      return false;
    }
    if (peripheralInteractor.isWaitingForMovementInteractionsToFinish()) {
      LOG.debug("{}: Cannot send, waiting for peripheral operations to be completed: {}",
                vehicle.getName(),
                peripheralInteractor.pendingRequiredInteractionsByDestination());
      return false;
    }
    return true;
  }

  /**
   * Allocate the resources needed for executing the next command.
   */
  private void allocateForNextCommand() {
    checkState(!isWaitingForAllocation(),
               "%s: Already waiting for allocation: %s",
               vehicle.getName(),
               pendingResources);

    // Find out which resources are actually needed for the next command.
    pendingCommand = futureCommands.poll();
    pendingResources = getNeededResources(pendingCommand);
    LOG.debug("{}: Allocating resources: {}", vehicle.getName(), pendingResources);
    scheduler.allocate(this, pendingResources);
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
    allocatedResources.clear();
    vehicleService.updateVehicleAllocatedResources(vehicle.getReference(), List.of());
  }

  /**
   * Returns the next command expected to be executed by the vehicle, skipping the current one.
   *
   * @return The next command expected to be executed by the vehicle.
   */
  private MovementCommand findNextCommand() {
    MovementCommand nextCommand = commandsSent.stream()
        .skip(1)
        .filter(cmd -> cmd != null)
        .findFirst()
        .orElse(null);

    if (nextCommand == null) {
      nextCommand = pendingCommand;
    }

    if (nextCommand == null) {
      nextCommand = futureCommands.stream()
          .filter(cmd -> cmd != null)
          .findFirst()
          .orElse(null);
    }

    return nextCommand;
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
      checkArgument(mayAllocateNow(requiredResource),
                    "%s: Current position may not be allocated now: %s",
                    vehicle.getName(),
                    point.getName());
      freeAllResources();
      try {
        scheduler.allocateNow(this, requiredResource);
        allocatedResources.add(requiredResource);
      }
      catch (ResourceAllocationException exc) {
        // May never happen. After a successful call to `mayAllocateNow` the allocation should
        // always succeed.
        LOG.error("{}: Could not allocate now although permission previously granted: {}",
                  vehicle.getName(),
                  point.getName(),
                  exc);
        throw new IllegalArgumentException(
            vehicle.getName()
            + ": Could not allocate now although permission previously granted: "
            + point.getName()
        );
      }
      vehicleService.updateVehicleAllocatedResources(vehicle.getReference(),
                                                     toListOfResourceSets(allocatedResources));
    }

    updatePosition(toReference(point), null);
  }

  @SuppressWarnings("deprecation")
  private void updatePositionWithOrder(String position, Point point) {
    // If a drive order is being processed, check if the reported position
    // is the one we expect.
    MovementCommand moveCommand = commandsSent.getFirst();

    Point dstPoint = moveCommand.getStep().getDestinationPoint();
    if (dstPoint.getName().equals(position)) {
      // Update the vehicle's progress index.
      vehicleService.updateVehicleRouteProgressIndex(vehicle.getReference(),
                                                     moveCommand.getStep().getRouteIndex());
    }
    else if (position == null) {
      LOG.info("{}: Resetting position for vehicle", vehicle.getName());
    }
    else {
      LOG.warn("{}: Reported position: {}, expected: {}",
               vehicle.getName(),
               position,
               dstPoint.getName());
    }

    updatePosition(toReference(point), extractNextPosition(findNextCommand()));
  }

  private void updatePosition(TCSObjectReference<Point> posRef,
                              TCSObjectReference<Point> nextPosRef) {
    vehicleService.updateVehiclePosition(vehicle.getReference(), posRef);
    vehicleService.updateVehicleNextPosition(vehicle.getReference(), nextPosRef);
  }

  private void onIntegrationLevelChange(Vehicle prevVehicleState,
                                        Vehicle currVehicleState) {
    Vehicle.IntegrationLevel prevIntegrationLevel = prevVehicleState.getIntegrationLevel();
    Vehicle.IntegrationLevel currIntegrationLevel = currVehicleState.getIntegrationLevel();

    synchronized (commAdapter) {
      if (currIntegrationLevel == Vehicle.IntegrationLevel.TO_BE_IGNORED) {
        // Reset the vehicle's position to free all allocated resources
        resetVehiclePosition();
        vehicleService.updateVehiclePrecisePosition(vehicle.getReference(), null);
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
        vehicleService.updateVehiclePrecisePosition(vehicle.getReference(),
                                                    processModel.getPrecisePosition());
      }
      else if ((currIntegrationLevel == Vehicle.IntegrationLevel.TO_BE_RESPECTED
                || currIntegrationLevel == Vehicle.IntegrationLevel.TO_BE_UTILIZED)
          && (prevIntegrationLevel == Vehicle.IntegrationLevel.TO_BE_IGNORED
              || prevIntegrationLevel == Vehicle.IntegrationLevel.TO_BE_NOTICED)) {
        // Allocate the vehicle's current position and implicitly update its model's position
        allocateVehiclePosition();
      }
    }
  }

  private void resetVehiclePosition() {
    synchronized (commAdapter) {
      checkState(currentDriveOrder == null, "%s: Vehicle has a drive order", vehicle.getName());
      checkState(!isWaitingForAllocation(),
                 "%s: Vehicle is waiting for resource allocation (%s)",
                 vehicle.getName(),
                 pendingResources);

      setVehiclePosition(null);
    }
  }

  private void allocateVehiclePosition() {
    VehicleProcessModel processModel = commAdapter.getProcessModel();
    // We don't want to set the vehicle position right away, since the vehicle's currently
    // allocated resources would be freed in the first place. We need to check, if the vehicle's
    // current position is already part of it's allocated resoruces.
    if (!alreadyAllocated(processModel.getPosition())) {
      // Set vehicle's position to allocate the resources
      setVehiclePosition(processModel.getPosition());
      vehicleService.updateVehiclePrecisePosition(vehicle.getReference(),
                                                  processModel.getPrecisePosition());
    }
  }

  private boolean alreadyAllocated(String position) {
    return allocatedResources.stream()
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

  private List<Set<TCSResource<?>>> remainingRequiredClaim(@Nonnull TransportOrder order) {
    List<MovementCommand> futureMovementCommands = order.getAllDriveOrders().stream()
        .skip(order.getCurrentDriveOrderIndex())
        .map(driveOrder -> movementCommandMapper.toMovementCommands(driveOrder, order))
        .flatMap(movementCommandQueue -> movementCommandQueue.stream())
        .collect(Collectors.toList());

    // Commands that we have already processed should not be included in the remaining claim.
    MovementCommand lastCommandProcessed = lastCommandProcessed();
    if (lastCommandProcessed != null
        && Objects.equals(order.getCurrentDriveOrder(), lastCommandProcessed.getDriveOrder())) {
      futureMovementCommands = futureMovementCommands.stream()
          .dropWhile(command -> !command.equalsInMovement(lastCommandProcessed))
          .skip(1)
          .collect(Collectors.toCollection(ArrayList::new));
    }

    return futureMovementCommands.stream()
        .filter(command -> command.getStep().getDestinationPoint().isHaltingPosition())
        .map(command -> getNeededResources(command))
        .collect(Collectors.toList());
  }

  private boolean isForcedRerouting(DriveOrder newOrder) {
    if (lastCommandExecutedRouteIndex == TransportOrder.ROUTE_STEP_INDEX_DEFAULT) {
      LOG.debug("{}: No route progress, yet. Not considering rerouting as forced.",
                vehicle.getName());
      return false;
    }

    // If it's a forced rerouting, the step after the one the vehicle executed last should be marked
    // accordingly.
    Step nextPendingStep = newOrder.getRoute().getSteps().get(lastCommandExecutedRouteIndex + 1);
    if (nextPendingStep.getReroutingType() == ReroutingType.FORCED) {
      return true;
    }

    return false;
  }

  /**
   * Returns the last movement command that has been processed by this vehicle controller in a way
   * that is relevant in the context of rerouting.
   * <p>
   * Generally, a movement command is processed in multiple stages. It is:
   * <ol>
   * <li>Added to the <code>futureCommands</code> queue (when a transport order for the vehicle
   * associated with this controller is set or updated).</li>
   * <li>Removed from the <code>futureCommands</code> queue and set as the
   * <code>pendingCommand</code> (when allocating the resources needed for executing the next
   * command).</li>
   * <li>Unset as the <code>pendingCommand</code> and set as the
   * <code>interactionPendingCommand</code> (when the resources have been allocated successfully).
   * </li>
   * <li>Unset as the <code>interactionPendingCommand</code> and added to the
   * <code>commandsSent</code> queue (when interactions related to the command have been reported
   * as finished and the command has been handed over to the vehicle driver).</li>
   * <li>Removed from the <code>commandsSent</code> queue and set as the
   * <code>lastCommandExecuted</code> (when the driver reports that the command has been executed)
   * </li>
   * </ol>
   * </p>
   * <p>
   * The earliest stage a movement command can be in that is relevant in the context of rerouting is
   * when it is set as the <code>interactionPendingCommand</code>. At this stage, the resources for
   * the command have already been (successfully) allocated, and it will either be handed over to
   * the vehicle driver if the associated peripheral interactions are successfully finished, or
   * otherwise discarded. Rerouting should therefore take place from this command (or rather the
   * respective step) at the earliest.
   * </p>
   * <p>
   * <code>pendingCommand</code> (as well as everything prior to that) is not relevant here, as the
   * allocation for corresponding resources is still pending at this stage, and all pending
   * allocations are cleared upon rerouting.
   * </p>
   *
   * @return A movement command or {@code null} if there is no movement command that has been
   * processed by this vehicle controller in a way that is relevant in the context of rerouting.
   */
  @Nullable
  private MovementCommand lastCommandProcessed() {
    return Optional.ofNullable(interactionsPendingCommand)
        .or(() -> Optional.ofNullable(commandsSent.peekLast()))
        .orElse(lastCommandExecuted);
  }

  private Set<TCSResource<?>> extractPointAndLocation(Set<TCSResource<?>> resources) {
    return resources.stream()
        .filter(resource -> resource instanceof Point || resource instanceof Location)
        .collect(Collectors.toSet());
  }

  private Set<TCSResource<?>> extractPath(Set<TCSResource<?>> resources) {
    return resources.stream()
        .filter(resource -> resource instanceof Path)
        .collect(Collectors.toSet());
  }
}
