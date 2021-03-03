/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static com.google.common.base.Preconditions.checkState;
import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.ParkingPositionSupplier;
import org.opentcs.components.kernel.RechargePositionSupplier;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.VehicleController;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.opentcs.util.ExplainedBoolean;
import org.opentcs.util.QueueProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches new transport orders to idle vehicles that need the least time to
 * process them.
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
   * The task doing the actual dispatching.
   */
  private DispatcherTask dispatcherTask;
  /**
   * A flag indiciating whether to park vehicles that have finished their order
   * and for which a new order is not available.
   */
  private final boolean parkIdleVehicles;
  /**
   * The strategy used for finding suitable parking positions.
   */
  private final ParkingPositionSupplier parkingPosSupplier;
  /**
   * A flag indicating whether to automatically create recharge orders for
   * vehicles that do not have an order.
   */
  private final boolean rechargeVehiclesWhenIdle;
  /**
   * A flag indicating whether to automatically create recharge orders for
   * vehicles whose energy level is critical.
   */
  private final boolean rechargeVehiclesWhenEnergyCritical;
  /**
   * The strategy used for finding suitable recharge locations.
   */
  private final RechargePositionSupplier rechargePosSupplier;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;
  /**
   * A list of vehicles that are to be disabled/made UNAVAILABLE after they have
   * finished/aborted their current transport orders.
   */
  private final Set<TCSObjectReference<Vehicle>> vehiclesToDisable
      = Collections.synchronizedSet(new HashSet<TCSObjectReference<Vehicle>>());
  /**
   * Reservations of orders for vehicles.
   */
  private final Map<TCSObjectReference<TransportOrder>, TCSObjectReference<Vehicle>> orderReservations
      = Collections.synchronizedMap(new HashMap<TCSObjectReference<TransportOrder>, TCSObjectReference<Vehicle>>());

  /**
   * Creates a new instance.
   *
   * @param newRouter The <code>Router</code> instance calculating route costs.
   * @param newKernel The local kernel instance.
   * @param parkingPosSupplier The strategy used for finding suitable parking
   * positions.
   * @param rechargeStrategy The strategy used for finding suitable recharge
   * locations.
   * @param vehicleControllerPool The vehicle controller pool.
   * @param parkIdleVehicles Whether to park vehicles that have finished their
   * order and for which a new order is not available.
   * @param rechargeVehiclesWhenIdle Whether to automatically create recharge
   * orders for vehicles that do not have an order.
   * @param rechargeVehiclesWhenEnergyCritical Whether to automatically create
   * recharge orders for vehicles whose energy level is critical.
   */
  @Inject
  public DefaultDispatcher(Router newRouter,
                           LocalKernel newKernel,
                           ParkingPositionSupplier parkingPosSupplier,
                           RechargePositionSupplier rechargeStrategy,
                           VehicleControllerPool vehicleControllerPool,
                           @ParkWhenIdle boolean parkIdleVehicles,
                           @RechargeWhenIdle boolean rechargeVehiclesWhenIdle,
                           @RechargeWhenEnergyCritical boolean rechargeVehiclesWhenEnergyCritical) {
    this.router = requireNonNull(newRouter, "newRouter");
    this.kernel = requireNonNull(newKernel, "newKernel");
    this.parkingPosSupplier = requireNonNull(parkingPosSupplier, "parkingPosSupplier");
    this.rechargePosSupplier = requireNonNull(rechargeStrategy, "rechargeStrategy");
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
    this.parkIdleVehicles = parkIdleVehicles;
    this.rechargeVehiclesWhenIdle = rechargeVehiclesWhenIdle;
    this.rechargeVehiclesWhenEnergyCritical = rechargeVehiclesWhenEnergyCritical;
  }

  // Methods declared in interface Dispatcher start here.
  @Override
  public void initialize() {
    if (initialized) {
      LOG.debug("Already initialized, doing nothing.");
      return;
    }
    vehiclesToDisable.clear();
    orderReservations.clear();
    parkingPosSupplier.initialize();
    rechargePosSupplier.initialize();
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
    dispatcherTask.terminate();
    parkingPosSupplier.terminate();
    rechargePosSupplier.terminate();
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
   * Returns available vehicles for the given order.
   *
   * @param order The order for which to find available vehicles.
   * @return Available vehicles for the given order, or an empty list.
   */
  protected List<Vehicle> findVehiclesForOrder(TransportOrder order) {
    requireNonNull(order, "order");

    List<Vehicle> result = new LinkedList<>();
    // Check if the order or its wrapping sequence have an intended vehicle.
    TCSObjectReference<Vehicle> vRefIntended = null;
    // If the order belongs to an order sequence, check if a vehicle is already
    // processing it or, if not, if the sequence is intended for a specific
    // vehicle.
    if (order.getWrappingSequence() != null) {
      OrderSequence seq = kernel.getTCSObject(OrderSequence.class, order.getWrappingSequence());
      if (seq.getProcessingVehicle() != null) {
        vRefIntended = seq.getProcessingVehicle();
      }
      else if (seq.getIntendedVehicle() != null) {
        vRefIntended = seq.getIntendedVehicle();
      }
    }
    // If there's no order sequence, but the order itself is intended for a
    // specific vehicle, take that.
    else if (order.getIntendedVehicle() != null) {
      vRefIntended = order.getIntendedVehicle();
    }
    // If the transport order has an intended vehicle, get only that one - but
    // only if it is at a known position, is IDLE and either isn't processing
    // any order sequence or is processing exactly the one that this order
    // belongs to.
    if (vRefIntended != null) {
      Vehicle intendedVehicle = kernel.getTCSObject(Vehicle.class, vRefIntended);
      if (availableForTransportOrder(intendedVehicle)
          && (intendedVehicle.getOrderSequence() == null
              || Objects.equals(intendedVehicle.getOrderSequence(),
                                order.getWrappingSequence()))) {
        result.add(intendedVehicle);
      }
    }
    // If there's no intended vehicle, get all vehicles that are at a known
    // position, are IDLE, aren't processing any order sequences or are
    // processing exactly the one that this order belongs to.
    else {
      for (Vehicle curVehicle : kernel.getTCSObjects(Vehicle.class)) {
        if (availableForTransportOrder(curVehicle)
            && (curVehicle.getOrderSequence() == null
                || Objects.equals(curVehicle.getOrderSequence(),
                                  order.getWrappingSequence()))) {
          result.add(curVehicle);
        }
      }
    }
    return result;
  }

  /**
   * Returns available transport orders for the given vehicle.
   *
   * @param vehicle The vehicle for which to find available transport orders.
   * @return Available transport orders for the given vehicle.
   */
  protected Set<TransportOrder> findOrdersForVehicle(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    Optional<TransportOrder> reservedOrder = findReservedOrder(vehicle);
    if (reservedOrder.isPresent()) {
      return Collections.singleton(reservedOrder.get());
    }
    // No reservation for this vehicle? Select available orders from the pool.
    Set<TransportOrder> result = Assignments.getOrdersForVehicle(
        kernel.getTCSObjects(OrderSequence.class),
        kernel.getTCSObjects(TransportOrder.class),
        vehicle);
    // Filter out all transport orders with reservations. (If there was a
    // reservation for this vehicle, we would have found it above.)
    if (result != null) {
      Iterator<TransportOrder> iter = result.iterator();
      while (iter.hasNext()) {
        if (orderReservations.containsKey(iter.next().getReference())) {
          iter.remove();
        }
      }
    }
    return result;
  }

  /**
   * Returns the order reserved for the given vehicle, if one such order exists
   * and it is in an assignable state (i.e. not in a final state and not
   * withdrawn).
   *
   * @param vehicle The vehicle for which to find the reserved order.
   * @return The order reserved for the given vehicle, if one such order exists.
   */
  protected Optional<TransportOrder> findReservedOrder(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    // Check if there's an order reserved for this vehicle that is in an
    // assignable state. If yes, return that.
    return orderReservations.entrySet().stream()
        .filter(entry -> vehicle.getReference().equals(entry.getValue()))
        .findFirst()
        .map(entry -> kernel.getTCSObject(TransportOrder.class, entry.getKey()))
        .filter(order -> !order.getState().isFinalState()
            && !order.hasState(TransportOrder.State.WITHDRAWN));
  }

  private boolean orderMandatory(TransportOrder order,
                                 Set<OrderSequence> sequences) {
    // Check if the selected order MUST be assigned right now or if an order for
    // recharging would be possible. If the selected order is part of an order
    // sequence and is already being processed, we cannot shove in another order
    // but have to finish the sequence first.
    if (order.getWrappingSequence() != null) {
      for (OrderSequence seq : sequences) {
        if (order.getWrappingSequence().equals(seq.getReference())
            && seq.getProcessingVehicle() != null) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Assigns the most acceptable/best fitting transport order from the given set
   * to the given vehicle, or sends it parking, if no acceptable order was found
   * and auto parking is enabled.
   *
   * @param vehicle The vehicle.
   * @param transportOrders The set of transport orders to choose from.
   */
  private void assignBestOrderToVehicle(Vehicle vehicle,
                                        Set<TransportOrder> transportOrders,
                                        Set<OrderSequence> sequences) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(transportOrders, "transportOrders");
    requireNonNull(sequences, "sequences");

    final Point vehiclePosition = kernel.getTCSObject(Point.class, vehicle.getCurrentPosition());
    // Assuming the transport orders are sorted correctly, we can now just
    // grab the first one that can be processed by the given vehicle.
    final Iterator<TransportOrder> orderIter = transportOrders.iterator();
    TransportOrder selectedOrder = null;
    Optional<List<DriveOrder>> driveOrders = Optional.empty();
    while (selectedOrder == null && orderIter.hasNext()) {
      TransportOrder curOrder = orderIter.next();
      boolean canProcess;
      // Get a route for the vehicle.
      driveOrders = router.getRoute(vehicle, vehiclePosition, curOrder);
      canProcess = driveOrders.isPresent();
      if (!canProcess) {
        LOG.debug("{}: No route for order {}", vehicle.getName(), curOrder);
        kernel.addTransportOrderRejection(curOrder.getReference(),
                                          new Rejection(vehicle.getReference(), "Unroutable"));
      }
      // Check if the vehicle can process the order right now.
      canProcess = canProcess && checkProcessability(vehicle, curOrder);
      // If the vehicle can process this order, choose it.
      if (canProcess) {
        selectedOrder = curOrder;
      }
    }
    // A flag to remember if we already created an order for the vehicle.
    boolean orderCreated = false;
    // If automatic creation of recharge orders is enabled, the vehicle's energy
    // level is critical and the selected order (if any) is not mandatory,
    // create an order to recharge and assign that instead.
    if (rechargeVehiclesWhenEnergyCritical
        && vehicle.isEnergyLevelCritical()
        && (selectedOrder == null
            || !orderMandatory(selectedOrder, sequences))) {
      // If the vehicle is already charging, don't create a new order but leave
      // it like that.
      if (vehicle.hasState(Vehicle.State.CHARGING)) {
        LOG.debug("{}: Energy level critical, but vehicle is already charging - leaving it alone.",
                  vehicle.getName());
        orderCreated = true;
      }
      else {
        LOG.debug("{}: Energy level critical, looking for recharge location.", vehicle.getName());
        orderCreated = rechargeVehicle(vehicle, vehiclePosition);
      }
    }

    if (!orderCreated) {
      // If we found a transport order, assign it to the vehicle.
      if (selectedOrder != null) {
        LOG.debug("{}: Selected transport order {} for assignment",
                  vehicle.getName(),
                  selectedOrder.getName());
        assignTransportOrder(vehicle, selectedOrder, driveOrders.get());
        orderCreated = true;
      }
      else {
        LOG.debug("{}: Didn't find acceptable transport order for assignment", vehicle.getName());
      }
    }
    // If the vehicle's energy level is not "good" any more, send it to a
    // charging station.
    if (!orderCreated && rechargeVehiclesWhenIdle && vehicle.isEnergyLevelDegraded()) {
      // If the vehicle is already charging, don't create a new order but leave
      // it like that.
      if (vehicle.hasState(Vehicle.State.CHARGING)) {
        LOG.debug("{}: No transport order, but vehicle is already charging - leaving it alone.",
                  vehicle.getName());
        orderCreated = true;
      }
      else {
        LOG.debug("{}: No transport order, looking for recharge location.", vehicle.getName());
        orderCreated = rechargeVehicle(vehicle, vehiclePosition);
      }
    }
    // If auto parking is enabled and the vehicle's not at a parking position,
    // yet, send it to one.
    if (!orderCreated && parkIdleVehicles && !vehiclePosition.isParkingPosition()) {
      if (vehicle.hasState(Vehicle.State.CHARGING) && vehicle.getEnergyLevel() < 100) {
        LOG.debug("{}: Not at parking position, but charging and energy < 100% - leaving it alone.",
                  vehicle.getName());
        orderCreated = true;
      }
      else {
        LOG.debug("{}: Not at a parking position, looking for one.", vehicle.getName());
        orderCreated = parkVehicle(vehicle, vehiclePosition);
      }
    }
    LOG.debug("{}: orderCreated is {}", vehicle.getName(), orderCreated);
    // Make sure there are no reservations left for the vehicle. (Theoretically,
    // it would be possible that an order was reserved for a vehicle and then
    // refused by it...)
    clearOrderReservations(vehicle.getReference());
  }

  /**
   * Sends a vehicle to a parking position.
   *
   * @param vehicle The vehicle to be parked.
   * @param vehiclePosition The vehicle's current position.
   * @return <code>true</code> if, and only if, a parking order was actually
   * created and assigned.
   */
  private boolean parkVehicle(Vehicle vehicle,
                              Point vehiclePosition) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(vehiclePosition, "vehiclePosition");

    // Get a suitable parking position for the vehicle.
    Optional<Point> parkPos = parkingPosSupplier.findParkingPosition(vehicle);
    LOG.debug("Parking position for {}: {}", vehicle, parkPos);
    // If we could not find a suitable parking position at all, just leave the
    // vehicle where it is.
    if (!parkPos.isPresent()) {
      LOG.warn("{}: Did not find a suitable parking position.", vehicle.getName());
      return false;
    }
    // Wrap the name of the parking position in a dummy location reference.
    TCSObjectReference<Location> parkLocRef
        = TCSObjectReference.getDummyReference(Location.class, parkPos.get().getName());
    // Create a destination.
    Destination parkDest = new Destination(parkLocRef, Destination.OP_PARK);
    List<Destination> parkDests = Collections.singletonList(parkDest);
    // Create a transport order for parking and verify its processability.
    TransportOrder parkOrder = kernel.createTransportOrder(parkDests);
    kernel.setTransportOrderDispensable(parkOrder.getReference(), true);
    kernel.setTransportOrderIntendedVehicle(parkOrder.getReference(), vehicle.getReference());
    Optional<List<DriveOrder>> driveOrders = router.getRoute(vehicle, vehiclePosition, parkOrder);
    if (checkProcessability(vehicle, parkOrder)) {
      // Assign the parking order.
      assignTransportOrder(vehicle, parkOrder, driveOrders.get());
      return true;
    }
    else {
      // Mark the order as failed, since the vehicle does not want to execute it.
      updateTransportOrderState(parkOrder.getReference(), TransportOrder.State.FAILED);
      return false;
    }
  } // void parkVehicle()

  /**
   * Sends a vehicle to a charging location.
   *
   * @param vehicle The vehicle to be parked.
   * @param vehiclePosition The vehicle's current position.
   * @return <code>true</code> if, and only if, a charging order was actually
   * created and assigned.
   */
  private boolean rechargeVehicle(Vehicle vehicle, Point vehiclePosition) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(vehiclePosition, "vehiclePosition");

    List<Destination> rechargeDests = rechargePosSupplier.findRechargeSequence(vehicle);
    if (rechargeDests.isEmpty()) {
      LOG.warn("{}: Did not find a suitable recharge sequence for vehicle", vehicle.getName());
      return false;
    }
    // Create a transport order for recharging and verify its processability.
    TransportOrder rechargeOrder = kernel.createTransportOrder(rechargeDests);
    // The recharge order may be withdrawn unless its energy level is critical.
    if (!vehicle.isEnergyLevelCritical()) {
      kernel.setTransportOrderDispensable(rechargeOrder.getReference(), true);
    }
    kernel.setTransportOrderIntendedVehicle(rechargeOrder.getReference(), vehicle.getReference());
    Optional<List<DriveOrder>> driveOrders
        = router.getRoute(vehicle, vehiclePosition, rechargeOrder);
    if (checkProcessability(vehicle, rechargeOrder)) {
      // Assign the recharge order.
      assignTransportOrder(vehicle, rechargeOrder, driveOrders.get());
      return true;
    }
    else {
      // Mark the order as failed, since the vehicle does not want to execute it.
      updateTransportOrderState(rechargeOrder.getReference(), TransportOrder.State.FAILED);
      return false;
    }
  }

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
    orderReservations.remove(orderRef);
    // Set the vehicle's and transport order's state.
    kernel.setVehicleProcState(vehicleRef, Vehicle.ProcState.PROCESSING_ORDER);
    updateTransportOrderState(orderRef, TransportOrder.State.BEING_PROCESSED);
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
    if (Assignments.mustAssign(driveOrder, vehicle)) {
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

  /**
   * Checks if the given vehicle could process the given order right now.
   *
   * @param vehicle The vehicle.
   * @param order The order.
   * @return <code>true</code> if, and only if, the given vehicle can process
   * the given order.
   */
  private boolean checkProcessability(Vehicle vehicle, TransportOrder order) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(order, "order");

    // If there isn't any vehicle controller for this vehicle, it cannot process
    // the order.
    ExplainedBoolean result = vehicleControllerPool.getVehicleController(vehicle.getName())
        .canProcess(Assignments.getOperations(order));
    if (result.isTrue()) {
      return true;
    }
    else {
      // The vehicle controller/communication adapter does not want to process
      // the order. Add a rejection for it.
      Rejection rejection = new Rejection(vehicle.getReference(), result.getReason());
      LOG.debug("Order {} rejected by {}, reason: {}",
                order.getName(),
                vehicle.getName(),
                rejection.getReason());
      kernel.addTransportOrderRejection(order.getReference(), rejection);
      return false;
    }
  }

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

  /**
   * Checks if the given vehicle is available for processing a transport order.
   *
   * @param vehicle The vehicle to be checked.
   * @return <code>true</code> if, and only if, the given vehicle is available
   * for processing a transport order.
   */
  private boolean availableForTransportOrder(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    // A vehicle must be at a known position.
    if (vehicle.getCurrentPosition() == null) {
      return false;
    }
    boolean hasDispensableOrder = false;
    // The vehicle must not be processing any order. If it is, the order must be
    // dispensable.
    if (!vehicle.hasProcState(Vehicle.ProcState.IDLE)) {
      if (vehicle.hasProcState(Vehicle.ProcState.PROCESSING_ORDER)) {
        TransportOrder order = kernel.getTCSObject(TransportOrder.class,
                                                   vehicle.getTransportOrder());
        if (order.isDispensable()) {
          hasDispensableOrder = true;

          // Check if there's already an order reservation for this vehicle.
          // There should not be more than one reservation in advance, so if we
          // already have one, this vehicle is not available.
          for (TCSObjectReference<Vehicle> curEntry : orderReservations.values()) {
            if (vehicle.getReference().equals(curEntry)) {
              return false;
            }
          }
        }
        else {
          // Vehicle is processing an order and it's not dispensable.
          return false;
        }
      }
      else {
        // Vehicle's state is not PROCESSING_ORDER (and not IDLE, either).
        return false;
      }
    }
    // The physical vehicle must either be processing a dispensable order, or be
    // in an idle state, or it must be charging and have reached an acceptable
    // energy level already.
    if (!(hasDispensableOrder
          || vehicle.hasState(Vehicle.State.IDLE)
          || (vehicle.hasState(Vehicle.State.CHARGING)
              && !vehicle.isEnergyLevelCritical()))) {
      return false;
    }
    return true;
  }

  /**
   * Removes any order reservation for the given vehicle.
   *
   * @param vehicleRef The vehicle.
   */
  private void clearOrderReservations(TCSObjectReference<Vehicle> vehicleRef) {
    requireNonNull(vehicleRef, "vehicleRef");

    orderReservations.values().removeIf(value -> vehicleRef.equals(value));
  }

  private void finishAbortion(TCSObjectReference<TransportOrder> orderRef,
                              Vehicle vehicle,
                              boolean disableVehicle) {
    requireNonNull(orderRef, "orderRef");
    requireNonNull(vehicle, "vehicle");

    // The current transport order has been aborted - update its state
    // and that of the vehicle.
    updateTransportOrderState(orderRef, TransportOrder.State.FAILED);
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

  private void updateTransportOrderState(TCSObjectReference<TransportOrder> ref,
                                         TransportOrder.State newState) {
    switch (newState) {
      case FINISHED:
        setTOStateFinished(ref);
        break;
      case FAILED:
        setTOStateFailed(ref);
        break;
      default:
        // Set the transport order's state.
        kernel.setTransportOrderState(ref, newState);
    }
  }

  /**
   * Properly sets a transport order to a finished state, setting related
   * properties.
   *
   * @param ref A reference to the transport order to be modified.
   * @throws ObjectUnknownException If the referenced order could not be found.
   */
  private void setTOStateFinished(TCSObjectReference<TransportOrder> ref) {
    requireNonNull(ref, "ref");

    // Set the transport order's state.
    kernel.setTransportOrderState(ref, TransportOrder.State.FINISHED);
    TransportOrder order = kernel.getTCSObject(TransportOrder.class, ref);
    // If it is part of an order sequence, we should proceed to its next order.
    if (order.getWrappingSequence() != null) {
      OrderSequence seq = kernel.getTCSObject(OrderSequence.class,
                                              order.getWrappingSequence());
      // Sanity check: The finished order must be the next one in the sequence;
      // if it is not, something has already gone wrong.
      checkState(ref.equals(seq.getNextUnfinishedOrder()),
                 "Finished TO %s != next unfinished TO %s in sequence %s",
                 ref,
                 seq.getNextUnfinishedOrder(),
                 seq);
      kernel.setOrderSequenceFinishedIndex(seq.getReference(),
                                           seq.getFinishedIndex() + 1);
      // If the sequence is complete and this was its last order, the sequence
      // is also finished.
      if (seq.isComplete() && seq.getNextUnfinishedOrder() == null) {
        kernel.setOrderSequenceFinished(seq.getReference());
        // Reset the processing vehicle's back reference on the sequence.
        kernel.setVehicleOrderSequence(seq.getProcessingVehicle(), null);
      }
    }
    // A transport order has been finished - look for others for which all
    // dependencies have been finished now, mark them as dispatchable and put
    // them into the queue.
    kernel.getTCSObjects(TransportOrder.class).stream()
        .filter(o -> o.hasState(TransportOrder.State.ACTIVE))
        .filter(o -> !hasUnfinishedDependencies(o))
        .forEach(o -> {
          updateTransportOrderState(o.getReference(), TransportOrder.State.DISPATCHABLE);
          LOG.debug("Dispatching order {} which has just become dispatchable", o.getName());
          this.dispatch(o);
        });
  }

  /**
   * Properly sets a transport order to a failed state, setting related
   * properties.
   *
   * @param ref A reference to the transport order to be modified.
   * @throws ObjectUnknownException If the referenced order could not be found.
   */
  private void setTOStateFailed(TCSObjectReference<TransportOrder> ref) {
    requireNonNull(ref, "ref");

    TransportOrder failedOrder = kernel.getTCSObject(TransportOrder.class, ref);
    kernel.setTransportOrderState(ref, TransportOrder.State.FAILED);
    // A transport order has failed - check if it's part of an order
    // sequence that we need to take care of.
    if (failedOrder.getWrappingSequence() != null) {
      OrderSequence sequence = kernel.getTCSObject(OrderSequence.class,
                                                   failedOrder.getWrappingSequence());

      if (sequence.isFailureFatal()) {
        // Mark the sequence as complete to make sure no further orders are
        // added.
        kernel.setOrderSequenceComplete(sequence.getReference());
        // Mark all orders of the sequence that are not in a final state as
        // FAILED.
        sequence.getOrders().stream()
            .map(curRef -> kernel.getTCSObject(TransportOrder.class, curRef))
            .filter(o -> !o.getState().isFinalState())
            .forEach(o -> updateTransportOrderState(o.getReference(), TransportOrder.State.FAILED));
        // Move the finished index of the sequence to its end.
        kernel.setOrderSequenceFinishedIndex(sequence.getReference(),
                                             sequence.getOrders().size() - 1);
      }
      else {
        // Since failure of an order in the sequence is not fatal, increment the
        // finished index of the sequence by one to move to the next order.
        kernel.setOrderSequenceFinishedIndex(sequence.getReference(),
                                             sequence.getFinishedIndex() + 1);
      }
      // Mark the sequence as finished if there's nothing more to do in it.
      if (sequence.isComplete() && sequence.getNextUnfinishedOrder() == null) {
        kernel.setOrderSequenceFinished(sequence.getReference());
        // If the sequence was assigned to a vehicle, reset its back reference
        // on the sequence to make it available for orders again.
        if (sequence.getProcessingVehicle() != null) {
          kernel.setVehicleOrderSequence(sequence.getProcessingVehicle(), null);
        }
      }
    }
  }

  /**
   * Checks if a transport order's dependencies are completely satisfied or not.
   *
   * @param orderRef A reference to the transport order to be checked.
   * @return <code>false</code> if all the order's dependencies are finished (or
   * don't exist any more), else <code>true</code>.
   */
  private boolean hasUnfinishedDependencies(TransportOrder order) {
    requireNonNull(order, "order");

    // Assume that FINISHED orders do not have unfinished dependencies.
    if (order.hasState(TransportOrder.State.FINISHED)) {
      return false;
    }
    // Check if any transport order referenced as a an explicit dependency
    // (really still exists and) is not finished.
    if (order.getDependencies().stream()
        .map(depRef -> kernel.getTCSObject(TransportOrder.class, depRef))
        .anyMatch(dep -> dep != null && !dep.hasState(TransportOrder.State.FINISHED))) {
      return true;
    }

    // Check if the transport order is part of an order sequence and if yes,
    // if it's the next unfinished order in the sequence.
    if (order.getWrappingSequence() != null) {
      OrderSequence seq = kernel.getTCSObject(OrderSequence.class, order.getWrappingSequence());
      if (!order.getReference().equals(seq.getNextUnfinishedOrder())) {
        return true;
      }
    }
    // All referenced transport orders either don't exist (any more) or have
    // been finished already.
    return false;
  }

// Inner classes start here.
  /**
   * Annotation type for injecting whether to park when idle.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface ParkWhenIdle {
    // Nothing here.
  }

  /**
   * Annotation type for injecting whether to recharge when idle.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface RechargeWhenIdle {
    // Nothing here.
  }

  /**
   * Annotation type for injecting whether to recharge when energy is critical.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface RechargeWhenEnergyCritical {
    // Nothing here.
  }

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
        LOG.debug("Dispatchable content of unhandled class {}, ignored",
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
        updateTransportOrderState(order.getReference(), TransportOrder.State.UNROUTABLE);
      }
      else {
        LOG.info("Marking transport order {} as ACTIVE", order.getName());
        updateTransportOrderState(order.getReference(), TransportOrder.State.ACTIVE);
        // The transport order has been activated - dispatch it.
        // Check if it has unfinished dependencies.
        if (!hasUnfinishedDependencies(order)) {
          LOG.info("Marking transport order {} as DISPATCHABLE", order.getName());
          updateTransportOrderState(order.getReference(), TransportOrder.State.DISPATCHABLE);
        }
      }
    }

    private void dispatchTransportOrderDispatchable(TransportOrder order) {
      requireNonNull(order, "order");

      List<Vehicle> vehicles = findVehiclesForOrder(order);
      // Get the vehicle that needs the least time to process the
      // transport order.
      Vehicle closestVehicle = null;
      long closestCosts = Long.MAX_VALUE;
      List<DriveOrder> closestDriveOrders = null;
      for (Vehicle curVehicle : vehicles) {
        Point curPosition = kernel.getTCSObject(Point.class, curVehicle.getCurrentPosition());
        // Get a route for the vehicle, starting at it's current position.
        Optional<List<DriveOrder>> tmpDriveOrders = router.getRoute(curVehicle, curPosition, order);
        // Check if the vehicle can process the order right now.
        if (tmpDriveOrders.isPresent() && checkProcessability(curVehicle, order)) {
          long costs = 0;
          for (DriveOrder curDriveOrder : tmpDriveOrders.get()) {
            costs += curDriveOrder.getRoute().getCosts();
          }
          if (costs < closestCosts) {
            closestVehicle = curVehicle;
            closestDriveOrders = tmpDriveOrders.get();
            closestCosts = costs;
          }
        }
      }
      // If we found a vehicle that can process the transport order, assign the
      // order to it, store the computed route in the transport order, correct
      // the state of vehicle and transport order and start processing of the
      // order.
      if (closestVehicle != null) {
        // If the vehicle still has an order, just remember that the new order
        // is reserved for this vehicle and initiate abortion of the old one.
        // Once the abortion is complete, the vehicle will automatically be
        // re-dispatched and will then pick up the new order.
        if (closestVehicle.getTransportOrder() != null) {
          LOG.debug("Reserving {} for {} ", order, closestVehicle);
          // Remember that the new order is reserved for this vehicle.
          orderReservations.put(order.getReference(), closestVehicle.getReference());
          // Abort the vehicle's current order.
          abortOrder(closestVehicle, false, false, false);
        }
        else {
          // Make sure the vehicle is not in the queue any more before we
          // re-dispatch it.
          removeFromQueue(closestVehicle);
          assignTransportOrder(closestVehicle, order, closestDriveOrders);
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
      Set<TransportOrder> transportOrders = findOrdersForVehicle(vehicle);
      // If the result was null, the vehicle should not receive any order at
      // this point. Otherwise select one.
      if (transportOrders != null) {
        assignBestOrderToVehicle(vehicle,
                                 transportOrders,
                                 kernel.getTCSObjects(OrderSequence.class));
      }
      else {
        LOG.debug("{}: Suppressing order assignment.", vehicle.getName());
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
        updateTransportOrderState(vehicleOrderRef, TransportOrder.State.FINISHED);
        // Update the vehicle's procState, implicitly dispatching it again.
        kernel.setVehicleProcState(vehicleRef, Vehicle.ProcState.IDLE);
        kernel.setVehicleTransportOrder(vehicleRef, null);
        // Let the router know that the vehicle doesn't have a route any more.
        router.selectRoute(vehicle, null);
      }
      else {
        LOG.debug("{}: Assigning next drive order", vehicle.getName());
        // The vehicle is still processing a transport order. Get the next
        // drive order to be processed from the kernel (and not from our
        // possibly outdated copy of the transport order).
        DriveOrder currentDriveOrder = vehicleOrder.getCurrentDriveOrder();
        if (Assignments.mustAssign(currentDriveOrder, vehicle)) {
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
          clearOrderReservations(vehicle.getReference());
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
          updateTransportOrderState(order.getReference(),
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
        updateTransportOrderState(order.getReference(), TransportOrder.State.WITHDRAWN);
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
