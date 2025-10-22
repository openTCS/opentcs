// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.parking;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.DriveOrderRouteAssigner;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.opentcs.strategies.basic.dispatching.selection.candidates.CompositeAssignmentCandidateSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.vehicles.CompositeReparkVehicleSelectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates parking orders for idle vehicles already at a parking position to send them to higher
 * prioritized parking positions.
 */
public class PrioritizedReparkPhase
    extends
      AbstractParkingPhase {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PrioritizedReparkPhase.class);

  private final CompositeReparkVehicleSelectionFilter vehicleSelectionFilter;
  private final ParkingPositionPriorityComparator priorityComparator;

  @Inject
  public PrioritizedReparkPhase(
      InternalTransportOrderService orderService,
      PrioritizedParkingPositionSupplier parkingPosSupplier,
      CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter,
      TransportOrderUtil transportOrderUtil,
      DefaultDispatcherConfiguration configuration,
      CompositeReparkVehicleSelectionFilter vehicleSelectionFilter,
      ParkingPositionPriorityComparator priorityComparator,
      DriveOrderRouteAssigner driveOrderRouteAssigner
  ) {
    super(
        orderService,
        parkingPosSupplier,
        assignmentCandidateSelectionFilter,
        transportOrderUtil,
        configuration,
        driveOrderRouteAssigner
    );
    this.vehicleSelectionFilter = requireNonNull(vehicleSelectionFilter, "vehicleSelectionFilter");
    this.priorityComparator = requireNonNull(priorityComparator, "priorityComparator");
  }

  @Override
  public void run() {
    if (!getConfiguration().parkIdleVehicles()
        || !getConfiguration().considerParkingPositionPriorities()
        || !getConfiguration().reparkVehiclesToHigherPriorityPositions()) {
      return;
    }

    LOG.debug("Looking for parking vehicles to send to higher prioritized parking positions...");

    getOrderService().fetch(Vehicle.class).stream()
        .filter(vehicle -> vehicleSelectionFilter.apply(vehicle).isEmpty())
        .sorted((vehicle1, vehicle2) -> {
          // Sort the vehicles based on the priority of the parking position they occupy
          Point point1
              = getOrderService().fetch(Point.class, vehicle1.getCurrentPosition()).orElseThrow();
          Point point2
              = getOrderService().fetch(Point.class, vehicle2.getCurrentPosition()).orElseThrow();
          return priorityComparator.compare(point1, point2);
        })
        .forEach(vehicle -> createParkingOrder(vehicle));
  }
}
