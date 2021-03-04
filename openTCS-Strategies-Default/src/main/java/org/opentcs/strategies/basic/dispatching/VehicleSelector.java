/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;

/**
 * Selects vehicles for transport orders.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleSelector
    implements Lifecycle {

  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * The local kernel instance.
   */
  private final LocalKernel kernel;
  /**
   * Checks processability of transport orders for vehicles.
   */
  private final ProcessabilityChecker processabilityChecker;
  /**
   * Stores reservations of orders for vehicles.
   */
  private final OrderReservationPool orderReservationPool;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param router Computes routes/routing costs.
   * @param kernel The kernel to be used.
   * @param processabilityChecker Checks processability of transport orders for vehicles.
   * @param orderReservationPool Stores reservations of orders for vehicles.
   */
  @Inject
  public VehicleSelector(@Nonnull Router router,
                         @Nonnull LocalKernel kernel,
                         @Nonnull ProcessabilityChecker processabilityChecker,
                         @Nonnull OrderReservationPool orderReservationPool) {
    this.router = requireNonNull(router, "router");
    this.kernel = requireNonNull(kernel, "kernel");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processAbilityChecker");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
  }

  @Override
  public void initialize() {
    if (initialized) {
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
    if (!initialized) {
      return;
    }
    initialized = false;
  }

  @Nonnull
  public VehicleOrderSelection selectVehicle(TransportOrder order) {
    requireNonNull(order, "order");
    // Get the vehicle that needs the least time to process the
    // transport order.
    Vehicle selectedVehicle = null;
    long closestCosts = Long.MAX_VALUE;
    List<DriveOrder> closestDriveOrders = null;
    for (Vehicle curVehicle : findVehiclesForOrder(order)) {
      Point curPosition = kernel.getTCSObject(Point.class, curVehicle.getCurrentPosition());
      // Get a route for the vehicle, starting at it's current position.
      Optional<List<DriveOrder>> tmpDriveOrders = router.getRoute(curVehicle, curPosition, order);
      // Check if the vehicle can process the order right now.
      if (tmpDriveOrders.isPresent()
          && processabilityChecker.checkProcessability(curVehicle, order)) {
        long costs = 0;
        for (DriveOrder curDriveOrder : tmpDriveOrders.get()) {
          costs += curDriveOrder.getRoute().getCosts();
        }
        if (costs < closestCosts) {
          selectedVehicle = curVehicle;
          closestDriveOrders = tmpDriveOrders.get();
          closestCosts = costs;
        }
      }
    }
    return new VehicleOrderSelection(order, selectedVehicle, closestDriveOrders);
  }

  /**
   * Returns available vehicles for the given order.
   *
   * @param order The order for which to find available vehicles.
   * @return Available vehicles for the given order, or an empty list.
   */
  private List<Vehicle> findVehiclesForOrder(TransportOrder order) {
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
          if (!orderReservationPool.findReservations(vehicle.getReference()).isEmpty()) {
            return false;
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

}
