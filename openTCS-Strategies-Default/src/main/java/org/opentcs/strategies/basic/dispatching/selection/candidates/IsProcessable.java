/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection.candidates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.selection.AssignmentCandidateSelectionFilter;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters assignment candidates with which the transport order is actually processable by the
 * vehicle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class IsProcessable
    implements AssignmentCandidateSelectionFilter {

  /**
   * An error code indicating that there's a conflict between the category of a transport order and
   * the categories a vehicle is able to process.
   */
  private static final String ORDER_CATEGORY_CONFLICT = "notProcessableOrderCategory";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(IsProcessable.class);
  /**
   * The vehicle controller pool.
   */
  private final VehicleControllerPool vehicleControllerPool;

  /**
   * Creates a new instance.
   *
   * @param vehicleControllerPool The controller pool to be worked with.
   */
  @Inject
  public IsProcessable(VehicleControllerPool vehicleControllerPool) {
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
  }

  @Override
  public Collection<String> apply(AssignmentCandidate candidate) {
    ExplainedBoolean result = checkProcessability(candidate.getVehicle(),
                                                  candidate.getTransportOrder());
    return result.getValue()
        ? new ArrayList<>()
        : Arrays.asList(candidate.getVehicle().getName() + "(" + result.getReason() + ")");
  }

  /**
   * Checks if the given vehicle could process the given order right now.
   *
   * @param vehicle The vehicle.
   * @param order The order.
   * @return <code>true</code> if, and only if, the given vehicle can process the given order.
   */
  private ExplainedBoolean checkProcessability(Vehicle vehicle, TransportOrder order) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(order, "order");

    // Check for matching categories
    if (!vehicle.getProcessableCategories().contains(OrderConstants.CATEGORY_ANY)
        && !vehicle.getProcessableCategories().contains(order.getCategory())) {
      LOG.debug("Category '{}' of order '{}' not in categories '{}' of vehicle '{}'.",
                order.getCategory(),
                order.getName(),
                vehicle.getProcessableCategories(),
                vehicle.getName());
      return new ExplainedBoolean(false, ORDER_CATEGORY_CONFLICT);
    }

    return vehicleControllerPool.getVehicleController(vehicle.getName())
        .canProcess(operationSequence(order));
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
