// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.InternalVehicleService;
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
 */
public class TransportOrderUtil
    implements
      Lifecycle {

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
   * This class's configuration.
   */
  private final DefaultDispatcherConfiguration configuration;
  /**
   * Whether this instance is initialized.
   */
  private boolean initialized;

  @Inject
  public TransportOrderUtil(
      @Nonnull
      InternalTransportOrderService transportOrderService,
      @Nonnull
      InternalVehicleService vehicleService,
      @Nonnull
      DefaultDispatcherConfiguration configuration,
      @Nonnull
      Router router,
      @Nonnull
      VehicleControllerPool vehicleControllerPool
  ) {
    this.transportOrderService = requireNonNull(transportOrderService, "transportOrderService");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.router = requireNonNull(router, "router");
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

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
      OrderSequence seq = transportOrderService.fetchObject(
          OrderSequence.class,
          order.getWrappingSequence()
      );
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
        .forEach(
            order -> updateTransportOrderState(
                order.getReference(),
                TransportOrder.State.DISPATCHABLE
            )
        );
  }

  public void updateTransportOrderState(
      @Nonnull
      TCSObjectReference<TransportOrder> ref,
      @Nonnull
      TransportOrder.State newState
  ) {
    requireNonNull(ref, "ref");
    requireNonNull(newState, "newState");

    LOG.debug("Updating state of transport order {} to {}...", ref.getName(), newState);
    switch (newState) {
      case FINISHED:
        markOrderAndSequenceAsFinished(ref);
        break;
      case FAILED:
        transportOrderService.updateTransportOrderState(ref, TransportOrder.State.FAILED);
        updateSequenceOfFailedTransportOrder(ref);
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
  public void assignTransportOrder(
      Vehicle vehicle,
      TransportOrder transportOrder,
      List<DriveOrder> driveOrders
  ) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(transportOrder, "transportOrder");
    requireNonNull(driveOrders, "driveOrders");

    LOG.debug("Assigning vehicle {} to order {}.", vehicle.getName(), transportOrder.getName());
    final TCSObjectReference<Vehicle> vehicleRef = vehicle.getReference();
    final TCSObjectReference<TransportOrder> orderRef = transportOrder.getReference();
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
    // Update the transport order's copy.
    TransportOrder updatedOrder = transportOrderService.fetchObject(TransportOrder.class, orderRef);
    // If the drive order must be assigned, do so.
    if (mustAssign(updatedOrder.getCurrentDriveOrder(), vehicle)) {
      // Let the vehicle controller know about the first drive order.
      vehicleControllerPool.getVehicleController(vehicle.getName())
          .setTransportOrder(updatedOrder);
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

  /**
   * Let a given vehicle abort any order it may currently be processing.
   *
   * @param vehicle The vehicle which should abort its order.
   * @param immediateAbort Whether to abort the order immediately instead of
   * just withdrawing it for a smooth abortion.
   */
  public void abortOrder(Vehicle vehicle, boolean immediateAbort) {
    requireNonNull(vehicle, "vehicle");

    if (vehicle.getTransportOrder() == null) {
      return;
    }

    abortAssignedOrder(
        transportOrderService.fetchObject(TransportOrder.class, vehicle.getTransportOrder()),
        vehicle,
        immediateAbort
    );
  }

  public void abortOrder(TransportOrder order, boolean immediateAbort) {
    requireNonNull(order, "order");

    if (order.getState().isFinalState()) {
      LOG.info(
          "Transport order '{}' already in final state '{}', skipping withdrawal.",
          order.getName(),
          order.getState()
      );
      return;
    }

    if (order.getProcessingVehicle() == null) {
      updateTransportOrderState(order.getReference(), TransportOrder.State.FAILED);
    }
    else {
      abortAssignedOrder(
          order,
          vehicleService.fetchObject(Vehicle.class, order.getProcessingVehicle()),
          immediateAbort
      );
    }
  }

  /**
   * Skips all transport orders in the given order sequence until the first transport order that
   * should be dispatched/executed is found.
   * A transport order should be dispatched if it is not dispensable or
   * if it is the last order in the sequence.
   *
   * @param sequenceRef The reference to the order sequence
   * @return The next transport order that should be dispatched/executed or Optional.empty()
   */
  public Optional<TransportOrder> nextDispatchableOrderInSequence(
      TCSObjectReference<OrderSequence> sequenceRef
  ) {
    OrderSequence seq = transportOrderService.fetchObject(OrderSequence.class, sequenceRef);
    // If the order sequence's next order is not available, yet, the vehicle should wait for it.
    if (seq.getNextUnfinishedOrder() == null) {
      return Optional.empty();
    }

    TransportOrder nextUnfinishedOrder
        = transportOrderService.fetchObject(TransportOrder.class, seq.getNextUnfinishedOrder());

    // If the order sequence's next order is not available because it was not properly finished.
    if (nextUnfinishedOrder.hasState(TransportOrder.State.WITHDRAWN)) {
      return Optional.empty();
    }

    while (nextUnfinishedOrder.isDispensable()
        && !nextUnfinishedOrder.getReference().equals(seq.getOrders().getLast())) {
      //If the transport order is dispensable and not the last order in the sequence,
      //it should be skipped.
      skipOrderInSequence(nextUnfinishedOrder);

      //Look for next orders, using an up-to-date copy of the sequence.
      seq = transportOrderService
          .fetchObject(OrderSequence.class, nextUnfinishedOrder.getWrappingSequence());
      if (seq.getNextUnfinishedOrder() == null) {
        return Optional.empty();
      }

      nextUnfinishedOrder
          = transportOrderService.fetchObject(TransportOrder.class, seq.getNextUnfinishedOrder());
      if (nextUnfinishedOrder.hasState(TransportOrder.State.WITHDRAWN)) {
        return Optional.empty();
      }
    }
    // Return the next order to be processed for the sequence.
    return Optional.of(nextUnfinishedOrder);
  }

  public void finishAbortion(Vehicle vehicle) {
    finishAbortion(vehicle.getTransportOrder(), vehicle);
  }

  private void finishAbortion(TCSObjectReference<TransportOrder> orderRef, Vehicle vehicle) {
    requireNonNull(orderRef, "orderRef");
    requireNonNull(vehicle, "vehicle");

    LOG.debug("{}: Aborted order {}", vehicle.getName(), orderRef.getName());

    // The current transport order has been aborted - update its state
    // and that of the vehicle.
    updateTransportOrderState(orderRef, TransportOrder.State.FAILED);

    vehicleService.updateVehicleProcState(vehicle.getReference(), Vehicle.ProcState.IDLE);
    vehicleService.updateVehicleTransportOrder(vehicle.getReference(), null);
  }

  /**
   * Aborts a given transport order known to be assigned to a given vehicle.
   *
   * @param vehicle The vehicle the order is assigned to.
   * @param order The order.
   * @param immediateAbort Whether to abort the order immediately instead of
   * just withdrawing it for a smooth abortion.
   */
  private void abortAssignedOrder(
      TransportOrder order,
      Vehicle vehicle,
      boolean immediateAbort
  ) {
    requireNonNull(order, "order");
    requireNonNull(vehicle, "vehicle");
    checkArgument(
        !order.getState().isFinalState(),
        "%s: Order already in final state: %s",
        vehicle.getName(),
        order.getName()
    );

    // Mark the order as withdrawn so we can react appropriately when the
    // vehicle reports the remaining movements as finished.
    updateTransportOrderState(order.getReference(), TransportOrder.State.WITHDRAWN);

    VehicleController vehicleController
        = vehicleControllerPool.getVehicleController(vehicle.getName());

    if (immediateAbort) {
      LOG.info("{}: Immediate abort of transport order {}...", vehicle.getName(), order.getName());
      vehicleController.abortTransportOrder(true);
      finishAbortion(order.getReference(), vehicle);
    }
    else {
      vehicleController.abortTransportOrder(false);
    }
  }

  /**
   * Skips the given order in its wrapping sequence.
   *
   * @param order The transport order to be skipped. Must be part of an order sequence and must be
   * the next order that sequence.
   * @throws IllegalArgumentException If the given order is not part of an order sequence or not the
   * next order in the sequence.
   */
  private void skipOrderInSequence(TransportOrder order)
      throws IllegalArgumentException {
    requireNonNull(order, "order");

    OrderSequence seq = extractWrappingSequence(order)
        .orElseThrow(
            () -> new IllegalArgumentException(order.getName() + ": Not part of a sequence")
        );
    checkArgument(
        seq.getNextUnfinishedOrder().equals(order.getReference()),
        "%s: Not next order in sequence: %s",
        order.getName(),
        seq.getName()
    );

    if (order.getProcessingVehicle() == null) {
      transportOrderService.updateTransportOrderState(
          order.getReference(),
          TransportOrder.State.FAILED
      );
      updateSequenceOfFailedTransportOrder(order.getReference());
      markNewDispatchableOrders();
    }
    else {
      abortAssignedOrder(
          order,
          vehicleService.fetchObject(Vehicle.class, order.getProcessingVehicle()),
          false
      );
    }
  }

  /**
   * Properly marks a transport order as FINISHED, also updating its wrapping sequence, if any.
   *
   * @param ref A reference to the transport order to be modified.
   */
  private void markOrderAndSequenceAsFinished(TCSObjectReference<TransportOrder> ref) {
    requireNonNull(ref, "ref");

    TransportOrder order = transportOrderService.fetchObject(TransportOrder.class, ref);
    Optional<OrderSequence> osOpt = extractWrappingSequence(order);

    // Sanity check: The finished order must be the next one in the sequence.
    osOpt.ifPresent(
        seq -> checkArgument(
            ref.equals(seq.getNextUnfinishedOrder()),
            "TO %s != next unfinished TO %s in sequence %s",
            ref,
            seq.getNextUnfinishedOrder(),
            seq
        )
    );

    transportOrderService.updateTransportOrderState(ref, TransportOrder.State.FINISHED);

    osOpt.ifPresent(seq -> {
      transportOrderService.updateOrderSequenceFinishedIndex(
          seq.getReference(),
          seq.getFinishedIndex() + 1
      );

      // Finish the order sequence, using an up-to-date copy.
      finishOrderSequence(
          transportOrderService.fetchObject(
              OrderSequence.class,
              seq.getReference()
          )
      );
    });
  }

  /**
   * Updates the order sequence of a failed transport order, if it has one.
   *
   * @param ref A reference to the failed transport order.
   */
  private void updateSequenceOfFailedTransportOrder(TCSObjectReference<TransportOrder> ref) {
    requireNonNull(ref, "ref");

    TransportOrder failedOrder = transportOrderService.fetchObject(TransportOrder.class, ref);

    Optional<OrderSequence> osOpt = extractWrappingSequence(failedOrder);
    osOpt.ifPresent(seq -> {
      if (seq.isFailureFatal()
          && !failedOrder.isDispensable()) {
        // Mark the sequence as complete to make sure no further orders are added.
        transportOrderService.markOrderSequenceComplete(seq.getReference());
        // Mark all orders of the sequence that are not in a final state as FAILED.
        seq.getOrders().stream()
            .map(curRef -> transportOrderService.fetchObject(TransportOrder.class, curRef))
            .filter(o -> !o.getState().isFinalState())
            .forEach(
                o -> transportOrderService.updateTransportOrderState(
                    o.getReference(),
                    TransportOrder.State.FAILED
                )
            );
        // Move the finished index of the sequence to its end.
        transportOrderService.updateOrderSequenceFinishedIndex(
            seq.getReference(),
            seq.getOrders().size() - 1
        );
      }
      else {
        // (Only) If the failed order is the next unfinished order in the sequence, increment
        // the sequence's finished index to process the next order.
        if (failedOrder.getReference().equals(seq.getNextUnfinishedOrder())) {
          transportOrderService.updateOrderSequenceFinishedIndex(
              seq.getReference(),
              seq.getFinishedIndex() + 1
          );
        }
      }

      // Finish the order sequence, using an up-to-date copy.
      finishOrderSequence(
          transportOrderService.fetchObject(
              OrderSequence.class,
              failedOrder.getWrappingSequence()
          )
      );
    });
  }

  private void finishOrderSequence(OrderSequence sequence) {
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

  private Optional<OrderSequence> extractWrappingSequence(TransportOrder order) {
    return order.getWrappingSequence() == null
        ? Optional.empty()
        : Optional.of(
            transportOrderService.fetchObject(
                OrderSequence.class,
                order.getWrappingSequence()
            )
        );
  }
}
