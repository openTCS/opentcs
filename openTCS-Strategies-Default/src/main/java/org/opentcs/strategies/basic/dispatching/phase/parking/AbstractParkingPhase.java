/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.parking;

import java.util.Arrays;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.opentcs.strategies.basic.dispatching.selection.candidates.CompositeAssignmentCandidateSelectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class for parking phases.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class AbstractParkingPhase
    implements Phase {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractParkingPhase.class);
  /**
   * The transport order service.
   */
  private final InternalTransportOrderService orderService;
  /**
   * The strategy used for finding suitable parking positions.
   */
  @SuppressWarnings("deprecation")
  private final org.opentcs.components.kernel.ParkingPositionSupplier parkingPosSupplier;
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * A collection of predicates for filtering assignment candidates.
   */
  private final CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter;
  /**
   * Provides service functions for working with transport orders.
   */
  private final TransportOrderUtil transportOrderUtil;
  /**
   * The dispatcher configuration.
   */
  private final DefaultDispatcherConfiguration configuration;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @SuppressWarnings("deprecation")
  public AbstractParkingPhase(
      InternalTransportOrderService orderService,
      org.opentcs.components.kernel.ParkingPositionSupplier parkingPosSupplier,
      Router router,
      CompositeAssignmentCandidateSelectionFilter assignmentCandidateSelectionFilter,
      TransportOrderUtil transportOrderUtil,
      DefaultDispatcherConfiguration configuration) {
    this.router = requireNonNull(router, "router");
    this.orderService = requireNonNull(orderService, "orderService");
    this.parkingPosSupplier = requireNonNull(parkingPosSupplier, "parkingPosSupplier");
    this.assignmentCandidateSelectionFilter = requireNonNull(assignmentCandidateSelectionFilter,
                                                             "assignmentCandidateSelectionFilter");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    parkingPosSupplier.initialize();

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

    parkingPosSupplier.terminate();

    initialized = false;
  }

  public TransportOrderService getOrderService() {
    return orderService;
  }

  public DefaultDispatcherConfiguration getConfiguration() {
    return configuration;
  }

  protected void createParkingOrder(Vehicle vehicle) {
    Point vehiclePosition = orderService.fetchObject(Point.class, vehicle.getCurrentPosition());

    // Get a suitable parking position for the vehicle.
    Optional<Point> parkPos = parkingPosSupplier.findParkingPosition(vehicle);
    LOG.debug("Parking position for {}: {}", vehicle, parkPos);
    // If we could not find a suitable parking position at all, just leave the vehicle where it is.
    if (!parkPos.isPresent()) {
      LOG.info("{}: Did not find a suitable parking position.", vehicle.getName());
      return;
    }
    // Create a destination for the point.
    List<DestinationCreationTO> parkDests = Arrays.asList(
        new DestinationCreationTO(parkPos.get().getName(), DriveOrder.Destination.OP_PARK)
    );
    // Create a transport order for parking and verify its processability.
    TransportOrder parkOrder = orderService.createTransportOrder(
        new TransportOrderCreationTO("Park-", parkDests)
            .withIncompleteName(true)
            .withDispensable(true)
            .withIntendedVehicleName(vehicle.getName())
    );
    Optional<AssignmentCandidate> candidate = computeCandidate(vehicle, vehiclePosition, parkOrder)
        .filter(c -> assignmentCandidateSelectionFilter.apply(c).isEmpty());
    // XXX Change this to Optional.ifPresentOrElse() once we're at Java 9+.
    if (candidate.isPresent()) {
      transportOrderUtil.assignTransportOrder(candidate.get().getVehicle(),
                                              candidate.get().getTransportOrder(),
                                              candidate.get().getDriveOrders());
    }
    else {
      // Mark the order as failed, since the vehicle cannot execute it.
      orderService.updateTransportOrderState(parkOrder.getReference(), TransportOrder.State.FAILED);
    }
  }

  private Optional<AssignmentCandidate> computeCandidate(Vehicle vehicle,
                                                         Point vehiclePosition,
                                                         TransportOrder order) {
    return router.getRoute(vehicle, vehiclePosition, order)
        .map(driveOrders -> new AssignmentCandidate(vehicle, order, driveOrders));
  }
}
