/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.data.model.Vehicle;
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
   * The transport order service.
   */
  private final InternalTransportOrderService transportOrderService;
  /**
   * The vehicle controller pool.
   */
  private final VehicleControllerPool vehicleControllerPool;

  /**
   * Creates a new instance.
   *
   * @param transportOrderService The transport order service.
   * @param vehicleControllerPool The controller pool to be worked with.
   */
  @Inject
  public ProcessabilityChecker(InternalTransportOrderService transportOrderService,
                               VehicleControllerPool vehicleControllerPool) {
    this.transportOrderService = requireNonNull(transportOrderService, "transportOrderService");
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
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
      LOG.debug("Category '{}' of order '{}' not in categories '{}' of vehicle '{}'.",
                order.getCategory(),
                order.getName(),
                vehicle.getProcessableCategories(),
                vehicle.getName());
      return false;
    }

    ExplainedBoolean result = vehicleControllerPool.getVehicleController(vehicle.getName())
        .canProcess(operationSequence(order));
    if (result.isTrue()) {
      return true;
    }
    else {
      // The vehicle controller/communication adapter does not want to process
      // the order. Add a rejection for it.
      Rejection rejection = new Rejection(vehicle.getReference(), result.getReason());
      LOG.debug("Order '{}' rejected by vehicle '{}', reason: '{}'",
                order.getName(),
                vehicle.getName(),
                rejection.getReason());
      transportOrderService.registerTransportOrderRejection(order.getReference(), rejection);
      return false;
    }
  }

  /**
   * Returns the sequence of operations to be executed when processing the given transport order.
   *
   * @param order The transport order from which to extract the sequence of operations.
   * @return The sequence of operations to be executed when processing the given transport order.
   */
  private List<String> operationSequence(TransportOrder order) {
    return order.getFutureDriveOrders().stream()
        .map(driveOrder -> driveOrder.getDestination().getOperation())
        .collect(Collectors.toList());
  }

}
