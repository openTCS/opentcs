// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.selection.candidates;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
 */
public class IsProcessable
    implements
      AssignmentCandidateSelectionFilter {

  /**
   * An error code indicating that there's a conflict between the type of a transport order and
   * the types a vehicle is allowed to process.
   */
  private static final String ORDER_TYPE_CONFLICT = "notAllowedOrderType";
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
    ExplainedBoolean result = checkProcessability(
        candidate.getVehicle(),
        candidate.getTransportOrder()
    );
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

    // Check for matching order types
    if (vehicle.getAcceptableOrderTypes().stream()
        .noneMatch(
            orderType -> orderType.getName().equals(OrderConstants.TYPE_ANY)
                || orderType.getName().equals(order.getType())
        )) {
      LOG.debug(
          "Type '{}' of order '{}' not in acceptable types '{}' of vehicle '{}'.",
          order.getType(),
          order.getName(),
          vehicle.getAcceptableOrderTypes(),
          vehicle.getName()
      );
      return new ExplainedBoolean(false, ORDER_TYPE_CONFLICT);
    }

    return vehicleControllerPool.getVehicleController(vehicle.getName()).canProcess(order);
  }
}
