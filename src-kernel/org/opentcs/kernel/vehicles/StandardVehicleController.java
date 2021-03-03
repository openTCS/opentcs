/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import org.opentcs.access.LocalKernel;
import org.opentcs.algorithms.ResourceAllocationException;
import org.opentcs.algorithms.ResourceUser;
import org.opentcs.algorithms.Scheduler;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.drivers.AdapterCommand;
import org.opentcs.drivers.CommunicationAdapter;
import org.opentcs.drivers.LoadHandlingDevice;
import org.opentcs.drivers.MovementCommand;
import org.opentcs.drivers.Processability;
import org.opentcs.drivers.VehicleController;
import org.opentcs.drivers.VehicleManager;
import org.opentcs.util.configuration.ConfigurationStore;

/**
 * Realizes a bidirectional connection between openTCS and a communication
 * adapter controlling a vehicle.
 * <hr>
 * <h4>Configuration entries</h4>
 * <dl>
 * <dt><b>watchdogSleepTime:</b></dt>
 * <dd>An integer defining the time (in ms) between periodic alive checks of the
 * communication adapter (default: 5000).</dd>
 * <dt><b>ignoreUnknownPositions:</b></dt>
 * <dd>Whether to ignore unknown positions or reset the vehicle's position upon
 * encountering them.</dd>
 * </dl>
 * <hr>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class StandardVehicleController
    implements VehicleController, VehicleManager, ResourceUser {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(StandardVehicleController.class.getName());
  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(StandardVehicleController.class.getName());
  /**
   * A flag indicating whether to ignore unknown positions completely or to
   * reset the vehicle's position when encountering them.
   */
  private static final boolean ignoreUnknownPositions;
  /**
   * The local kernel.
   */
  private final LocalKernel localKernel;
  /**
   * The scheduler maintaining the resources.
   */
  private final Scheduler scheduler;
  /**
   * The name of the vehicle controlled by this controller.
   */
  private final String vehicleName;
  /**
   * This controller's <em>enabled</em> flag.
   */
  private volatile boolean enabled;
  /**
   * The vehicle controlled by this controller/the communication adapter.
   */
  private volatile Vehicle controlledVehicle;
  /**
   * The communication adapter controlling the physical vehicle.
   */
  private final CommunicationAdapter commAdapter;
  /**
   * A list of commands that still need to be sent to the communication adapter.
   */
  private final Queue<MovementCommand> futureCommands = new LinkedList<>();
  /**
   * A command for which a resource allocation is pending and which has not yet
   * been sent to the adapter.
   */
  private volatile MovementCommand pendingCommand;
  /**
   * A set of resources for which allocation is pending.
   */
  private volatile Set<TCSResource> pendingResources;
  /**
   * A list of commands that have been sent to the communication adapter.
   */
  private final Queue<MovementCommand> commandsSent = new LinkedList<>();
  /**
   * The resources this controller has allocated for each command.
   */
  private final Queue<Set<TCSResource>> allocatedResources = new LinkedList<>();
  /**
   * The drive order that the vehicle currently has to process.
   */
  private volatile DriveOrder currentDriveOrder;
  /**
   * The communication adapter's last known state.
   */
  private volatile CommunicationAdapter.State commAdapterState
      = CommunicationAdapter.State.UNKNOWN;
  /**
   * The capacity of the communication adapter's command queue.
   */
  private volatile int adapterCommandQueueCapacity = 1;
  /**
   * Flag indicating that we're currently waiting for resources to be allocated
   * by the scheduler, ensuring that we do not allocate more than one set of
   * resources at a time (which can cause deadlocks).
   */
  private volatile boolean waitingForAllocation;

  static {
    ignoreUnknownPositions = configStore.getBoolean("ignoreUnknownPositions",
                                                    true);
  }

  /**
   * Creates a new StandardVehicleController associated with the given vehicle.
   *
   * @param vehicle The vehicle this vehicle controller will be associated with.
   * @param adapter The communication adapter of the associated vehicle.
   * @param kernel The kernel instance maintaining the model.
   */
  public StandardVehicleController(Vehicle vehicle,
                                   CommunicationAdapter adapter,
                                   LocalKernel kernel) {
    log.finer("method entry");

    controlledVehicle = Objects.requireNonNull(vehicle, "vehicle is null");
    commAdapter = Objects.requireNonNull(adapter, "adapter is null");
    localKernel = Objects.requireNonNull(kernel, "kernel is null");

    vehicleName = vehicle.getName();
    scheduler = kernel.getScheduler();
    // Add a first entry into allocatedResources to shift freeing of resources
    // in commandExecuted() by one - we need to free the resources allocated for
    // the command before the one executed there.
    allocatedResources.add(null);
  }

  // Implementation of interface VehicleController starts here.
  @Override
  public void setDriveOrder(DriveOrder newOrder,
                            Map<String, String> orderProperties) {
    log.finer("method entry");
    synchronized (futureCommands) {
      if (newOrder == null) {
        log.fine("newOrder is null, clearing drive order");
        clearDriveOrder();
        return;
      }
      // If there still is a drive order that hasn't been finished/removed, yet,
      // throw an exception.
      if (currentDriveOrder != null) {
        throw new IllegalStateException(vehicleName + " still has an order!\n"
            + " current order: " + currentDriveOrder + "\n"
            + " new order: " + newOrder);
      }

      currentDriveOrder = newOrder;
      setVehicleRouteProgressIndex(Vehicle.ROUTE_INDEX_DEFAULT);
      createFutureCommands(newOrder, orderProperties);

      // The communication adapter MUST have capacity for a new command - its
      // queue should be empty.
      assert canSendNextCommand() : "Cannot send next command for some reason";
      allocateForNextCommand();
      // Set the vehicle's next expected position.
      Point nextPoint = newOrder.getRoute().getSteps().get(0).getDestinationPoint();
      localKernel.setVehicleNextPosition(controlledVehicle.getReference(),
                                         nextPoint.getReference());
    }
  }

  @Override
  public void abortDriveOrder() {
    log.finer("method entry");
    synchronized (futureCommands) {
      if (currentDriveOrder == null) {
        log.warning(vehicleName + ": No drive order to be aborted");
        return;
      }
      futureCommands.clear();
    }
  }

  @Override
  public void clearCommandQueue() {
    log.finer("method entry");
    synchronized (futureCommands) {
      commAdapter.clearCommandQueue();
      commandsSent.clear();
      futureCommands.clear();
      pendingCommand = null;
      // Free all resources that were reserved for future commands...
      final Set<TCSResource> neededResources = allocatedResources.poll();
      final Iterator<Set<TCSResource>> resIter = allocatedResources.iterator();
      while (resIter.hasNext()) {
        final Set<TCSResource> resSet = resIter.next();
        if (resSet != null) {
          scheduler.free(this, resSet);
        }
        resIter.remove();
      }
      // Put the resources for the current command/position back in...
      allocatedResources.add(neededResources);
    }
  }

  @Override
  public Processability canProcess(List<String> operations) {
    Objects.requireNonNull(operations, "operations is null");
    return commAdapter.canProcess(operations);
  }

  // Methods declared in interface ResourceUser start here.
  @Override
  public String getId() {
    log.finer("method entry");
    return controlledVehicle.getName();
  }

  @Override
  public boolean allocationSuccessful(final Set<TCSResource> resources) {
    log.finer("method entry");
    Objects.requireNonNull(resources, "resources is null");

    // Look up the command the resources were required for.
    final MovementCommand command;
    synchronized (futureCommands) {
      // Check if we've actually been waiting for these resources now. If not,
      // let the scheduler know that we don't want them.
      if (pendingResources == null || !pendingResources.equals(resources)) {
        return false;
      }

      command = pendingCommand;
      // If there was no command in the queue, it must have been withdrawn in
      // the meantime - let the scheduler know that we don't need the resources
      // any more.
      if (command == null) {
        waitingForAllocation = false;
        pendingResources = null;
        return false;
      }
      pendingCommand = null;
      pendingResources = null;
    }
    allocatedResources.add(resources);
    // Send the command to the communication adapter.
    boolean commandAdded = commAdapter.addCommand(command);
    assert commandAdded : "Comm adapter did not accept for its queue";
    commandAdded = commandsSent.offer(command);
    assert commandAdded : "Sent commands queue did not accept command";
    // Check if the communication adapter has capacity for another command.
    synchronized (futureCommands) {
      waitingForAllocation = false;
      if (canSendNextCommand()) {
        allocateForNextCommand();
      }
    }
    // Let the scheduler know we've accepted the resources given.
    return true;
  }

  @Override
  public void allocationFailed(final Set<TCSResource> resources) {
    log.finer("method entry");
    Objects.requireNonNull(resources, "resources is null");
    throw new IllegalStateException("Failed to allocate: " + resources);
  }

  // Implementation of interface VehicleManager starts here.
  @Override
  public void setAdapterCommandQueueCapacity(int capacity) {
    log.finer("method entry");
    if (capacity < 1) {
      throw new IllegalArgumentException("capacity is less than 1");
    }
    adapterCommandQueueCapacity = capacity;
  }

  @Override
  public void setVehiclePosition(final String position) {
    log.finer("method entry");
    // Place the vehicle on the given position, regardless of what the kernel
    // might think. The vehicle is physically there, even if it shouldn't be.
    // The same is true for null values - if the vehicle says it's not on any
    // known position, it has to be treated as a fact.
    try {
      final Point point;
      final TCSObjectReference<Point> pointRef;
      if (position == null) {
        point = null;
        pointRef = null;
      }
      else {
        point = localKernel.getTCSObject(Point.class, position);
        // If the new position is not in the model, either ignore it or reset
        // the vehicle's position. (Some vehicles/drivers send intermediate
        // positions that cannot be order destinations and thus do not exist in
        // the model.
        if (point == null) {
          log.warning(vehicleName + " at unknown position: " + position);
          if (ignoreUnknownPositions) {
            return;
          }
          else {
            pointRef = null;
          }
        }
        else {
          pointRef = point.getReference();
        }
      }
      synchronized (futureCommands) {
        // If the current drive order is null, just set the vehicle's position.
        if (currentDriveOrder == null) {
          log.fine(vehicleName + ": Reported new position " + point
              + " and we do not have a drive order.");
          // Allocate only the resources required for occupying the new position.
          freeAllResources();
          // If the vehicle is at an unknown position, it's impossible to say
          // which resources it needs, so don't allocate any in that case.
          if (point != null) {
            Set<TCSResource> requiredResource = new HashSet<>();
            requiredResource.add(point);
            scheduler.allocateNow(this, requiredResource);
            allocatedResources.add(requiredResource);
          }
          localKernel.setVehiclePosition(controlledVehicle.getReference(),
                                         pointRef);
          localKernel.setVehicleNextPosition(controlledVehicle.getReference(),
                                             null);
          updateVehicleInstance();
        }
        else if (commandsSent.isEmpty()) {
          log.fine(vehicleName + ": Reported new position " + point
              + " and we have a drive order but didn't send any commands.");
          // We have a drive order, but can't remember sending a command to the
          // vehicle. Just set the position without touching the resources, as
          // that might cause even more damage when we actually send commands
          // to the vehicle.
          localKernel.setVehiclePosition(controlledVehicle.getReference(),
                                         pointRef);
          localKernel.setVehicleNextPosition(controlledVehicle.getReference(),
                                             null);
          updateVehicleInstance();
        }
        else {
          final Iterator<MovementCommand> commandIter = commandsSent.iterator();
          // If a drive order is being processed, check if the reported position
          // is the one we expect.
          final MovementCommand moveCommand = commandIter.next();
          assert moveCommand != null : "moveCommand is null";

          // Predict the vehicle's next position.
          final TCSObjectReference<Point> nextPosition;
          MovementCommand nextCommand = null;
          while (nextCommand == null && commandIter.hasNext()) {
            nextCommand = commandIter.next();
          }
          if (nextCommand == null) {
            nextCommand = pendingCommand;
          }
          Iterator<MovementCommand> futureCmdsIter = futureCommands.iterator();
          while (nextCommand == null && futureCmdsIter.hasNext()) {
            nextCommand = futureCmdsIter.next();
          }
          if (nextCommand == null) {
            nextPosition = null;
          }
          else {
            nextPosition
                = nextCommand.getStep().getDestinationPoint().getReference();
          }

          final Point dstPoint = moveCommand.getStep().getDestinationPoint();
          if (dstPoint.getName().equals(position)) {
            // Update the vehicle's progress index.
            setVehicleRouteProgressIndex(moveCommand.getStep().getRouteIndex());
            // Let the scheduler know where we are now.
            scheduler.setRouteIndex(this, moveCommand.getStep().getRouteIndex());
          }
          else {
            log.warning("Reported position: " + position
                + "; expected: " + dstPoint.getName());
          }
          localKernel.setVehiclePosition(controlledVehicle.getReference(),
                                         pointRef);
          localKernel.setVehicleNextPosition(controlledVehicle.getReference(),
                                             nextPosition);
          updateVehicleInstance();
        }
      }
    }
    catch (ResourceAllocationException exc) {
      throw new IllegalStateException("Cannot allocate required "
          + "resources immediately", exc);
    }
  }

  @Override
  public void setVehiclePrecisePosition(Triple position) {
    // Change the state of controlled vehicle via the kernel, not directly,
    // since the kernel might have to emit events for it and it needs to be
    // synchronized.
    localKernel.setVehiclePrecisePosition(controlledVehicle.getReference(),
                                          position);
    // Get us a fresh copy of the modified vehicle object.
    updateVehicleInstance();
  }

  @Override
  public void setVehicleOrientationAngle(double angle) {
    // Change the state of controlled vehicle via the kernel, not directly,
    // since the kernel might have to emit events for it and it needs to be
    // synchronized.
    localKernel.setVehicleOrientationAngle(controlledVehicle.getReference(),
                                           angle);
    // Get us a fresh copy of the modified vehicle object.
    updateVehicleInstance();
  }

  @Override
  public void setVehicleEnergyLevel(int energyLevel) {
    log.finer("method entry");
    if (energyLevel < 0 || energyLevel > 100) {
      throw new IllegalArgumentException("energyLevel not in [0..100]: "
          + energyLevel);
    }
    // Change the state of controlled vehicle via the kernel, not directly,
    // since the kernel might have to emit events for it and it needs to be
    // synchronized.
    localKernel.setVehicleEnergyLevel(controlledVehicle.getReference(),
                                      energyLevel);
    // Get us a fresh copy of the modified vehicle object.
    updateVehicleInstance();
  }

  @Override
  public void setVehicleRechargeOperation(String rechargeOperation) {
    log.finer("method entry");
    Objects.requireNonNull(rechargeOperation, "rechargeOperation is null");
    // Change the state of controlled vehicle via the kernel, not directly,
    // since the kernel might have to emit events for it and it needs to be
    // synchronized.
    localKernel.setVehicleRechargeOperation(controlledVehicle.getReference(),
                                            rechargeOperation);
    // Get us a fresh copy of the modified vehicle object.
    updateVehicleInstance();
  }

  @Override
  public void setVehicleLoadHandlingDevices(List<LoadHandlingDevice> devices) {
    Objects.requireNonNull(devices, "devices is null");
    // Change the state of controlled vehicle via the kernel, not directly,
    // since the kernel might have to emit events for it and it needs to be
    // synchronized.
    localKernel.setVehicleLoadHandlingDevices(controlledVehicle.getReference(),
                                              devices);
    // Get us a fresh copy of the modified vehicle object.
    updateVehicleInstance();
  }

  @Override
  public void setVehicleMaxVelocity(int velocity) {
    log.finer("method entry");
    // Change the state of controlled vehicle via the kernel, not directly,
    // since the kernel might have to emit events for it and it needs to be
    // synchronized.
    localKernel.setVehicleMaxVelocity(controlledVehicle.getReference(),
                                      velocity);
    // Get us a fresh copy of the modified vehicle object.
    updateVehicleInstance();
  }

  @Override
  public void setVehicleMaxReverseVelocity(int velocity) {
    log.finer("method entry");
    // Change the state of controlled vehicle via the kernel, not directly,
    // since the kernel might have to emit events for it and it needs to be
    // synchronized.
    localKernel.setVehicleMaxReverseVelocity(controlledVehicle.getReference(),
                                             velocity);
    // Get us a fresh copy of the modified vehicle object.
    updateVehicleInstance();
  }

  @Override
  public void setVehicleProperty(String key, String value) {
    log.finer("method entry");
    Objects.requireNonNull(key, "key is null");
    localKernel.setTCSObjectProperty(controlledVehicle.getReference(),
                                     key,
                                     value);
  }

  @Override
  public void setVehicleState(Vehicle.State newState) {
    log.finer("method entry");
    Objects.requireNonNull(newState, "newState is null");
    updateVehicleState(newState);
  }

  @Override
  public void setAdapterState(CommunicationAdapter.State newState) {
    log.finer("method entry");
    Objects.requireNonNull(newState, "newState is null");
    updateCommAdapterState(newState);
  }

  @Override
  public void setOrderProperty(String key, String value) {
    log.finer("method entry");
    Objects.requireNonNull(key, "key is null");
    // If we currently do not have an order, do nada.
    if (currentDriveOrder == null) {
      return;
    }
    localKernel.setTCSObjectProperty(currentDriveOrder.getTransportOrder(),
                                     key,
                                     value);
  }

  @Override
  public void commandExecuted(final AdapterCommand executedCommand) {
    log.finer("method entry");
    Objects.requireNonNull(executedCommand, "executedCommand is null");
    synchronized (futureCommands) {
      // Check if the executed command is the one we expect at this point.
      final MovementCommand expectedCommand = commandsSent.peek();
      if (executedCommand.equals(expectedCommand)) {
        // Remove the command from the queue, since it has been processed
        // successfully.
        commandsSent.remove();
        // Free resources allocated for the command before the one now executed.
        final Set<TCSResource> oldResources = allocatedResources.poll();
        if (oldResources != null) {
          log.fine(vehicleName + ": Freeing resources: " + oldResources);
          scheduler.free(this, oldResources);
        }
        else {
          log.fine("Nothing to free...");
        }
        // Check if there are more commands to be processed for the current drive
        // order.
        if (pendingCommand == null && futureCommands.isEmpty()) {
          log.fine(vehicleName + ": No more commands in current drive order");
          // Check if there are still commands that have been sent to the
          // communication adapter but not yet executed. If not, the whole order
          // has been executed completely - let the kernel know about that so it
          // can send us the next drive order.
          if (commandsSent.isEmpty() && !waitingForAllocation) {
            log.fine(vehicleName + ": Current drive order processed");
            // Let the kernel/dispatcher know that the drive order has been
            // processed completely (by setting its state to AWAITING_ORDER).
            currentDriveOrder = null;
            setVehicleRouteProgressIndex(Vehicle.ROUTE_INDEX_DEFAULT);
            updateVehicleProcState(Vehicle.ProcState.AWAITING_ORDER);
          }
        }
        else {
          // There are more commands to be processed.
          // Check if the communication adapter has capacity for another command
          // and we're not waiting for an allocation, yet.
          if (canSendNextCommand()) {
            allocateForNextCommand();
          }
        }
      }
      else {
        log.warning("Communication adapter executed unexpected command: "
            + executedCommand);
        // XXX The communication adapter executed an unexpected command. Do
        // something!
      }
    }
  }

  // Implementation of interface KernelExtension starts here.
  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void enable() {
    log.finer("method entry");
    if (!enabled) {
      enabled = true;
      if (commAdapter == null) {
        log.severe("commAdapter is null, cannot enable");
        throw new IllegalStateException("commAdapter is null, cannot enable");
      }
    }
    else {
      log.warning("Already enabled, doing nothing.");
    }
  }

  @Override
  public void disable() {
    log.finer("method entry");
    if (enabled) {
      enabled = false;
      // Free all allocated resources.
      freeAllResources();
    }
    else {
      log.warning("Not enabled, doing nothing.");
    }
  }

  @Override
  public void sendCommAdapterMessage(Object message) {
    log.finer("method entry");
    commAdapter.processMessage(message);
  }

  // Methods not declared in any interface start here.
  private void clearDriveOrder() {
    currentDriveOrder = null;

    // Clear pending resource allocations. If they still arrive, we will
    // refuse them in allocationSuccessful().
    waitingForAllocation = false;
    pendingResources = null;

    setVehicleRouteProgressIndex(Vehicle.ROUTE_INDEX_DEFAULT);
  }

  /**
   * Genereates the MovementCommands for the new order.
   *
   * @param newOrder The new order.
   * @param orderProperties The properties of the transport order the new order
   * belongs to.
   */
  private void createFutureCommands(DriveOrder newOrder,
                                    Map<String, String> orderProperties) {
    // Start processing the new order, i.e. fill futureCommands with
    // corresponding command objects.
    final String op = newOrder.getDestination().getOperation();
    final Route orderRoute = newOrder.getRoute();
    final Point finalDestination = orderRoute.getFinalDestinationPoint();
    final Map<String, String> destProperties
        = newOrder.getDestination().getProperties();
    final Iterator<Step> stepIter = orderRoute.getSteps().iterator();
    while (stepIter.hasNext()) {
      Step curStep = stepIter.next();
      // Ignore report positions on the route.
      if (curStep.getDestinationPoint().isHaltingPosition()) {
        String operation;
        Location opLocation;
        if (!stepIter.hasNext()) {
          operation = op;
          opLocation = localKernel.getTCSObject(Location.class,
                                                newOrder.getDestination().getLocation());
          // FIXME We used to put dummy references for orders to points in here,
          // but there are no dummies for locations. Is that a problem?
        }
        else {
          operation = MovementCommand.NO_OPERATION;
          opLocation = null;
        }
        futureCommands.add(new MovementCommand(curStep,
                                               operation,
                                               opLocation,
                                               finalDestination,
                                               op,
                                               mergeProperties(orderProperties,
                                                               destProperties)));
      }
    }
  }

  /**
   * Sets the state of the vehicle's communication adapter to the given one.
   *
   * @param newState The communication adapter's new state.
   */
  private void updateCommAdapterState(CommunicationAdapter.State newState) {
    assert newState != null : "newState is null";
    commAdapterState = newState;
    localKernel.setVehicleAdapterState(controlledVehicle.getReference(),
                                       newState);
    // If the adapter's state is unknown, the vehicle's state is unknown, too.
    if (newState.equals(CommunicationAdapter.State.UNKNOWN)) {
      updateVehicleState(Vehicle.State.UNKNOWN);
    }
    updateVehicleInstance();
  }

  /**
   * Sets the vehicle's state to the given one.
   *
   * @param newState The vehicle's new state.
   */
  private void updateVehicleState(Vehicle.State newState) {
    assert newState != null : "newState is null";
    // Change the state of controlled vehicle via the kernel, not directly,
    // since the kernel might have to emit events for it and it needs to be
    // synchronized.
    // If the communication adapter knows the state of the vehicle and is not
    // marked as connected with us, mark it as connected now. - It knows the
    // vehicle's state, so it must be connected to it.
    if (!Vehicle.State.UNKNOWN.equals(newState)
        && !CommunicationAdapter.State.CONNECTED.equals(commAdapterState)) {
      updateCommAdapterState(CommunicationAdapter.State.CONNECTED);
    }
    localKernel.setVehicleState(controlledVehicle.getReference(), newState);
    // Get us a fresh copy of the modified vehicle object.
    updateVehicleInstance();
  }

  /**
   * Sets the vehicle's processing state to the given one.
   *
   * @param newState The vehicle's new processing state.
   */
  private void updateVehicleProcState(Vehicle.ProcState newState) {
    assert newState != null : "newState is null";
    // Change the state of controlled vehicle via the kernel, not directly,
    // since the kernel might have to emit events for it and it needs to be
    // synchronized.
    localKernel.setVehicleProcState(controlledVehicle.getReference(),
                                    newState);
    // Get us a fresh copy of the modified vehicle object.
    updateVehicleInstance();
  }

  /**
   * Updates the reference to the controlled vehicle.
   */
  private void updateVehicleInstance() {
    controlledVehicle
        = localKernel.getTCSObject(Vehicle.class,
                                   controlledVehicle.getReference());
    if (controlledVehicle == null) {
      throw new IllegalStateException("kernel lost a vehicle");
    }
  }

  /**
   * Checks if we can send another command to the communication adapter without
   * overflowing its capacity and with respect to the number of commands still
   * in our queue and allocation requests to the scheduler in progress.
   *
   * @return <code>true</code> if, and only if, we can send another command.
   */
  private boolean canSendNextCommand() {
    final int sendableCommands = Math.min(
        adapterCommandQueueCapacity - commandsSent.size(),
        futureCommands.size());
    if (sendableCommands <= 0) {
      log.fine("Cannot send, number of sendable commands: " + sendableCommands);
      return false;
    }
    if (waitingForAllocation) {
      log.fine("Cannot send, waiting for allocation");
      return false;
    }
    return true;
  }

  /**
   * Allocate the resources needed for executing the next command.
   */
  private void allocateForNextCommand() {
    assert pendingCommand == null : "pendingCommand != null";
    // Find out which resources are actually needed for the next command.
    final MovementCommand moveCmd = futureCommands.poll();
    pendingResources = getNeededResources(moveCmd);
    log.fine("Allocating resources: " + pendingResources);
    scheduler.allocate(this, pendingResources);
    // Remember that we're waiting for an allocation. This ensures that we only
    // wait for one allocation at a time, and that we get the resources from the
    // scheduler in the right order.
    waitingForAllocation = true;
    pendingCommand = moveCmd;
  }

  /**
   * Returns a set of resources needed for executing the given command.
   *
   * @param cmd The command for which to return the needed resources.
   * @return A set of resources needed for executing the given command.
   */
  private Set<TCSResource> getNeededResources(MovementCommand cmd) {
    assert cmd != null : "cmd is null";
    final Set<TCSResource> result = new HashSet<>();
    final Point destinationPoint = cmd.getStep().getDestinationPoint();
    result.add(destinationPoint);
    final Path path = cmd.getStep().getPath();
    if (path != null) {
      result.add(path);
    }
    return result;
  }

  /**
   * Frees all resources allocated for the vehicle.
   */
  private void freeAllResources() {
    for (Set<TCSResource> curResources : allocatedResources) {
      // The queue may contain null elements - don't call free() for them.
      if (curResources != null) {
        scheduler.free(this, curResources);
      }
    }
    allocatedResources.clear();
  }

  /**
   * Sets the vehicle's index of the last route step travelled for the current
   * drive order of its current transport order.
   *
   * @param index The new index.
   */
  private void setVehicleRouteProgressIndex(int index) {
    localKernel.setVehicleRouteProgressIndex(controlledVehicle.getReference(),
                                             index);
  }

  /**
   * Merges the properties of a transport order and those of a drive order.
   *
   * @param orderProps The properties of a transport order.
   * @param destProps The properties of a drive order destination.
   * @return The merged properties.
   */
  private static Map<String, String> mergeProperties(Map<String, String> orderProps,
                                                     Map<String, String> destProps) {
    assert orderProps != null : "orderProps is null";
    assert destProps != null : "destProps is null";

    Map<String, String> result = new HashMap<>();
    result.putAll(orderProps);
    result.putAll(destProps);
    return result;
  }
}
