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
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.VehicleController;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides service functions for working with transport orders and their states.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderUtil
    implements Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TransportOrderUtil.class);
  /**
   * The transport order service.
   */
  private final InternalTransportOrderService transportOrderService;
  /**
   * The vehicle service.
   */
  private final InternalVehicleService vehicleService;
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * The vehicle controller pool.
   */
  private final VehicleControllerPool vehicleControllerPool;
  /**
   * Stores reservations of transport orders for vehicles.
   */
  private final OrderReservationPool orderReservationPool;
  /**
   * This class's configuration.
   */
  private final DefaultDispatcherConfiguration configuration;
  /**
   * A list of vehicles that are to be disabled/made UNAVAILABLE after they have
   * finished/aborted their current transport orders.
   */
  private final Set<TCSObjectReference<Vehicle>> vehiclesToDisable = ConcurrentHashMap.newKeySet();
  /**
   * Whether this instance is initialized.
   */
  private boolean initialized;

  @Inject
  public TransportOrderUtil(@Nonnull InternalTransportOrderService transportOrderService,
                            @Nonnull InternalVehicleService vehicleService,
                            @Nonnull DefaultDispatcherConfiguration configuration,
                            @Nonnull Router router,
                            @Nonnull VehicleControllerPool vehicleControllerPool,
                            @Nonnull OrderReservationPool orderReservationPool) {
    this.transportOrderService = requireNonNull(transportOrderService, "transportOrderService");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.router = requireNonNull(router, "router");
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    vehiclesToDisable.clear();

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    initialized = false;
  }

  /**
   * Checks if a transport order's dependencies are completely satisfied or not.
   *
   * @param order A reference to the transport order to be checked.
   * @return <code>false</code> if all the order's dependencies are finished (or
   * don't exist any more), else <code>true</code>.
   */
  public boolean hasUnfinishedDependencies(TransportOrder order) {
    requireNonNull(order, "order");

    // Assume that FINISHED orders do not have unfinished dependencies.
    if (order.hasState(TransportOrder.State.FINISHED)) {
      return false;
    }
    // Check if any transport order referenced as a an explicit dependency
    // (really still exists and) is not finished.
    if (order.getDependencies().stream()
        .map(depRef -> transportOrderService.fetchObject(TransportOrder.class, depRef))
        .anyMatch(dep -> dep != null && !dep.hasState(TransportOrder.State.FINISHED))) {
      return true;
    }

    // Check if the transport order is part of an order sequence and if yes,
    // if it's the next unfinished order in the sequence.
    if (order.getWrappingSequence() != null) {
      OrderSequence seq = transportOrderService.fetchObject(OrderSequence.class,
                                                            order.getWrappingSequence());
      if (!order.getReference().equals(seq.getNextUnfinishedOrder())) {
        return true;
      }
    }
    // All referenced transport orders either don't exist (any more) or have
    // been finished already.
    return false;
  }

  /**
   * Finds transport orders that are ACTIVE and do not have any unfinished dependencies (any more),
   * marking them as DISPATCHABLE.
   */
  public void markNewDispatchableOrders() {
    transportOrderService.fetchObjects(TransportOrder.class).stream()
        .filter(order -> order.hasState(TransportOrder.State.ACTIVE))
        .filter(order -> !hasUnfinishedDependencies(order))
        .forEach(order -> updateTransportOrderState(order.getReference(),
                                                    TransportOrder.State.DISPATCHABLE));
  }

  public void updateTransportOrderState(@Nonnull TCSObjectReference<TransportOrder> ref,
                                        @Nonnull TransportOrder.State newState) {
    requireNonNull(ref, "ref");
    requireNonNull(newState, "newState");

    LOG.info("Updating state of transport order {} to {}...", ref.getName(), newState);
    switch (newState) {
      case FINISHED:
        setTOStateFinished(ref);
        break;
      case FAILED:
        setTOStateFailed(ref);
        break;
      default:
        // Set the transport order's state.
        transportOrderService.updateTransportOrderState(ref, newState);
    }
  }

  /**
   * Assigns a transport order to a vehicle, stores a route for the vehicle in
   * the transport order, adjusts the state of vehicle and transport order
   * and starts processing.
   *
   * @param vehicle The vehicle that is supposed to process the transport order.
   * @param transportOrder The transport order to be processed.
   * @param driveOrders The list of drive orders describing the route for the vehicle.
   */
  public void assignTransportOrder(Vehicle vehicle,
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
    vehicleService.updateVehicleProcState(vehicleRef, Vehicle.ProcState.PROCESSING_ORDER);
    updateTransportOrderState(orderRef, TransportOrder.State.BEING_PROCESSED);
    // Add cross references between vehicle and transport order/order sequence.
    vehicleService.updateVehicleTransportOrder(vehicleRef, orderRef);
    if (transportOrder.getWrappingSequence() != null) {
      vehicleService.updateVehicleOrderSequence(vehicleRef, transportOrder.getWrappingSequence());
      transportOrderService
          .updateOrderSequenceProcessingVehicle(transportOrder.getWrappingSequence(), vehicleRef);
    }
    transportOrderService.updateTransportOrderProcessingVehicle(orderRef, vehicleRef, driveOrders);
    // Let the router know about the route chosen.
    router.selectRoute(vehicle, Collections.unmodifiableList(driveOrders));
    // Update the transport order's copy.
    transportOrder = transportOrderService.fetchObject(TransportOrder.class, orderRef);
    DriveOrder driveOrder = transportOrder.getCurrentDriveOrder();
    // If the drive order must be assigned, do so.
    if (mustAssign(driveOrder, vehicle)) {
      // Let the vehicle controller know about the first drive order.
      vehicleControllerPool.getVehicleController(vehicle.getName())
          .setDriveOrder(driveOrder, transportOrder.getProperties());
    }
    // If the drive order need not be assigned, let the kernel know that the
    // vehicle is waiting for its next order - it will be dispatched again for
    // the next drive order, then.
    else {
      vehicleService.updateVehicleProcState(vehicleRef, Vehicle.ProcState.AWAITING_ORDER);
    }
  } // void assignTransportOrder()

  /**
   * Checks if the given drive order must be processed or could/should be left out.
   * Orders that should be left out are those with destinations at which the
   * vehicle is already present and which require no destination operation.
   *
   * @param driveOrder The drive order to be processed.
   * @param vehicle The vehicle that would process the order.
   * @return <code>true</code> if, and only if, the given drive order must be
   * processed; <code>false</code> if the order should/must be left out.
   */
  public boolean mustAssign(DriveOrder driveOrder, Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    // Removing a vehicle's drive order is always allowed.
    if (driveOrder == null) {
      return true;
    }
    // Check if all orders are to be assigned.
    if (configuration.assignRedundantOrders()) {
      return true;
    }
    Point destPoint = driveOrder.getRoute().getFinalDestinationPoint();
    String destOp = driveOrder.getDestination().getOperation();
    // We use startsWith(OP_NOP) here because that makes it possible to have
    // multiple different operations ("NOP.*") that all do nothing.
    if (destPoint.getReference().equals(vehicle.getCurrentPosition())
        && (destOp.startsWith(DriveOrder.Destination.OP_NOP)
            || destOp.equals(DriveOrder.Destination.OP_MOVE))) {
      return false;
    }
    return true;
  }

  public void finishAbortion(Vehicle vehicle) {
    finishAbortion(vehicle.getTransportOrder(),
                   vehicle,
                   vehiclesToDisable.contains(vehicle.getReference()));
  }

  private void finishAbortion(TCSObjectReference<TransportOrder> orderRef,
                              Vehicle vehicle,
                              boolean disableVehicle) {
    requireNonNull(orderRef, "orderRef");
    requireNonNull(vehicle, "vehicle");

    LOG.debug("{}: Aborted order {}", vehicle.getName(), orderRef.getName());

    // The current transport order has been aborted - update its state
    // and that of the vehicle.
    updateTransportOrderState(orderRef, TransportOrder.State.FAILED);
    // Check if we're supposed to disable the vehicle and set its proc
    // state accordingly.
    if (disableVehicle) {
      vehicleService.updateVehicleProcState(vehicle.getReference(), Vehicle.ProcState.UNAVAILABLE);
      vehicleService.updateVehicleIntegrationLevel(vehicle.getReference(),
                                                   Vehicle.IntegrationLevel.TO_BE_RESPECTED);
      vehiclesToDisable.remove(vehicle.getReference());
    }
    else {
      vehicleService.updateVehicleProcState(vehicle.getReference(), Vehicle.ProcState.IDLE);
    }
    vehicleService.updateVehicleTransportOrder(vehicle.getReference(), null);
    // Let the router know that the vehicle doesn't have a route any more.
    router.selectRoute(vehicle, null);
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
  public void abortOrder(Vehicle vehicle,
                         boolean immediateAbort,
                         boolean disableVehicle,
                         boolean resetVehiclePosition) {
    TCSObjectReference<TransportOrder> orderRef = vehicle.getTransportOrder();

    // If the vehicle does NOT have an order, update its processing state now.
    if (orderRef == null) {
      if (disableVehicle) {
        vehicleService.updateVehicleProcState(vehicle.getReference(), Vehicle.ProcState.UNAVAILABLE);
        vehicleService.updateVehicleIntegrationLevel(vehicle.getReference(),
                                                     Vehicle.IntegrationLevel.TO_BE_RESPECTED);
        // Since the vehicle is now disabled, release any order reservations
        // for it, too. Disabled vehicles should not keep reservations, and
        // this is a good fallback trigger to get rid of them in general.
        orderReservationPool.removeReservations(vehicle.getReference());
      }
      else {
        vehicleService.updateVehicleProcState(vehicle.getReference(), Vehicle.ProcState.IDLE);
      }
    }
    else {
      abortAssignedOrder(transportOrderService.fetchObject(TransportOrder.class, orderRef),
                         vehicle,
                         immediateAbort,
                         disableVehicle);
    }
    // If requested, reset the vehicle position to null and free all resources.
    if (immediateAbort && resetVehiclePosition) {
      vehicleService.updateVehicleIntegrationLevel(vehicle.getReference(),
                                                   Vehicle.IntegrationLevel.TO_BE_IGNORED);
    }
  }

  public void abortOrder(TransportOrder order,
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
                         vehicleService.fetchObject(Vehicle.class, vehicleRef),
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
      updateTransportOrderState(order.getReference(),
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
    transportOrderService.updateTransportOrderState(ref, TransportOrder.State.FINISHED);
    TransportOrder order = transportOrderService.fetchObject(TransportOrder.class, ref);
    // If it is part of an order sequence, we should proceed to its next order.
    if (order.getWrappingSequence() != null) {
      OrderSequence seq = transportOrderService.fetchObject(OrderSequence.class,
                                                            order.getWrappingSequence());
      // Sanity check: The finished order must be the next one in the sequence;
      // if it is not, something has already gone wrong.
      checkState(ref.equals(seq.getNextUnfinishedOrder()),
                 "Finished TO %s != next unfinished TO %s in sequence %s",
                 ref,
                 seq.getNextUnfinishedOrder(),
                 seq);
      transportOrderService.updateOrderSequenceFinishedIndex(seq.getReference(),
                                                             seq.getFinishedIndex() + 1);
      // Get an up-to-date copy of the order sequence
      seq = transportOrderService.fetchObject(OrderSequence.class, seq.getReference());
      // If the sequence is complete and this was its last order, the sequence
      // is also finished.
      if (seq.isComplete() && seq.getNextUnfinishedOrder() == null) {
        transportOrderService.markOrderSequenceFinished(seq.getReference());
        // Reset the processing vehicle's back reference on the sequence.
        vehicleService.updateVehicleOrderSequence(seq.getProcessingVehicle(), null);
      }
    }
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

    TransportOrder failedOrder = transportOrderService.fetchObject(TransportOrder.class, ref);
    transportOrderService.updateTransportOrderState(ref, TransportOrder.State.FAILED);
    // A transport order has failed - check if it's part of an order
    // sequence that we need to take care of.
    if (failedOrder.getWrappingSequence() == null) {
      return;
    }
    OrderSequence sequence = transportOrderService.fetchObject(OrderSequence.class,
                                                               failedOrder.getWrappingSequence());

    if (sequence.isFailureFatal()) {
      // Mark the sequence as complete to make sure no further orders are
      // added.
      transportOrderService.markOrderSequenceComplete(sequence.getReference());
      // Mark all orders of the sequence that are not in a final state as
      // FAILED.
      sequence.getOrders().stream()
          .map(curRef -> transportOrderService.fetchObject(TransportOrder.class, curRef))
          .filter(o -> !o.getState().isFinalState())
          .forEach(o -> updateTransportOrderState(o.getReference(), TransportOrder.State.FAILED));
      // Move the finished index of the sequence to its end.
      transportOrderService.updateOrderSequenceFinishedIndex(sequence.getReference(),
                                                             sequence.getOrders().size() - 1);
    }
    else {
      // Since failure of an order in the sequence is not fatal, increment the
      // finished index of the sequence by one to move to the next order.
      transportOrderService.updateOrderSequenceFinishedIndex(sequence.getReference(),
                                                             sequence.getFinishedIndex() + 1);
    }
    // The sequence may have changed. Get an up-to-date copy.
    sequence = transportOrderService.fetchObject(OrderSequence.class, failedOrder.getWrappingSequence());
    // Mark the sequence as finished if there's nothing more to do in it.
    if (sequence.isComplete() && sequence.getNextUnfinishedOrder() == null) {
      transportOrderService.markOrderSequenceFinished(sequence.getReference());
      // If the sequence was assigned to a vehicle, reset its back reference
      // on the sequence to make it available for orders again.
      if (sequence.getProcessingVehicle() != null) {
        vehicleService.updateVehicleOrderSequence(sequence.getProcessingVehicle(), null);
      }
    }
  }
}
