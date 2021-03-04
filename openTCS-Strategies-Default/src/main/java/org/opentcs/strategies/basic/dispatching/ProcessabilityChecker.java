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
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks processability of transport orders for vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ProcessabilityChecker {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ProcessabilityChecker.class);
  /**
   * The local kernel instance.
   */
  private final LocalKernel kernel;
  /**
   * The vehicle controller pool.
   */
  private final VehicleControllerPool vehicleControllerPool;
  /**
   * Stores reservations of orders for vehicles.
   */
  private final OrderReservationPool orderReservationPool;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel to be worked with.
   * @param vehicleControllerPool The controller pool to be worked with.
   * @param orderReservationPool Stores reservations of orders for vehicles.
   */
  @Inject
  public ProcessabilityChecker(LocalKernel kernel,
                               VehicleControllerPool vehicleControllerPool,
                               OrderReservationPool orderReservationPool) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
  }

  /**
   * Checks if the given vehicle could process the given order right now.
   *
   * @param vehicle The vehicle.
   * @param order The order.
   * @return <code>true</code> if, and only if, the given vehicle can process
   * the given order.
   */
  public boolean checkProcessability(Vehicle vehicle, TransportOrder order) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(order, "order");
    
    // Check for matching categories
    if (!vehicle.getProcessableCategories().contains(order.getCategory())) {
      LOG.debug("Vehicle {} not able to process order {} with category '{}'. Processable "
          + "categories: {}",
                vehicle.getName(),
                order.getName(),
                order.getCategory(),
                vehicle.getProcessableCategories());
      return false;
    }

    ExplainedBoolean result = vehicleControllerPool.getVehicleController(vehicle.getName())
        .canProcess(getOperations(order));
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

  public boolean availableForTransportOrder(Vehicle vehicle, TransportOrder order) {
    return availableForTransportOrder(vehicle)
        && (vehicle.getOrderSequence() == null
            || Objects.equals(vehicle.getOrderSequence(),
                              order.getWrappingSequence()));
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

  /**
   * Returns the sequence of operations to be executed when processing the given transport order.
   *
   * @param order The transport order from which to extract the sequence of operations.
   * @return The sequence of operations to be executed when processing the given transport order.
   */
  private List<String> getOperations(TransportOrder order) {
    requireNonNull(order, "order");
    List<String> result = new LinkedList<>();
    for (DriveOrder curDriveOrder : order.getFutureDriveOrders()) {
      result.add(curDriveOrder.getDestination().getOperation());
    }
    return result;
  }

}
