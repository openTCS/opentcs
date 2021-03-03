/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.dispatching;

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
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.algorithms.Dispatcher;
import org.opentcs.algorithms.ParkingStrategy;
import org.opentcs.algorithms.RechargeStrategy;
import org.opentcs.algorithms.ResourceUser;
import org.opentcs.algorithms.Router;
import org.opentcs.algorithms.Scheduler;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.Processability;
import org.opentcs.drivers.VehicleController;
import org.opentcs.drivers.VehicleControllerPool;
import org.opentcs.util.QueueProcessor;

/**
 * Dispatches new transport orders to idle vehicles that need the least time to
 * process them.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class OrderSequenceDispatcher
    implements Dispatcher {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(OrderSequenceDispatcher.class.getName());
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * The Scheduler instance managing resources.
   */
  private final Scheduler scheduler;
  /**
   * The local kernel instance.
   */
  private final LocalKernel kernel;
  /**
   * The task doing the actual dispatching.
   */
  private final DispatcherTask dispatcherTask = new DispatcherTask();
  /**
   * A flag indiciating whether to park vehicles that have finished their order
   * and for which a new order is not available.
   */
  private final boolean parkIdleVehicles;
  /**
   * The strategy used for finding suitable parking positions.
   */
  private final ParkingStrategy parkingStrategy;
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
  private final RechargeStrategy rechargeStrategy;
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
   * @param newScheduler The <code>Scheduler</code> instance managing resources.
   * @param newKernel The local kernel instance.
   * @param parkingStrategy The strategy used for finding suitable parking
   * positions.
   * @param rechargeStrategy The strategy used for finding suitable recharge
   * locations.
   * @param parkIdleVehicles Whether to park vehicles that have finished their
   * order and for which a new order is not available.
   * @param rechargeVehiclesWhenIdle Whether to automatically create recharge
   * orders for vehicles that do not have an order.
   * @param rechargeVehiclesWhenEnergyCritical Whether to automatically create
   * recharge orders for vehicles whose energy level is critical.
   */
  @Inject
  public OrderSequenceDispatcher(Router newRouter,
                                 Scheduler newScheduler,
                                 LocalKernel newKernel,
                                 ParkingStrategy parkingStrategy,
                                 RechargeStrategy rechargeStrategy,
                                 @ParkWhenIdle boolean parkIdleVehicles,
                                 @RechargeWhenIdle boolean rechargeVehiclesWhenIdle,
                                 @RechargeWhenEnergyCritical boolean rechargeVehiclesWhenEnergyCritical) {
    log.finer("method entry");
    this.router = requireNonNull(newRouter, "newRouter is null");
    this.scheduler = requireNonNull(newScheduler, "newScheduler is null");
    this.kernel = requireNonNull(newKernel, "newKernel is null");
    this.parkingStrategy = requireNonNull(parkingStrategy, "parkingStrategy");
    this.rechargeStrategy = requireNonNull(rechargeStrategy, "rechargeStrategy");
    this.parkIdleVehicles = parkIdleVehicles;
    this.rechargeVehiclesWhenIdle = rechargeVehiclesWhenIdle;
    this.rechargeVehiclesWhenEnergyCritical = rechargeVehiclesWhenEnergyCritical;
  }

  // Methods declared in interface Dispatcher start here.
  @Override
  public void dispatch(Vehicle vehicle) {
    log.finer("method entry");
    Objects.requireNonNull(vehicle, "vehicle is null");

    // Check if this vehicle is interesting for the dispatcher at all.
    if (!vehicleDispatchable(vehicle)) {
      return;
    }
    // Insert the object into the dispatching task's queue and notify it.
    dispatcherTask.addToQueue(new Dispatchable(vehicle));
  }

  @Override
  public void dispatch(TransportOrder order) {
    log.finer("method entry");
    Objects.requireNonNull(order, "order is null");

    // We only dispatch orders that are actually dispatchable here.
    if (!order.hasState(TransportOrder.State.DISPATCHABLE)) {
      throw new IllegalArgumentException("order is not DISPATCHABLE but "
          + order.getState());
    }
    log.fine("Received order " + order
        + ". Wrapping sequence: " + order.getWrappingSequence()
        + ". Drive orders: " + order.getAllDriveOrders()
        + ". Intended vehicle: " + order.getIntendedVehicle()
        + ". Provided properties: " + order.getProperties());

    // Insert the object into the dispatching task's queue and notify it.
    dispatcherTask.addToQueue(new Dispatchable(order));
  }

  @Override
  public void withdrawOrder(Vehicle vehicle,
                            boolean disableVehicle) {
    log.finer("method entry");
    Objects.requireNonNull(vehicle, "vehicle is null");
    Withdrawal withdrawal = new Withdrawal(vehicle, disableVehicle);
    // XXX Check that the vehicle really does have an order that can be
    // withdrawn?
    dispatcherTask.addToQueue(new Dispatchable(withdrawal));
  }

  @Override
  public void initialize() {
    // Initialize the dispatching thread.
    Thread dispatcherThread = new Thread(dispatcherTask,
                                         getClass().getName() + "-DispatcherTask");
    dispatcherThread.start();
  }

  @Override
  public void terminate() {
    dispatcherTask.terminate();
  }

  @Override
  public synchronized String getInfo() {
    log.finer("method entry");
    return "";
  }

  // Class-specific methods start here.
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
    assert vehicle != null;
    assert transportOrders != null;
    assert sequences != null;

    final Point vehiclePosition = kernel.getTCSObject(Point.class,
                                                      vehicle.getCurrentPosition());
    // Assuming the transport orders are sorted correctly, we can now just
    // grab the first one that can be processed by the given vehicle.
    final Iterator<TransportOrder> orderIter = transportOrders.iterator();
    TransportOrder selectedOrder = null;
    List<DriveOrder> driveOrders = null;
    while (selectedOrder == null && orderIter.hasNext()) {
      TransportOrder curOrder = orderIter.next();
      boolean canProcess;
      // Get a route for the vehicle.
      driveOrders = router.getRoute(vehicle, vehiclePosition, curOrder);
      canProcess = driveOrders != null;
      if (!canProcess) {
        log.fine(vehicle.getName() + ": No route for order " + curOrder);
        Rejection rejection = new Rejection(vehicle.getReference(),
                                            "Unroutable");
        kernel.addTransportOrderRejection(curOrder.getReference(), rejection);
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
        log.fine(vehicle.getName() + ": Energy level critical, but vehicle is "
            + "already charging - leaving it alone.");
        orderCreated = true;
      }
      else {
        log.fine(vehicle.getName() + ": Energy level critical, "
            + "looking for recharge location.");
        orderCreated = rechargeVehicle(vehicle, vehiclePosition);
      }
    }

    if (!orderCreated) {
      // If we found a transport order, assign it to the vehicle.
      if (selectedOrder != null) {
        log.fine(vehicle.getName() + ": Selected transport order "
            + selectedOrder.getName() + " for assignment");
        assignTransportOrder(vehicle, selectedOrder, driveOrders);
        orderCreated = true;
      }
      else {
        log.fine(vehicle.getName() + ": Didn't find acceptable transport order "
            + "for assignment");
      }
    }
    // If the vehicle's energy level is not "good" any more, send it to a
    // charging station.
    if (!orderCreated
        && rechargeVehiclesWhenIdle
        && vehicle.isEnergyLevelDegraded()) {
      // If the vehicle is already charging, don't create a new order but leave
      // it like that.
      if (vehicle.hasState(Vehicle.State.CHARGING)) {
        log.fine(vehicle.getName() + ": No transport order, but vehicle is "
            + "already charging - leaving it alone.");
        orderCreated = true;
      }
      else {
        log.fine(vehicle.getName() + ": No transport order, looking for "
            + "recharge location.");
        orderCreated = rechargeVehicle(vehicle, vehiclePosition);
      }
    }
    // If auto parking is enabled and the vehicle's not at a parking position,
    // yet, send it to one.
    if (!orderCreated
        && parkIdleVehicles
        && !vehiclePosition.isParkingPosition()) {
      if (vehicle.hasState(Vehicle.State.CHARGING)
          && vehicle.getEnergyLevel() < 100) {
        log.fine(vehicle.getName() + ": Not at a parking position, but charging"
            + " and energy level < 100% - leaving it alone.");
        orderCreated = true;
      }
      else {
        log.fine(vehicle + ": Not at a parking position, looking for one.");
        orderCreated = parkVehicle(vehicle, vehiclePosition);
      }
    }
    log.fine(vehicle.getName() + ": orderCreated is " + orderCreated);
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
    assert vehicle != null;
    assert vehiclePosition != null;

    // Get a suitable parking position for the vehicle.
    Point parkPos = parkingStrategy.getParkingPosition(vehicle);
    log.fine("Parking position for " + vehicle + ": " + parkPos);
    // If we could not find a suitable parking position at all, just leave the
    // vehicle where it is.
    if (parkPos == null) {
      log.warning(vehicle.getName()
          + ": Did not find a suitable parking position.");
      return false;
    }
    // Wrap the name of the parking position in a dummy location reference.
    TCSObjectReference<Location> parkLocRef
        = TCSObjectReference.getDummyReference(Location.class,
                                               parkPos.getName());
    // Create a destination.
    Destination parkDest = new Destination(parkLocRef, Destination.OP_PARK);
    List<Destination> parkDests = Collections.singletonList(parkDest);
    // Create a transport order for parking and verify its processability.
    TransportOrder parkOrder = kernel.createTransportOrder(parkDests);
    kernel.setTransportOrderDispensable(parkOrder.getReference(), true);
    kernel.setTransportOrderIntendedVehicle(parkOrder.getReference(),
                                            vehicle.getReference());
    List<DriveOrder> driveOrders = router.getRoute(vehicle,
                                                   vehiclePosition,
                                                   parkOrder);
    if (checkProcessability(vehicle, parkOrder)) {
      // Assign the parking order.
      assignTransportOrder(vehicle, parkOrder, driveOrders);
      return true;
    }
    else {
      // Mark the order as failed, since the vehicle does not want to it.
      kernel.setTransportOrderState(parkOrder.getReference(),
                                    TransportOrder.State.FAILED);
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
  private boolean rechargeVehicle(Vehicle vehicle,
                                  Point vehiclePosition) {
    assert vehicle != null;
    assert vehiclePosition != null;

    Location rechargeLoc = rechargeStrategy.getRechargeLocation(vehicle);
    if (rechargeLoc == null) {
      log.warning(vehicle.getName()
          + ": Did not find a suitable recharge location providing operation "
          + vehicle.getRechargeOperation());
      return false;
    }
    Map<String, String> dstProps = new HashMap<>();
    // XXX Set recharge intensity via Destination properties here.
    // dstProps.put("tcs:rechargeIntensity", );
    Destination dst = new Destination(rechargeLoc.getReference(),
                                      vehicle.getRechargeOperation(),
                                      dstProps);
    // Create a transport order for recharging and verify its processability.
    TransportOrder rechargeOrder
        = kernel.createTransportOrder(Collections.singletonList(dst));
    // The recharge order may be withdrawn unless its energy level is critical.
    if (!vehicle.isEnergyLevelCritical()) {
      kernel.setTransportOrderDispensable(rechargeOrder.getReference(), true);
    }
    kernel.setTransportOrderIntendedVehicle(rechargeOrder.getReference(),
                                            vehicle.getReference());
    List<DriveOrder> driveOrders = router.getRoute(vehicle,
                                                   vehiclePosition,
                                                   rechargeOrder);
    if (checkProcessability(vehicle, rechargeOrder)) {
      // Assign the recharge order.
      assignTransportOrder(vehicle, rechargeOrder, driveOrders);
      return true;
    }
    else {
      // Mark the order as failed, since the vehicle does not want to it.
      kernel.setTransportOrderState(rechargeOrder.getReference(),
                                    TransportOrder.State.FAILED);
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
    log.finer("method entry");
    assert vehicle != null;
    assert transportOrder != null;
    assert driveOrders != null;

    log.fine("Assigning vehicle " + vehicle.getName() + " to order "
        + transportOrder.getName());
    final TCSObjectReference<Vehicle> vehicleRef = vehicle.getReference();
    final TCSObjectReference<TransportOrder> orderRef
        = transportOrder.getReference();
    // If the transport order was reserved, forget the reservation now.
    orderReservations.remove(orderRef);
    // Set the vehicle's and transport order's state.
    kernel.setVehicleProcState(vehicleRef,
                               Vehicle.ProcState.PROCESSING_ORDER);
    kernel.setTransportOrderState(orderRef,
                                  TransportOrder.State.BEING_PROCESSED);
    // Add cross references between vehicle and transport order/order sequence.
    kernel.setVehicleTransportOrder(vehicleRef, orderRef);
    if (transportOrder.getWrappingSequence() != null) {
      kernel.setVehicleOrderSequence(vehicleRef,
                                     transportOrder.getWrappingSequence());
      kernel.setOrderSequenceProcessingVehicle(
          transportOrder.getWrappingSequence(),
          vehicleRef);
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
      VehicleController vehicleController
          = vehicleControllerPool().getVehicleController(vehicle.getName());
      scheduler.setRoute((ResourceUser) vehicleController,
                         driveOrder.getRoute());
      vehicleController.setDriveOrder(driveOrder,
                                      transportOrder.getProperties());
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
    assert vehicle != null;
    assert order != null;
    final VehicleController vehicleController
        = vehicleControllerPool().getVehicleController(vehicle.getName());
    // If there isn't any vehicle controller for this vehicle, it cannot process
    // the order.
    if (vehicleController == null) {
      // XXX Should we add a rejection in this case, too?
      return false;
    }
    Processability result
        = vehicleController.canProcess(Assignments.getOperations(order));
    if (result.isCanProcess()) {
      return true;
    }
    else {
      // The vehicle controller/communication adapter does not want to process
      // the order. Add a rejection for it.
      Rejection rejection
          = new Rejection(vehicle.getReference(), result.getReason());
      log.fine("Order " + order.getName()
          + " rejected by " + vehicle.getName()
          + ", reason: " + rejection.getReason());
      kernel.addTransportOrderRejection(order.getReference(), rejection);
      return false;
    }
  }

  private static boolean vehicleDispatchable(Vehicle vehicle) {
    assert vehicle != null;

    if (vehicle.getCurrentPosition() == null) {
      log.fine(vehicle.getName() + ": unknown position -> not dispatchable");
      return false;
    }
    // ProcState IDLE, State CHARGING and energy level not high enough? Then let
    // it charge a bit longer.
    if (vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && vehicle.hasState(Vehicle.State.CHARGING)
        && vehicle.isEnergyLevelCritical()) {
      log.fine(vehicle.getName() + ": state is CHARGING, energy level "
          + vehicle.getEnergyLevel() + "<=" + vehicle.getEnergyLevelCritical()
          + " -> not (yet) dispatchable");
      return false;
    }
    if (!vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && !vehicle.hasProcState(Vehicle.ProcState.AWAITING_ORDER)) {
      log.fine(vehicle.getName() + ": procState is " + vehicle.getProcState()
          + " -> not dispatchable");
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
    assert vehicle != null;

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
   * Returns available vehicles for the given order.
   *
   * @param order The order for which to find available vehicles.
   * @return Available vehicles for the given order.
   */
  private List<Vehicle> getVehiclesForOrder(TransportOrder order) {
    assert order != null;

    List<Vehicle> result = new LinkedList<>();
    // Check if the order or its wrapping sequence have an intended vehicle.
    TCSObjectReference<Vehicle> vRefIntended = null;
    // If the order belongs to an order sequence, check if a vehicle is already
    // processing it or, if not, if the sequence is intended for a specific
    // vehicle.
    if (order.getWrappingSequence() != null) {
      OrderSequence seq = kernel.getTCSObject(OrderSequence.class,
                                              order.getWrappingSequence());
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
      Vehicle intendedVehicle = kernel.getTCSObject(Vehicle.class,
                                                    vRefIntended);
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

  private Set<TransportOrder> getOrderForVehicle(Vehicle vehicle) {
    assert vehicle != null;

    // Check if there's an order reserved for this vehicle. If yes, return that.
    for (Map.Entry<TCSObjectReference<TransportOrder>, TCSObjectReference<Vehicle>> curEntry : orderReservations.entrySet()) {
      if (vehicle.getReference().equals(curEntry.getValue())) {
        TransportOrder order = kernel.getTCSObject(TransportOrder.class,
                                                   curEntry.getKey());
        return Collections.singleton(order);
      }
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

  private void clearOrderReservations(TCSObjectReference<Vehicle> vehicleRef) {
    assert vehicleRef != null;

    Iterator<Map.Entry<TCSObjectReference<TransportOrder>, TCSObjectReference<Vehicle>>> iter
        = orderReservations.entrySet().iterator();
    while (iter.hasNext()) {
      if (vehicleRef.equals(iter.next().getValue())) {
        iter.remove();
      }
    }
  }

  private void finishAbortion(TCSObjectReference<TransportOrder> orderRef,
                              Vehicle vehicle,
                              boolean disableVehicle) {
    assert orderRef != null;
    assert vehicle != null;

    // The current transport order has been aborted - update its state
    // and that of the vehicle.
    kernel.setTransportOrderState(orderRef,
                                  TransportOrder.State.FAILED);
    // Check if we're supposed to disable the vehicle and set its proc
    // state accordingly.
    if (disableVehicle) {
      kernel.setVehicleProcState(vehicle.getReference(),
                                 Vehicle.ProcState.UNAVAILABLE);
      vehiclesToDisable.remove(vehicle.getReference());
    }
    else {
      kernel.setVehicleProcState(vehicle.getReference(),
                                 Vehicle.ProcState.IDLE);
    }
    kernel.setVehicleTransportOrder(vehicle.getReference(), null);
    // Let the router know that the vehicle doesn't have a route any more.
    router.selectRoute(vehicle, null);

  }

  /**
   * Returns the kernel's vehicle controller pool.
   *
   * @return The kernel's vehicle controller pool.
   */
  private VehicleControllerPool vehicleControllerPool() {
    return kernel.getVehicleControllerPool();
  }

// Inner classes start here.
  /**
   * Annotation type for injecting whether to park when idle.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  static @interface ParkWhenIdle {
    // Nothing here.
  }

  /**
   * Annotation type for injecting whether to recharge when idle.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  static @interface RechargeWhenIdle {
    // Nothing here.
  }

  /**
   * Annotation type for injecting whether to recharge when energy is critical.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  static @interface RechargeWhenEnergyCritical {
    // Nothing here.
  }

  /**
   * The task doing the actual dispatching of transport orders and vehicles.
   */
  private class DispatcherTask
      extends QueueProcessor<Dispatchable> {

    /**
     * Creates a new DispatcherTask.
     */
    DispatcherTask() {
    }

    @Override
    protected void processQueueElement(Dispatchable dispatchable) {
      if (dispatchable == null) {
        log.warning("dispatchable is null, ignored");
      }
      else if (dispatchable.getDispatchable() instanceof TransportOrder) {
        dispatchTransportOrder((TransportOrder) dispatchable.getDispatchable());
      }
      else if (dispatchable.getDispatchable() instanceof Vehicle) {
        dispatchVehicle((Vehicle) dispatchable.getDispatchable());
      }
      else if (dispatchable.getDispatchable() instanceof Withdrawal) {
        abortOrder((Withdrawal) dispatchable.getDispatchable());
      }
      // If dispatchable is null, we've been terminated or the queue was
      // empty. If dispatchable is of an unhandled subtype, we just ignore
      // it.
      else {
        log.warning("Dispatchable content null or of unhandled class, ignored");
      }
    }

    /**
     * Tries to dispatch a transport order by finding a vehicle to process it.
     *
     * @param order The transport order to be dispatched.
     */
    private void dispatchTransportOrder(TransportOrder order) {
      log.finer("method entry");
      assert order != null;
      if (!order.getState().equals(TransportOrder.State.DISPATCHABLE)) {
        log.warning("Transport order " + order.getName()
            + " not dispatchable (any more?), ignored");
        return;
      }
      List<Vehicle> vehicles = getVehiclesForOrder(order);
      // Get the vehicle that needs the least time to process the
      // transport order.
      Vehicle closestVehicle = null;
      long closestCosts = Long.MAX_VALUE;
      List<DriveOrder> closestDriveOrders = null;
      for (Vehicle curVehicle : vehicles) {
        Point curPosition = kernel.getTCSObject(Point.class,
                                                curVehicle.getCurrentPosition());
        // Get a route for the vehicle, starting at it's current position.
        List<DriveOrder> tmpDriveOrders = router.getRoute(curVehicle,
                                                          curPosition,
                                                          order);
        // Check if the vehicle can process the order right now.
        if (tmpDriveOrders != null && checkProcessability(curVehicle, order)) {
          long costs = 0;
          for (DriveOrder curDriveOrder : tmpDriveOrders) {
            costs += curDriveOrder.getRoute().getCosts();
          }
          if (costs < closestCosts) {
            closestVehicle = curVehicle;
            closestDriveOrders = tmpDriveOrders;
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
          log.fine("Reserving " + order + " for " + closestVehicle);
          // Remember that the new order is reserved for this vehicle.
          orderReservations.put(order.getReference(),
                                closestVehicle.getReference());
          // Abort the vehicle's current order.
          abortOrder(closestVehicle, false);
        }
        else {
          // Make sure the vehicle is not in the queue any more before we
          // re-dispatch it.
          removeFromQueue(new Dispatchable(closestVehicle));
          assignTransportOrder(closestVehicle, order, closestDriveOrders);
        }
      }
      else {
        log.fine("Did not find a vehicle to dispatch order to.");
      }
    }

    /**
     * Tries to dispatch a vehicle by finding a transport order to be processed
     * by it.
     *
     * @param vehicle The vehicle to be dispatched.
     */
    private void dispatchVehicle(Vehicle vehicle) {
      log.finer("method entry");
      assert vehicle != null;
      // - If ProcState.IDLE, look for a transport order to assign
      // - If ProcState.AWAITING_ORDER, drive order is finished
      //   - If whole transport order is finished, set ProcState.IDLE, which
      //     will dispatch the vehicle again automatically.

      // Filter undispatchable vehicles first.
      if (!vehicleDispatchable(vehicle)) {
        return;
      }

      // Is the vehicle idle/without a transport order?
      if (vehicle.hasProcState(Vehicle.ProcState.IDLE)) {
        log.fine(vehicle.getName() + ": IDLE, looking for a transport order");
        // The vehicle is not processing a transport order - try to find one for
        // it.
        Set<TransportOrder> transportOrders = getOrderForVehicle(vehicle);
        // If the result was null, the vehicle should not receive any order at
        // this point. Otherwise select one.
        if (transportOrders != null) {
          assignBestOrderToVehicle(vehicle,
                                   transportOrders,
                                   kernel.getTCSObjects(OrderSequence.class));
        }
        else {
          log.fine(vehicle.getName() + ": Suppressing order assignment.");
        }
      }
      // Is the vehicle waiting for the next drive order?
      else if (vehicle.hasProcState(Vehicle.ProcState.AWAITING_ORDER)) {
        log.fine(vehicle.getName() + ": Finished a drive order");
        final TCSObjectReference<Vehicle> vehicleRef = vehicle.getReference();
        final TCSObjectReference<TransportOrder> vehicleOrderRef
            = vehicle.getTransportOrder();
        // The vehicle is processing a transport order and has finished a drive
        // order. See if we're coping with a withdrawal or, if not, if there's
        // another drive order to be processed.
        kernel.setTransportOrderNextDriveOrder(vehicleOrderRef);
        TransportOrder vehicleOrder = kernel.getTCSObject(TransportOrder.class,
                                                          vehicleOrderRef);

        if (vehicleOrder.hasState(TransportOrder.State.WITHDRAWN)) {
          log.fine(vehicle.getName() + ": Aborted order " + vehicleOrder
              + ". Wrapping sequence: " + vehicleOrder.getWrappingSequence()
              + ". Drive orders: " + vehicleOrder.getAllDriveOrders()
              + ". Intended vehicle: " + vehicleOrder.getIntendedVehicle()
              + ". Provided properties: " + vehicleOrder.getProperties());
          finishAbortion(vehicleOrderRef,
                         vehicle,
                         vehiclesToDisable.contains(vehicleRef));
        }
        else if (vehicleOrder.getCurrentDriveOrder() == null) {
          log.fine(vehicle.getName() + ": Finished order " + vehicleOrder
              + ". Wrapping sequence: " + vehicleOrder.getWrappingSequence()
              + ". Drive orders: " + vehicleOrder.getAllDriveOrders()
              + ". Intended vehicle: " + vehicleOrder.getIntendedVehicle()
              + ". Provided properties: " + vehicleOrder.getProperties());
          // The current transport order has been finished - update its state
          // and that of the vehicle.
          kernel.setTransportOrderState(vehicleOrderRef,
                                        TransportOrder.State.FINISHED);
          // Update the vehicle's procState, implicitly dispatching it again.
          kernel.setVehicleProcState(vehicleRef, Vehicle.ProcState.IDLE);
          kernel.setVehicleTransportOrder(vehicleRef, null);
          // Let the router know that the vehicle doesn't have a route any more.
          router.selectRoute(vehicle, null);
        }
        else {
          log.fine(vehicle.getName() + ": Assigning next drive order");
          // The vehicle is still processing a transport order. Get the next
          // drive order to be processed from the kernel (and not from our
          // possibly outdated copy of the transport order).
          TransportOrder transportOrder
              = kernel.getTCSObject(TransportOrder.class, vehicleOrderRef);
          DriveOrder currentDriveOrder = transportOrder.getCurrentDriveOrder();
          // If the drive order must be assigned, do so.
          if (Assignments.mustAssign(currentDriveOrder, vehicle)) {
            // Let the vehicle controller know about the new drive order.
            VehicleController vehicleController
                = vehicleControllerPool().getVehicleController(vehicle.getName());
            scheduler.setRoute((ResourceUser) vehicleController,
                               currentDriveOrder.getRoute());
            vehicleController.setDriveOrder(currentDriveOrder,
                                            transportOrder.getProperties());
          }
          // If the drive order need not be assigned, let the kernel know that
          // the vehicle is waiting for its next order - it will be dispatched
          // again for the next drive order, then.
          else {
            kernel.setVehicleProcState(vehicleRef,
                                       Vehicle.ProcState.AWAITING_ORDER);
          }
        }
      }
      // If the vehicle is in any other processing state, it shouldn't be here.
      else {
        assert false;
      }
    } // void dispatchVehicle()

    /**
     * Abort an order processed by a vehicle.
     *
     * @param withdrawal The object describing the withdrawal.
     */
    private void abortOrder(Withdrawal withdrawal) {
      log.finer("method entry");
      assert withdrawal != null;
      abortOrder(withdrawal.getVehicle(), withdrawal.getDisableVehicle());
    } // method abortOrder(Withdrawal)

    /**
     * Abort an order processed by a vehicle.
     *
     * @param vehicle The vehicle which should abort its order.
     * @param disableVehicle Whether to disable the vehicle, i.e. set its
     * procState to UNAVAILABLE.
     */
    private void abortOrder(Vehicle vehicle, boolean disableVehicle) {
      log.finer("method entry");
      TCSObjectReference<TransportOrder> orderRef = vehicle.getTransportOrder();

      // If the vehicle does NOT have an order, update its processing state now.
      if (orderRef == null) {
        if (disableVehicle) {
          kernel.setVehicleProcState(vehicle.getReference(),
                                     Vehicle.ProcState.UNAVAILABLE);
          // Since the vehicle is now disabled, release any order reservations
          // for it, too. Disabled vehicles should not keep reservations, and
          // this is a good fallback trigger to get rid of them in general.
          clearOrderReservations(vehicle.getReference());
        }
        else {
          kernel.setVehicleProcState(vehicle.getReference(),
                                     Vehicle.ProcState.IDLE);
        }
        return;
      }
      VehicleController vehicleController
          = vehicleControllerPool().getVehicleController(vehicle.getName());
      // Mark the order as withdrawn so we can react appropriately when the
      // vehicle reports the remaining movements as finished
      if (orderRef != null) {
        TransportOrder order = kernel.getTCSObject(TransportOrder.class,
                                                   orderRef);
        if (order.hasState(TransportOrder.State.WITHDRAWN)) {
          log.info(vehicle.getName()
              + ": Transport order already WITHDRAWN, immediate abort...");
          if (vehicleController != null) {
            vehicleController.setDriveOrder(null, null);
            vehicleController.clearCommandQueue();
          }
          finishAbortion(orderRef, vehicle, disableVehicle);
          return;
        }
        else {
          kernel.setTransportOrderState(orderRef,
                                        TransportOrder.State.WITHDRAWN);
        }
      }
      // vehicle has aborted the order.
      if (disableVehicle) {
        log.fine(vehicle.getName() + ": To be disabled later");
        vehiclesToDisable.add(vehicle.getReference());
      }
      if (vehicleController != null) {
        vehicleController.abortDriveOrder();
        // XXX What if the controller does not have any more movements to be
        // finished? Will it ever re-dispatch the vehicle in that case?
      }
      else {
        log.warning(vehicle.getName() + ": No vehicle controller attached");
      }
    }
  } // class DispatcherTask
}
