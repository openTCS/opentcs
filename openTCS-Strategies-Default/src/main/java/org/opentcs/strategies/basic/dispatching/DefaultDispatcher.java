/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static com.google.common.base.Preconditions.checkState;
import java.util.Collections;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.VehicleController;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.opentcs.util.QueueProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches transport orders and vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultDispatcher
    implements Dispatcher {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultDispatcher.class);
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * The local kernel instance.
   */
  private final LocalKernel kernel;
  /**
   * The vehicle controller pool.
   */
  private final VehicleControllerPool vehicleControllerPool;
  /**
   *
   */
  private final Assignments assignments;
  /**
   * Selects vehicles for available transport orders.
   */
  private final VehicleSelector vehicleSelector;
  /**
   * Selects transport orders for available vehicles.
   */
  private final TransportOrderSelector orderSelector;
  /**
   * Stores reservations of transport orders for vehicles.
   */
  private final OrderReservationPool orderReservationPool;
  /**
   * Provides services/utility methods for working with transport orders.
   */
  private final TransportOrderService transportOrderService;
  /**
   * This class's configuration.
   */
  private final DefaultDispatcherConfiguration configuration;
  /**
   * The task doing the actual dispatching.
   */
  private DispatcherTask dispatcherTask;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;
  /**
   * A list of vehicles that are to be disabled/made UNAVAILABLE after they have
   * finished/aborted their current transport orders.
   */
  private final Set<TCSObjectReference<Vehicle>> vehiclesToDisable = ConcurrentHashMap.newKeySet();

  /**
   * Creates a new instance.
   *
   * @param newRouter Computes route costs.
   * @param kernel The kernel instance to work with.
   * @param vehicleControllerPool The vehicle controller pool to work with.
   * @param assignments
   * @param orderReservationPool Stores reservations of transport orders for vehicles.
   * @param vehicleSelector Selects vehicles for available transport orders.
   * @param orderSelector Selects transport orders for available vehicles.
   * @param transportOrderService Provides services for working with transport orders.
   * @param configuration Provides runtime configuration data.
   */
  @Inject
  public DefaultDispatcher(Router newRouter,
                           LocalKernel kernel,
                           VehicleControllerPool vehicleControllerPool,
                           Assignments assignments,
                           OrderReservationPool orderReservationPool,
                           VehicleSelector vehicleSelector,
                           TransportOrderSelector orderSelector,
                           TransportOrderService transportOrderService,
                           DefaultDispatcherConfiguration configuration) {
    this.router = requireNonNull(newRouter, "newRouter");
    this.kernel = requireNonNull(kernel, "newKernel");
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
    this.assignments = requireNonNull(assignments, "assignments");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.vehicleSelector = requireNonNull(vehicleSelector, "vehicleSelector");
    this.orderSelector = requireNonNull(orderSelector, "orderSelector");
    this.transportOrderService = requireNonNull(transportOrderService, "transportOrderService");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  // Methods declared in interface Dispatcher start here.
  @Override
  public void initialize() {
    if (initialized) {
      LOG.debug("Already initialized, doing nothing.");
      return;
    }
    vehiclesToDisable.clear();
    orderReservationPool.clear();
    orderSelector.initialize();
    vehicleSelector.initialize();
    // Initialize the dispatching thread.
    dispatcherTask = new DispatcherTask();
    new Thread(dispatcherTask, getClass().getName() + "-DispatcherTask").start();
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      LOG.debug("Not initialized, doing nothing.");
      return;
    }
    LOG.info("Terminating...");
    orderSelector.terminate();
    vehicleSelector.terminate();
    dispatcherTask.terminate();
    initialized = false;
  }

  @Override
  public void dispatch(Vehicle incomingVehicle) {
    requireNonNull(incomingVehicle, "incomingVehicle");
    checkState(initialized, "Not initialized");

    LOG.debug("Dispatching vehicle {}", incomingVehicle.getName());
    // Get an up-to-date copy of the kernel's object first.
    Vehicle vehicle = kernel.getTCSObject(Vehicle.class,
                                          incomingVehicle.getReference());
    // Check if this vehicle is interesting for the dispatcher at all.
    if (!vehicleDispatchable(vehicle)) {
      LOG.debug("Vehicle {} not dispatchable.", vehicle.getName());
      return;
    }
    // Insert the object into the dispatching task's queue and notify it.
    dispatcherTask.addToQueue(vehicle);
  }

  @Override
  public void dispatch(TransportOrder incomingOrder) {
    requireNonNull(incomingOrder, "incomingOrder");
    checkState(initialized, "Not initialized");

    LOG.debug("Dispatching transport order {}", incomingOrder.getName());
    // Get an up-to-date copy of the kernel's object first.
    TransportOrder order = kernel.getTCSObject(TransportOrder.class,
                                               incomingOrder.getReference());
    if (order.getState().isFinalState()) {
      LOG.info("Transport order {} already marked as {}, not dispatching it.",
               order.getName(),
               order.getState());
      return;
    }

    LOG.debug("Received order {}. Wrapping Sequence: {}. Intended vehicle: {}. Provided properties: {}. Drive orders: {}",
              order,
              order.getWrappingSequence(),
              order.getIntendedVehicle(),
              order.getProperties(),
              order.getAllDriveOrders());

    // Insert the object into the dispatching task's queue and notify it.
    dispatcherTask.addToQueue(order);
  }

  @Override
  public void withdrawOrder(TransportOrder order,
                            boolean immediateAbort,
                            boolean disableVehicle) {
    requireNonNull(order, "order");
    checkState(initialized, "Not initialized");

    dispatcherTask.addToQueue(new WithdrawalByOrder(order, immediateAbort, disableVehicle));
  }

  @Override
  public void withdrawOrder(Vehicle vehicle,
                            boolean fastAbort,
                            boolean disableVehicle) {
    requireNonNull(vehicle, "vehicle");
    checkState(initialized, "Not initialized");

    dispatcherTask.addToQueue(new WithdrawalByVehicle(vehicle, fastAbort, disableVehicle, false));
  }

  @Override
  public void releaseVehicle(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    dispatcherTask.addToQueue(new WithdrawalByVehicle(vehicle, true, true, true));
  }

  @Override
  public synchronized String getInfo() {
    return "";
  }

  // Class-specific methods start here.
  /**
   * Assigns a transport order to a vehicle, stores a route for the vehicle in
   * the transport order, adjusts the state of vehicle and transport order
   * and starts processing.
   *
   * @param vehicle The vehicle that is supposed to process the transport
   * order.
   * @param transportOrder The transport order to be processed.
   * @param driveOrders The list of drive orders describing the route for the
   * vehicle.
   */
  private void assignTransportOrder(Vehicle vehicle,
                                    TransportOrder transportOrder,
                                    List<DriveOrder> driveOrders) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(transportOrder, "transportOrder");
    requireNonNull(driveOrders, "driveOrders");

    LOG.debug("Assigning vehicle {} to order {}.", vehicle.getName(), transportOrder.getName());
    final TCSObjectReference<Vehicle> vehicleRef = vehicle.getReference();
    final TCSObjectReference<TransportOrder> orderRef = transportOrder.getReference();
    // If the transport order was reserved, forget the reservation now.
    orderReservationPool.removeReservation(orderRef);
    // Set the vehicle's and transport order's state.
    kernel.setVehicleProcState(vehicleRef, Vehicle.ProcState.PROCESSING_ORDER);
    transportOrderService.updateTransportOrderState(orderRef, TransportOrder.State.BEING_PROCESSED);
    // Add cross references between vehicle and transport order/order sequence.
    kernel.setVehicleTransportOrder(vehicleRef, orderRef);
    if (transportOrder.getWrappingSequence() != null) {
      kernel.setVehicleOrderSequence(vehicleRef, transportOrder.getWrappingSequence());
      kernel.setOrderSequenceProcessingVehicle(transportOrder.getWrappingSequence(), vehicleRef);
    }
    kernel.setTransportOrderProcessingVehicle(orderRef, vehicleRef);
    kernel.setTransportOrderFutureDriveOrders(orderRef, driveOrders);
    kernel.setTransportOrderInitialDriveOrder(orderRef);
    // Let the router know about the route chosen.
    router.selectRoute(vehicle, Collections.unmodifiableList(driveOrders));
    // Update the transport order's copy.
    transportOrder = kernel.getTCSObject(TransportOrder.class, orderRef);
    DriveOrder driveOrder = transportOrder.getCurrentDriveOrder();
    // If the drive order must be assigned, do so.
    if (assignments.mustAssign(driveOrder, vehicle)) {
      // Let the vehicle controller know about the first drive order.
      vehicleControllerPool.getVehicleController(vehicle.getName())
          .setDriveOrder(driveOrder, transportOrder.getProperties());
    }
    // If the drive order need not be assigned, let the kernel know that the
    // vehicle is waiting for its next order - it will be dispatched again for
    // the next drive order, then.
    else {
      kernel.setVehicleProcState(vehicleRef, Vehicle.ProcState.AWAITING_ORDER);
    }
  } // void assignTransportOrder()

  private static boolean vehicleDispatchable(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    if (vehicle.getCurrentPosition() == null) {
      LOG.debug("{}: unknown position -> not dispatchable", vehicle.getName());
      return false;
    }
    // ProcState IDLE, State CHARGING and energy level not high enough? Then let
    // it charge a bit longer.
    if (vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && vehicle.hasState(Vehicle.State.CHARGING)
        && vehicle.isEnergyLevelCritical()) {
      LOG.debug("{}: state is CHARGING, energy level {}<={} -> not (yet) dispatchable",
                vehicle.getName(),
                vehicle.getEnergyLevel(),
                vehicle.getEnergyLevelCritical());
      return false;
    }
    // Only dispatch vehicles that are either not processing any order at all or
    // are waiting for the next drive order.
    if (!vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && !vehicle.hasProcState(Vehicle.ProcState.AWAITING_ORDER)) {
      LOG.debug("{}: procState is {} -> not dispatchable",
                vehicle.getName(),
                vehicle.getProcState());
      return false;
    }
    return true;
  }

  private void finishAbortion(TCSObjectReference<TransportOrder> orderRef,
                              Vehicle vehicle,
                              boolean disableVehicle) {
    requireNonNull(orderRef, "orderRef");
    requireNonNull(vehicle, "vehicle");

    // The current transport order has been aborted - update its state
    // and that of the vehicle.
    transportOrderService.updateTransportOrderState(orderRef, TransportOrder.State.FAILED);
    // Check if we're supposed to disable the vehicle and set its proc
    // state accordingly.
    if (disableVehicle) {
      kernel.setVehicleProcState(vehicle.getReference(), Vehicle.ProcState.UNAVAILABLE);
      vehiclesToDisable.remove(vehicle.getReference());
    }
    else {
      kernel.setVehicleProcState(vehicle.getReference(), Vehicle.ProcState.IDLE);
    }
    kernel.setVehicleTransportOrder(vehicle.getReference(), null);
    // Let the router know that the vehicle doesn't have a route any more.
    router.selectRoute(vehicle, null);
  }

// Inner classes start here.
  /**
   * The task doing the actual dispatching of transport orders and vehicles.
   */
  private class DispatcherTask
      extends QueueProcessor<Object> {

    /**
     * Creates a new DispatcherTask.
     */
    DispatcherTask() {
    }

    @Override
    protected void processQueueElement(Object dispatchable) {
      try {
        doProcessQueueElement(dispatchable);
      }
      catch (Exception exc) {
        LOG.error("Unhandled exception processing {}", dispatchable, exc);
      }
    }

    private void doProcessQueueElement(Object dispatchable) {
      // If dispatchable is null, we've been terminated or the queue was
      // empty.
      if (dispatchable == null) {
        LOG.debug("dispatchable is null, ignored");
      }
      else if (dispatchable instanceof TransportOrder) {
        TransportOrder order = (TransportOrder) dispatchable;
        LOG.debug("Dispatching transport order {}", order.getName());
        dispatchTransportOrder(order);
      }
      else if (dispatchable instanceof Vehicle) {
        Vehicle vehicle = (Vehicle) dispatchable;
        LOG.debug("Dispatching vehicle {}", vehicle.getName());
        dispatchVehicle(vehicle);
      }
      else if (dispatchable instanceof WithdrawalByVehicle) {
        WithdrawalByVehicle withdrawal = (WithdrawalByVehicle) dispatchable;
        LOG.debug("Dispatching withdrawal for vehicle {}", withdrawal.getVehicle().getName());
        abortOrder(withdrawal.getVehicle(),
                   withdrawal.isImmediateAbort(),
                   withdrawal.isDisablingVehicle(),
                   withdrawal.isResettingVehiclePosition());
      }
      else if (dispatchable instanceof WithdrawalByOrder) {
        WithdrawalByOrder withdrawal = (WithdrawalByOrder) dispatchable;
        LOG.debug("Dispatching withdrawal for transport order {}", withdrawal.getOrder().getName());
        abortOrder(withdrawal.getOrder(),
                   withdrawal.isImmediateAbort(),
                   withdrawal.isDisablingVehicle());
      }
      // If dispatchable is of an unhandled subtype, we just ignore it.
      else {
        LOG.warn("Dispatchable content of unhandled class {}, ignored",
                 dispatchable.getClass().getName());
      }
    }

    /**
     * Tries to dispatch a transport order by finding a vehicle to process it.
     *
     * @param order The transport order to be dispatched.
     */
    private void dispatchTransportOrder(TransportOrder incomingOrder) {
      requireNonNull(incomingOrder, "order");

      // Get an up-to-date copy of the kernel's object first.
      TransportOrder order = kernel.getTCSObject(TransportOrder.class,
                                                 incomingOrder.getReference());
      LOG.debug("Dispatched order {} is in state {}", order.getName(), order.getState());
      if (order.hasState(TransportOrder.State.RAW)) {
        dispatchTransportOrderRaw(order);
      }

      // The above may have updated the order's state/contents. Update it.
      order = kernel.getTCSObject(TransportOrder.class, incomingOrder.getReference());
      if (order.hasState(TransportOrder.State.DISPATCHABLE)) {
        dispatchTransportOrderDispatchable(order);
      }
    }

    private void dispatchTransportOrderRaw(TransportOrder order) {
      requireNonNull(order, "order");

      // Check if the transport order is routable.
      if (router.checkRoutability(order).isEmpty()) {
        LOG.info("Marking transport order {} as UNROUTABLE", order.getName());
        transportOrderService.updateTransportOrderState(order.getReference(),
                                                        TransportOrder.State.UNROUTABLE);
      }
      else {
        LOG.info("Marking transport order {} as ACTIVE", order.getName());
        transportOrderService.updateTransportOrderState(order.getReference(),
                                                        TransportOrder.State.ACTIVE);
        // The transport order has been activated - dispatch it.
        // Check if it has unfinished dependencies.
        if (!transportOrderService.hasUnfinishedDependencies(order)) {
          LOG.info("Marking transport order {} as DISPATCHABLE", order.getName());
          transportOrderService.updateTransportOrderState(order.getReference(),
                                                          TransportOrder.State.DISPATCHABLE);
        }
      }
    }

    private void dispatchTransportOrderDispatchable(TransportOrder order) {
      requireNonNull(order, "order");

      // Get the vehicle that needs the least time to process the
      // transport order.
      VehicleOrderSelection vehicleSelection = vehicleSelector.selectVehicle(order);
      // If we found a vehicle that can process the transport order, assign the
      // order to it, store the computed route in the transport order, correct
      // the state of vehicle and transport order and start processing of the
      // order.
      if (vehicleSelection.getVehicle() != null) {
        Vehicle selectedVehicle = vehicleSelection.getVehicle();
        // If the vehicle still has an order, just remember that the new order
        // is reserved for this vehicle and initiate abortion of the old one.
        // Once the abortion is complete, the vehicle will automatically be
        // re-dispatched and will then pick up the new order.
        if (selectedVehicle.getTransportOrder() != null) {
          LOG.debug("Reserving {} for {} ", order, selectedVehicle);
          // Remember that the new order is reserved for this vehicle.
          orderReservationPool.addReservation(order.getReference(), selectedVehicle.getReference());
          // Abort the vehicle's current order.
          abortOrder(selectedVehicle, false, false, false);
        }
        else {
          // Make sure the vehicle is not in the queue any more before we
          // re-dispatch it.
          removeFromQueue(selectedVehicle);
          assignTransportOrder(selectedVehicle, order, vehicleSelection.getDriveOrders());
        }
      }
      else {
        LOG.debug("Did not find a vehicle to dispatch order to.");
      }
    }

    /**
     * Tries to dispatch a vehicle by finding a transport order to be processed
     * by it.
     *
     * @param vehicle The vehicle to be dispatched.
     */
    private void dispatchVehicle(Vehicle incomingVehicle) {
      requireNonNull(incomingVehicle, "incomingVehicle");

      // Get an up-to-date copy of the kernel's object first.
      Vehicle vehicle = kernel.getTCSObject(Vehicle.class, incomingVehicle.getReference());
      // Filter undispatchable vehicles first.
      if (!vehicleDispatchable(vehicle)) {
        return;
      }

      // Is the vehicle idle/without a transport order?
      if (vehicle.hasProcState(Vehicle.ProcState.IDLE)) {
        dispatchVehicleIdle(vehicle);
      }
      // Is the vehicle waiting for the next drive order?
      else if (vehicle.hasProcState(Vehicle.ProcState.AWAITING_ORDER)) {
        dispatchVehicleAwaitingOrder(vehicle);
      }
      // If the vehicle is in any other processing state, it shouldn't be here.
      else {
        throw new IllegalStateException(vehicle.getName() + " is in proc state "
            + vehicle.getProcState());
      }
    } // void dispatchVehicle()

    /**
     * Dispatch the given vehicle; it is not processing a transport order - try
     * to find one for it.
     *
     * @param vehicle The vehicle.
     */
    private void dispatchVehicleIdle(Vehicle vehicle) {
      requireNonNull(vehicle, "vehicle");

      LOG.debug("{}: IDLE, looking for a transport order", vehicle.getName());
      VehicleOrderSelection orderSelection = orderSelector.selectTransportOrder(vehicle);
      if (orderSelection.getTransportOrder() != null) {
        assignTransportOrder(vehicle, orderSelection.getTransportOrder(), orderSelection.getDriveOrders());
      }
      else {
        LOG.debug("{}: No order to be assigned to vehicle.", vehicle.getName());
      }
    }

    /**
     * Dispatch the given vehicle; it is processing a transport order and has
     * finished a drive order - either finish a pending withdrawal or assign the
     * next drive order to it, if one exists.
     *
     * @param vehicle The vehicle.
     */
    private void dispatchVehicleAwaitingOrder(Vehicle vehicle) {
      requireNonNull(vehicle, "vehicle");

      LOG.debug("{}: Finished a drive order", vehicle.getName());
      final TCSObjectReference<Vehicle> vehicleRef = vehicle.getReference();
      final TCSObjectReference<TransportOrder> vehicleOrderRef = vehicle.getTransportOrder();
      // The vehicle is processing a transport order and has finished a drive
      // order. See if we're coping with a withdrawal or, if not, if there's
      // another drive order to be processed.
      kernel.setTransportOrderNextDriveOrder(vehicleOrderRef);
      TransportOrder vehicleOrder = kernel.getTCSObject(TransportOrder.class, vehicleOrderRef);

      if (vehicleOrder.hasState(TransportOrder.State.WITHDRAWN)) {
        LOG.debug(vehicle.getName() + ": Aborted order " + vehicleOrder
            + ". Wrapping sequence: " + vehicleOrder.getWrappingSequence()
            + ". Drive orders: " + vehicleOrder.getAllDriveOrders()
            + ". Intended vehicle: " + vehicleOrder.getIntendedVehicle()
            + ". Provided properties: " + vehicleOrder.getProperties());
        finishAbortion(vehicleOrderRef, vehicle, vehiclesToDisable.contains(vehicleRef));
      }
      else if (vehicleOrder.getCurrentDriveOrder() == null) {
        LOG.debug(vehicle.getName() + ": Finished order " + vehicleOrder
            + ". Wrapping sequence: " + vehicleOrder.getWrappingSequence()
            + ". Drive orders: " + vehicleOrder.getAllDriveOrders()
            + ". Intended vehicle: " + vehicleOrder.getIntendedVehicle()
            + ". Provided properties: " + vehicleOrder.getProperties());
        // The current transport order has been finished - update its state
        // and that of the vehicle.
        transportOrderService.updateTransportOrderState(vehicleOrderRef,
                                                        TransportOrder.State.FINISHED);
        // Update the vehicle's procState, implicitly dispatching it again.
        kernel.setVehicleProcState(vehicleRef, Vehicle.ProcState.IDLE);
        kernel.setVehicleTransportOrder(vehicleRef, null);
        // Let the router know that the vehicle doesn't have a route any more.
        router.selectRoute(vehicle, null);
        // Dispatch transport orders that are dispatchable now that this one has been finished.
        for (TransportOrder order : transportOrderService.findNewDispatchableOrders()) {
          LOG.debug("Dispatching order {} which has just become dispatchable", order.getName());
          DefaultDispatcher.this.dispatch(order);
        }
      }
      else {
        LOG.debug("{}: Assigning next drive order", vehicle.getName());
        // The vehicle is still processing a transport order. Get the next
        // drive order to be processed from the kernel (and not from our
        // possibly outdated copy of the transport order).
        DriveOrder currentDriveOrder = vehicleOrder.getCurrentDriveOrder();
        if (assignments.mustAssign(currentDriveOrder, vehicle)) {
          // Let the vehicle controller know about the new drive order.
          vehicleControllerPool.getVehicleController(vehicle.getName())
              .setDriveOrder(currentDriveOrder, vehicleOrder.getProperties());
        }
        // If the drive order need not be assigned, let the kernel know that
        // the vehicle is waiting for its next order - it will be dispatched
        // again for the next drive order, then.
        else {
          kernel.setVehicleProcState(vehicleRef, Vehicle.ProcState.AWAITING_ORDER);
        }
      }
    }

    /**
     * Let a given vehicle abort any order it may currently be processing.
     *
     * @param vehicle The vehicle which should abort its order.
     * @param immediateAbort Whether to abort the order immediately instead of
     * just withdrawing it for a smooth abortion.
     * @param disableVehicle Whether to disable the vehicle, i.e. set its
     * procState to UNAVAILABLE.
     */
    private void abortOrder(Vehicle vehicle,
                            boolean immediateAbort,
                            boolean disableVehicle,
                            boolean resetVehiclePosition) {
      TCSObjectReference<TransportOrder> orderRef = vehicle.getTransportOrder();

      // If the vehicle does NOT have an order, update its processing state now.
      if (orderRef == null) {
        if (disableVehicle) {
          kernel.setVehicleProcState(vehicle.getReference(), Vehicle.ProcState.UNAVAILABLE);
          // Since the vehicle is now disabled, release any order reservations
          // for it, too. Disabled vehicles should not keep reservations, and
          // this is a good fallback trigger to get rid of them in general.
          orderReservationPool.removeReservations(vehicle.getReference());
        }
        else {
          kernel.setVehicleProcState(vehicle.getReference(), Vehicle.ProcState.IDLE);
        }
      }
      else {
        abortAssignedOrder(kernel.getTCSObject(TransportOrder.class, orderRef),
                           vehicle,
                           immediateAbort,
                           disableVehicle);
      }
      // If requested, reset the vehicle position to null and free all resources.
      if (immediateAbort && resetVehiclePosition) {
        // XXX Doesn't work with NullVehicleController, (== without a communication adapter), yet.
        vehicleControllerPool.getVehicleController(vehicle.getName()).resetVehiclePosition();
      }
    }

    private void abortOrder(TransportOrder order,
                            boolean immediateAbort,
                            boolean disableVehicle) {
      TCSObjectReference<Vehicle> vehicleRef = order.getProcessingVehicle();

      // If the order is NOT currently being processed by any vehicle, just make
      // sure it's not going to be processed later, either.
      if (vehicleRef == null) {
        if (!order.getState().isFinalState()) {
          transportOrderService.updateTransportOrderState(order.getReference(),
                                                          TransportOrder.State.FAILED);
        }
      }
      else {
        abortAssignedOrder(order,
                           kernel.getTCSObject(Vehicle.class, vehicleRef),
                           immediateAbort,
                           disableVehicle);
      }
    }

    /**
     * Aborts a given transport order known to be assigned to a given vehicle.
     *
     * @param vehicle The vehicle the order is assigned to.
     * @param order The order.
     * @param immediateAbort Whether to abort the order immediately instead of
     * just withdrawing it for a smooth abortion.
     * @param disableVehicle Whether to disable the vehicle, i.e. set its
     * procState to UNAVAILABLE.
     */
    private void abortAssignedOrder(TransportOrder order,
                                    Vehicle vehicle,
                                    boolean immediateAbort,
                                    boolean disableVehicle) {
      requireNonNull(order, "order");
      requireNonNull(vehicle, "vehicle");

      // Mark the order as withdrawn so we can react appropriately when the
      // vehicle reports the remaining movements as finished
      if (!order.getState().isFinalState()
          && !order.hasState(TransportOrder.State.WITHDRAWN)) {
        transportOrderService.updateTransportOrderState(order.getReference(),
                                                        TransportOrder.State.WITHDRAWN);
      }

      VehicleController vehicleController
          = vehicleControllerPool.getVehicleController(vehicle.getName());

      if (immediateAbort) {
        LOG.info("{}: Immediate abort of transport order {}...",
                 vehicle.getName(),
                 order.getName());
        vehicleController.clearDriveOrder();
        vehicleController.clearCommandQueue();
        finishAbortion(order.getReference(), vehicle, disableVehicle);
      }
      else {
        if (disableVehicle) {
          // Remember that the vehicle should be disabled after finishing the
          // orders it already received.
          LOG.debug("{}: To be disabled later", vehicle.getName());
          vehiclesToDisable.add(vehicle.getReference());
        }
        vehicleController.abortDriveOrder();
        // XXX What if the controller does not have any more movements to be
        // finished? Will it ever re-dispatch the vehicle in that case?
      }
    }

  } // class DispatcherTask
}
